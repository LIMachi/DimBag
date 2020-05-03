package com.limachi.dimensional_bags.common.data.container.slot;

import com.limachi.dimensional_bags.common.network.packets.DimBagDataSlotChanged;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SyncSlot extends SlotItemHandler {

    public SyncSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public void onSlotChanged() { //queue the changed to be sync

    }

    public void loadFromPacket(DimBagDataSlotChanged pack) { //load changes perfomed by another entity/tileentity on the server

    }
}
