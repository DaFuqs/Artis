package io.github.alloffabric.artis.inventory;

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class ArtisCraftingScreen extends CottonInventoryScreen<ArtisRecipeProvider> {
    public ArtisCraftingScreen(ArtisRecipeProvider gui, PlayerEntity player, Text title) {
        super(gui, player, title);
    }

    public ArtisCraftingScreen(ArtisRecipeProvider gui, PlayerInventory inventory, Text title) {
        super(gui, inventory.player, title);
    }
    
}