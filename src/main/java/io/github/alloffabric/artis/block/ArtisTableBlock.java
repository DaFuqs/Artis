package io.github.alloffabric.artis.block;

import io.github.alloffabric.artis.api.ArtisTableType;
import io.github.alloffabric.artis.inventory.ArtisRecipeProvider;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class ArtisTableBlock extends Block implements ExtendedScreenHandlerFactory {
	
	private final ArtisTableType type;
	
	public ArtisTableBlock(ArtisTableType type, Block.Settings settings) {
		super(settings);
		this.type = type;
	}
	
	public ArtisTableType getType() {
		return type;
	}
	
	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, @NotNull PlayerEntity player, Hand hand, BlockHitResult hit) {
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
		return new ArtisRecipeProvider(Registries.SCREEN_HANDLER.get(type.getId()), type, syncId, player, ScreenHandlerContext.create(player.world, player.getBlockPos()));
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
