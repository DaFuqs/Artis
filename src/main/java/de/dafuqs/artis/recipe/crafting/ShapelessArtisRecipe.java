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
import net.minecraft.util.collection.*;
import net.minecraft.util.dynamic.*;
import net.minecraft.world.*;

public class ShapelessArtisRecipe extends ArtisCraftingRecipeBase {
	
	protected DefaultedList<IngredientStack> ingredientStacks;
	
	public ShapelessArtisRecipe(ArtisCraftingRecipeType type, String group, DefaultedList<IngredientStack> ingredientStacks, IngredientStack catalyst, int catalystCost, ItemStack result) {
		super(type, group, catalyst, catalystCost, result);
		this.ingredientStacks = ingredientStacks;
		this.serializer = type.getShapelessSerializer();
	}
	
	@Override
	public boolean matches(ArtisCraftingInventory inventory, World world) {
		if (!super.matches(inventory, world)) {
			return false;
		}
		
		RecipeMatcher recipeMatcher = new RecipeMatcher();
		int foundCount = 0;
		for (int slot = 0; slot < inventory.size(); ++slot) {
			ItemStack itemStack = inventory.getStack(slot);
			if (!itemStack.isEmpty()) {
				++foundCount;
				recipeMatcher.addInput(itemStack, 1);
			}
		}
		
		return foundCount == this.ingredientStacks.size() && recipeMatcher.match(this, null);
	}
	
	@Override
	public boolean fits(int width, int height) {
		return this.ingredientStacks.size() >= width * height;
	}
	
	@Override
	public DefaultedList<IngredientStack> getIngredientStacks() {
		return this.ingredientStacks;
	}
	
	@Override
	public int getWidth() {
		return this.getType().getWidth();
	}
	
	@Override
	public int getHeight() {
		return this.getType().getHeight();
	}
	
	@Override
	public boolean isShapeless() {
		return true;
	}
	
	@Override
	public void useUpIngredients(ArtisCraftingInventory inventory, PlayerEntity player) {
		for (IngredientStack ingredientStack : this.ingredientStacks) {
			for (int slot = 0; slot < inventory.size(); slot++) {
				ItemStack slotStack = inventory.getStack(slot);
				if (ingredientStack.matches(slotStack)) {
					ItemStack remainder = slotStack.getRecipeRemainder();
					slotStack.decrement(ingredientStack.count());
					if (slotStack.isEmpty()) {
						inventory.setStack(slot, remainder);
					} else {
						player.dropStack(remainder);
					}
					break;
				}
			}
		}
	}
	
	public static class Serializer implements RecipeSerializer<ShapelessArtisRecipe> {
		
		private static final MapCodec<ShapelessArtisRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
				ArtisCraftingRecipeType.CODEC.fieldOf("type").forGetter((recipe) -> recipe.type),
				Codec.STRING.optionalFieldOf("group", "").forGetter((recipe) -> recipe.group),
				IngredientStack.CODEC.listOf().fieldOf("input").flatXmap(ingredientStacks -> {
					IngredientStack[] results = ingredientStacks.stream().filter((ingredient) -> !ingredient.ingredient().isEmpty()).toArray(IngredientStack[]::new);
					if (results.length == 0) {
						return DataResult.error(() -> "No ingredients for shapeless recipe");
					} else {
						return results.length > maxSize ? DataResult.error(() -> "Too many ingredients for shapeless recipe") : DataResult.success(DefaultedList.copyOf(IngredientStack.EMPTY, results));
					}
				}, DataResult::success).forGetter((recipe) -> recipe.ingredientStacks),
				IngredientStack.CODEC.optionalFieldOf("catalyst_stack", IngredientStack.EMPTY).forGetter((recipe) -> recipe.catalyst),
				Codecs.NONNEGATIVE_INT.optionalFieldOf("catalyst_amount", 0).forGetter((recipe) -> recipe.catalystAmount),
				ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter((recipe) -> recipe.result)
		).apply(instance, ShapelessArtisRecipe::new));
		
		public static final PacketCodec<RegistryByteBuf, ShapelessArtisRecipe> PACKET_CODEC = PacketCodec.tuple(
				ArtisCraftingRecipeType.PACKET_CODEC, ShapelessArtisRecipe::getType,
				PacketCodecs.STRING, ShapelessArtisRecipe::getGroup,
				IngredientStack.PACKET_CODEC, ShapelessArtisRecipe::getIngredientStacks,
				IngredientStack.PACKET_CODEC, ShapelessArtisRecipe::getCatalyst,
				PacketCodecs.INTEGER, ShapelessArtisRecipe::getCatalystAmount,
				ItemStack.PACKET_CODEC, ShapelessArtisRecipe::getResult,
				ShapelessArtisRecipe::new);
		
		public MapCodec<ShapelessArtisRecipe> codec() {
			return CODEC;
		}
		
		public PacketCodec<RegistryByteBuf, ShapelessArtisRecipe> packetCodec() {
			return PACKET_CODEC;
		}
		
	}
	
}
