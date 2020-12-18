package com.limachi.dimensional_bags.common.container;

import com.limachi.dimensional_bags.common.Registries;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;

public class BaseNBTSincContainer extends BaseContainer {

    public BaseNBTSincContainer(int windowId, PlayerInventory playerInv, ContainerConnectionType connectionType, TileEntity tileEntity, int itemSlot) {
        super(Registries.BASE_CONTAINER.get(), windowId, playerInv, connectionType, tileEntity, itemSlot);
    }

    public BaseNBTSincContainer(ContainerType<? extends BaseContainer> type, int windowId, PlayerInventory playerInv, ContainerConnectionType connectionType, TileEntity tileEntity, int itemSlot) {
        super(type, windowId, playerInv, connectionType, tileEntity, itemSlot);
    }

    public BaseNBTSincContainer(int windowId, PlayerInventory playerInv, PacketBuffer extraData) {
        super(Registries.BASE_CONTAINER.get(), windowId, playerInv, extraData);
    }

    public BaseNBTSincContainer(ContainerType<? extends BaseContainer> type, int windowId, PlayerInventory playerInv, PacketBuffer extraData) {
        super(type, windowId, playerInv, extraData);
    }
}
