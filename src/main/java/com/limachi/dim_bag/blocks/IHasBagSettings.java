package com.limachi.dim_bag.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public interface IHasBagSettings {
    InteractionResult openSettings(Player player, BlockPos pos);
}
