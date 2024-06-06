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
import org.jetbrains.annotations.*;
import oshi.util.tuples.*;

public class ShapedArtisRecipe extends ArtisCraftingRecipeBase {
	
	protected final int width;
	protected final int height;
	
	public ShapedArtisRecipe(ArtisCraftingRecipeType type, Identifier id, String group, int width, int height, DefaultedList<IngredientStack> ingredients, ItemStack output, IngredientStack catalyst, int catalystCost) {
		super(type, id, group, ingredients, output, catalyst, catalystCost);
		this.serializer = type.getShapedSerializer();
		
		this.width = width;
		this.height = height;
	}
	
	@Override
	public boolean matches(ArtisCraftingInventory inventory, World world) {
		if (!super.matches(inventory, world)) {
			return false;
		}
		return getRecipeOrientation(inventory) != null;
	}
	
	@Override
	public boolean fits(int width, int height) {
		return this.width <= width && this.height <= height;
	}
	
	
	public int getWidth() {
		return this.width;
	}
	
	public int getHeight() {
		return this.height;
	}
	
	@Override
	public boolean isShapeless() {
		return false;
	}
	
	private boolean matchesPattern(ArtisCraftingInventory inv, int offsetX, int offsetY, boolean flipped) {
		for (int i = 0; i < inv.getWidth(); ++i) {
			for (int j = 0; j < inv.getHeight(); ++j) {
				int k = i - offsetX;
				int l = j - offsetY;
				IngredientStack ingredientStack = IngredientStack.EMPTY;
				if (k >= 0 && l >= 0 && k < this.width && l < this.height) {
					if (flipped) {
						ingredientStack = this.ingredientStacks.get(this.width - k - 1 + l * this.width);
					} else {
						ingredientStack = this.ingredientStacks.get(k + l * this.width);
					}
				}
				
				if (!ingredientStack.matches(inv.getStack(i + j * inv.getWidth()))) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	// Triplet<XOffset, YOffset, Flipped>
	public @Nullable Triplet<Integer, Integer, Boolean> getRecipeOrientation(ArtisCraftingInventory inv) {
		for (int i = 0; i <= inv.getWidth() - this.width; ++i) {
			for (int j = 0; j <= inv.getHeight() - this.height; ++j) {
				if (this.matchesPattern(inv, i, j, true)) {
					return new Triplet<>(i, j, true);
				}
				if (this.matchesPattern(inv, i, j, false)) {
					return new Triplet<>(i, j, false);
				}
			}
		}
		return null;
	}
	
	@Override
	public void useUpIngredients(ArtisCraftingInventory inventory, PlayerEntity player) {
		Triplet<Integer, Integer, Boolean> orientation = getRecipeOrientation(inventory);
		if (orientation != null) {
			decrementIngredientStacks(inventory, orientation, player);
		}
	}
	
	protected void decrementIngredientStacks(ArtisCraftingInventory inventory, Triplet<Integer, Integer, Boolean> orientation, PlayerEntity player) {
		for (int x = 0; x < this.width; x++) {
			for (int y = 0; y < this.height; y++) {
				int ingredientStackId = orientation.getC() ? ((this.width - 1) - x) + this.width * y : x + this.width * y;
				int invStackId = (x + orientation.getA()) + inventory.getWidth() * (y + orientation.getB());
				
				IngredientStack ingredientStackAtPos = this.getIngredientStacks().get(ingredientStackId);
				ItemStack invStack = inventory.getStack(invStackId);
				
				if (!invStack.isEmpty()) {
					Item recipeReminderItem = invStack.getItem().getRecipeRemainder();
					if (recipeReminderItem == null) {
						invStack.decrement(ingredientStackAtPos.count());
					} else {
						if (inventory.getStack(invStackId).getCount() == ingredientStackAtPos.count()) {
							ItemStack remainderStack = recipeReminderItem.getDefaultStack();
							remainderStack.setCount(ingredientStackAtPos.count());
							inventory.setStack(invStackId, remainderStack);
						} else {
							inventory.getStack(invStackId).decrement(ingredientStackAtPos.count());
							ItemStack remainderStack = recipeReminderItem.getDefaultStack();
							player.giveItemStack(remainderStack);
						}
					}
				}
				
			}
		}
	}
	
	public static class Serializer implements RecipeSerializer<ShapedArtisRecipe> {
		public static final MapCodec<ShapedArtisRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
			return instance.group(Codec.STRING.optionalFieldOf("group", "").forGetter((recipe) -> {
				return recipe.group;
			}), CraftingRecipeCategory.CODEC.fieldOf("category").orElse(CraftingRecipeCategory.MISC).forGetter((recipe) -> {
				return recipe.category;
			}), RawShapedRecipe.CODEC.forGetter((recipe) -> {
				return recipe.raw;
			}), ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter((recipe) -> {
				return recipe.result;
			}), Codec.BOOL.optionalFieldOf("show_notification", true).forGetter((recipe) -> {
				return recipe.showNotification;
			})).apply(instance, ShapedArtisRecipe::new);
		});
		public static final PacketCodec<RegistryByteBuf, ShapedArtisRecipe> PACKET_CODEC = PacketCodec.ofStatic(ShapedArtisRecipe.Serializer::write, ShapedArtisRecipe.Serializer::read);
		
		public Serializer() {
		}
		
		public MapCodec<ShapedArtisRecipe> codec() {
			return CODEC;
		}
		
		public PacketCodec<RegistryByteBuf, ShapedArtisRecipe> packetCodec() {
			return PACKET_CODEC;
		}
		
		private static ShapedArtisRecipe read(RegistryByteBuf buf) {
			String string = buf.readString();
			CraftingRecipeCategory craftingRecipeCategory = buf.readEnumConstant(CraftingRecipeCategory.class);
			RawShapedRecipe rawShapedRecipe = RawShapedRecipe.PACKET_CODEC.decode(buf);
			ItemStack itemStack = ItemStack.PACKET_CODEC.decode(buf);
			boolean bl = buf.readBoolean();
			return new ShapedArtisRecipe(string, craftingRecipeCategory, rawShapedRecipe, itemStack, bl);
		}
		
		private static void write(RegistryByteBuf buf, ShapedArtisRecipe recipe) {
			buf.writeString(recipe.group);
			buf.writeEnumConstant(recipe.category);
			RawShapedRecipe.PACKET_CODEC.encode(buf, recipe.raw);
			ItemStack.PACKET_CODEC.encode(buf, recipe.result);
			buf.writeBoolean(recipe.showNotification);
		}
	}
	
}
