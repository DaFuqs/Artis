package de.dafuqs.artis.inventory.slot;

import de.dafuqs.artis.inventory.crafting.*;
import io.github.cottonmc.cotton.gui.*;
import io.github.cottonmc.cotton.gui.widget.*;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;

public class ArtisCraftingResultSlot extends WItemSlot {
	
	private final PlayerEntity player;
	private final ArtisCraftingInventory craftingInventory;
	private final Inventory inventory;
	
	public ArtisCraftingResultSlot(PlayerEntity player, ArtisCraftingInventory craftingInventory, Inventory resultInv, int startIndex, int slotsWide, int slotsHigh, boolean big) {
		super(resultInv, startIndex, slotsWide, slotsHigh, big);
		this.player = player;
		this.craftingInventory = craftingInventory;
		this.inventory = resultInv;
	}
	
	@Override
	protected ValidatedSlot createSlotPeer(Inventory inventory, int index, int x, int y) {
		return new ValidatedArtisCraftingResultSlot(player, craftingInventory, this.inventory, index, x, y);
	}
}
