package com.limachi.dimensional_bags.common.container;

import com.limachi.dimensional_bags.common.inventory.BaseInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import static com.limachi.dimensional_bags.common.references.GUIs.ScreenParts.*;

public class BaseContainer extends Container {

    protected PlayerInventory playerInv;
    protected BaseInventory openInv;
    protected boolean client;
    protected int inventoryChangeCount;

    public BaseContainer(ContainerType<? extends BaseContainer> type, int windowId, PlayerInventory playerInv, PacketBuffer extraData) { //client side/registry constructor
        super(type, windowId);
        this.playerInv = playerInv;
        this.openInv = new BaseInventory(extraData);
        this.client = true;
        this.inventoryChangeCount = openInv.getSizeSignature();
    }

    public BaseContainer(ContainerType<? extends BaseContainer> type, int windowId, ServerPlayerEntity player, BaseInventory openInv) { //server side constructor
        super(type, windowId);
        this.playerInv = player.inventory;
        this.openInv = openInv;
        this.client = false;
        this.inventoryChangeCount = openInv.getSizeSignature();
    }

    protected void addPlayerSlots(int px, int py) {
        int dx = px + PLAYER_INVENTORY_FIRST_SLOT_X + 1;
        int dy = py + PLAYER_BELT_FIRST_SLOT_Y + 1;
        for (int x = 0; x < PLAYER_INVENTORY_COLUMNS; ++x)
            addSlot(new Slot(playerInv, x, dx + x * SLOT_SIZE_X, dy));
        dy = py + PLAYER_INVENTORY_FIRST_SLOT_Y + 1;
        for (int y = 0; y < PLAYER_INVENTORY_ROWS; ++y)
            for (int x = 0; x < PLAYER_INVENTORY_COLUMNS; ++x)
                addSlot(new Slot(playerInv, x + (y + 1) * PLAYER_INVENTORY_COLUMNS, dx + x * SLOT_SIZE_X, dy + y * SLOT_SIZE_Y));
    }

    public int getRows() { return openInv.getRows(); }

    public int getColumns() { return openInv.getColumns(); }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int position)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(position);

        if(slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if(position >= 36 && !this.mergeItemStack(itemstack1, 0, 36, false))
                return ItemStack.EMPTY;
            else if(!this.mergeItemStack(itemstack1, 36, this.inventorySlots.size(), false))
                return ItemStack.EMPTY;
            if(itemstack1.getCount() == 0)
                slot.putStack(ItemStack.EMPTY);
            else
                slot.onSlotChanged();
        }
        return itemstack;
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return true;
    }
}
