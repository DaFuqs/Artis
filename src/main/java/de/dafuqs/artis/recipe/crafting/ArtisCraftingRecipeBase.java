package de.dafuqs.artis.recipe.crafting;

import de.dafuqs.artis.*;
import de.dafuqs.artis.api.*;
import de.dafuqs.artis.inventory.crafting.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.recipe.*;
import net.minecraft.registry.*;
import net.minecraft.util.*;
import net.minecraft.util.collection.*;
import net.minecraft.world.*;

import java.util.*;

public abstract class ArtisCraftingRecipeBase implements Recipe<ArtisCraftingInventory>, ArtisCraftingRecipe {
	
	protected final ArtisCraftingRecipeType type;
	protected RecipeSerializer<? extends ArtisCraftingRecipe> serializer;
	
	protected final Identifier id;
	protected final String group;
	protected final DefaultedList<IngredientStack> ingredientStacks;
	protected final ItemStack output;
	protected final IngredientStack catalyst;
	protected final int catalystCost;
	
	protected ArtisCraftingRecipeBase(ArtisCraftingRecipeType recipeType, Identifier id, String group, DefaultedList<IngredientStack> ingredientStacks, ItemStack output, IngredientStack catalyst, int catalystCost) {
		this.type = recipeType;
		
		this.id = id;
		this.group = group;
		this.ingredientStacks = ingredientStacks;
		this.output = output;
		this.catalyst = catalyst;
		this.catalystCost = catalystCost;
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
				return toTest.getMaxDamage() - toTest.getDamage() >= catalystCost;
			} else if (toTest.getItem() instanceof SpecialCatalyst specialCatalyst) {
				return specialCatalyst.matchesCatalyst(toTest, catalystCost);
			} else {
				return toTest.getCount() >= catalystCost;
			}
		}
		return true;
	}
	
	@Override
	public ItemStack craft(ArtisCraftingInventory inventory, RegistryWrapper.WrapperLookup lookup) {
		return this.output.copy();
	}
	
	@Override
	public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup) {
		return this.output;
	}
	
	public IngredientStack getCatalyst() {
		return catalyst;
	}
	
	public int getCatalystCost() {
		return catalystCost;
	}
	
	public ItemStack getRawOutput() {
		return this.output;
	}
	
	@Override
	public List<IngredientStack> getIngredientStacks() {
		return this.ingredientStacks;
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
					catalystStack.setDamage(catalystStack.getDamage() + getCatalystCost());
					if (catalystStack.getDamage() >= catalystStack.getMaxDamage()) {
						catalystInventory.set(i, remainder);
					}
				} else if (catalystStack.getItem() instanceof SpecialCatalyst specialCatalyst) {
					specialCatalyst.consumeCatalyst(catalystStack, getCatalystCost());
				} else {
					catalystStack.decrement(getCatalystCost());
				}
			}
		}
	}
	
	public Identifier getId() {
		return this.id;
	}
	
}
