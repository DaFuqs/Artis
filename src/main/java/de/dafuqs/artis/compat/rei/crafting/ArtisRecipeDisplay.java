package de.dafuqs.artis.compat.rei.crafting;

import de.dafuqs.artis.api.*;
import de.dafuqs.artis.compat.rei.*;
import me.shedaniel.rei.api.common.category.*;
import me.shedaniel.rei.api.common.display.*;
import me.shedaniel.rei.api.common.display.basic.*;
import me.shedaniel.rei.api.common.entry.*;
import me.shedaniel.rei.api.common.util.*;
import net.minecraft.recipe.*;

import java.util.*;

public class ArtisRecipeDisplay extends BasicDisplay implements SimpleGridMenuDisplay {
	
	private final ArtisCraftingRecipe display;
	private final ArtisCraftingRecipeType type;
	private final EntryIngredient catalyst;
	private final int catalystCost;
	
	public ArtisRecipeDisplay(RecipeEntry<ArtisCraftingRecipe> recipe) {
		super(REIHelper.toEntryIngredients(recipe.value().getIngredientStacks()), Collections.singletonList(EntryIngredients.of(recipe.value().getRawResult())));
		this.display = recipe.value();
		this.type = (ArtisCraftingRecipeType) recipe.value().getType();
		this.catalyst = REIHelper.ofIngredientStack(recipe.value().getCatalyst());
		this.catalystCost = recipe.value().getCatalystAmount();
	}
	
	/**
	 * When using Shift click on the plus button in the REI gui to autofill crafting grids
	 */
	public ArtisRecipeDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, RecipeEntry<ArtisCraftingRecipe> recipe) {
		super(inputs, outputs);
		ArtisCraftingRecipe r = recipe.value();
		this.display = recipe.value();
		this.type = (ArtisCraftingRecipeType) r.getType();
		this.catalyst = REIHelper.ofIngredientStack(r.getCatalyst());
		this.catalystCost = r.getCatalystAmount();
	}
	
	public ArtisCraftingRecipe getDisplay() {
		return display;
	}
	
	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return type.getCategoryIdentifier();
	}
	
	@Override
	public int getWidth() {
		return display.getWidth();
	}
	
	@Override
	public int getHeight() {
		return display.getHeight();
	}
	
	public EntryIngredient getCatalyst() {
		return catalyst;
	}
	
	public int getCatalystCost() {
		return catalystCost;
	}
	
}
