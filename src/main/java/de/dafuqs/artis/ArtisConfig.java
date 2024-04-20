package de.dafuqs.artis;

import com.google.gson.*;
import de.dafuqs.artis.api.*;
import net.fabricmc.fabric.api.object.builder.v1.block.*;
import net.fabricmc.loader.api.*;
import net.minecraft.block.*;
import net.minecraft.registry.*;
import net.minecraft.util.*;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.*;

import java.io.*;
import java.util.*;

public class ArtisConfig {
	
	public static final Identifier ARTIS_TABLE_BLOCK_TAG_IDENTIFIER = new Identifier(Artis.MODID, "blocks/crafting_tables");
	
	public static void loadConfig() {
		try {
			File file = FabricLoader.getInstance().getConfigDir().resolve("artis-recrafted.json5").toFile();
			if (!file.exists()) {
				Artis.log(Level.WARN, "Config file not found! Generating an empty file.");
				file.createNewFile();
				FileOutputStream out = new FileOutputStream(file, false);
				out.write(DEFAULT_CONFIG.getBytes());
				out.flush();
				out.close();
				return;
			}
			
			JsonObject json = JsonHelper.deserialize(new FileReader(file), true);
			loadEntries(json);
			
		} catch (Exception e) {
			Artis.log(Level.ERROR, "Error loading config: " + e.getMessage());
		}
	}
	
	private static void loadEntries(@NotNull JsonObject json) {
		List<String> keys = new ArrayList<>(json.keySet());
		Collections.sort(keys);
		for (String key : keys) {
			if (ArtisBlocks.ARTIS_TABLE_TYPES.containsId(new Identifier(key))) {
				Artis.log(Level.ERROR, "Table type named " + key + " already exists, skipping it...");
				continue;
			}
			JsonElement elem = json.get(key);
			if (elem instanceof JsonObject config) {
				ArtisCraftingRecipeType type = getType(key, config);
				Block.Settings settings;
				if (config.has("settings")) {
					try {
						settings = parseSettings(config.get("settings").getAsJsonObject());
					} catch (Exception e) {
						Artis.log(Level.ERROR, "Table type named " + key + " has invalid block settings set. Using defaults." + e.getMessage());
						settings = FabricBlockSettings.copyOf(Blocks.CRAFTING_TABLE);
					}
				} else {
					settings = FabricBlockSettings.copyOf(Blocks.CRAFTING_TABLE);
				}
				ArtisBlocks.registerTable(type, settings);
			}
		}
		
		ArtisResources.registerPack();
	}
	
	public static Block.@NotNull Settings parseSettings(com.google.gson.JsonObject json) {
		if (json == null) {
			Artis.log(Level.ERROR, "Cannot parse block settings that aren't a json object!");
			return FabricBlockSettings.copyOf(Blocks.CRAFTING_TABLE);
		}
		FabricBlockSettings settings;
		if (json.has("copy")) {
			String blockSettingsOf = json.get("copy").getAsString();
			Block block = Registries.BLOCK.get(Identifier.tryParse(blockSettingsOf));
			if (block != Blocks.AIR) {
				settings = FabricBlockSettings.copyOf(block);
			} else {
				Artis.log(Level.ERROR, "Specified Block \"" + blockSettingsOf + "\" does not exist. Falling back to Crafting Table...");
				settings = FabricBlockSettings.copyOf(Blocks.CRAFTING_TABLE);
			}
		} else {
			settings = FabricBlockSettings.copyOf(Blocks.CRAFTING_TABLE);
		}
		
		if (JsonHelper.getBoolean(json, "requires_tool", false)) {
			settings.requiresTool();
		}
		if (JsonHelper.getBoolean(json,"collidable", false)) {
			settings.collidable(true);
		}
		if (JsonHelper.getBoolean(json, "non_opaque", false)) {
			settings.nonOpaque();
		}
		if (json.has("light_level")) {
			settings.luminance(JsonHelper.getInt(json, "light_level", 0));
		}
		if (json.has("hardness")) {
			settings.hardness(JsonHelper.getFloat(json, "hardness", 0));
		}
		if (json.has("resistance")) {
			settings.resistance(JsonHelper.getFloat(json, "resistance", 0));
		}
		if (json.has("slipperiness")) {
			settings.slipperiness(JsonHelper.getFloat(json, "slipperiness", 0));
		}
		if (json.has("break_instantly")) {
			settings.breakInstantly();
		}
		return settings;
	}
	
	@Contract("_, _ -> new")
	static @NotNull ArtisCraftingRecipeType getType(@NotNull String key, @NotNull JsonObject json) {
		String tableType = JsonHelper.getString(json,  "type", "normal");
		Identifier id;
		String name;
		boolean blockEntity;
		if (!key.contains(":")) {
			id = new Identifier(Artis.MODID, key);
			name = JsonHelper.getString(json,  "display_name");
			
			blockEntity = JsonHelper.getBoolean(json, "block_entity", false);
		} else {
			id = new Identifier(key);
			if (json.has("display_name")) {
				name = JsonHelper.getString(json, "display_name");
			} else {
				if (tableType.equals("existing_block")) {
					name = Language.getInstance().get(Registries.BLOCK.get(id).getName().getString());
				} else if (tableType.equals("existing_item")) {
					name = Language.getInstance().get(Registries.ITEM.get(id).getName().getString());
				} else {
					name = key;
				}
			}
			
			blockEntity = false;
		}
		int width = JsonHelper.getInt(json, "width", 3);
		int height = JsonHelper.getInt(json, "height", 3);
		if (width > 7) {
			Artis.log(Level.WARN, "Only tables up to 7 columns are supported. Anything higher may break visually.");
			if (width > 9) {
				Artis.log(Level.ERROR, "Table type named " + key + " has too many columns, clamping it to 9");
				width = 9;
			}
		}
		if (height > 7) {
			Artis.log(Level.WARN, "Only tables up to 7 rows are supported. Anything higher may, and likely will, break visually.");
			if (height > 9) {
				Artis.log(Level.ERROR, "Table type named " + key + " has too many rows, clamping it to 9");
				height = 9;
			}
		}
		boolean catalystSlot = JsonHelper.getBoolean(json, "catalyst_slot", false);
		boolean includeNormalRecipes = JsonHelper.getBoolean(json, "normal_recipes", false);
		
		List<Identifier> blockTags = new ArrayList<>();
		blockTags.add(ARTIS_TABLE_BLOCK_TAG_IDENTIFIER);
		if (json.has("tags")) {
			JsonArray blockTagArray = json.get("tags").getAsJsonArray();
			for (JsonElement currentElement : blockTagArray) {
				String currentString = currentElement.getAsString();
				Identifier identifier = Identifier.tryParse(currentString);
				
				if (identifier == null) {
					Artis.log(Level.WARN, "Tag " + currentString + " could not be applied. Valid identifier?");
				} else {
					blockTags.add(identifier);
				}
			}
		}
		
		if (tableType.equals("existing_block")) {
			if (json.has("color")) {
				return new ArtisExistingBlockType(id, name, width, height, blockEntity, catalystSlot, includeNormalRecipes, extractColor(json), blockTags);
			}
			return new ArtisExistingBlockType(id, name, width, height, blockEntity, catalystSlot, includeNormalRecipes, blockTags);
		} else if (tableType.equals("existing_item")) {
			if (json.has("color")) {
				return new ArtisExistingItemType(id, name, width, height, catalystSlot, includeNormalRecipes, extractColor(json), blockTags);
			}
			return new ArtisExistingItemType(id, name, width, height, catalystSlot, includeNormalRecipes, blockTags);
		}
		if (json.has("color")) {
			return new ArtisCraftingRecipeType(id, name, width, height, blockEntity, catalystSlot, includeNormalRecipes, extractColor(json), blockTags);
		}
		
		return new ArtisCraftingRecipeType(id, name, width, height, blockEntity, catalystSlot, includeNormalRecipes, blockTags);
	}
	
	private static @NotNull Integer extractColor(@NotNull JsonObject json) {
		return Integer.decode(JsonHelper.getString(json, "color").replace("#", "0x"));
	}
	
	private static final String DEFAULT_CONFIG = """
{
  "minecraft:fletching_table": {
    "type": "existing_block",
    "normal_recipes": true,
    "width": 1,
    "height": 3,
    "catalyst_slot": true
  },
  "minecraft:stick": {
    "type": "existing_item",
    "width": 2,
    "height": 3,
    "catalyst_slot": false
  },
  "test_table43": {
    "display_name": "Test Table 4x3",
    "width": 4,
    "height": 3,
    "block_entity": true,
    "settings": {
      "copy": "minecraft:crafting_table",
      "non_opaque": true,
      "hardness": 4,
      "resistance": 3
    },
    "requires_tool": true,
    "tags": [
      "minecraft:blocks/needs_stone_tool",
      "minecraft:blocks/mineable/pickaxe"
    ]
  },
  "test_table66": {
    "display_name": "Test Table 6x6",
    "width": 6,
    "height": 6,
    "normal_recipes": true,
    "block_entity": false,
    "color": "#7F76D1",
    "settings": {
      "material": "wood",
      "non_opaque": true
    },
    "catalyst_slot": true,
    "tags": [
      "minecraft:blocks/needs_stone_tool",
      "minecraft:blocks/mineable/pickaxe"
    ]
  },
  "test_table77": {
    "display_name": "Test Table 7x7",
    "width": 7,
    "height": 7,
    "block_entity": true,
    "color": "#7F76D1",
    "settings": {
      "material": "wood",
      "non_opaque": true
    },
    "catalyst_slot": true,
    "tags": [
      "minecraft:blocks/needs_stone_tool",
      "minecraft:blocks/mineable/pickaxe"
    ]
  }
}
""";
	
}
