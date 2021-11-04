package com.limachi.dimensional_bags.common.tileentities;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface IInstallUpgradeTE {
    ItemStack installUpgrades(PlayerEntity player, ItemStack stack);
}
