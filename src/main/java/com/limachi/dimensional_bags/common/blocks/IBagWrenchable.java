package com.limachi.dimensional_bags.common.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IBagWrenchable {
    ActionResultType wrenchWithBag(World world, BlockPos pos, BlockState state, Direction face);
}
