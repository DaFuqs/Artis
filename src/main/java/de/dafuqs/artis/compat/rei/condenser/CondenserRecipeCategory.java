package de.dafuqs.artis.compat.rei.condenser;

import com.google.common.collect.*;
import de.dafuqs.artis.*;
import de.dafuqs.artis.compat.rei.*;
import de.dafuqs.artis.inventory.condenser.*;
import me.shedaniel.math.*;
import me.shedaniel.rei.api.client.gui.*;
import me.shedaniel.rei.api.client.gui.widgets.*;
import me.shedaniel.rei.api.client.registry.display.*;
import me.shedaniel.rei.api.common.category.*;
import me.shedaniel.rei.api.common.util.*;
import net.fabricmc.api.*;
import net.minecraft.text.*;
import org.jetbrains.annotations.*;

import java.text.*;
import java.util.*;

@Environment(EnvType.CLIENT)
public class CondenserRecipeCategory implements DisplayCategory<CondenserRecipeDisplay> {
	
	private static final DecimalFormat format = new DecimalFormat("###.##");
	
	@Override
	public CategoryIdentifier<CondenserRecipeDisplay> getCategoryIdentifier() {
		return ArtisPlugins.CONDENSER;
	}
	
	@Override
	public Text getTitle() {
		return ArtisBlocks.CONDENSER_BLOCK.getName();
	}
	
	@Override
	public Renderer getIcon() {
		return EntryStacks.of(ArtisBlocks.CONDENSER_BLOCK);
	}
	
	@Override
	public int getDisplayHeight() {
		return 61;
	}
	
	@Override
	public List<Widget> setupDisplay(@NotNull CondenserRecipeDisplay display, @NotNull Rectangle bounds) {
		Point startPoint = new Point(bounds.getCenterX() - 41, bounds.y + 10);
		
		List<Widget> widgets = Lists.newArrayList();
		widgets.add(Widgets.createRecipeBase(bounds));
		widgets.add(Widgets.createResultSlotBackground(new Point(startPoint.x + 61, startPoint.y + 9)));
		
		if (display.preservesInput) {
			widgets.add(Widgets.createTexturedWidget(CondenserScreen.BACKGROUND, startPoint.x + 21, startPoint.y, 176, 31, 9, 7));
		}
		
		String cookingTimeString = format.format(display.timeTicks / 20D);
		Text tooltipText;
		if (display.fuelPerTick == 0) {
			widgets.add(Widgets.createBurningFire(new Point(startPoint.x + 1, startPoint.y + 20)).animationDurationTicks(Double.MAX_VALUE));
			tooltipText = Text.translatable("artis.recipe.tooltip.no_fuel", cookingTimeString);
		} else if (display.fuelPerTick == 1) {
			widgets.add(Widgets.createBurningFire(new Point(startPoint.x + 1, startPoint.y + 20)).animationDurationMS(10000));
			tooltipText = Text.translatable("artis.recipe.tooltip.normal_fuel", cookingTimeString);
		} else {
			widgets.add(Widgets.createBurningFire(new Point(startPoint.x + 1, startPoint.y + 20)).animationDurationMS(10000F / display.fuelPerTick));
			tooltipText = Text.translatable("artis.recipe.tooltip.increased_fuel", cookingTimeString, display.fuelPerTick);
		}
		widgets.add(Widgets.createLabel(new Point(startPoint.x - 26, startPoint.y + 38), tooltipText).leftAligned().noShadow().color(0xFF404040, 0xFFBBBBBB));
		
		widgets.add(Widgets.createArrow(new Point(startPoint.x + 24, startPoint.y + 8)).animationDurationTicks(display.timeTicks));
		widgets.add(Widgets.createSlot(new Point(startPoint.x + 61, startPoint.y + 9)).entries(display.getOutputEntries().getFirst()).disableBackground().markOutput());
		widgets.add(Widgets.createSlot(new Point(startPoint.x + 1, startPoint.y + 1)).entries(display.getInputEntries().getFirst()).markInput());
		
		return widgets;
	}
	
	
}