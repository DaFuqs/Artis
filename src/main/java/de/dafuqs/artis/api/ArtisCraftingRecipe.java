package de.dafuqs.artis.api;

import de.dafuqs.artis.*;
import de.dafuqs.artis.inventory.crafting.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.recipe.*;

import java.util.*;

public interface ArtisCraftingRecipe extends Recipe<ArtisCraftingInventory> {
	List<IngredientStack> getIngredientStacks();
	
	IngredientStack getCatalyst();
	
	int getCatalystAmount();
	
	ItemStack getRawResult();
	
	int getWidth();
	
	int getHeight();
	
	void useUpCatalyst(ArtisCraftingInventory inventory, PlayerEntity player);
	
	void useUpIngredients(ArtisCraftingInventory inventory, PlayerEntity player);
	
	boolean isShapeless();
}
