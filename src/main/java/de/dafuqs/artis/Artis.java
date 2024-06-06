package de.dafuqs.artis;

import de.dafuqs.artis.event.*;
import de.dafuqs.artis.inventory.*;
import de.dafuqs.artis.recipe.*;
import net.fabricmc.api.*;
import net.minecraft.registry.*;
import org.slf4j.*;

public class Artis implements ModInitializer {
	
	public static final String MOD_ID = "artis";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	
	@Override
	public void onInitialize() {
		ArtisConfig.loadConfig();
		ArtisEvents.init();
		ArtisBlocks.register();
		ArtisRecipeTypes.register();
		ArtisScreenHandlers.register();
		
		Registry.register(Registries.ITEM_GROUP, ArtisItemGroups.ARTIS_GROUP_ID, ArtisItemGroups.ARTIS_GROUP);
	}
	
}
