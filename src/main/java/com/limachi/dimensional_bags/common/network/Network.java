package com.limachi.dimensional_bags.common.network;

import com.limachi.dimensional_bags.common.container.BagContainer;
import com.limachi.dimensional_bags.common.container.UpgradeContainer;
import com.limachi.dimensional_bags.common.container.WrappedPlayerInventoryContainer;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.inventory.PlayerInvWrapper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class Network {
    public static void openEyeInventory(ServerPlayerEntity player, EyeData data) {
        NetworkHooks.openGui(player, new INamedContainerProvider() {
            @Override
            public ITextComponent getDisplayName() {
                return new TranslationTextComponent("inventory.bag.name");
            }

            @Nullable
            @Override
            public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
                return BagContainer.CreateServer(windowId, inventory, data);
            }
        }, (buffer) -> {
            buffer.writeInt(data.getRows());
            buffer.writeInt(data.getColumns());
            data.getInventory().sizeAndRightsToBuffer(buffer);
        });
    }

    public static void openWrappedPlayerInventory(ServerPlayerEntity player, PlayerInvWrapper inv, TileEntity te) {
        NetworkHooks.openGui(player, new INamedContainerProvider() {
            @Override
            public ITextComponent getDisplayName() {
                return new TranslationTextComponent("inventory.player_interface.name");
            }

            @Nullable
            @Override
            public Container createMenu(int windowId, PlayerInventory playerInv, PlayerEntity player) {
                return WrappedPlayerInventoryContainer.createServer(windowId, playerInv, inv, te);
            }}, (buffer) -> {
                buffer.writeBoolean(inv.matchInventory(player.inventory));
                inv.sizeAndRightsToBuffer(buffer);
        });
    }

    public static void openEyeUpgrades(ServerPlayerEntity player, EyeData data) {
        NetworkHooks.openGui(player, new INamedContainerProvider() {
            @Override
            public ITextComponent getDisplayName() {
                return new TranslationTextComponent("inventory.upgrades.name");
            }

            @Nullable
            @Override
            public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
                return new UpgradeContainer(windowId, (ServerPlayerEntity) player, data);
            }
        }, data.getupgrades()::sizeAndRightsToBuffer);
    }
}
