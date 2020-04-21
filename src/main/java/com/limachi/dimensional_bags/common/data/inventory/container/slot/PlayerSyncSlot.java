package com.limachi.dimensional_bags.common.data.inventory.container.slot;

import net.minecraftforge.items.IItemHandler;

public class PlayerSyncSlot extends SyncSlot {
    public PlayerSyncSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }
}
