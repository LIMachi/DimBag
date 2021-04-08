package com.limachi.dimensional_bags.common.container.slot;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class LockedSlot extends Slot {

    public LockedSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) { super(inventoryIn, index, xPosition, yPosition); }

    @Override
    public boolean canTakeStack(PlayerEntity playerIn) { return false; }

    @Override
    public void putStack(ItemStack stack) {}

    @Override
    public boolean isItemValid(ItemStack stack) { return false; }

    @Override
    public void onSlotChange(ItemStack oldStackIn, ItemStack newStackIn) {}

    @Override
    protected void onCrafting(ItemStack stack, int amount) {}

    @Override
    protected void onSwapCraft(int numItemsCrafted) {}

    @Override
    protected void onCrafting(ItemStack stack) {}

    @Override
    public ItemStack onTake(PlayerEntity thePlayer, ItemStack stack) { return ItemStack.EMPTY; }

    @Override
    public void onSlotChanged() {}

    @Override
    public ItemStack decrStackSize(int amount) { return ItemStack.EMPTY; }
}
