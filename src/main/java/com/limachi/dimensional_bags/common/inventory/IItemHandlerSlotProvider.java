package com.limachi.dimensional_bags.common.inventory;

import net.minecraft.inventory.container.Slot;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;

public interface IItemHandlerSlotProvider extends IItemHandlerModifiable {
    default Slot createSlot(int index, int x, int y) {
        return new SlotItemHandler(this, index, x, y);
    }
}
