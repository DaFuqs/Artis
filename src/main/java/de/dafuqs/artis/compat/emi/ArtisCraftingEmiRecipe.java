package de.dafuqs.artis.compat.emi;

import de.dafuqs.artis.api.*;
import dev.emi.emi.api.recipe.*;
import dev.emi.emi.api.render.*;
import dev.emi.emi.api.stack.*;
import dev.emi.emi.api.widget.*;
import net.minecraft.item.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class ArtisCraftingEmiRecipe implements EmiRecipe {
	
	protected final ArtisCraftingRecipeType recipeType;
	protected final Identifier id;
	protected final List<EmiIngredient> input;
	protected final EmiStack output;
	protected final EmiIngredient catalyst;
	protected final int catalystCost;
	
	protected final int width;
	protected final int height;
	protected final boolean shapeless;
	
	public ArtisCraftingEmiRecipe(ArtisCraftingRecipe recipe) {
		this.recipeType = (ArtisCraftingRecipeType) recipe.getType();
		this.id = recipe.getId();
		this.input = EMIHelper.ofIngredientStacks(recipe.getIngredientStacks());
		this.output = EmiStack.of(recipe.getRawResult());
		this.catalyst = EMIHelper.ofIngredientStack(recipe.getCatalyst());
		this.catalystCost = recipe.getCatalystAmount();
		
		this.width = recipe.getWidth();
		this.height = recipe.getHeight();
		this.shapeless = recipe.isShapeless();
	}
	
	@Override
	public EmiRecipeCategory getCategory() {
		return ArtisEmiPlugin.CRAFTING.get(recipeType);
	}
	
	@Override
	public @Nullable Identifier getId() {
		return id;
	}
	
	@Override
	public List<EmiIngredient> getInputs() {
		return input;
	}
	
	@Override
	public List<EmiStack> getOutputs() {
		return List.of(output);
	}
	
	@Override
	public int getDisplayWidth() {
		return recipeType.getWidth() * 18 + 70;
	}
	
	@Override
	public int getDisplayHeight() {
		return recipeType.getHeight() * 18;
	}
	
	@Override
	public void addWidgets(WidgetHolder widgets) {
		
		int typeHeight = recipeType.getHeight();
		int typeWidth = recipeType.getWidth();
		
		// grid
		for (int y = 0; y < typeHeight; y++) {
			for (int x = 0; x < typeWidth; x++) {
				int index = x + y * typeWidth;
				EmiIngredient slotIngredient;
				if (shapeless) {
					slotIngredient = index < input.size() ? input.get(index) : EmiStack.of(ItemStack.EMPTY);
				} else {
					if(x < this.width) {
						int inputIndex = index - y * (typeWidth - this.width);
						slotIngredient = inputIndex < input.size() ? input.get(inputIndex) : EmiStack.of(ItemStack.EMPTY);
					} else {
						slotIngredient = EmiStack.of(ItemStack.EMPTY);
					}
				}
				widgets.addSlot(slotIngredient, x * 18, y * 18);
			}
		}
		
		// arrow & output
		int startX = typeWidth * 18;
		int startY = typeHeight * 9 - 26;
		widgets.addTexture(EmiTexture.EMPTY_ARROW, startX + 6, startY + 18);
		if (shapeless) {
			widgets.addTexture(EmiTexture.SHAPELESS, startX + 43, startY);
		}
		widgets.addSlot(output, startX + 38, startY + 14).large(true).recipeContext(this);
		
		// catalyst
		if (recipeType.hasCatalystSlot() && !catalyst.isEmpty()) {
			widgets.addSlot(catalyst, startX + 9, startY + 37);
			if(catalystCost > 0) {
				widgets.addText(Text.literal("-" + catalystCost), startX + 9, startY + 57, Formatting.RED.getColorValue(), false);
			} else {
				widgets.addText(Text.translatable("artis.recipe.tooltip.not_consumed"), startX + 2, startY + 57, 0x3f3f3f, false);
			}
		}
	}
	
}
