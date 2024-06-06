package de.dafuqs.artis.inventory;

import de.dafuqs.artis.*;
import de.dafuqs.artis.api.*;
import de.dafuqs.artis.inventory.condenser.*;
import de.dafuqs.artis.inventory.crafting.*;
import net.fabricmc.fabric.api.blockrenderlayer.v1.*;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.fabricmc.fabric.api.screenhandler.v1.*;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.render.*;
import net.minecraft.registry.*;
import net.minecraft.resource.featuretoggle.*;
import net.minecraft.screen.*;
import net.minecraft.util.*;

public class ArtisScreenHandlers {
	
	public static Identifier CONDENSER_ID = new Identifier(Artis.MODID, "condenser");
	public static ScreenHandlerType<CondenserScreenHandler> CONDENSER_SCREEN_HANDLER;
	
	public static <T extends ScreenHandler> ScreenHandlerType<T> registerSimple(Identifier id, ScreenHandlerType.Factory<T> factory) {
		ScreenHandlerType<T> type = new ScreenHandlerType<>(factory, FeatureSet.empty());
		return Registry.register(Registries.SCREEN_HANDLER, id, type);
	}
	
	public static <T extends ScreenHandler> ScreenHandlerType<T> registerExtended(Identifier id, ExtendedScreenHandlerType.ExtendedFactory<T> factory) {
		ScreenHandlerType<T> type = new ExtendedScreenHandlerType<>(factory);
		return Registry.register(Registries.SCREEN_HANDLER, id, type);
	}
	
	public static void register() {
		CONDENSER_SCREEN_HANDLER = registerSimple(CONDENSER_ID, CondenserScreenHandler::new);
	}
	
	public static void registerClient() {
		HandledScreens.register(CONDENSER_SCREEN_HANDLER, CondenserScreen::new);
		
		for (ArtisCraftingRecipeType type : ArtisBlocks.ARTIS_TABLE_TYPES) {
			ScreenHandlerType<ArtisRecipeProvider> screenHandlerType = (ScreenHandlerType<ArtisRecipeProvider>) Registries.SCREEN_HANDLER.get(type.getId());
			ScreenRegistry.<ArtisRecipeProvider, ArtisCraftingScreen>register(screenHandlerType, ArtisCraftingScreen::new);
			
			if (!(type instanceof ArtisExistingBlockType) && !(type instanceof ArtisExistingItemType)) {
				if (type.hasColor()) {
					ColorProviderRegistry.BLOCK.register((state, world, pos, index) -> type.getColor(), Registries.BLOCK.get(type.getId()));
					ColorProviderRegistry.ITEM.register((stack, index) -> type.getColor(), Registries.ITEM.get(type.getId()));
				}
				
				BlockRenderLayerMap.INSTANCE.putBlock(Registries.BLOCK.get(type.getId()), RenderLayer.getCutout());
			}
		}
	}
	
}
