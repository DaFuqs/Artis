package de.dafuqs.artis.inventory.crafting;

import de.dafuqs.artis.api.*;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.recipe.*;
import net.minecraft.util.collection.*;

import java.util.*;

public class ArtisCraftingInventory extends CraftingInventory {
	
	private final CraftingInventory craftingInventory;
	private final DefaultedList<ItemStack> catalystInventory;
	private final ArtisCraftingScreenHandler artisCraftingScreenHandler;
	
	private final int catalystSlotID;
	
	public ArtisCraftingInventory(ArtisCraftingScreenHandler artisCraftingScreenHandler, int width, int height) {
		super(artisCraftingScreenHandler, width, height);
		this.catalystSlotID = width * height;
		
		this.craftingInventory = new CraftingInventory(artisCraftingScreenHandler, width, height);
		this.catalystInventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
		this.artisCraftingScreenHandler = artisCraftingScreenHandler;
	}
	
	@Override
	public int size() {
		return this.craftingInventory.size() + catalystInventory.size();
	}
	
	@Override
	public boolean isEmpty() {
		return this.craftingInventory.isEmpty() && catalystInventory.isEmpty();
	}
	
	@Override
	public ItemStack getStack(int slot) {
		if (slot == catalystSlotID) {
			return catalystInventory.get(0);
		} else {
			return craftingInventory.getStack(slot);
		}
	}
	
	@Override
	public ItemStack removeStack(int slot) {
		if (slot == catalystSlotID) {
			return Inventories.removeStack(catalystInventory, 0);
		} else {
			return craftingInventory.removeStack(slot);
		}
	}
	
	@Override
	public ItemStack removeStack(int slot, int amount) {
		if (slot == catalystSlotID) {
			ItemStack stack = Inventories.splitStack(this.catalystInventory, 0, amount);
			onContentChanged();
			return stack;
		} else {
			return craftingInventory.removeStack(slot, amount);
		}
	}
	
	@Override
	public void setStack(int slot, ItemStack stack) {
		if (slot == catalystSlotID) {
			catalystInventory.set(0, stack);
		} else {
			craftingInventory.setStack(slot, stack);
		}
		onContentChanged();
	}
	
	public void onContentChanged() {
		this.artisCraftingScreenHandler.onContentChanged(this);
	}
	
	@Override
	public void clear() {
		this.craftingInventory.clear();
		this.catalystInventory.clear();
	}
	
	@Override
	public void provideRecipeInputs(RecipeMatcher finder) {
		this.craftingInventory.provideRecipeInputs(finder);
	}
	
	public ItemStack getCatalyst() {
		return getStack(getWidth() * getHeight());
	}
	
	public RecipeType<?> getType() {
		Optional<RecipeEntry<ArtisCraftingRecipe>> optionalRecipe = getPlayer().getEntityWorld().getRecipeManager().getFirstMatch(artisCraftingScreenHandler.getArtisCraftingRecipeType(), artisCraftingScreenHandler.getCraftInv(), getPlayer().getEntityWorld());
		if (optionalRecipe.isPresent()) {
			return artisCraftingScreenHandler.getArtisCraftingRecipeType();
		}
		
		Optional<CraftingRecipe> optionalCraftingRecipe = getPlayer().getEntityWorld().getRecipeManager().getFirstMatch(RecipeType.CRAFTING, artisCraftingScreenHandler.getCraftInv(), getPlayer().getEntityWorld());
		if (optionalCraftingRecipe.isPresent()) {
			return RecipeType.CRAFTING;
		}
		return artisCraftingScreenHandler.getArtisCraftingRecipeType();
	}
	
	public boolean shouldCompareCatalyst() {
		return artisCraftingScreenHandler.getArtisCraftingRecipeType().hasCatalystSlot();
	}
	
	public PlayerEntity getPlayer() {
		return artisCraftingScreenHandler.getPlayer();
	}
	
	public RecipeInputInventory getCraftingInventory() {
		return this.craftingInventory;
	}
	
	public DefaultedList<ItemStack> getCatalystInventory() {
		return this.catalystInventory;
	}
	
}
