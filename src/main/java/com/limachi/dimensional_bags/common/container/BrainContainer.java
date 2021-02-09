package com.limachi.dimensional_bags.common.container;

import com.limachi.dimensional_bags.common.Registries;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;

import com.limachi.dimensional_bags.StaticInit;

@StaticInit
public class BrainContainer extends BaseContainer {

    public static final String NAME = "brain";

    static {
        Registries.registerContainer(NAME, BrainContainer::new);
    }

    public String command;

    public BrainContainer(int windowId, PlayerInventory playerInv, PacketBuffer extraData) {
        super(Registries.getContainerType(NAME), windowId, playerInv, extraData);
    }

    public BrainContainer(int windowId, PlayerInventory playerInv, TileEntity te) {
        super(Registries.getContainerType(NAME), windowId, playerInv, ContainerConnectionType.TILE_ENTITY, te, 0);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
    }
}
