package com.limachi.dimensional_bags.common.managers.modes;

import com.limachi.dimensional_bags.common.managers.Mode;
import com.limachi.dimensional_bags.common.network.Network;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.world.World;

public class Settings extends Mode {

    public static final String ID = "Settings";

    public Settings() { super(ID, false, true); }

    @Override
    public ActionResultType onItemRightClick(int eyeId, World world, PlayerEntity player) {
        Network.openSettingsGui((ServerPlayerEntity)player, eyeId);
        return ActionResultType.SUCCESS;
    }
}