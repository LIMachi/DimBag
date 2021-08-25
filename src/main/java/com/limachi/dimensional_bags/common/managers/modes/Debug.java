package com.limachi.dimensional_bags.common.managers.modes;

import com.limachi.dimensional_bags.common.data.EyeDataMK2.WorldSavedDataManager;
import com.limachi.dimensional_bags.common.managers.Mode;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Util;
import net.minecraft.world.World;

public class Debug extends Mode {

    public Debug() { super("Debug", false, false); }

    @Override
    public ActionResultType onItemRightClick(int eyeId, World world, PlayerEntity player) {
        player.sendMessage(WorldSavedDataManager.prettyDebug(eyeId), Util.NIL_UUID);
        return ActionResultType.SUCCESS;
    }
}