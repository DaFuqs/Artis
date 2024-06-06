package de.dafuqs.artis.inventory.slot;

import de.dafuqs.artis.api.*;
import de.dafuqs.artis.inventory.crafting.*;
import io.github.cottonmc.cotton.gui.*;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.recipe.*;
import org.jetbrains.annotations.*;

public class ValidatedArtisResultSlot extends ValidatedSlot {
	
	private final ArtisCraftingInventory craftingInv;
	private final PlayerEntity player;
	private int amount;
	
	public ValidatedArtisResultSlot(PlayerEntity player, ArtisCraftingInventory inventory, Inventory inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
		this.player = player;
		this.craftingInv = inventory;
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
			stack.onCraftByPlayer(this.player.getWorld(), this.player, this.amount);
		}
		this.amount = 0;
	}
	
	@Override
	public void onTakeItem(PlayerEntity player, ItemStack stack) {
		this.onCrafted(stack);
		
		if (this.inventory instanceof CraftingResultInventory craftingResultInventory) {
			RecipeEntry<?> lastRecipe = craftingResultInventory.getLastRecipe();
			if(lastRecipe != null && lastRecipe.value() instanceof ArtisCraftingRecipe artisCraftingRecipe) {
				artisCraftingRecipe.useUpCatalyst(this.craftingInv, this.player);
				artisCraftingRecipe.useUpIngredients(this.craftingInv, this.player);
				this.craftingInv.onContentChanged();
			}
		}
	}
	
}