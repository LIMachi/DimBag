package com.limachi.dimensional_bags.common.managers.modes;

import com.limachi.dimensional_bags.common.managers.Mode;
import com.limachi.dimensional_bags.common.network.Network;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;

public class Settings extends Mode {

    public static final String ID = "Settings";

    public Settings() { super(ID, false, true); }

    @Override
    public ActionResult<ItemStack> onItemRightClick(int eyeId, World world, PlayerEntity player, int slot) {
        Network.openSettingsGui((ServerPlayerEntity) player, eyeId, slot);
        return ActionResult.resultSuccess(player.inventory.getStackInSlot(slot));
    }
}

//{ForgeData:{BagItemStack:{tag:{LocalModeManager:{Installed:["Default", "Settings", "PokeBall", "Elytra"]}}}}}