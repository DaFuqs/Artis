package de.dafuqs.artis.event;

import de.dafuqs.artis.*;
import de.dafuqs.artis.api.*;
import net.fabricmc.fabric.api.event.player.*;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.registry.*;
import net.minecraft.util.*;

public class ArtisEvents {
	
	public static void init() {
		UseBlockCallback.EVENT.register((playerEntity, world, hand, blockHitResult) -> {
			Block block = world.getBlockState(blockHitResult.getBlockPos()).getBlock();
			Identifier identifier = Registries.BLOCK.getId(block);
			if (ArtisBlocks.ARTIS_TABLE_TYPES.containsId(identifier)) {
				ArtisCraftingRecipeType type = ArtisBlocks.ARTIS_TABLE_TYPES.get(identifier);
				if (type instanceof ArtisExistingBlockType) {
					if (!world.isClient)
						playerEntity.openHandledScreen(new ArtisScreenFactory(type, block, blockHitResult));
					return ActionResult.SUCCESS;
				}
			}
			return ActionResult.PASS;
		});
		
		UseItemCallback.EVENT.register((playerEntity, world, hand) -> {
			if (!playerEntity.getStackInHand(hand).isEmpty()) {
				Item item = playerEntity.getStackInHand(hand).getItem();
				Identifier identifier = Registries.ITEM.getId(item);
				if (ArtisBlocks.ARTIS_TABLE_TYPES.containsId(identifier)) {
					ArtisCraftingRecipeType type = ArtisBlocks.ARTIS_TABLE_TYPES.get(identifier);
					if (type instanceof ArtisExistingItemType) {
						if (!world.isClient) {
							playerEntity.openHandledScreen(new ArtisScreenFactory(type, null, null));
						}
						return TypedActionResult.success(playerEntity.getStackInHand(hand));
					}
				}
			}
			return TypedActionResult.pass(playerEntity.getStackInHand(hand));
		});
	}
	
}
