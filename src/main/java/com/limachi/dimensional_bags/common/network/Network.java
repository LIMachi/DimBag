package com.limachi.dimensional_bags.common.network;

import com.limachi.dimensional_bags.common.container.*;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.InventoryData;
import com.limachi.dimensional_bags.common.inventory.PlayerInvWrapper;
import com.limachi.dimensional_bags.common.tileentities.BrainTileEntity;
import com.limachi.dimensional_bags.common.tileentities.GhostHandTileEntity;
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
    public static void openEyeInventory(ServerPlayerEntity player, int eyeId) {
        InventoryData data = InventoryData.getInstance(eyeId);
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
                    return new WrappedPlayerInventoryContainer(windowId, playerInv, te, inv);
                }
            }, (buffer) -> {
                WrappedPlayerInventoryContainer.writeParameters(buffer, te, inv.matchInventory(player.inventory), inv);
        });
    }

    public static void openBrainInterface(ServerPlayerEntity player, BrainTileEntity te) {
        NetworkHooks.openGui(player, new INamedContainerProvider() {
            @Override
            public ITextComponent getDisplayName() {
                return new TranslationTextComponent("inventory.brain.name");
            }

            @Nullable
            @Override
            public Container createMenu(int windowId, PlayerInventory playerInv, PlayerEntity player) {
                return new BrainContainer(windowId, playerInv, te);
            }
        }, (buffer)-> BaseContainer.writeBaseParameters(buffer, BaseContainer.ContainerConnectionType.TILE_ENTITY, te, 0));
    }

    public static void openGhostHandInterface(ServerPlayerEntity player, GhostHandTileEntity te) {
        NetworkHooks.openGui(player, new INamedContainerProvider() {
            @Override
            public ITextComponent getDisplayName() {
                return new TranslationTextComponent("inventory.ghost_hand.name");
            }

            @Nullable
            @Override
            public Container createMenu(int windowId, PlayerInventory playerInv, PlayerEntity player) {
                return new GhostHandContainer(windowId, playerInv, te);
            }
        }, (buffer)-> BaseContainer.writeBaseParameters(buffer, BaseContainer.ContainerConnectionType.TILE_ENTITY, te, 0));
    }

    public static void openSettingsGui(ServerPlayerEntity player, int eyeId, int slot) {
        NetworkHooks.openGui(player, new INamedContainerProvider() {
            @Override
            public ITextComponent getDisplayName() { return new TranslationTextComponent("inventory.settings.name"); }

            @Nullable
            @Override
            public Container createMenu(int windowId, PlayerInventory playerInv, PlayerEntity player) {
                return new SettingsContainer(windowId, playerInv, slot);
            }
        }, buffer->BaseContainer.writeBaseParameters(buffer, BaseContainer.ContainerConnectionType.ITEM, null, slot));
    }
}
