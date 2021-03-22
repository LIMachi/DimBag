package com.limachi.dimensional_bags.common.container;

import com.google.common.collect.Lists;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.inventory.EmptySimpleHandler;
import com.limachi.dimensional_bags.common.inventory.ISimpleItemHandler;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import com.limachi.dimensional_bags.StaticInit;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.NonNullList;

import java.util.List;
/*
@StaticInit
public class SimpleGUIContainer extends SimpleContainer {

    public static final String NAME = "settings";

    static {
        Registries.registerContainer(NAME, SettingsContainer::new);
    }

    public SettingsContainer(int windowId, PlayerInventory playerInv, int eye) {
        super(Registries.getContainerType(NAME), windowId, playerInv, ContainerConnectionType.ITEM, null, itemSlot);
    }

    public SettingsContainer(int windowId, PlayerInventory playerInv, PacketBuffer extraData) {
        super(Registries.getContainerType(NAME), windowId, playerInv, extraData);
    }
}
*/