package com.limachi.dimensional_bags.common.container;

import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.inventory.BagInventory;
import com.limachi.dimensional_bags.common.inventory.BaseInventory;
import com.limachi.dimensional_bags.common.network.Network;
import com.limachi.dimensional_bags.common.references.GUIs;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.SlotItemHandler;

import static com.limachi.dimensional_bags.common.references.GUIs.ScreenParts.*;

public class BagContainer extends BaseContainer {

    public BagContainer(int windowId, PlayerInventory playerInv, PacketBuffer extraData) { //client
        super(Registries.BAG_CONTAINER.get(), windowId, playerInv, extraData);
        addSlots();
    }

    public BagContainer(int windowId, ServerPlayerEntity player, BaseInventory openInv) { //server
        super(Registries.BAG_CONTAINER.get(), windowId, player, openInv);
        addSlots();
    }

    private void addSlots() {
        int sx = GUIs.BagScreen.calculateShiftLeft(getColumns());
        int sy = GUIs.BagScreen.calculateYSize(getRows());
        addPlayerSlots(sx > 0 ? sx * SLOT_SIZE_X : 0, sy - PLAYER_INVENTORY_Y);
        sx = PART_SIZE_X + 1 + (sx < 0 ? -sx * SLOT_SIZE_X : 0);
        sy = PART_SIZE_Y * 2 + 1;
        for (int y = 0; y < getRows(); ++y)
            for (int x = 0; x < getColumns(); ++x)
                if (x + y * getColumns() < openInv.getSize())
                    this.addSlot(new SlotItemHandler(openInv, x + y * getColumns(), sx + SLOT_SIZE_X * x, sy + SLOT_SIZE_Y * y));
    }

    @Override
    public void detectAndSendChanges() {
        if (!client && this.openInv.getSizeSignature() != this.inventoryChangeCount) //size changed, should reopen the gui client side (via openGUI server side) to sync the new size/item position
            Network.openEyeInventory((ServerPlayerEntity) playerInv.player, (BagInventory) openInv);
        else
            super.detectAndSendChanges();
    }
}
