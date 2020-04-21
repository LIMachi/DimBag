package com.limachi.dimensional_bags.common.data.inventory.container.slot;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class LockedSlot extends Slot { //prevent the bag items to be moved while the container is open

    public LockedSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean canTakeStack(PlayerEntity player) { return false; }

    @Override
    public boolean isItemValid(ItemStack stack) { return false; }
}
