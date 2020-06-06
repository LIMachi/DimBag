package com.limachi.dimensional_bags.common.inventory;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;

public class PlayerInventoryWrapper extends BaseInventory { //mix of BaseInventory and InvWrapper

    WeakReference<PlayerInventory> inv;

    public PlayerInventoryWrapper() { //server side, only called if the player is offline/unavailable
        super(41, 41, 1, () -> {});
        this.inv = new WeakReference<>(null);
    }

    public PlayerInventoryWrapper(PlayerInventory inv) { //server side
        super(41, 41, 1, inv::markDirty); //rows and columns aren't used, but are still set
        this.inv = new WeakReference<>(inv);
        for (int i = 0; i < items.length; ++i)
            items[i].stack = inv.getStackInSlot(i);
    }

    public void resyncPlayerInventory(PlayerInventory inv) { //overide the weak reference if necessary
        if (getInv() == null || getInv() != inv) {
            this.inv = new WeakReference<>(inv);
            for (int i = 0; i < items.length; ++i)
                items[i].stack = inv.getStackInSlot(i);
        }
    }

    public ServerPlayerEntity getPlayer() {
        PlayerInventory inv = getInv();
        if (inv != null)
            return (ServerPlayerEntity) inv.player;
        return null;
    }

    public PlayerInventoryWrapper(PacketBuffer buffer) { //client side, fake inventory
        super(buffer);
        this.inv = new WeakReference<>(null);
    }

    @Override
    public void resizeInventory(int size, int rows, int columns) {}

    private PlayerInventory getInv() { return inv.get(); }

    private void SyncToInv(int slot) {
        PlayerInventory inv = getInv();
        if (inv == null) return;
        inv.setInventorySlotContents(slot, getStackInSlot(slot));
        inv.markDirty();
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        ItemStack out = super.insertItem(slot, stack, simulate);
        if (!simulate)
            SyncToInv(slot);
        return out;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack out = super.extractItem(slot, amount, simulate);
        if (!simulate)
            SyncToInv(slot);
        return out;
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        super.setStackInSlot(slot, stack);
        SyncToInv(slot);
    }
}
