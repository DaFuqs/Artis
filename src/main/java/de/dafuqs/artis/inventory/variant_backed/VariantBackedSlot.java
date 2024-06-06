package de.dafuqs.artis.inventory.variant_backed;

import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.screen.slot.*;

/**
 * A slot that can be combined with a VariantBackedInventory
 * to store an amount > 64 into the slot.
 * This slot will display a max of 64 items to the user to extract at once
 * You will need a different way to show that > 64 count to the user, like syncing it via PropertyDelegate
 */
public class VariantBackedSlot extends Slot {
	
	private final int index;
	
	public VariantBackedSlot(Inventory inventory, int index, int x, int y) {
		super(inventory, index, x, y);
		this.index = index;
	}
	
	@Override
	public boolean canInsert(ItemStack stack) {
		ItemStack slotStack = inventory.getStack(index);
		return slotStack.isEmpty() || ItemStack.areEqual(slotStack, stack);
	}
	
	@Override
	public void setStack(ItemStack stack) {
		super.setStack(stack);
	}
	
	@Override
	public ItemStack insertStack(ItemStack insertStack, int insertCount) {
		if (!insertStack.isEmpty() && this.canInsert(insertStack)) {
			ItemStack existingStack = this.getStack();
			int availableAmount = Math.min(insertCount, insertStack.getCount());
			if (existingStack.isEmpty()) {
				long finalInsertAmount = Math.min(availableAmount, getMaxItemCount());
				ItemStack setStack = insertStack.split((int) finalInsertAmount);
				this.setStack(setStack);
			} else if (ItemStack.areEqual(existingStack, insertStack)) {
				long leftover = addAmount(availableAmount);
				insertStack.decrement((int) (availableAmount - leftover));
			}
		}
		markDirty();
		return insertStack;
	}
	
	public int getMaxItemCount() {
		if (this.inventory instanceof VariantBackedInventory variantBackedInventory) {
			return variantBackedInventory.getMaxCountPerStackForSlot(this.index);
		} else {
			return this.inventory.getMaxCountPerStack();
		}
	}
	
	@Override
	public ItemStack takeStack(int amount) {
		markDirty();
		return super.takeStack(amount);
	}
	
	public long addAmount(long amount) {
		if (this.inventory instanceof VariantBackedInventory variantBackedInventory) {
			return variantBackedInventory.addAmount(this.index, amount);
		}
		markDirty();
		return 0;
	}
	
}
