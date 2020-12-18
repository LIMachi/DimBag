package com.limachi.dimensional_bags.common.container.slot;

import com.limachi.dimensional_bags.common.inventory.Wrapper;
import net.minecraftforge.items.SlotItemHandler;

public class InvWrapperSlot {}/*extends SlotItemHandler implements IIORightsSlot {
    protected Wrapper inv;

    public InvWrapperSlot(Wrapper inv, int index, int x, int y) { super(inv, index, x, y); this.inv = inv; }

    public Wrapper getWrapper() { return inv; }

    @Override
    public int getSlotStackLimit() { return inv.getRights(this.getSlotIndex()).maxStack; }

    @Override
    public byte getRights() {
        return (byte)(inv.getRights(this.getSlotIndex()).flags & 3);
    }

    @Override
    public void setRightsFlag(byte rights) {
        Wrapper.IORights ior = inv.getRights(this.getSlotIndex());
        ior.flags = (byte)((ior.flags & ~3) | rights);
        inv.setRights(this.getSlotIndex(), ior);
    }

    @Override
    public void setRights(Wrapper.IORights rights) {
        inv.setRights(this.getSlotIndex(), rights);
    }
}
*/