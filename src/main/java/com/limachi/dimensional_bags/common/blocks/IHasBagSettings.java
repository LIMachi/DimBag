package com.limachi.dimensional_bags.common.blocks;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;

public interface IHasBagSettings {
    ActionResultType openSettings(PlayerEntity player);
}
