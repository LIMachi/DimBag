package com.limachi.dimensional_bags.common.container.slot;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public class DisabledSlot extends LockedSlot {

    public static final Inventory EMPTY_INVENTORY = new Inventory(0);

    public DisabledSlot(int xPosition, int yPosition) { super(EMPTY_INVENTORY, 0, xPosition, yPosition); }

    @Override
    public boolean isEnabled() { return false; }

    @Override
    public int getSlotStackLimit() { return 0; }

    @Override
    public int getItemStackLimit(ItemStack stack) { return 0; }
}
