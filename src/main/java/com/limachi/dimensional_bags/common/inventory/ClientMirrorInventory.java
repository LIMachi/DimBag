package com.limachi.dimensional_bags.common.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
/*
public class ClientMirrorInventory extends ItemStackHandler implements IItemHandlerClientFactory {

    protected int[] slotSizes;

    public ClientMirrorInventory() {
        super();
    }

    public ClientMirrorInventory(ISimpleItemHandler serverHandler) {
        super();
        slotSizes = new int[serverHandler.getSlots()];
        for (int i = 0; i < slotSizes.length; ++i)
            slotSizes[i] = serverHandler.getSlotLimit(i);
    }

    @Override
    public IItemHandlerModifiable clientFactory(PacketBuffer buff) {
        slotSizes = buff.readVarIntArray();
        this.setSize(slotSizes.length);
        return this;
    }

    @Override
    public void writeClientFactoryBuffer(PacketBuffer buff) {
        buff.writeVarIntArray(slotSizes);
    }

    @Override
    public int getSlotLimit(int slot) {
        return slot >= 0 && slot < slotSizes.length ? slotSizes[slot] : 0;
    }

    @Override
    protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
        int l = getSlotLimit(slot);
        int m = stack.getMaxStackSize() * (int)Math.ceil((double)l / 64.);
        return Math.min(l, m);
    }
}
*/