package io.github.alloffabric.artis.inventory.slot;

import io.github.alloffabric.artis.api.ArtisCraftingRecipe;
import io.github.alloffabric.artis.api.SpecialCatalyst;
import io.github.alloffabric.artis.inventory.ArtisCraftingInventory;
import io.github.cottonmc.cotton.gui.ValidatedSlot;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeUnlocker;
import net.minecraft.util.collection.DefaultedList;

public class ValidatedArtisResultSlot extends ValidatedSlot {

    private final ArtisCraftingInventory craftingInv;
    private final PlayerEntity player;
    private int amount;
    private final int syncId;

    public ValidatedArtisResultSlot(PlayerEntity player, ArtisCraftingInventory inventory, Inventory inventoryIn, int index, int xPosition, int yPosition, int syncId) {
        super(inventoryIn, index, xPosition, yPosition);
        this.player = player;
        this.craftingInv = inventory;
        this.syncId = syncId;
    }

    @Override
    public void setStack(ItemStack stack) {
        //super.setStack(stack);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack takeStack(int amount) {
        if (this.hasStack()) {
            this.amount += Math.min(amount, this.getStack().getCount());
        }
        return super.takeStack(amount);
    }

    @Override
    protected void onCrafted(ItemStack stack, int amount) {
        this.amount += amount;
        this.onCrafted(stack);
    }

    @Override
    protected void onTake(int amount) {
        this.amount += amount;
    }

    @Override
    protected void onCrafted(ItemStack stack) {
        if (this.amount > 0) {
            stack.onCraft(this.player.world, this.player, this.amount);
        }
        this.amount = 0;
    }

    @Override
    public void onTakeItem(PlayerEntity player, ItemStack stack) {
        this.onCrafted(stack);
        DefaultedList<ItemStack> remainders = getRemainders(); //= player.world.getRecipeManager().getRemainingStacks(craftingInv.getType(), this.craftingInv, player.world);
        for (int i = 0; i < remainders.size() - 1; ++i) {
            ItemStack input = this.craftingInv.getStack(i);
            ItemStack remainder = remainders.get(i);
            if (!input.isEmpty()) {
                this.craftingInv.removeStack(i, 1);
                input = this.craftingInv.getStack(i);
            }

            if (!remainder.isEmpty()) {
                if (input.isEmpty()) {
                    this.craftingInv.setStack(i, remainder);
                } else if (ItemStack.areItemsEqualIgnoreDamage(input, remainder) && ItemStack.areNbtEqual(input, remainder)) {
                    remainder.increment(input.getCount());
                    this.craftingInv.setStack(i, remainder);
                } else if (!this.player.getInventory().insertStack(remainder)) {
                    this.player.dropItem(remainder, false);
                }
            }
        }

        if (this.inventory instanceof RecipeUnlocker) {
            Recipe lastRecipe = ((RecipeUnlocker) inventory).getLastRecipe();
            if (lastRecipe instanceof ArtisCraftingRecipe) {
                ArtisCraftingRecipe recipe = (ArtisCraftingRecipe) lastRecipe;
                int catalystSlot = remainders.size() - 1;
                ItemStack remainder = remainders.get(catalystSlot).copy();
                if (!remainder.isEmpty()) {
                    this.craftingInv.setStack(catalystSlot, remainder);
                } else {
                    ItemStack catalyst = this.craftingInv.getCatalyst().copy();
                    if (catalyst.isDamageable()) {
                        catalyst.damage(recipe.getCatalystCost(), craftingInv.getPlayer(), (user) -> user.sendToolBreakStatus(user.getActiveHand()));
                    } else if (catalyst.getItem() instanceof SpecialCatalyst) {
                        catalyst = ((SpecialCatalyst) catalyst.getItem()).consume(catalyst, recipe.getCatalystCost());
                    } else {
                        catalyst.decrement(recipe.getCatalystCost());
                    }
                    this.craftingInv.setStack(catalystSlot, catalyst);
                    if (!player.world.isClient)
                        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, new ScreenHandlerSlotUpdateS2CPacket(syncId, 0, 37, catalyst));
                }
            }
        }
    }

    //note: inventory is actually CraftingResultInventory, so it's a safe cast
    public DefaultedList<ItemStack> getRemainders() {
        Recipe<CraftingInventory> lastRecipe = (Recipe<CraftingInventory>) ((CraftingResultInventory)this.inventory).getLastRecipe();
        if (lastRecipe != null &&
                lastRecipe.matches(craftingInv, player.world))
            return lastRecipe.getRemainder(craftingInv);
        else return craftingInv.getStacks();
    }
}