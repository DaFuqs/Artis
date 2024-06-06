package de.dafuqs.artis.compat.emi;

import de.dafuqs.artis.*;
import de.dafuqs.artis.api.*;
import de.dafuqs.artis.recipe.*;
import dev.emi.emi.api.*;
import dev.emi.emi.api.recipe.*;
import dev.emi.emi.api.stack.*;
import net.minecraft.inventory.*;
import net.minecraft.recipe.*;
import net.minecraft.registry.*;
import net.minecraft.util.*;

import java.util.*;
import java.util.function.*;

public class ArtisEmiPlugin implements EmiPlugin {
	
	public static final Map<ArtisCraftingRecipeType, EmiRecipeCategory> CRAFTING = new HashMap<>();
	public static final EmiRecipeCategory CONDENSER = new EmiRecipeCategory(new Identifier(Artis.MOD_ID, "condenser"), EmiStack.of(ArtisBlocks.CONDENSER_BLOCK));
	
	@Override
	public void register(EmiRegistry registry) {
		registerCategories(registry);
		registerRecipes(registry);
	}

	public void registerCategories(EmiRegistry registry) {
		registry.addCategory(CONDENSER);
		
		for (ArtisCraftingRecipeType tableType : ArtisBlocks.ARTIS_TABLE_TYPES) {
			
			EmiStack stack;
			if (tableType instanceof ArtisExistingItemType) {
				stack = EmiStack.of(Registries.ITEM.get(tableType.getId()));
			} else {
				stack = EmiStack.of(Registries.BLOCK.get(tableType.getId()).asItem());
			}
			
			EmiRecipeCategory category = new EmiRecipeCategory(tableType.getId(), stack);
			
			CRAFTING.put(tableType, category);
			registry.addCategory(category);
			registry.addWorkstation(category, stack);
			
			if(tableType.shouldIncludeNormalRecipes()) {
				registry.addWorkstation(VanillaEmiRecipeCategories.CRAFTING, stack);
			}
		}
		
		registry.addWorkstation(CONDENSER, EmiStack.of(ArtisBlocks.CONDENSER_BLOCK));
	}

	public void registerRecipes(EmiRegistry registry) {
		for(Map.Entry<ArtisCraftingRecipeType, EmiRecipeCategory> entry : CRAFTING.entrySet()) {
			addAll(registry, entry.getKey(), ArtisCraftingEmiRecipe::new);
		}
		
		addAll(registry, ArtisRecipeTypes.CONDENSER, CondenserEmiRecipe::new);
	}

	public <C extends Inventory, T extends Recipe<C>> void addAll(EmiRegistry registry, RecipeType<T> type, Function<T, EmiRecipe> constructor) {
		for (RecipeEntry<T> recipe : registry.getRecipeManager().listAllOfType(type)) {
			registry.addRecipe(constructor.apply(recipe.value()));
		}
	}
	
}
