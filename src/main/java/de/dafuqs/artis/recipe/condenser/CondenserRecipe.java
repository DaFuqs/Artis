package de.dafuqs.artis.recipe.condenser;

import de.dafuqs.artis.*;
import de.dafuqs.artis.inventory.variant_backed.*;
import de.dafuqs.artis.recipe.*;
import net.fabricmc.fabric.api.transfer.v1.item.*;
import net.fabricmc.fabric.api.transfer.v1.storage.base.*;
import de.dafuqs.matchbooks.recipe.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.recipe.*;
import net.minecraft.registry.*;
import net.minecraft.util.*;
import net.minecraft.util.collection.*;
import net.minecraft.world.*;

public class CondenserRecipe implements Recipe<Inventory> {
	
	protected final Identifier id;
	protected final String group;
	protected final IngredientStack input;
	protected final int fuelPerTick;
	protected final int timeTicks;
	protected final boolean preservesInput;
	protected final ItemStack output;
	
	public CondenserRecipe(Identifier id, String group, IngredientStack input, int fuelPerTick, int timeTicks, boolean preservesInput, ItemStack output) {
		this.id = id;
		this.group = group;
		this.input = input;
		this.output = output;
		this.fuelPerTick = fuelPerTick;
		this.preservesInput = preservesInput;
		this.timeTicks = timeTicks;
	}
	
	@Override
	public boolean matches(Inventory inv, World world) {
		if (inv instanceof VariantBackedInventory variantBackedInventory) {
			SingleVariantStorage<ItemVariant> input = variantBackedInventory.getStorage(0);
			ItemStack invStack = input.variant.toStack((int) input.amount);
			return this.input.test(invStack);
		} else {
			return this.input.getIngredient().test(inv.getStack(0));
		}
	}
	
	@Override
	public ItemStack craft(Inventory inventory, DynamicRegistryManager registryManager) {
		return null;
	}
	
	@Override
	public boolean fits(int width, int height) {
		return true;
	}
	
	@Override
	public ItemStack getOutput(DynamicRegistryManager registryManager) {
		return this.output;
	}
	
	@Override
	public ItemStack createIcon() {
		return new ItemStack(ArtisBlocks.CONDENSER_BLOCK);
	}
	
	@Override
	public Identifier getId() {
		return this.id;
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
		defaultedList.add(this.input.getIngredient());
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
	
	public ItemStack getRawOutput() {
		return this.output;
	}
	
}
