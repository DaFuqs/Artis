package de.dafuqs.artis.compat.rei.crafting;

import de.dafuqs.artis.api.*;
import de.dafuqs.artis.compat.rei.*;
import me.shedaniel.rei.api.common.category.*;
import me.shedaniel.rei.api.common.display.*;
import me.shedaniel.rei.api.common.display.basic.*;
import me.shedaniel.rei.api.common.entry.*;
import me.shedaniel.rei.api.common.registry.*;
import me.shedaniel.rei.api.common.util.*;
import net.minecraft.recipe.*;
import net.minecraft.util.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class ArtisRecipeDisplay extends BasicDisplay implements SimpleGridMenuDisplay {
	
	private final ArtisCraftingRecipe display;
	private final ArtisCraftingRecipeType type;
	private final EntryIngredient catalyst;
	private final int catalystCost;
	
	public ArtisRecipeDisplay(@NotNull ArtisCraftingRecipe recipe) {
		super(REIHelper.toEntryIngredients(recipe.getIngredientStacks()), Collections.singletonList(EntryIngredients.of(recipe.getRawOutput())));
		this.display = recipe;
		this.type = (ArtisCraftingRecipeType) recipe.getType();
		this.catalyst = REIHelper.ofIngredientStack(recipe.getCatalyst());
		this.catalystCost = recipe.getCatalystCost();
	}
	
	/**
	 * When using Shift click on the plus button in the REI gui to autofill crafting grids
	 */
	public ArtisRecipeDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs, @NotNull ArtisCraftingRecipe recipe) {
		super(inputs, outputs);
		this.display = recipe;
		this.type = (ArtisCraftingRecipeType) recipe.getType();
		this.catalyst = REIHelper.ofIngredientStack(recipe.getCatalyst());
		this.catalystCost = recipe.getCatalystCost();
	}
	
	public static BasicDisplay.Serializer<ArtisRecipeDisplay> serializer() {
		return ArtisRecipeDisplay.Serializer.ofSimple(ArtisRecipeDisplay::simple).inputProvider(ArtisRecipeDisplay::getInputEntries);
	}
	
	private static @NotNull ArtisRecipeDisplay simple(List<EntryIngredient> inputs, List<EntryIngredient> outputs, @NotNull Identifier identifier) {
		RecipeEntry<?> recipe = RecipeManagerContext.getInstance().byId(identifier);
		return new ArtisRecipeDisplay(inputs, outputs, recipe.value());
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
