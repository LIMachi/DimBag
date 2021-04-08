package com.limachi.dimensional_bags.common.inventory;

import net.minecraft.inventory.container.Slot;

public interface ISlotProvider {
    Slot createSlot(int index, int x, int y);
}
