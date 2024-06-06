package de.dafuqs.artis;

import de.dafuqs.artis.api.*;
import net.fabricmc.fabric.api.itemgroup.v1.*;
import net.minecraft.item.*;
import net.minecraft.registry.*;
import net.minecraft.text.*;
import net.minecraft.util.*;

public class ArtisItemGroups {
	
	public static final Identifier ARTIS_GROUP_ID = new Identifier(Artis.MOD_ID, "group");
	
	public static final ItemGroup ARTIS_GROUP = FabricItemGroup.builder()
			.displayName(Text.translatable("itemGroup.artis.group"))
			.icon(() -> {
				if (!ArtisBlocks.ARTIS_TABLE_TYPES.isEmpty()) {
					ArtisCraftingRecipeType firstTableType = ArtisBlocks.ARTIS_TABLE_TYPES.get(0);
					if (firstTableType instanceof ArtisExistingItemType) {
						return new ItemStack(Registries.ITEM.get(firstTableType.getId()));
					} else {
						return new ItemStack(Registries.BLOCK.get(firstTableType.getId()).asItem());
					}
				} else {
					return new ItemStack(ArtisBlocks.CONDENSER_BLOCK);
				}
			})
			.entries((displayContext, entries) -> {
						for (ArtisCraftingRecipeType tableType : ArtisBlocks.ARTIS_TABLE_TYPES) {
							if (tableType instanceof ArtisExistingItemType) {
								entries.add(Registries.ITEM.get(tableType.getId()));
							} else {
								entries.add(Registries.BLOCK.get(tableType.getId()).asItem());
							}
						}
						entries.add(ArtisBlocks.CONDENSER_BLOCK);
					}
			).build();
	
}
