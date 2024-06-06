package de.dafuqs.artis.compat.rei.crafting;

import com.google.common.collect.*;
import de.dafuqs.artis.api.*;
import de.dafuqs.artis.compat.rei.*;
import me.shedaniel.math.*;
import me.shedaniel.rei.api.client.gui.*;
import me.shedaniel.rei.api.client.gui.widgets.*;
import me.shedaniel.rei.api.client.registry.display.*;
import me.shedaniel.rei.api.common.category.*;
import me.shedaniel.rei.api.common.entry.*;
import me.shedaniel.rei.api.common.util.*;
import net.fabricmc.api.*;
import net.minecraft.registry.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import org.jetbrains.annotations.*;

import java.util.*;

@Environment(EnvType.CLIENT)
public class ArtisRecipeCategory implements DisplayCategory<ArtisRecipeDisplay> {
	
	private final ArtisCraftingRecipeType artisCraftingRecipeType;
	
	public ArtisRecipeCategory(ArtisCraftingRecipeType artisCraftingRecipeType) {
		this.artisCraftingRecipeType = artisCraftingRecipeType;
	}
	
	public static int getSlotWithSize(@NotNull ArtisRecipeDisplay recipeDisplay, int num, int craftingGridWidth) {
		int x = num % recipeDisplay.getDisplay().getWidth();
		int y = (num - x) / recipeDisplay.getDisplay().getWidth();
		return craftingGridWidth * y + x;
	}
	
	@Override
	public CategoryIdentifier<? extends ArtisRecipeDisplay> getCategoryIdentifier() {
		return artisCraftingRecipeType.getCategoryIdentifier();
	}
	
	@Override
	public Renderer getIcon() {
		if (artisCraftingRecipeType instanceof ArtisExistingItemType) {
			return EntryStacks.of(Registries.ITEM.get(artisCraftingRecipeType.getId()));
		} else {
			return EntryStacks.of(Registries.BLOCK.get(artisCraftingRecipeType.getId()));
		}
	}
	
	@Override
	public Text getTitle() {
		return Text.translatable("recipe.category." + artisCraftingRecipeType.getId().getPath());
	}
	
	@Override
	public List<Widget> setupDisplay(ArtisRecipeDisplay recipeDisplay, @NotNull Rectangle bounds) {
		int displayHeight = getDisplayHeight();
		int displayWidth = getDisplayWidth(recipeDisplay);
		
		Point startPoint = new Point(bounds.getCenterX() - (displayWidth / 2) + 17, bounds.getCenterY() - (displayHeight / 2) + 15);
		
		if (artisCraftingRecipeType.hasCatalystSlot() && artisCraftingRecipeType.getHeight() == 1) {
			bounds.setSize(bounds.width, bounds.height + 18);
		}
		
		// base
		List<ColorableEntryWidget> slots = Lists.newArrayList();
		List<Widget> widgets = artisCraftingRecipeType.hasColor()
				? new LinkedList<>(List.of(Widgets.createRecipeBase(bounds).color(artisCraftingRecipeType.getColor())))
				: new LinkedList<>(List.of(Widgets.createRecipeBase(bounds)));
		
		int color = artisCraftingRecipeType.hasColor() ? artisCraftingRecipeType.getColor() : 0xFFFFFF;
		
		// grid
		for (int y = 0; y < artisCraftingRecipeType.getHeight(); y++) {
			for (int x = 0; x < artisCraftingRecipeType.getWidth(); x++) {
				slots.add(ColorableEntryWidget.create(startPoint.x + 1 + x * 18, startPoint.y + 1 + y * 18, color));
			}
		}
		List<EntryIngredient> inputs = recipeDisplay.getInputEntries();
		for (int i = 0; i < inputs.size(); i++) {
			if (!inputs.get(i).isEmpty()) {
				slots.get(getSlotWithSize(recipeDisplay, i, artisCraftingRecipeType.getWidth())).entries(inputs.get(i));
			}
		}
		widgets.addAll(slots);
		
		// arrow
		widgets.add(TransparentArrowWidget.create(new Point(slots.getLast().getX() + 24, startPoint.y + (displayHeight / 2) - 23)).disableAnimation());
		
		// output
		List<EntryIngredient> output = recipeDisplay.getOutputEntries();
		widgets.add(ColorableEntryWidget.create(slots.getLast().getX() + 55, startPoint.y + (displayHeight / 2) - 22, color).markOutput().entries(output.getFirst()));
		
		// catalyst
		EntryIngredient catalyst = recipeDisplay.getCatalyst();
		if (artisCraftingRecipeType.hasCatalystSlot() && !catalyst.isEmpty()) {
			widgets.add(ColorableEntryWidget.create(slots.getLast().getX() + 28, startPoint.y + (displayHeight / 2) - 4, color).entries(catalyst));
			
			if (recipeDisplay.getCatalystCost() > 0) {
				widgets.add(Widgets.createLabel(
						new Point(35 + slots.getLast().getX(), 15 + startPoint.y + (displayHeight / 2)),
						Text.literal(Formatting.RED + "-" + recipeDisplay.getCatalystCost())
				).centered());
			} else {
				widgets.add(Widgets.createLabel(
						new Point(18 + slots.getLast().getX(), 15 + startPoint.y + (displayHeight / 2)),
						Text.translatable("artis.recipe.tooltip.not_consumed")
				).leftAligned());
			}
		}
		
		return widgets;
	}
	
	@Override
	public int getDisplayHeight() {
		return 29 + (artisCraftingRecipeType.getHeight() * 18);
	}
	
	@Override
	public int getDisplayWidth(ArtisRecipeDisplay display) {
		return 90 + (artisCraftingRecipeType.getWidth() * 18);
	}
	
}