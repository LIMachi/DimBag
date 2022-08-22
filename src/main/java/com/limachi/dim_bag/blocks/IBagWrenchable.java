package com.limachi.dim_bag.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IBagWrenchable {
    InteractionResult wrenchWithBag(Level world, BlockPos pos, BlockState state, Direction face);
}
