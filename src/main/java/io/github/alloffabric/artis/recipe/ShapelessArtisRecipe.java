package io.github.alloffabric.artis.recipe;

import io.github.alloffabric.artis.api.ArtisCraftingRecipe;
import io.github.alloffabric.artis.api.ArtisTableType;
import io.github.alloffabric.artis.api.SpecialCatalyst;
import io.github.alloffabric.artis.compat.nbtcrafting.NbtCraftingUtil;
import io.github.alloffabric.artis.inventory.ArtisCraftingInventory;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class ShapelessArtisRecipe extends ShapelessRecipe implements ArtisCraftingRecipe {
    private final RecipeType type;
    private final RecipeSerializer serializer;
    private final Ingredient catalyst;
    private final int catalystCost;

    public ShapelessArtisRecipe(RecipeType type, RecipeSerializer serializer, Identifier id, String group, DefaultedList<Ingredient> ingredients, ItemStack output, Ingredient catalyst, int catalystCost) {
        super(id, group, output, ingredients);
        this.type = type;
        this.serializer = serializer;
        this.catalyst = catalyst;
        this.catalystCost = catalystCost;
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    public boolean matches(CraftingInventory inventory, World world) {
        if (!(inventory instanceof ArtisCraftingInventory)) return false;
        ArtisCraftingInventory artis = (ArtisCraftingInventory) inventory;
        ItemStack toTest = artis.getCatalyst();
        if (artis.shouldCompareCatalyst()) {
            if (!catalyst.test(toTest)) return false;
            if (toTest.isDamageable()) {
                if (toTest.getMaxDamage() - toTest.getDamage() < catalystCost) return false;
            } else if (toTest.getItem() instanceof SpecialCatalyst) {
                if (!((SpecialCatalyst) toTest.getItem()).matches(toTest, catalystCost)) return false;
            } else {
                if (toTest.getCount() < catalystCost) return false;
            }
        }
        return super.matches(inventory, world);
    }

    @Override
    public ItemStack craft(CraftingInventory inv) {
        if (FabricLoader.getInstance().isModLoaded("nbtcrafting")) {
            return NbtCraftingUtil.getOutputStack(getOutput(), getIngredients(), inv);
        }
        return this.getOutput().copy();
    }

    @Override
    public RecipeType getType() {
        return type;
    }

    @Override
    public RecipeSerializer getSerializer() {
        return serializer;
    }

    @Override
    public Ingredient getCatalyst() {
        return catalyst;
    }

    @Override
    public int getCatalystCost() {
        return catalystCost;
    }

    @Override
    public int getWidth() {
        return ((ArtisTableType) type).getWidth();
    }

    @Override
    public int getHeight() {
        return ((ArtisTableType) type).getHeight();
    }

}
