package de.dafuqs.artis.block;

import de.dafuqs.artis.*;
import de.dafuqs.artis.api.*;
import de.dafuqs.artis.inventory.crafting.*;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.recipe.*;
import net.minecraft.screen.*;
import net.minecraft.text.*;
import net.minecraft.util.collection.*;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.*;

public class ArtisTableBlockEntity extends LootableContainerBlockEntity implements SidedInventory, RecipeInputProvider {
	
	private ArtisCraftingRecipeType tableType;
	protected DefaultedList<ItemStack> inventory;
	
	public ArtisTableBlockEntity(BlockPos pos, BlockState state) {
		super(ArtisBlocks.ARTIS_BLOCK_ENTITY, pos, state);
	}
	
	public ArtisTableBlockEntity(@NotNull ArtisCraftingRecipeType tableType, BlockPos pos, BlockState state) {
		super(ArtisBlocks.ARTIS_BLOCK_ENTITY, pos, state);
		
		this.tableType = tableType;
		this.inventory = DefaultedList.ofSize(tableType.getWidth() + tableType.getHeight() + (tableType.hasCatalystSlot() ? 1 : 0) + 1, ItemStack.EMPTY);
	}
	
	@Override
	protected Text getContainerName() {
		return tableType.getName();
	}
	
	@Override
	protected DefaultedList<ItemStack> getHeldStacks() {
		return this.inventory;
	}
	
	@Override
	protected void setHeldStacks(DefaultedList<ItemStack> inventory) {
		this.inventory = inventory;
	}
	
	@Override
	protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
		return new ArtisCraftingScreenHandler(this.tableType.getScreenHandlerType(), this.tableType, syncId, playerInventory, ScreenHandlerContext.create(world, pos));
	}
	
	@Override
	public int size() {
		return this.inventory.size();
	}
	
	@Override
	public int[] getAvailableSlots(Direction side) {
		if (side == Direction.DOWN) {
			return new int[]{tableType.getOutputSlotIndex()};
		} else if (side == Direction.UP) {
			// something tells me there is an easier way to do this...
			int slotCount = tableType.getWidth() + tableType.getHeight();
			int[] slots = new int[slotCount];
			for (int i = 0; i < slotCount; i++) {
				slots[i] = i;
			}
			return slots;
		} else if (tableType.hasCatalystSlot()) {
			return new int[]{tableType.getCatalystSlotIndex()};
		}
		return new int[]{};
	}
	
	@Override
	public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
		return true;
	}
	
	@Override
	public boolean canExtract(int slot, ItemStack stack, Direction dir) {
		return true;
	}
	
	@Override
	public void provideRecipeInputs(RecipeMatcher finder) {
		for (ItemStack itemStack : this.inventory) {
			finder.addInput(itemStack);
		}
	}
	
}