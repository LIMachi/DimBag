package com.limachi.dimensional_bags.lib.common.inventory;

import net.minecraft.inventory.container.Slot;

public interface ISlotProvider {
    Slot createSlot(int index, int x, int y);
}
