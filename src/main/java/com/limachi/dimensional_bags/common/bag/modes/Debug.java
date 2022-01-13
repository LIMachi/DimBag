package com.limachi.dimensional_bags.common.bag.modes;

import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.WorldSavedDataManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Util;
import net.minecraft.world.World;

public class Debug extends AbstractMode {

    public Debug() { super("Debug", false, false); }

    @Override
    public ActionResultType onItemRightClick(int bagId, World world, PlayerEntity player) {
        player.sendMessage(WorldSavedDataManager.prettyDebug(bagId), Util.NIL_UUID);
        return ActionResultType.SUCCESS;
    }
}