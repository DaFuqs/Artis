package de.dafuqs.artis.recipe.crafting;

import com.google.gson.*;
import de.dafuqs.artis.api.*;
import de.dafuqs.matchbooks.recipe.*;
import net.minecraft.item.*;
import net.minecraft.recipe.*;
import net.minecraft.util.*;

public interface ArtisCraftingRecipeSerializer<R extends ArtisCraftingRecipe> extends RecipeSerializer<R> {
	
	default String readGroup(JsonObject jsonObject) {
		return JsonHelper.getString(jsonObject, "group", "");
	}
	
	default ItemStack readOutput(JsonObject jsonObject) {
		return ShapedRecipe.outputFromJson(JsonHelper.getObject(jsonObject, "result"));
	}
	
	default IngredientStack readCatalyst(JsonObject jsonObject) {
		return JsonHelper.hasElement(jsonObject, "catalyst") ? RecipeParser.ingredientStackFromJson(jsonObject.get("catalyst").getAsJsonObject()) : IngredientStack.EMPTY;
	}
	
	default int readCatalystCost(JsonObject jsonObject) {
		return JsonHelper.hasElement(jsonObject, "cost") ? JsonHelper.getInt(jsonObject, "cost") : 0;
	}
	
}
