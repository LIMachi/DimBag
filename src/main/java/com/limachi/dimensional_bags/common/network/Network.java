package com.limachi.dimensional_bags.common.network;

import com.limachi.dimensional_bags.common.container.BagContainer;
import com.limachi.dimensional_bags.common.container.UpgradeContainer;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.inventory.BagInventory;
import com.limachi.dimensional_bags.common.inventory.UpgradeInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class Network {
    public static void openEyeInventory(ServerPlayerEntity player, BagInventory eyeInventory) {
        NetworkHooks.openGui(player, new INamedContainerProvider() {
            @Override
            public ITextComponent getDisplayName() {
                return eyeInventory.getDisplayName();
            }

            @Nullable
            @Override
            public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
                return new BagContainer(windowId, (ServerPlayerEntity) player, eyeInventory);
            }
        }, eyeInventory::toBytes);
    }

    public static void openEyeUpgrades(ServerPlayerEntity player, EyeData data) {
        NetworkHooks.openGui(player, new INamedContainerProvider() {
            @Override
            public ITextComponent getDisplayName() {
                return data.getupgrades().getDisplayName();
            }

            @Nullable
            @Override
            public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
                return new UpgradeContainer(windowId, (ServerPlayerEntity) player, data);
            }
        }, data.getupgrades()::toBytes);
    }
}
