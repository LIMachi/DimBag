package com.limachi.dimensional_bags.common.managers.modes;

import com.limachi.dimensional_bags.client.render.screen.ManualScreen;
import com.limachi.dimensional_bags.common.managers.Mode;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.world.World;

public class Manual extends Mode {

    public static final String ID = "Manual";

    public Manual() { super(ID, false, true); }

    @Override
    public ActionResultType onItemRightClick(int eyeId, World world, PlayerEntity player) {
        ManualScreen.open();
        return ActionResultType.SUCCESS;
    }
}