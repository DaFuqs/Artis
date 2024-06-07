package de.dafuqs.artis.compat.emi;

import de.dafuqs.artis.inventory.condenser.*;
import de.dafuqs.artis.recipe.condenser.*;
import dev.emi.emi.api.recipe.*;
import dev.emi.emi.api.render.*;
import dev.emi.emi.api.stack.*;
import dev.emi.emi.api.widget.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import org.jetbrains.annotations.*;

import java.text.*;
import java.util.*;

public class CondenserEmiRecipe implements EmiRecipe {
	
	private static final DecimalFormat FORMAT = new DecimalFormat("###.##");
	
	protected final Identifier id;
	protected final EmiIngredient input;
	protected final EmiStack output;
	protected final int timeTicks;
	protected final int fuelPerTick;
	protected final boolean preservesInput;
	
	public CondenserEmiRecipe(CondenserRecipe recipe) {
		this.id = recipe.getId();
		this.input = EMIHelper.ofIngredientStack(recipe.getInput());
		this.output = EmiStack.of(recipe.getResult());
		this.timeTicks = recipe.getTimeTicks();
		this.fuelPerTick = recipe.getFuelPerTick();
		this.preservesInput = recipe.preservesInput();
	}
	
	@Override
	public EmiRecipeCategory getCategory() {
		return ArtisEmiPlugin.CONDENSER;
	}
	
	@Override
	public @Nullable Identifier getId() {
		return id;
	}
	
	@Override
	public List<EmiIngredient> getInputs() {
		return List.of(input);
	}
	
	@Override
	public List<EmiStack> getOutputs() {
		return List.of(output);
	}
	
	@Override
	public int getDisplayWidth() {
		return 134;
	}
	
	@Override
	public int getDisplayHeight() {
		return 50;
	}
	
	@Override
	public void addWidgets(WidgetHolder holder) {
		holder.addSlot(input, 23, 1); // input
		holder.addFillingArrow(47, 8, timeTicks * 50); // arrow
		holder.addSlot(output, 77, 4).large(true); // output
		
		if (preservesInput) {
			holder.addTexture(CondenserScreen.BACKGROUND, 44, 0, 9, 7, 176, 31);
		}
		
		// fire + description
		String cookingTimeString = FORMAT.format(timeTicks / 20D);
		Text tooltipText;
		if (fuelPerTick == 0) {
			holder.addTexture(EmiTexture.FULL_FLAME, 25, 21);
			tooltipText = Text.translatable("artis.recipe.tooltip.no_fuel", cookingTimeString);
		} else if (fuelPerTick == 1) {
			holder.addAnimatedTexture(EmiTexture.FULL_FLAME, 25, 21, 10000, false, true, true);
			tooltipText = Text.translatable("artis.recipe.tooltip.normal_fuel", cookingTimeString);
		} else {
			holder.addAnimatedTexture(EmiTexture.FULL_FLAME, 25, 21, 10000 / fuelPerTick, false, true, true);
			tooltipText = Text.translatable("artis.recipe.tooltip.increased_fuel", cookingTimeString, fuelPerTick);
		}
		holder.addText(tooltipText, getDisplayWidth() / 2, 38, 0x3f3f3f, false).horizontalAlign(TextWidget.Alignment.CENTER);
	}
	
}
