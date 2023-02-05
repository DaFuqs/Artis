package de.dafuqs.artis.recipe.crafting;

import com.google.common.collect.*;
import com.google.gson.*;
import de.dafuqs.artis.api.*;
import net.minecraft.item.*;
import net.minecraft.network.*;
import net.minecraft.recipe.*;
import net.minecraft.util.*;
import net.minecraft.util.collection.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class ShapedArtisSerializer implements RecipeSerializer<ShapedArtisRecipe> {
    private final ArtisTableType type;

    public ShapedArtisSerializer(ArtisTableType type) {
        this.type = type;
    }

    private static @NotNull DefaultedList<Ingredient> getIngredients(String @NotNull [] pattern, @NotNull Map<String, Ingredient> key, int width, int height) {
        DefaultedList<Ingredient> ingredients = DefaultedList.ofSize(width * height, Ingredient.EMPTY);
        Set<String> symbols = Sets.newHashSet(key.keySet());
        symbols.remove(" ");

        for (int i = 0; i < pattern.length; ++i) {
            for (int j = 0; j < pattern[i].length(); ++j) {
                String symbol = pattern[i].substring(j, j + 1);
                Ingredient ingredient = key.get(symbol);
                if (ingredient == null) {
                    throw new JsonSyntaxException("Pattern references symbol '" + symbol + "' but it's not defined in the key");
                }

                symbols.remove(symbol);
                ingredients.set(j + width * i, ingredient);
            }
        }

        if (!symbols.isEmpty()) {
            throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + symbols);
        } else {
            return ingredients;
        }
    }

    private static @NotNull Map<String, Ingredient> getComponents(@NotNull JsonObject json) {
        Map<String, Ingredient> map_1 = Maps.newHashMap();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            if ((entry.getKey()).length() != 1) {
                throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' is an invalid symbol (must be 1 character only).");
            }

            if (" ".equals(entry.getKey())) {
                throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
            }

            map_1.put(entry.getKey(), Ingredient.fromJson(entry.getValue()));
        }

        map_1.put(" ", Ingredient.EMPTY);
        return map_1;
    }

    static String @NotNull [] combinePattern(String @NotNull ... pattern) {
        int startIndex = 2147483647;
        int int_2 = 0;
        int int_3 = 0;
        int readLength = 0;

        for (int i = 0; i < pattern.length; ++i) {
            String row = pattern[i];
            startIndex = Math.min(startIndex, findNextIngredient(row));
            int endIndex = findNextIngredientReverse(row);
            int_2 = Math.max(int_2, endIndex);
            if (endIndex < 0) {
                if (int_3 == i) {
                    ++int_3;
                }

                ++readLength;
            } else {
                readLength = 0;
            }
        }

        if (pattern.length == readLength) {
            return new String[0];
        } else {
            String[] newPattern = new String[pattern.length - readLength - int_3];

            for (int i = 0; i < newPattern.length; ++i) {
                newPattern[i] = pattern[i + int_3].substring(startIndex, int_2 + 1);
            }

            return newPattern;
        }
    }

    private static int findNextIngredient(@NotNull String ingredients) {
        int i;
        for (i = 0; i < ingredients.length() && ingredients.charAt(i) == ' '; ++i) ;
        return i;
    }

    private static int findNextIngredientReverse(@NotNull String ingredients) {
        int i;
        for (i = ingredients.length() - 1; i >= 0 && ingredients.charAt(i) == ' '; --i) ;
        return i;
    }

    @Override
    public ShapedArtisRecipe read(Identifier id, JsonObject jsonObject) {
        String group = JsonHelper.getString(jsonObject, "group", "");
        Map<String, Ingredient> key = getComponents(JsonHelper.getObject(jsonObject, "key"));
        String[] pattern = combinePattern(getPattern(JsonHelper.getArray(jsonObject, "pattern")));
        int width = pattern[0].length();
        int height = pattern.length;
        DefaultedList<Ingredient> ingredients = getIngredients(pattern, key, width, height);
        ItemStack output = ShapedRecipe.outputFromJson(JsonHelper.getObject(jsonObject, "result"));
        Ingredient catalyst = JsonHelper.hasElement(jsonObject, "catalyst") ? Ingredient.fromJson(jsonObject.get("catalyst")) : Ingredient.ofStacks(ItemStack.EMPTY);
        int cost = JsonHelper.hasElement(jsonObject, "cost") ? JsonHelper.getInt(jsonObject, "cost") : 0;
        return new ShapedArtisRecipe(type, this, id, group, width, height, ingredients, output, catalyst, cost);
    }

    @Override
    public ShapedArtisRecipe read(Identifier id, @NotNull PacketByteBuf buf) {
        int width = buf.readVarInt();
        int height = buf.readVarInt();
        String group = buf.readString(32767);

        DefaultedList<Ingredient> ingredients = DefaultedList.ofSize(width * height, Ingredient.EMPTY);
        ingredients.replaceAll(ignored -> Ingredient.fromPacket(buf));

        ItemStack output = buf.readItemStack();

        Ingredient catalyst = Ingredient.fromPacket(buf);
        int cost = buf.readInt();

        return new ShapedArtisRecipe(type, this, id, group, width, height, ingredients, output, catalyst, cost);
    }

    @Override
    public void write(@NotNull PacketByteBuf buf, @NotNull ShapedArtisRecipe recipe) {
        buf.writeVarInt(recipe.getWidth());
        buf.writeVarInt(recipe.getHeight());
        buf.writeString(recipe.getGroup());

        for (Ingredient ingredient : recipe.getIngredients()) {
            ingredient.write(buf);
        }

        buf.writeItemStack(recipe.getOutput());

        recipe.getCatalyst().write(buf);
        buf.writeInt(recipe.getCatalystCost());
    }

    private String @NotNull [] getPattern(@NotNull JsonArray array) {
        String[] pattern = new String[array.size()];
        if (pattern.length > type.getHeight()) {
            throw new JsonSyntaxException("Invalid pattern for " + type.getId().toString() + ": too many rows, " + type.getHeight() + " is maximum");
        } else if (pattern.length == 0) {
            throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
        } else {
            for (int i = 0; i < pattern.length; ++i) {
                String row = JsonHelper.asString(array.get(i), "pattern[" + i + "]");
                if (row.length() > type.getWidth()) {
                    throw new JsonSyntaxException("Invalid pattern for " + type.getId().toString() + ": too many columns, " + type.getWidth() + " is maximum");
                }

                if (i > 0 && pattern[0].length() != row.length()) {
                    throw new JsonSyntaxException("Invalid pattern: each row must be the same width");
                }

                pattern[i] = row;
            }

            return pattern;
        }
    }
}
