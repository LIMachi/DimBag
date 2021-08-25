package com.limachi.dimensional_bags.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public abstract class RSReactiveBlock extends Block {

    public static final IntegerProperty POWER_RECEIVED = IntegerProperty.create("power_received", 0, 15);

    public RSReactiveBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(defaultBlockState().setValue(POWER_RECEIVED, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWER_RECEIVED);
    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) //only do the default behavior if the new state is of a different block
            super.onRemove(state, worldIn, pos, newState, isMoving);
        worldIn.updateNeighbourForOutputSignal(pos, this);
    }

    public static boolean isPowered(BlockState state) { return state.getValue(POWER_RECEIVED) > 0; }

    public static int getPower(BlockState state) { return state.getValue(POWER_RECEIVED); }

    public static void setPowered(World world, BlockPos pos, int state) {
        if (world.getBlockState(pos).getBlock() instanceof RSReactiveBlock && getPower(world.getBlockState(pos)) != state)
            world.setBlock(pos, world.getBlockState(pos).getBlock().defaultBlockState().setValue(POWER_RECEIVED, state), Constants.BlockFlags.DEFAULT_AND_RERENDER);
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!worldIn.isClientSide())
            setPowered(worldIn, pos, worldIn.hasNeighborSignal(pos) ? worldIn.getBestNeighborSignal(pos) : 0);
    }
}
