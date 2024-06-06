package de.dafuqs.artis.block;

import de.dafuqs.artis.api.*;
import de.dafuqs.artis.inventory.crafting.*;
import net.fabricmc.fabric.api.screenhandler.v1.*;
import net.minecraft.block.*;
import net.minecraft.entity.player.*;
import net.minecraft.network.*;
import net.minecraft.registry.*;
import net.minecraft.screen.*;
import net.minecraft.server.network.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import net.minecraft.util.hit.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import org.jetbrains.annotations.*;

public class ArtisTableBlock extends Block implements ExtendedScreenHandlerFactory {
	
	private final ArtisCraftingRecipeType type;
	
	public ArtisTableBlock(ArtisCraftingRecipeType type, Block.Settings settings) {
		super(settings);
		this.type = type;
	}
	
	public ArtisCraftingRecipeType getType() {
		return type;
	}
	
	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (!player.isSneaking()) {
			if (!world.isClient()) {
				player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
			}
			return ActionResult.SUCCESS;
		}
		return ActionResult.PASS;
	}
	
	@Override
	public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
		return this;
	}
	
	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
		return new ArtisCraftingScreenHandler(Registries.SCREEN_HANDLER.get(type.getId()), type, syncId, player, ScreenHandlerContext.create(player.getWorld(), player.getBlockPos()));
	}
	
	@Override
	public Text getDisplayName() {
		return this.getName();
	}
	
	@Override
	public void writeScreenOpeningData(@NotNull ServerPlayerEntity serverPlayerEntity, @NotNull PacketByteBuf packetByteBuf) {
		packetByteBuf.writeBlockPos(serverPlayerEntity.getBlockPos());
	}
	
}
