package com.limachi.dimensional_bags.common.network;

import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.data.container.DimBagContainer;
import com.limachi.dimensional_bags.common.data.container.SCActions;
import com.limachi.dimensional_bags.common.data.container.UpgradeContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class Network {

    public static void openGUIEye(ServerPlayerEntity player, final EyeData data, String translationKey) {
        NetworkHooks.openGui(player, new INamedContainerProvider() {
            @Override
            public ITextComponent getDisplayName() { return new TranslationTextComponent(translationKey); }

            @Nullable
            @Override
            public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity _player) {
                return new DimBagContainer(windowId, inventory, data);
            }
        }, data::toBytes);
    }

    public static void openGUIUpgrades(ServerPlayerEntity player, final EyeData data, String translationKey) {
        NetworkHooks.openGui(player, new INamedContainerProvider() {
            @Override
            public ITextComponent getDisplayName() { return new TranslationTextComponent(translationKey); }

            @Nullable
            @Override
            public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity _player) {
                return new UpgradeContainer(windowId, inventory, data);
            }
        }, data::toBytes);
    }

    public static void openGUIActions(ServerPlayerEntity player, final EyeData data, String translationKey) {
        NetworkHooks.openGui(player, new INamedContainerProvider() {
            @Override
            public ITextComponent getDisplayName() {
                return new TranslationTextComponent(translationKey);
            }

            @Nullable
            @Override
            public Container createMenu(int windowId, PlayerInventory _inventory, PlayerEntity _player) {
                return new SCActions(windowId, data);
            }
        }, data::toBytes);
    }
}
