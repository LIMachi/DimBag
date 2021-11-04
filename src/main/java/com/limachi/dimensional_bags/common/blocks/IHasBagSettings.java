package com.limachi.dimensional_bags.common.blocks;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;

public interface IHasBagSettings {
    ActionResultType openSettings(PlayerEntity player, BlockPos pos);
}
