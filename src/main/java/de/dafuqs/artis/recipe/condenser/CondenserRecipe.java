package de.dafuqs.artis.recipe.condenser;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.*;
import de.dafuqs.artis.*;
import de.dafuqs.artis.inventory.variant_backed.*;
import de.dafuqs.artis.recipe.*;
import net.fabricmc.fabric.api.transfer.v1.item.*;
import net.fabricmc.fabric.api.transfer.v1.storage.base.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.network.*;
import net.minecraft.network.codec.*;
import net.minecraft.recipe.*;
import net.minecraft.registry.*;
import net.minecraft.util.collection.*;
import net.minecraft.util.dynamic.*;
import net.minecraft.world.*;

public class CondenserRecipe implements Recipe<Inventory> {
	
	protected final String group;
	protected final IngredientStack input;
	protected final int fuelPerTick;
	protected final int timeTicks;
	protected final boolean preservesInput;
	protected final ItemStack result;
	
	public CondenserRecipe(String group, IngredientStack input, int fuelPerTick, int timeTicks, boolean preservesInput, ItemStack result) {
		this.group = group;
		this.input = input;
		this.result = result;
		this.fuelPerTick = fuelPerTick;
		this.preservesInput = preservesInput;
		this.timeTicks = timeTicks;
	}
	
	@Override
	public boolean matches(Inventory inv, World world) {
		if (inv instanceof VariantBackedInventory variantBackedInventory) {
			SingleVariantStorage<ItemVariant> input = variantBackedInventory.getStorage(0);
			ItemStack invStack = input.variant.toStack((int) input.amount);
			return this.input.matches(invStack);
		} else {
			return this.input.ingredient().test(inv.getStack(0));
		}
	}
	
	@Override
	public ItemStack craft(Inventory inventory, RegistryWrapper.WrapperLookup lookup) {
		return this.result.copy();
	}
	
	@Override
	public boolean fits(int width, int height) {
		return true;
	}
	
	@Override
	public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup) {
		return this.result;
	}
	
	@Override
	public ItemStack createIcon() {
		return new ItemStack(ArtisBlocks.CONDENSER_BLOCK);
	}
	
	@Override
	public String getGroup() {
		return this.group;
	}
	
	@Override
	public RecipeSerializer<?> getSerializer() {
		return ArtisRecipeTypes.CONDENSER_RECIPE_SERIALIZER;
	}
	
	@Override
	public RecipeType<?> getType() {
		return ArtisRecipeTypes.CONDENSER;
	}
	
	// use getInput() where possible
	@Deprecated
	@Override
	public DefaultedList<Ingredient> getIngredients() {
		DefaultedList<Ingredient> defaultedList = DefaultedList.of();
		defaultedList.add(this.input.ingredient());
		return defaultedList;
	}
	
	public IngredientStack getInput() {
		return input;
	}
	
	public int getFuelPerTick() {
		return fuelPerTick;
	}
	
	public int getTimeTicks() {
		return timeTicks;
	}
	
	public boolean preservesInput() {
		return preservesInput;
	}
	
	public ItemStack getResult() {
		return this.result;
	}
	
	public static class Serializer implements RecipeSerializer<CondenserRecipe> {
		
		private static final MapCodec<CondenserRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
				Codec.STRING.optionalFieldOf("group", "").forGetter((recipe) -> recipe.group),
				IngredientStack.CODEC.fieldOf("input").forGetter((recipe) -> recipe.input),
				Codecs.NONNEGATIVE_INT.fieldOf("fuel_per_tick").forGetter((recipe) -> recipe.fuelPerTick),
				Codecs.POSITIVE_INT.fieldOf("time").forGetter((recipe) -> recipe.timeTicks),
				Codec.BOOL.fieldOf("preserves_input").forGetter((recipe) -> recipe.preservesInput),
				ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter((recipe) -> recipe.result)
		).apply(instance, CondenserRecipe::new));
		
		public static final PacketCodec<RegistryByteBuf, CondenserRecipe> PACKET_CODEC = PacketCodec.tuple(
				PacketCodecs.STRING, CondenserRecipe::getGroup,
				IngredientStack.PACKET_CODEC, CondenserRecipe::getInput,
				PacketCodecs.INTEGER, CondenserRecipe::getFuelPerTick,
				PacketCodecs.INTEGER, CondenserRecipe::getTimeTicks,
				PacketCodecs.BOOL, CondenserRecipe::preservesInput,
				ItemStack.PACKET_CODEC, CondenserRecipe::getResult,
				CondenserRecipe::new);
		
		public MapCodec<CondenserRecipe> codec() {
			return CODEC;
		}
		
		public PacketCodec<RegistryByteBuf, CondenserRecipe> packetCodec() {
			return PACKET_CODEC;
		}
		
	}
	
}
