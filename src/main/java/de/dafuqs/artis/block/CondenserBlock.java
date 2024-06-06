package de.dafuqs.artis.block;

import de.dafuqs.artis.*;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.loot.context.*;
import net.minecraft.particle.*;
import net.minecraft.sound.*;
import net.minecraft.state.*;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.*;
import net.minecraft.util.*;
import net.minecraft.util.hit.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class CondenserBlock extends BlockWithEntity {
	
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
	public static final BooleanProperty LIT = Properties.LIT;
	
	public CondenserBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(LIT, false));
	}
	
	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (world.isClient) {
			return ActionResult.SUCCESS;
		} else {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof CondenserBlockEntity condenserBlockEntity) {
				player.openHandledScreen(condenserBlockEntity);
			}
			return ActionResult.CONSUME;
		}
	}
	
	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new CondenserBlockEntity(pos, state);
	}
	
	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation) {
		return state.with(FACING, rotation.rotate(state.get(FACING)));
	}
	
	@Override
	public BlockState mirror(BlockState state, BlockMirror mirror) {
		return state.rotate(mirror.getRotation(state.get(FACING)));
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, LIT);
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return world.isClient ? null : checkType(type, ArtisBlocks.CONDENSER_BLOCK_ENTITY, CondenserBlockEntity::tick);
	}
	
	@Override
	public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
		ItemStack itemStack = super.getPickStack(world, pos, state);
		world.getBlockEntity(pos, ArtisBlocks.CONDENSER_BLOCK_ENTITY).ifPresent((blockEntity) -> {
			blockEntity.setStackNbt(itemStack);
		});
		return itemStack;
	}
	
	@Override
	public List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
		List<ItemStack> stacks = super.getDroppedStacks(state, builder);
		BlockEntity blockEntity = builder.getOptional(LootContextParameters.BLOCK_ENTITY);
		if (blockEntity instanceof CondenserBlockEntity condenserBlockEntity) {
			for (ItemStack stack : stacks) {
				if (stack.getItem() == this.asItem()) {
					condenserBlockEntity.setStackNbt(stack);
				}
			}
		}
		return stacks;
	}
	
	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		if (state.get(LIT)) {
			double centerX = pos.getX() + 0.5;
			double centerY = pos.getY() + 0.5;
			double centerZ = pos.getZ() + 0.5;
			if (random.nextDouble() < 0.1) {
				world.playSound(centerX, centerY, centerZ, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
			}
			
			Direction direction = state.get(FACING);
			Direction.Axis axis = direction.getAxis();
			double g = 0.52;
			double h = random.nextDouble() * 0.6 - 0.3;
			double xOffset = axis == Direction.Axis.X ? direction.getOffsetX() * g : h;
			double yOffset = random.nextDouble() * 6.0 / 16.0;
			double zOffset = axis == Direction.Axis.Z ? direction.getOffsetZ() * g : h;
			world.addParticle(ParticleTypes.SMOKE, centerX + xOffset, centerY + yOffset, centerZ + zOffset, 0.0, 0.0, 0.0);
			world.addParticle(ParticleTypes.FLAME, centerX + xOffset, centerY + yOffset, centerZ + zOffset, 0.0, 0.0, 0.0);
		}
	}
	
}
