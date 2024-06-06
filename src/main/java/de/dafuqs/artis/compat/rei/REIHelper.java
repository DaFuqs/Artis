package de.dafuqs.artis.compat.rei;

import de.dafuqs.artis.*;
import de.dafuqs.artis.api.*;
import me.shedaniel.math.*;
import me.shedaniel.rei.api.common.entry.*;
import me.shedaniel.rei.api.common.util.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.stream.*;

public class REIHelper {
	
	public static List<EntryIngredient> toEntryIngredients(List<IngredientStack> ingredientStacks) {
		return ingredientStacks.stream().map(REIHelper::ofIngredientStack).collect(Collectors.toCollection(ArrayList::new));
	}
	
	public static EntryIngredient ofIngredientStack(@NotNull IngredientStack ingredientStack) {
		return EntryIngredients.ofItemStacks(ingredientStack.getStacks());
	}
	
	public Rectangle getClickArea(ArtisCraftingRecipeType type) {
		ContainerLayout containerLayout = new ContainerLayout(type.getWidth(), type.getHeight(), type.hasCatalystSlot());
		return new Rectangle(containerLayout.getArrowX() + 8, containerLayout.getArrowY() + 2, 21, 16);
	}
	
}
