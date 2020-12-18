package com.limachi.dimensional_bags.common.container;

import com.limachi.dimensional_bags.common.container.slot.InvWrapperSlot;
import com.limachi.dimensional_bags.common.inventory.InventoryUtils;
import com.limachi.dimensional_bags.common.inventory.NBTStoredItemHandler;
import com.limachi.dimensional_bags.common.inventory.PlayerInvWrapper;
import com.limachi.dimensional_bags.common.inventory.Wrapper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import java.util.ArrayList;

import static com.limachi.dimensional_bags.common.references.GUIs.ScreenParts.*;

public class BaseWrappedInventoryContainer {}/*extends Container {

    protected PlayerInvWrapper playerInv;
    protected InventoryUtils.IIORIghtItemHandler openInv;
    protected boolean client;

    public BaseWrappedInventoryContainer(ContainerType<? extends BaseWrappedInventoryContainer> type, int windowId) { //common constructor
        super(type, windowId);
    }

    protected BaseWrappedInventoryContainer(ContainerType<? extends BaseWrappedInventoryContainer> type, int windowId, PlayerInventory playerInv, PacketBuffer extraData) { //client side/registry constructor
        super(type, windowId);
        this.playerInv = new PlayerInvWrapper(playerInv);
        this.openInv = NBTStoredItemHandler.createInPlace(extraData.readCompoundTag());
        this.client = true;
    }

    protected BaseWrappedInventoryContainer(ContainerType<? extends BaseWrappedInventoryContainer> type, int windowId, ServerPlayerEntity player, InventoryUtils.IIORIghtItemHandler openInv) { //server side constructor
        super(type, windowId);
        this.playerInv = new PlayerInvWrapper(player.inventory);
        this.openInv = openInv;
        this.client = false;
    }

    public InventoryUtils.ItemStackIORights getRights(int slot) {
        if (slot < 36 || slot >= inventorySlots.size()) return new InventoryUtils.ItemStackIORights();
        return openInv.getRightsInSlot(slot - 36);
    }

    public void changeRights(int slot, InventoryUtils.ItemStackIORights rights) {
        openInv.setRightsInSlot(slot, rights);
    }

    protected void addPlayerSlots(int px, int py) {
        int dx = px + PLAYER_INVENTORY_FIRST_SLOT_X + 1;
        int dy = py + PLAYER_BELT_FIRST_SLOT_Y + 1;
        for (int x = 0; x < PLAYER_INVENTORY_COLUMNS; ++x)
            addSlot(new InvWrapperSlot(playerInv, x, dx + x * SLOT_SIZE_X, dy));
        dy = py + PLAYER_INVENTORY_FIRST_SLOT_Y + 1;
        for (int y = 0; y < PLAYER_INVENTORY_ROWS; ++y)
            for (int x = 0; x < PLAYER_INVENTORY_COLUMNS; ++x)
                addSlot(new InvWrapperSlot(playerInv, x + (y + 1) * PLAYER_INVENTORY_COLUMNS, dx + x * SLOT_SIZE_X, dy + y * SLOT_SIZE_Y));
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int position)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(position);

        if(slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if(position >= 36 && !Wrapper.mergeItemStack(inventorySlots, itemstack1, 0, 36, false, new ArrayList<>()))
                return ItemStack.EMPTY;
            else if(!Wrapper.mergeItemStack(inventorySlots, itemstack1, 36, this.inventorySlots.size(), false, new ArrayList<>()))
                return ItemStack.EMPTY;
            if(itemstack1.getCount() == 0)
                slot.putStack(ItemStack.EMPTY);
            else
                slot.onSlotChanged();
        }
        return itemstack;
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) { return true; }
}*/
