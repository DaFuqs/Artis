package de.dafuqs.artis.block;

import de.dafuqs.artis.*;
import de.dafuqs.artis.inventory.condenser.*;
import de.dafuqs.artis.recipe.*;
import de.dafuqs.artis.recipe.condenser.*;
import dev.architectury.registry.fuel.*;
import net.fabricmc.fabric.api.networking.v1.*;
import net.fabricmc.fabric.api.screenhandler.v1.*;
import net.fabricmc.fabric.api.transfer.v1.item.*;
import net.fabricmc.fabric.api.transfer.v1.storage.base.*;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.datafixer.fix.*;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.network.*;
import net.minecraft.recipe.*;
import net.minecraft.screen.*;
import net.minecraft.server.network.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import net.minecraft.util.collection.*;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.*;
import net.minecraft.world.*;
import org.apache.logging.log4j.*;
import org.jetbrains.annotations.*;

public class CondenserBlockEntity extends BlockEntity implements NamedScreenHandlerFactory  {

    public final SingleVariantStorage<ItemVariant> input = new SingleVariantStorage<>() {
        @Override
        protected ItemVariant getBlankVariant() {
            return ItemVariant.blank();
        }

        @Override
        protected long getCapacity(ItemVariant variant) {
            return Integer.MAX_VALUE;
        }
    };
    public final SingleVariantStorage<ItemVariant> fuel = new SingleVariantStorage<>() {
        @Override
        protected ItemVariant getBlankVariant() {
            return ItemVariant.blank();
        }

        @Override
        protected long getCapacity(ItemVariant variant) {
            return 64;
        }
    };
    public final SingleVariantStorage<ItemVariant> output = new SingleVariantStorage<>() {
        @Override
        protected ItemVariant getBlankVariant() {
            return ItemVariant.blank();
        }

        @Override
        protected long getCapacity(ItemVariant variant) {
            return 64;
        }
    };

    protected int burnTime;
    protected int fuelTime;
    protected int cookTime;
    protected int cookTimeTotal;
    protected @Nullable CondenserRecipe cachedRecipe;

    protected final PropertyDelegate propertyDelegate;

    public CondenserBlockEntity(BlockPos pos, BlockState state) {
        super(ArtisBlocks.CONDENSER_BLOCK_ENTITY, pos, state);
        this.propertyDelegate = new PropertyDelegate() {
            public int get(int index) {
                return switch (index) {
                    case 0 -> CondenserBlockEntity.this.burnTime;
                    case 1 -> CondenserBlockEntity.this.fuelTime;
                    case 2 -> CondenserBlockEntity.this.cookTime;
                    case 3 -> CondenserBlockEntity.this.cookTimeTotal;
                    case 4 -> (int) CondenserBlockEntity.this.input.amount;
                    default -> 0;
                };
            }

            public void set(int index, int value) {
                switch (index) {
                    case 0 -> CondenserBlockEntity.this.burnTime = value;
                    case 1 -> CondenserBlockEntity.this.fuelTime = value;
                    case 2 -> CondenserBlockEntity.this.cookTime = value;
                    case 3 -> CondenserBlockEntity.this.cookTimeTotal = value;
                    case 4 -> CondenserBlockEntity.this.input.amount = value;
                }
            }

            public int size() {
                return 5;
            }
        };
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.put("InputVariant", this.input.variant.toNbt());
        nbt.putLong("InputCount", this.input.amount);
        nbt.put("FuelVariant", this.fuel.variant.toNbt());
        nbt.putLong("FuelCount", this.fuel.amount);
        nbt.put("OutputVariant", this.output.variant.toNbt());
        nbt.putLong("OutputCount", this.output.amount);

        nbt.putShort("BurnTime", (short)this.burnTime);
        nbt.putShort("FuelTime", (short)this.fuelTime);
        nbt.putShort("CookTime", (short)this.cookTime);
        nbt.putShort("CookTimeTotal", (short)this.cookTimeTotal);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.input.variant = ItemVariant.fromNbt(nbt.getCompound("InputVariant"));
        this.input.amount = nbt.getLong("InputCount");
        this.fuel.variant = ItemVariant.fromNbt(nbt.getCompound("FuelVariant"));
        this.fuel.amount = nbt.getLong("FuelCount");
        this.output.variant = ItemVariant.fromNbt(nbt.getCompound("OutputVariant"));
        this.output.amount = nbt.getLong("OutputCount");

        this.burnTime = nbt.getShort("BurnTime");
        this.fuelTime = nbt.getShort("FuelTime");
        this.cookTime = nbt.getShort("CookTime");
        this.cookTimeTotal = nbt.getShort("CookTimeTotal");
    }

    private boolean isBurning() {
        return this.burnTime > 0;
    }

    public static void tick(World world, BlockPos pos, BlockState state, CondenserBlockEntity blockEntity) {
        boolean wasBurning = blockEntity.isBurning();
        boolean shouldMarkDirty = false;
        if (blockEntity.isBurning()) {
            if(blockEntity.cachedRecipe == null) {
                blockEntity.burnTime--;
            } else {
                blockEntity.burnTime -= blockEntity.cachedRecipe.getFuelPerTick();
            }
        }

        ItemVariant fuelVariant = blockEntity.fuel.amount == 0 ? ItemVariant.blank() : blockEntity.fuel.variant;
        ItemVariant inputVariant = blockEntity.input.amount == 0 ? ItemVariant.blank() : blockEntity.input.variant;
        if (!blockEntity.isBurning() && (fuelVariant.isBlank() || inputVariant.isBlank())) {
            if (!blockEntity.isBurning() && blockEntity.cookTime > 0) {
                blockEntity.cookTime = MathHelper.clamp(blockEntity.cookTime - 2, 0, blockEntity.cookTimeTotal);
            }
        } else {
            Inventory inventory = new VariantBackedInventory(blockEntity, blockEntity.input, blockEntity.fuel, blockEntity.output);
            CondenserRecipe recipe;
            if(blockEntity.cachedRecipe != null && blockEntity.cachedRecipe.matches(inventory, world)) {
                recipe = blockEntity.cachedRecipe;
            } else {
                recipe = world.getRecipeManager().getFirstMatch(ArtisRecipeTypes.CONDENSER, inventory, world).orElse(null);
                blockEntity.cachedRecipe = recipe;
                if(recipe == null) {
                    blockEntity.cookTimeTotal = 0;
                } else {
                    blockEntity.cookTimeTotal = recipe.getTime();
                }
                shouldMarkDirty = true;
            }

            // find and use fuel
            if (!blockEntity.isBurning() && canAcceptRecipeOutput(recipe, inventory)) {
                blockEntity.burnTime = blockEntity.burnTime + FuelRegistry.get(fuelVariant.toStack());
                blockEntity.fuelTime = blockEntity.burnTime;
                if (blockEntity.isBurning()) {
                    shouldMarkDirty = true;
                    if (blockEntity.fuel.amount > 0) {
                        Item item = fuelVariant.getItem();
                        blockEntity.fuel.amount -= 1;
                        if (blockEntity.fuel.amount == 0) {
                            Item fuelRemainder = item.getRecipeRemainder();
                            if(fuelRemainder != null) {
                                blockEntity.fuel.variant = ItemVariant.of(fuelRemainder);
                                blockEntity.fuel.amount = 1;
                            }
                        }
                    }
                }
            }

            // progress cooking
            if (blockEntity.isBurning() && canAcceptRecipeOutput(recipe, inventory)) {
                ++blockEntity.cookTime;
                if (blockEntity.cookTime == blockEntity.cookTimeTotal) {
                    blockEntity.cookTime = 0;
                    craftRecipe(recipe, blockEntity, inventory);
                    shouldMarkDirty = true;
                }
            } else {
                blockEntity.cookTime = 0;
                if(blockEntity.burnTime < 0) {
                    blockEntity.burnTime = 0;
                }
            }
        }

        // if burning state changed => update blockstate
        if (wasBurning != blockEntity.isBurning()) {
            shouldMarkDirty = true;
            state = state.with(CondenserBlock.LIT, blockEntity.isBurning());
            world.setBlockState(pos, state, 3);
        }

        if (shouldMarkDirty) {
            markDirty(world, pos, state);
        }
    }

    /*private static int getCookTime(World world, Inventory inventory) {
        return world.getRecipeManager().getFirstMatch(ArtisRecipeTypes.CONDENSER, inventory, world).map(CondenserRecipe::getTime).orElse(200);
    }*/

    private static boolean canAcceptRecipeOutput(@Nullable CondenserRecipe recipe, @NotNull Inventory inventory) {
        if (recipe != null && !(inventory.getStack(0)).isEmpty()) {
            ItemStack recipeOutput = recipe.getOutput();
            if (recipeOutput.isEmpty()) {
                return false;
            } else {
                ItemStack existingOutput = inventory.getStack(2);
                if (existingOutput.isEmpty()) {
                    return true;
                } else if (!existingOutput.isItemEqualIgnoreDamage(recipeOutput)) {
                    return false;
                }
                return existingOutput.getCount() + recipeOutput.getCount() <= existingOutput.getMaxCount();
            }
        }
        return false;
    }

    private static boolean craftRecipe(CondenserRecipe recipe, CondenserBlockEntity condenser, Inventory inventory) {
        if (canAcceptRecipeOutput(recipe, inventory)) {
            ItemVariant output = condenser.output.variant;
            ItemStack recipeOutput = recipe.getOutput();
            if (output.isBlank()) {
                condenser.output.variant = ItemVariant.of(recipeOutput);
                condenser.output.amount = recipeOutput.getCount();
            } else if (output.isOf(recipeOutput.getItem())) {
                condenser.output.amount += recipeOutput.getCount();
            }
            condenser.input.amount -= recipe.getInput().getCount();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText("block.artis.condenser");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new CondenserScreenHandler(syncId, inv, this);
    }

    public PropertyDelegate getPropertyDelegate() {
        return this.propertyDelegate;
    }

}
