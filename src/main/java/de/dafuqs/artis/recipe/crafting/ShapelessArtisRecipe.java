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
import net.minecraft.recipe.book.*;
import net.minecraft.util.*;
import net.minecraft.util.collection.*;
import net.minecraft.world.*;

import java.util.*;

public class ShapelessArtisRecipe extends ArtisCraftingRecipeBase {
	
	public ShapelessArtisRecipe(ArtisCraftingRecipeType type, Identifier id, String group, DefaultedList<IngredientStack> ingredients, ItemStack output, IngredientStack catalyst, int catalystCost) {
		super(type, id, group, ingredients, output, catalyst, catalystCost);
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
		private static final MapCodec<ShapelessArtisRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(Codec.STRING.optionalFieldOf("group", "").forGetter((recipe) -> recipe.group), CraftingRecipeCategory.CODEC.fieldOf("category").orElse(CraftingRecipeCategory.MISC).forGetter((recipe) -> recipe.category), ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter((recipe) -> recipe.result), Ingredient.DISALLOW_EMPTY_CODEC.listOf().fieldOf("ingredients").flatXmap((ingredients) -> {
			Ingredient[] ingredients2 = ingredients.stream().filter((ingredient) -> !ingredient.isEmpty()).toArray(Ingredient[]::new);
			if (ingredients2.length == 0) {
				return DataResult.error(() -> "No ingredients for shapeless recipe");
			} else {
				return ingredients2.length > 9 ? DataResult.error(() -> "Too many ingredients for shapeless recipe") : DataResult.success(DefaultedList.copyOf(Ingredient.EMPTY, ingredients2));
			}
		}, DataResult::success).forGetter((recipe) -> recipe.ingredients)).apply(instance, ShapelessArtisRecipe::new));
		public static final PacketCodec<RegistryByteBuf, ShapelessArtisRecipe> PACKET_CODEC = PacketCodec.ofStatic(ShapelessArtisRecipe.Serializer::write, ShapelessArtisRecipe.Serializer::read);
		
		public Serializer() {
		}
		
		public MapCodec<ShapelessArtisRecipe> codec() {
			return CODEC;
		}
		
		public PacketCodec<RegistryByteBuf, ShapelessArtisRecipe> packetCodec() {
			return PACKET_CODEC;
		}
		
		private static ShapelessArtisRecipe read(RegistryByteBuf buf) {
			String string = buf.readString();
			CraftingRecipeCategory craftingRecipeCategory = buf.readEnumConstant(CraftingRecipeCategory.class);
			int i = buf.readVarInt();
			DefaultedList<Ingredient> defaultedList = DefaultedList.ofSize(i, Ingredient.EMPTY);
			defaultedList.replaceAll((empty) -> Ingredient.PACKET_CODEC.decode(buf));
			ItemStack itemStack = ItemStack.PACKET_CODEC.decode(buf);
			return new ShapelessArtisRecipe(string, craftingRecipeCategory, itemStack, defaultedList);
		}
		
		private static void write(RegistryByteBuf buf, ShapelessArtisRecipe recipe) {
			buf.writeString(recipe.group);
			buf.writeEnumConstant(recipe.category);
			buf.writeVarInt(recipe.ingredients.size());
			Iterator var2 = recipe.ingredients.iterator();
			
			while(var2.hasNext()) {
				Ingredient ingredient = (Ingredient)var2.next();
				Ingredient.PACKET_CODEC.encode(buf, ingredient);
			}
			
			ItemStack.PACKET_CODEC.encode(buf, recipe.result);
		}
	}
	
}
