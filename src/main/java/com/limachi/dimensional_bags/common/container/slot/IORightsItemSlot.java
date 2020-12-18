package com.limachi.dimensional_bags.common.container.slot;

import com.limachi.dimensional_bags.common.inventory.InventoryUtils;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class IORightsItemSlot extends SlotItemHandler {
    InventoryUtils.ItemStackIORights rights;

    public IORightsItemSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition, InventoryUtils.ItemStackIORights rights) {
        super(itemHandler, index, xPosition, yPosition);
        this.rights = rights;
    }
}
