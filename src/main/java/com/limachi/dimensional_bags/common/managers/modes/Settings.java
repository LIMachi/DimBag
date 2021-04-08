package com.limachi.dimensional_bags.common.managers.modes;

import com.limachi.dimensional_bags.common.container.SettingsContainer;
import com.limachi.dimensional_bags.common.managers.Mode;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.world.World;

public class Settings extends Mode {

    public static final String ID = "Settings";

    public Settings() { super(ID, false, true); }

    @Override
    public ActionResultType onItemRightClick(int eyeId, World world, PlayerEntity player) {
        new SettingsContainer(0, player.inventory, eyeId).open(player);
        return ActionResultType.SUCCESS;
    }
}