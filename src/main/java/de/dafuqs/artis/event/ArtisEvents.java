package de.dafuqs.artis.event;

import de.dafuqs.artis.*;
import de.dafuqs.artis.api.ArtisExistingBlockType;
import de.dafuqs.artis.api.ArtisExistingItemType;
import de.dafuqs.artis.api.ArtisTableType;
import de.dafuqs.artis.inventory.crafting.ArtisScreenFactory;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.registry.Registry;

public class ArtisEvents {
    
    public static void init() {
        UseBlockCallback.EVENT.register((playerEntity, world, hand, blockHitResult) -> {
            Block block = world.getBlockState(blockHitResult.getBlockPos()).getBlock();
            Identifier identifier = Registry.BLOCK.getId(block);
            if (ArtisBlocks.ARTIS_TABLE_TYPES.containsId(identifier)) {
                ArtisTableType type = ArtisBlocks.ARTIS_TABLE_TYPES.get(identifier);
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
                Identifier identifier = Registry.ITEM.getId(item);
                if (ArtisBlocks.ARTIS_TABLE_TYPES.containsId(identifier)) {
                    ArtisTableType type = ArtisBlocks.ARTIS_TABLE_TYPES.get(identifier);
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
