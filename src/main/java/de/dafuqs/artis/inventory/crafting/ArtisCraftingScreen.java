package de.dafuqs.artis.inventory.crafting;

import io.github.cottonmc.cotton.gui.client.*;
import net.fabricmc.api.*;
import net.minecraft.entity.player.*;
import net.minecraft.text.*;

@Environment(EnvType.CLIENT)
public class ArtisCraftingScreen extends CottonInventoryScreen<ArtisCraftingScreenHandler> {
	
	public ArtisCraftingScreen(ArtisCraftingScreenHandler gui, PlayerEntity player, Text title) {
		super(gui, player, title);
	}
	
	public ArtisCraftingScreen(ArtisCraftingScreenHandler gui, PlayerInventory inventory, Text title) {
		super(gui, inventory.player, title);
	}
	
}