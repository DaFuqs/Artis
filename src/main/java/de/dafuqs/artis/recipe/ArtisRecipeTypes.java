package de.dafuqs.artis.recipe;

import de.dafuqs.artis.*;
import de.dafuqs.artis.recipe.condenser.*;
import net.minecraft.recipe.*;
import net.minecraft.registry.*;
import net.minecraft.util.*;

public class ArtisRecipeTypes {
	
	public static String CONDENSER_RECIPE_ID = "condenser";
	public static RecipeSerializer<CondenserRecipe> CONDENSER_RECIPE_SERIALIZER;
	public static RecipeType<CondenserRecipe> CONDENSER;
	
	static <S extends RecipeSerializer<T>, T extends Recipe<?>> S registerSerializer(String id, S serializer) {
		return Registry.register(Registries.RECIPE_SERIALIZER, new Identifier(Artis.MOD_ID, id), serializer);
	}
	
	static <T extends Recipe<?>> RecipeType<T> registerRecipeType(String id) {
		return Registry.register(Registries.RECIPE_TYPE, new Identifier(Artis.MOD_ID, id), new RecipeType<T>() {
			@Override
			public String toString() {
				return "artis:" + id;
			}
		});
	}
	
	public static void register() {
		
		CONDENSER_RECIPE_SERIALIZER = registerSerializer(CONDENSER_RECIPE_ID, new CondenserRecipe.Serializer());
		CONDENSER = registerRecipeType(CONDENSER_RECIPE_ID);
	}
	
}
