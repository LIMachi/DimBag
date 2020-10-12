package com.limachi.dimensional_bags.common.container.slot;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class DisabledSlot extends Slot {
    public DisabledSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean isEnabled() { return false; }

    @Override
    public boolean isItemValid(ItemStack stack) { return false; }

    @Override
    public void putStack(ItemStack stack) {}

    @Override
    public void onSlotChanged() {}

    @Override
    public int getSlotStackLimit() { return 0; }

    @Override
    public int getItemStackLimit(ItemStack stack) { return 0; }

    @Override
    public ItemStack decrStackSize(int amount) { return ItemStack.EMPTY; }

    @Override
    public boolean canTakeStack(PlayerEntity playerIn) { return false; }
}
