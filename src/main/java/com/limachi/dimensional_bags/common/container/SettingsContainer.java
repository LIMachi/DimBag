package com.limachi.dimensional_bags.common.container;

import com.limachi.dimensional_bags.common.Registries;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

import com.limachi.dimensional_bags.StaticInit;

@StaticInit
public class SettingsContainer extends BaseContainer {

    public static final String NAME = "settings";

    static {
        Registries.registerContainer(NAME, BrainContainer::new);
    }

    public SettingsContainer(int windowId, PlayerInventory playerInv, int itemSlot) {
        super(Registries.getContainerType(NAME), windowId, playerInv, ContainerConnectionType.ITEM, null, itemSlot);
    }

    public SettingsContainer(int windowId, PlayerInventory playerInv, PacketBuffer extraData) {
        super(Registries.getContainerType(NAME), windowId, playerInv, extraData);
    }
}
