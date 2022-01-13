package com.limachi.dimensional_bags.lib.common.container.slot;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public class DisabledSlot extends LockedSlot {

    public static final Inventory EMPTY_INVENTORY = new Inventory(0);

    public DisabledSlot(int xPosition, int yPosition) { super(EMPTY_INVENTORY, 0, xPosition, yPosition); }

    @Override
    public boolean isActive() { return false; }

    @Override
    public int getMaxStackSize() { return 0; }

    @Override
    public int getMaxStackSize(ItemStack stack) { return 0; }
}
