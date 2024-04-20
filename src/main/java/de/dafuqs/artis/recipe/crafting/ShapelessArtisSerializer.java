package de.dafuqs.artis.recipe.crafting;

import com.google.gson.*;
import de.dafuqs.artis.api.*;
import de.dafuqs.matchbooks.recipe.*;
import net.minecraft.item.*;
import net.minecraft.network.*;
import net.minecraft.recipe.*;
import net.minecraft.util.*;
import net.minecraft.util.collection.*;
import org.jetbrains.annotations.*;

public class ShapelessArtisSerializer implements ArtisCraftingRecipeSerializer<ShapelessArtisRecipe> {
	
	private final ArtisCraftingRecipeType type;
	
	public ShapelessArtisSerializer(ArtisCraftingRecipeType type) {
		this.type = type;
	}
	
	private static @NotNull DefaultedList<IngredientStack> getIngredientStacks(@NotNull JsonArray array) {
		DefaultedList<IngredientStack> ingredients = DefaultedList.of();
		
		for (int i = 0; i < array.size(); ++i) {
			IngredientStack ingredientStack = RecipeParser.ingredientStackFromJson((array.get(i).getAsJsonObject()));
			if (!ingredientStack.isEmpty()) {
				ingredients.add(ingredientStack);
			}
		}
		return ingredients;
	}
	
	@Override
	public ShapelessArtisRecipe read(Identifier id, JsonObject jsonObject) {
		DefaultedList<IngredientStack> ingredientStacks = getIngredientStacks(JsonHelper.getArray(jsonObject, "ingredients"));
		if (ingredientStacks.isEmpty()) {
			throw new JsonParseException("No ingredients for shapeless recipe");
		} else if (ingredientStacks.size() > type.getWidth() * type.getHeight()) {
			throw new JsonParseException("Too many ingredients for shapeless " + type.getId().toString() + " recipe");
		}
		
		String group = readGroup(jsonObject);
		ItemStack output = readOutput(jsonObject);
		IngredientStack catalyst = readCatalyst(jsonObject);
		int cost = readCatalystCost(jsonObject);
		
		return new ShapelessArtisRecipe(type, id, group, ingredientStacks, output, catalyst, cost);
	}
	
	@Override
	public ShapelessArtisRecipe read(Identifier id, @NotNull PacketByteBuf buf) {
		String group = buf.readString(32767);
		int size = buf.readVarInt();
		
		DefaultedList<IngredientStack> ingredients = DefaultedList.ofSize(size, IngredientStack.EMPTY);
		ingredients.replaceAll(ignored -> IngredientStack.fromByteBuf(buf));
		
		ItemStack output = buf.readItemStack();
		
		IngredientStack catalyst = IngredientStack.fromByteBuf(buf);
		int cost = buf.readInt();
		
		return new ShapelessArtisRecipe(type, id, group, ingredients, output, catalyst, cost);
	}
	
	@Override
	public void write(@NotNull PacketByteBuf buf, ShapelessArtisRecipe recipe) {
		buf.writeString(recipe.getGroup());
		buf.writeVarInt(recipe.getIngredients().size());
		
		for (Ingredient ingredient : recipe.getIngredients()) {
			ingredient.write(buf);
		}
		buf.writeItemStack(recipe.getRawOutput());
		
		recipe.getCatalyst().write(buf);
		buf.writeInt(recipe.getCatalystCost());
	}
	
}
