package de.dafuqs.artis.compat.rei;

import de.dafuqs.artis.*;
import de.dafuqs.artis.api.*;
import de.dafuqs.artis.block.*;
import de.dafuqs.artis.compat.rei.condenser.*;
import de.dafuqs.artis.compat.rei.crafting.*;
import de.dafuqs.artis.inventory.condenser.*;
import de.dafuqs.artis.recipe.*;
import de.dafuqs.artis.recipe.condenser.*;
import me.shedaniel.math.*;
import me.shedaniel.rei.api.client.plugins.*;
import me.shedaniel.rei.api.client.registry.category.*;
import me.shedaniel.rei.api.client.registry.display.*;
import me.shedaniel.rei.api.client.registry.screen.*;
import me.shedaniel.rei.api.common.util.*;
import me.shedaniel.rei.plugin.common.*;
import net.fabricmc.api.*;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.registry.*;

import java.util.*;

@Environment(EnvType.CLIENT)
public class REIClientIntegration implements REIClientPlugin {
	
	public static final Map<ArtisCraftingRecipeType, ItemConvertible> iconMap = new HashMap<>();
	
	public REIClientIntegration() {
		for (ArtisTableBlock block : ArtisBlocks.ARTIS_TABLE_BLOCKS) {
			iconMap.put(block.getType(), block);
		}
	}
	
	@Override
	public void registerCategories(CategoryRegistry registry) {
		for (ArtisCraftingRecipeType type : ArtisBlocks.ARTIS_TABLE_TYPES) {
			registry.add(new ArtisRecipeCategory(type));
			
			if (type instanceof ArtisExistingItemType) {
				Item item = Registries.ITEM.get(type.getId());
				registry.addWorkstations(type.getCategoryIdentifier(), EntryStacks.of(item));
				if (type.shouldIncludeNormalRecipes()) {
					registry.addWorkstations(BuiltinPlugin.CRAFTING, EntryStacks.of(item));
				}
			} else {
				Block block = Registries.BLOCK.get(type.getId());
				registry.addWorkstations(type.getCategoryIdentifier(), EntryStacks.of(block));
				if (type.shouldIncludeNormalRecipes()) {
					registry.addWorkstations(BuiltinPlugin.CRAFTING, EntryStacks.of(block));
				}
			}
		}
		
		registry.add(new CondenserRecipeCategory());
		registry.addWorkstations(ArtisPlugins.CONDENSER, EntryStacks.of(ArtisBlocks.CONDENSER_BLOCK));
	}
	
	@Override
	public void registerDisplays(DisplayRegistry registry) {
		for (ArtisCraftingRecipeType type : ArtisBlocks.ARTIS_TABLE_TYPES) {
			registry.registerRecipeFiller(ArtisCraftingRecipe.class, type, ArtisRecipeDisplay::new);
		}
		
		registry.registerRecipeFiller(CondenserRecipe.class, ArtisRecipeTypes.CONDENSER, CondenserRecipeDisplay::new);
	}
	
	/**
	 * Where in the screens gui the player has to click
	 * to get to the recipe overview
	 */
	@Override
	public void registerScreens(ScreenRegistry registry) {
		// TODO: since this one screen handles all different sizes there needs to be a better way to handle this
		//for (ArtisTableType type : Artis.ARTIS_TABLE_TYPES) {
		//registry.registerContainerClickArea(type.getREIClickArea(), ArtisCraftingScreen.class, type.getCategoryIdentifier(), BuiltinPlugin.CRAFTING);
		//}
		registry.registerContainerClickArea(new Rectangle(81, 34, 21, 16), CondenserScreen.class, ArtisPlugins.CONDENSER);
	}
	
}
