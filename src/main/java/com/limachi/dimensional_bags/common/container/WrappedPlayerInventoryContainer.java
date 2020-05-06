package com.limachi.dimensional_bags.common.container;

import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.inventory.BaseInventory;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.SlotItemHandler;

import static com.limachi.dimensional_bags.common.references.GUIs.UpgradeScreen.*;
import static com.limachi.dimensional_bags.common.references.GUIs.ScreenParts.*;

public class WrappedPlayerInventoryContainer extends BaseContainer {

    public WrappedPlayerInventoryContainer(int windowId, PlayerInventory playerInv, PacketBuffer extraData) { //client
        super(Registries.PLAYER_CONTAINER.get(), windowId, playerInv, extraData);
        //addSlots();
    }

    public WrappedPlayerInventoryContainer(int windowId, ServerPlayerEntity player, BaseInventory openInv) { //server
        super(Registries.PLAYER_CONTAINER.get(), windowId, player, openInv);
        //addSlots();
    }

    private void addSlots() {
        addPlayerSlots(0, PLAYER_INVENTORY_PART_Y);
/*
        for (int y = 0; y < getRows(); ++y)
            for (int x = 0; x < getColumns(); ++x)
                if (x + y * getColumns() < openInv.getSize())
                    this.addSlot(new SlotItemHandler(openInv, x + y * getColumns(), sx + SLOT_SIZE_X * x, sy + SLOT_SIZE_Y * y));
    */}

    /*
    @Override
    public void detectAndSendChanges() {
        if (!client && this.openInv.getSizeSignature() != this.inventoryChangeCount) //size changed, should reopen the gui client side (via openGUI server side) to sync the new size/item position
            Network.openEyeInventory((ServerPlayerEntity) playerInv.player, (BagInventory) openInv);
        else
            super.detectAndSendChanges();
    }
    */
}
