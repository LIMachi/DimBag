package com.limachi.dimensional_bags.common.container;

import com.limachi.dimensional_bags.common.Registries;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

public class SettingsContainer extends BaseContainer {
    public SettingsContainer(int windowId, PlayerInventory playerInv, int itemSlot) {
        super(Registries.SETTINGS_CONTAINER.get(), windowId, playerInv, ContainerConnectionType.ITEM, null, itemSlot);
    }

    public SettingsContainer(int windowId, PlayerInventory playerInv, PacketBuffer extraData) {
        super(Registries.SETTINGS_CONTAINER.get(), windowId, playerInv, extraData);
    }
}
