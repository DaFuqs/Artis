package de.dafuqs.artis.api;

import com.mojang.serialization.*;
import de.dafuqs.artis.*;
import de.dafuqs.artis.compat.rei.crafting.*;
import de.dafuqs.artis.inventory.crafting.*;
import de.dafuqs.artis.recipe.crafting.*;
import me.shedaniel.math.*;
import me.shedaniel.rei.api.common.category.*;
import net.fabricmc.fabric.api.screenhandler.v1.*;
import net.minecraft.network.*;
import net.minecraft.network.codec.*;
import net.minecraft.recipe.*;
import net.minecraft.registry.*;
import net.minecraft.screen.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class ArtisCraftingRecipeType implements RecipeType<ArtisCraftingRecipe> {
	
	public static final Codec<ArtisCraftingRecipeType> CODEC = ;
	public static final PacketCodec<RegistryByteBuf, ArtisCraftingRecipeType> PACKET_CODEC = ;
	
	private final Identifier id;
	private final String name;
	private final int width;
	private final int height;
	private final boolean blockEntity;
	private final boolean catalystSlot;
	private final boolean includeNormalRecipes;
	private final RecipeSerializer<ShapedArtisRecipe> shaped;
	private final RecipeSerializer<ShapelessArtisRecipe> shapeless;
	private final List<Identifier> blockTags;
	private final ScreenHandlerType<ArtisCraftingScreenHandler> screenHandlerType;
	private int color = 0;
	private boolean hasColor = false;
	
	public ArtisCraftingRecipeType(Identifier id, String name, int width, int height, boolean blockEntity, boolean catalystSlot, boolean includeNormalRecipes, int color, List<Identifier> blockTags) {
		this(id, name, width, height, blockEntity, catalystSlot, includeNormalRecipes, blockTags);
		this.color = 0xFF000000 | color;
		this.hasColor = true;
	}
	
	public ArtisCraftingRecipeType(@NotNull Identifier id, String name, int width, int height, boolean blockEntity, boolean catalystSlot, boolean includeNormalRecipes, List<Identifier> blockTags) {
		this.id = id;
		this.name = name;
		this.width = width;
		this.height = height;
		this.blockEntity = blockEntity;
		this.catalystSlot = catalystSlot;
		this.includeNormalRecipes = includeNormalRecipes;
		Identifier shapedId = new Identifier(id.getNamespace(), id.getPath() + "_shaped");
		Identifier shapelessId = new Identifier(id.getNamespace(), id.getPath() + "_shapeless");
		this.shaped = Registry.register(Registries.RECIPE_SERIALIZER, shapedId, new ShapedArtisRecipe.Serializer());
		this.shapeless = Registry.register(Registries.RECIPE_SERIALIZER, shapelessId, new ShapelessArtisRecipe.Serializer());
		this.blockTags = blockTags;
		this.screenHandlerType = Registry.register(Registries.SCREEN_HANDLER, id, ArtisCraftingScreenHandler::new);
	}
	
	public Identifier getId() {
		return id;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public ScreenHandlerType<ArtisCraftingScreenHandler> getScreenHandlerType() {
		return this.screenHandlerType;
	}
	
	public boolean hasBlockEntity() {
		return blockEntity;
	}
	
	public boolean hasCatalystSlot() {
		return catalystSlot;
	}
	
	public boolean shouldIncludeNormalRecipes() {
		return includeNormalRecipes;
	}
	
	public int getCatalystSlotIndex() {
		if(hasCatalystSlot()) {
			return getWidth() * getHeight() + 1;
		}
		return -1;
	}
	
	public int getOutputSlotIndex() {
		return getWidth() * getHeight() + (hasCatalystSlot() ? 1 : 0) + 1;
	}
	
	public boolean hasColor() {
		return hasColor;
	}
	
	public int getColor() {
		return color;
	}
	
	public List<Identifier> getBlockTags() {
		return this.blockTags;
	}
	
	public CategoryIdentifier<ArtisRecipeDisplay> getCategoryIdentifier() {
		return CategoryIdentifier.of(id);
	}
	
	public String getRawName() {
		return this.name;
	}
	
	public Text getName() {
		return Text.translatable(getTranslationString());
	}
	
	public String getTableIDPath() {
		return getId().getPath();
	}
	
	public String getTranslationString() {
		return "block." + Artis.MOD_ID + "." + getTableIDPath();
	}
	
	public String getREITranslationString() {
		return "recipe.category." + getTableIDPath();
	}
	
	public String getEMITranslationString() {
		return "emi.category." + getId().getNamespace() + "." + getId().getPath();
	}
	
	@Override
	public String toString() {
		return this.id.toString();
	}
	
	public RecipeSerializer<ShapelessArtisRecipe> getShapelessSerializer() {
		return this.shapeless;
	}
	
	public RecipeSerializer<ShapedArtisRecipe> getShapedSerializer() {
		return this.shaped;
	}
	
}
