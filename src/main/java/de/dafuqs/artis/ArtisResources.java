package de.dafuqs.artis;

import de.dafuqs.artis.api.*;
import de.dafuqs.artis.block.*;
import net.devtech.arrp.api.*;
import net.devtech.arrp.json.blockstate.*;
import net.devtech.arrp.json.lang.*;
import net.devtech.arrp.json.models.*;
import net.devtech.arrp.json.tags.*;
import net.minecraft.registry.*;
import net.minecraft.util.*;
import org.jetbrains.annotations.*;

import java.util.*;

import static net.devtech.arrp.json.loot.JLootTable.*;

public class ArtisResources {
	
	public static final RuntimeResourcePack RESOURCE_PACK = RuntimeResourcePack.create("artis:resources");
	public static final JLang translations = JLang.lang();
	public static final HashMap<Identifier, JTag> blockTags = new HashMap<>();
	
	public static void registerDataForTable(@NotNull ArtisCraftingRecipeType artisCraftingRecipeType, @NotNull ArtisTableBlock block) {
		// loot table (drops)
		RESOURCE_PACK.addLootTable(block.getLootTableKey().getValue(),
				loot("minecraft:block")
						.pool(pool()
								.rolls(1)
								.entry(entry()
										.type("minecraft:item")
										.name(Registries.ITEM.getId(block.asItem()).toString()))
								.condition(predicate("minecraft:survives_explosion"))));
		
		// localisation
		translations.entry(artisCraftingRecipeType.getTranslationString(), artisCraftingRecipeType.getRawName());
		translations.entry(artisCraftingRecipeType.getREITranslationString(), artisCraftingRecipeType.getRawName() + " Crafting");
		translations.entry(artisCraftingRecipeType.getEMITranslationString(), artisCraftingRecipeType.getRawName() + " Crafting");
		
		// block tags (like mineable / break by tool, if set via the config)
		for (Identifier identifier : artisCraftingRecipeType.getBlockTags()) {
			if (blockTags.containsKey(identifier)) {
				blockTags.get(identifier).add(artisCraftingRecipeType.getId());
			} else {
				blockTags.put(identifier, JTag.tag().add(artisCraftingRecipeType.getId()));
			}
		}
		
		// block and item models
		JBlockModel blockModel = JState.model(new Identifier(Artis.MOD_ID, "block/table" + (artisCraftingRecipeType.hasColor() ? "_overlay" : "")));
		JModel model = JModel.model(new Identifier(Artis.MOD_ID, "block/table" + (artisCraftingRecipeType.hasColor() ? "_overlay" : "")));
		RESOURCE_PACK.addBlockState(JState.state(JState.variant(blockModel)), new Identifier(Artis.MOD_ID, artisCraftingRecipeType.getTableIDPath()));
		RESOURCE_PACK.addModel(model, new Identifier(Artis.MOD_ID, "item/" + artisCraftingRecipeType.getTableIDPath()));
	}
	
	public static void registerPack() {
		RESOURCE_PACK.addLang(new Identifier(Artis.MOD_ID, "en_us"), translations);
		for (Map.Entry<Identifier, JTag> tags : blockTags.entrySet()) {
			RESOURCE_PACK.addTag(tags.getKey(), tags.getValue());
		}
		
		RRPCallback.BEFORE_VANILLA.register(a -> a.add(RESOURCE_PACK));
	}
	
	public static void registerDataForExistingBlock(@NotNull ArtisExistingBlockType artisTableType) {
		String tableIdPath = artisTableType.getId().getPath();
		String tableIdNameSpace = artisTableType.getId().getNamespace();
		translations.entry("emi.category." + tableIdNameSpace + "." + tableIdPath, artisTableType.getRawName() + " Crafting");
		translations.entry("recipe.category." + tableIdPath, artisTableType.getRawName() + " Crafting");
	}
	
	public static void registerDataForExistingItem(@NotNull ArtisExistingItemType artisTableType) {
		String tableIdPath = artisTableType.getId().getPath();
		String tableIdNameSpace = artisTableType.getId().getNamespace();
		translations.entry("emi.category." + tableIdNameSpace + "." + tableIdPath, artisTableType.getRawName() + " Crafting");
		translations.entry("recipe.category." + tableIdPath, artisTableType.getRawName() + " Crafting");
	}
	
}
