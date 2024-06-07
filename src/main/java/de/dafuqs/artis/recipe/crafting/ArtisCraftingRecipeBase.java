package de.dafuqs.artis.recipe.crafting;

import de.dafuqs.artis.*;
import de.dafuqs.artis.api.*;
import de.dafuqs.artis.inventory.crafting.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.recipe.*;
import net.minecraft.registry.*;
import net.minecraft.world.*;

import java.util.*;

public abstract class ArtisCraftingRecipeBase implements Recipe<ArtisCraftingInventory>, ArtisCraftingRecipe {
	
	protected final ArtisCraftingRecipeType type;
	protected RecipeSerializer<? extends ArtisCraftingRecipe> serializer;
	
	protected final String group;
	protected final ItemStack result;
	protected final IngredientStack catalyst;
	protected final int catalystAmount;
	
	protected ArtisCraftingRecipeBase(ArtisCraftingRecipeType recipeType, String group, IngredientStack catalyst, int catalystAmount, ItemStack result) {
		this.type = recipeType;
		this.group = group;
		this.result = result;
		this.catalyst = catalyst;
		this.catalystAmount = catalystAmount;
	}
	
	@Override
	public boolean isIgnoredInRecipeBook() {
		return true;
	}
	
	@Override
	public ArtisCraftingRecipeType getType() {
		return type;
	}
	
	@Override
	public RecipeSerializer<? extends ArtisCraftingRecipe> getSerializer() {
		return serializer;
	}
	
	@Override
	public String getGroup() {
		return this.group;
	}
	
	@Override
	public boolean matches(ArtisCraftingInventory inventory, World world) {
		ItemStack toTest = inventory.getCatalyst();
		if (inventory.shouldCompareCatalyst()) {
			if (!catalyst.matches(toTest)) return false;
			if (toTest.isDamageable()) {
				return toTest.getMaxDamage() - toTest.getDamage() >= catalystAmount;
			} else if (toTest.getItem() instanceof SpecialCatalyst specialCatalyst) {
				return specialCatalyst.matchesCatalyst(toTest, catalystAmount);
			} else {
				return toTest.getCount() >= catalystAmount;
			}
		}
		return true;
	}
	
	@Override
	public ItemStack craft(ArtisCraftingInventory inventory, RegistryWrapper.WrapperLookup lookup) {
		return this.result.copy();
	}
	
	@Override
	public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup) {
		return this.result;
	}
	
	public IngredientStack getCatalyst() {
		return catalyst;
	}
	
	public int getCatalystAmount() {
		return catalystAmount;
	}
	
	public ItemStack getRawResult() {
		return this.result;
	}
	
	@Override
	public void useUpCatalyst(ArtisCraftingInventory inventory, PlayerEntity player) {
		List<ItemStack> catalystInventory = inventory.getCatalystInventory();
		
		for (int i = 0; i < catalystInventory.size(); i++) {
			ItemStack catalystStack = catalystInventory.get(i);
			
			ItemStack remainder = catalystStack.getRecipeRemainder();
			if (!remainder.isEmpty()) {
				catalystInventory.set(i, remainder);
			} else {
				if (catalystStack.isDamageable()) {
					catalystStack.setDamage(catalystStack.getDamage() + getCatalystAmount());
					if (catalystStack.getDamage() >= catalystStack.getMaxDamage()) {
						catalystInventory.set(i, remainder);
					}
				} else if (catalystStack.getItem() instanceof SpecialCatalyst specialCatalyst) {
					specialCatalyst.consumeCatalyst(catalystStack, getCatalystAmount());
				} else {
					catalystStack.decrement(getCatalystAmount());
				}
			}
		}
	}
	
}
