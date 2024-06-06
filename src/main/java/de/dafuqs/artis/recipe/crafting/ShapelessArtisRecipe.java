package de.dafuqs.artis.recipe.crafting;

import de.dafuqs.artis.*;
import de.dafuqs.artis.api.*;
import de.dafuqs.artis.inventory.crafting.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.recipe.*;
import net.minecraft.util.*;
import net.minecraft.util.collection.*;
import net.minecraft.world.*;

public class ShapelessArtisRecipe extends ArtisCraftingRecipeBase {
	
	public ShapelessArtisRecipe(ArtisCraftingRecipeType type, Identifier id, String group, DefaultedList<IngredientStack> ingredients, ItemStack output, IngredientStack catalyst, int catalystCost) {
		super(type, id, group, ingredients, output, catalyst, catalystCost);
		this.serializer = type.getShapelessSerializer();
	}
	
	@Override
	public boolean matches(ArtisCraftingInventory inventory, World world) {
		if (!super.matches(inventory, world)) {
			return false;
		}
		
		RecipeMatcher recipeMatcher = new RecipeMatcher();
		int foundCount = 0;
		for (int slot = 0; slot < inventory.size(); ++slot) {
			ItemStack itemStack = inventory.getStack(slot);
			if (!itemStack.isEmpty()) {
				++foundCount;
				recipeMatcher.addInput(itemStack, 1);
			}
		}
		
		return foundCount == this.ingredientStacks.size() && recipeMatcher.match(this, null);
	}
	
	@Override
	public boolean fits(int width, int height) {
		return this.ingredientStacks.size() >= width * height;
	}
	
	@Override
	public int getWidth() {
		return this.getType().getWidth();
	}
	
	@Override
	public int getHeight() {
		return this.getType().getHeight();
	}
	
	@Override
	public boolean isShapeless() {
		return true;
	}
	
	@Override
	public void useUpIngredients(ArtisCraftingInventory inventory, PlayerEntity player) {
		for (IngredientStack ingredientStack : this.ingredientStacks) {
			for (int slot = 0; slot < inventory.size(); slot++) {
				ItemStack slotStack = inventory.getStack(slot);
				if (ingredientStack.matches(slotStack)) {
					ItemStack remainder = slotStack.getRecipeRemainder();
					slotStack.decrement(ingredientStack.count());
					if (slotStack.isEmpty()) {
						inventory.setStack(slot, remainder);
					} else {
						player.dropStack(remainder);
					}
					break;
				}
			}
		}
	}
	
}
