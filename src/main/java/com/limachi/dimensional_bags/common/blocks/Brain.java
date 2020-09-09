package com.limachi.dimensional_bags.common.blocks;

import com.limachi.dimensional_bags.common.Registries;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class Brain extends Block implements ITileEntityProvider {

    public static final IntegerProperty POWER = IntegerProperty.create("power", 0, 15);

    public Brain() {
        super(Block.Properties.create(Material.ROCK).sound(SoundType.STONE));
        this.setDefaultState(getDefaultState().with(POWER, 0));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(POWER);
    }

    @Override
    public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        return blockState.get(POWER);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) { return Registries.BRAIN_TE.get().create(); }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.isIn(newState.getBlock())) //only do the default behavior if the new state is of a different block
            super.onReplaced(state, worldIn, pos, newState, isMoving);
        worldIn.notifyNeighborsOfStateChange(pos, this);
    }
}
