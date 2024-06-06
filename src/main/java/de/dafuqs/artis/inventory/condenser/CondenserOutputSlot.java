package de.dafuqs.artis.inventory.condenser;

import de.dafuqs.artis.inventory.variant_backed.*;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;

public class CondenserOutputSlot extends VariantBackedSlot {
	private final PlayerEntity player;
	private int amount;
	
	public CondenserOutputSlot(PlayerEntity player, Inventory inventory, int index, int x, int y) {
		super(inventory, index, x, y);
		this.player = player;
	}
	
	public boolean canInsert(ItemStack stack) {
		return false;
	}
	
	public ItemStack takeStack(int amount) {
		if (this.hasStack()) {
			this.amount += Math.min(amount, this.getStack().getCount());
		}
		return super.takeStack(amount);
	}
	
	public void onTakeItem(PlayerEntity player, ItemStack stack) {
		this.onCrafted(stack);
		super.onTakeItem(player, stack);
	}
	
	protected void onCrafted(ItemStack stack, int amount) {
		this.amount += amount;
		this.onCrafted(stack);
	}
	
	protected void onCrafted(ItemStack stack) {
		stack.onCraftByPlayer(this.player.getWorld(), this.player, this.amount);
		this.amount = 0;
	}
	
}
