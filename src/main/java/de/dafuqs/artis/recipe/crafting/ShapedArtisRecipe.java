package de.dafuqs.artis.recipe.crafting;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.*;
import de.dafuqs.artis.*;
import de.dafuqs.artis.api.*;
import de.dafuqs.artis.inventory.crafting.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.network.*;
import net.minecraft.network.codec.*;
import net.minecraft.recipe.*;
import net.minecraft.util.dynamic.*;
import net.minecraft.world.*;
import oshi.util.tuples.*;

import java.util.*;

public class ShapedArtisRecipe extends ArtisCraftingRecipeBase {
	
	final RawShapedArtisRecipe raw;
	
	public ShapedArtisRecipe(ArtisCraftingRecipeType type, String group, RawShapedArtisRecipe raw, IngredientStack catalyst, int catalystCost, ItemStack result) {
		super(type, group, catalyst, catalystCost, result);
		this.raw = raw;
		this.serializer = type.getShapedSerializer();
	}
	
	@Override
	public boolean matches(ArtisCraftingInventory inventory, World world) {
		if (!this.raw.matches(inventory)) {
			return false;
		}
		return this.raw.getRecipeOrientation(inventory) != null;
	}
	
	@Override
	public boolean fits(int width, int height) {
		return width >= this.raw.width() && height >= this.raw.height();
	}
	
	@Override
	public List<IngredientStack> getIngredientStacks() {
		return this.raw.ingredientStacks();
	}
	
	public RawShapedArtisRecipe getRawRecipe() {
		return this.raw;
	}
	
	@Override
	public int getWidth() {
		return this.raw.width();
	}
	
	@Override
	public int getHeight() {
		return this.raw.height();
	}
	
	@Override
	public void useUpIngredients(ArtisCraftingInventory inventory, PlayerEntity player) {
		Triplet<Integer, Integer, Boolean> orientation = this.raw.getRecipeOrientation(inventory);
		if (orientation != null) {
			this.raw.decrementIngredientStacks(inventory, orientation, player);
		}
	}
	
	@Override
	public boolean isShapeless() {
		return false;
	}
	
	public static class Serializer implements RecipeSerializer<ShapedArtisRecipe> {
		
		private static final MapCodec<ShapedArtisRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
				ArtisCraftingRecipeType.CODEC.fieldOf("type").forGetter((recipe) -> recipe.type),
				Codec.STRING.optionalFieldOf("group", "").forGetter((recipe) -> recipe.group),
				RawShapedArtisRecipe.CODEC.fieldOf("input").forGetter((recipe) -> recipe.raw),
				IngredientStack.CODEC.optionalFieldOf("catalyst_stack", IngredientStack.EMPTY).forGetter((recipe) -> recipe.catalyst),
				Codecs.NONNEGATIVE_INT.optionalFieldOf("catalyst_amount", 0).forGetter((recipe) -> recipe.catalystAmount),
				ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter((recipe) -> recipe.result)
		).apply(instance, ShapedArtisRecipe::new));
		
		public static final PacketCodec<RegistryByteBuf, ShapedArtisRecipe> PACKET_CODEC = PacketCodec.tuple(
				ArtisCraftingRecipeType.PACKET_CODEC, ShapedArtisRecipe::getType,
				PacketCodecs.STRING, ShapedArtisRecipe::getGroup,
				RawShapedRecipe.PACKET_CODEC, ShapedArtisRecipe::getRawRecipe,
				IngredientStack.PACKET_CODEC, ShapedArtisRecipe::getCatalyst,
				PacketCodecs.INTEGER, ShapedArtisRecipe::getCatalystAmount,
				ItemStack.PACKET_CODEC, ShapedArtisRecipe::getResult,
				ShapedArtisRecipe::new);
		
		public MapCodec<ShapedArtisRecipe> codec() {
			return CODEC;
		}
		
		public PacketCodec<RegistryByteBuf, ShapedArtisRecipe> packetCodec() {
			return PACKET_CODEC;
		}
		
	}
	
}
