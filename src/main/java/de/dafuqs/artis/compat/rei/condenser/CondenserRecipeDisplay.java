package de.dafuqs.artis.compat.rei.condenser;

import de.dafuqs.artis.compat.rei.*;
import de.dafuqs.artis.recipe.condenser.*;
import me.shedaniel.rei.api.common.category.*;
import me.shedaniel.rei.api.common.display.basic.*;
import me.shedaniel.rei.api.common.util.*;

import java.util.*;

public class CondenserRecipeDisplay extends BasicDisplay {
	
	protected final int timeTicks;
	protected final int fuelPerTick;
	protected final boolean preservesInput;
	
	public CondenserRecipeDisplay(CondenserRecipe recipe) {
		super(Collections.singletonList(REIHelper.ofIngredientStack(recipe.getInput())), Collections.singletonList(EntryIngredients.of(recipe.getResult())));
		this.timeTicks = recipe.getTimeTicks();
		this.fuelPerTick = recipe.getFuelPerTick();
		this.preservesInput = recipe.preservesInput();
	}
	
	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return ArtisPlugins.CONDENSER;
	}
	
}
