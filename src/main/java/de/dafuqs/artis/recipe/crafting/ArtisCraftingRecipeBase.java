package de.dafuqs.artis.recipe.crafting;

import de.dafuqs.artis.api.*;
import de.dafuqs.artis.inventory.crafting.*;
import de.dafuqs.matchbooks.recipe.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.recipe.*;
import net.minecraft.registry.*;
import net.minecraft.util.*;
import net.minecraft.util.collection.*;
import net.minecraft.world.*;

import java.util.*;

public abstract class ArtisCraftingRecipeBase implements Recipe<ArtisCraftingInventory>, ArtisCraftingRecipe {
	
	private final ArtisCraftingRecipeType type;
	protected RecipeSerializer<? extends ArtisCraftingRecipe> serializer;
	
	private final Identifier id;
	private final String group;
	protected final DefaultedList<IngredientStack> ingredientStacks;
	private final ItemStack output;
	private final IngredientStack catalyst;
	private final int catalystCost;
	
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
	public Identifier getId() {
		return this.id;
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
	public ItemStack getOutput(DynamicRegistryManager registryManager) {
		return this.output;
	}
	
	@Override
	public boolean matches(ArtisCraftingInventory inventory, World world) {
		ItemStack toTest = inventory.getCatalyst();
		if (inventory.shouldCompareCatalyst()) {
			if (!catalyst.test(toTest)) return false;
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
	public ItemStack craft(ArtisCraftingInventory inventory, DynamicRegistryManager registryManager) {
		return this.output.copy();
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
	
}
