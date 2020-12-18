package com.limachi.dimensional_bags.common.managers.modes;

import com.limachi.dimensional_bags.common.managers.Mode;
import com.limachi.dimensional_bags.common.managers.ModeManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

public class Automation extends Mode {

    public static final String ID = "Automation";

    public Automation() { super(ID, false, true); }

    @Override
    public ActionResultType onItemUse(int eyeId, World world, PlayerEntity player, BlockRayTraceResult ray) {
        if (ModeManager.execute(eyeId, modeManager -> {
            CompoundNBT nbt = modeManager.getModesNBT().getCompound("Automation");
//            if (nbt.getBoolean("use")) {
                nbt.putInt("state", 3);
                return true;
//            } else return false;
        }, false))
            return ActionResultType.SUCCESS;
        else
            return ActionResultType.PASS;
    }

    @Override
    public ActionResultType onItemRightClick(int eyeId, World world, PlayerEntity player) {
        if (ModeManager.execute(eyeId, modeManager -> {
            CompoundNBT nbt = modeManager.getModesNBT().getCompound("Automation");
//            if (nbt.getBoolean("click")) {
                nbt.putInt("state", 1);
                return true;
//            } else return false;
        }, false))
            return ActionResultType.SUCCESS;
        else
            return ActionResultType.PASS;
    }

    @Override
    public ActionResultType onAttack(int eyeId, PlayerEntity player, Entity entity) {
        if (ModeManager.execute(eyeId, modeManager -> {
            CompoundNBT nbt = modeManager.getModesNBT().getCompound("Automation");
//            if (nbt.getBoolean("attack")) {
                nbt.putInt("state", 2);
                return true;
//            } else return false;
        }, false))
            return ActionResultType.SUCCESS;
        else
            return ActionResultType.PASS;
    }
}
