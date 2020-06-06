package com.limachi.dimensional_bags.common.container.slot;

import com.limachi.dimensional_bags.common.inventory.Wrapper;
import net.minecraftforge.items.SlotItemHandler;

public class InvWrapperSlot extends SlotItemHandler {
    protected Wrapper inv;

    public InvWrapperSlot(Wrapper inv, int index, int x, int y) { super(inv, index, x, y); this.inv = inv; }

    @Override
    public int getSlotStackLimit() {
        return inv.getRights(this.getSlotIndex()).maxStack;
    }
}
