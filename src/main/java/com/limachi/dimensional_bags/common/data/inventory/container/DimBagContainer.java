package com.limachi.dimensional_bags.common.data.inventory.container;

import com.limachi.dimensional_bags.client.screen.BagGUI;
import com.limachi.dimensional_bags.common.data.DimBagData;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.init.Registries;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public class DimBagContainer extends Container {

    private final EyeData data;

    public DimBagContainer(int windowId, PlayerInventory inventory, EyeData data) {
        super(Registries.BAG_CONTAINER.get(), windowId);
        this.data = data;
        this.addSlots(inventory);
    }

    /*
    public DimBagContainer(int windowId, PlayerInventory inventory, int eyeId) {
        this(windowId, inventory, DimBagData.get(inventory.player.getServer()).getEyeData(eyeId));
    }
    */

    public DimBagContainer(int windowId, PlayerInventory inventory, PacketBuffer buff) {
        this(windowId, inventory, new EyeData(buff));
    }

    public int getRows() { return this.data.getRows(); }
    public int getColumns() { return this.data.getColumns(); }

    private void addSlots(PlayerInventory inventory) {
        int sx = BagGUI.calculateShiftLeft(data.getColumns());
        int sy = BagGUI.calculateYSize(data.getRows());
        int dx = BagGUI.PART_SIZE_X + 1 + (sx < 0 ? -sx * BagGUI.SLOT_SIZE_X : 0);
        int dy = BagGUI.PART_SIZE_Y * 2 + 1;
        for (int y = 0; y < this.data.getRows(); ++y)
            for (int x = 0; x < this.data.getColumns(); ++x)
                this.addSlot(new Slot(this.data, x + y * data.getColumns(), dx + BagGUI.SLOT_SIZE_X * x, dy + BagGUI.SLOT_SIZE_Y * y));
        dx = BagGUI.PART_SIZE_X + 1 + (sx > 0 ? sx * BagGUI.SLOT_SIZE_X : 0);
        dy = sy - BagGUI.PLAYER_INVENTORY_Y + BagGUI.PLAYER_INVENTORY_FIRST_SLOT_Y + 1;
        for (int y = 0; y < 3; ++y)
            for (int x = 0; x < 9; ++x)
                this.addSlot(new Slot(inventory, x + y * 9 + 9, dx + BagGUI.SLOT_SIZE_X * x, dy + BagGUI.SLOT_SIZE_Y * y));
        dy = sy - BagGUI.PLAYER_INVENTORY_Y + BagGUI.PLAYER_BELT_FIRST_SLOT_Y + 1;
        for (int x = 0; x < 9; ++x)
            this.addSlot(new Slot(inventory, x, dx + BagGUI.SLOT_SIZE_X * x, dy));
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) { //FIXME: for now, set it to true, will have to implement logic later
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack slotStack = slot.getStack();
            itemstack = slotStack.copy();

            int size = this.data.getRows() * this.data.getColumns();

            if (index < size) {
                if (!mergeItemStack(slotStack, size, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!mergeItemStack(slotStack, 0, size, false)) {
                return ItemStack.EMPTY;
            }
            if (slotStack.getCount() == 0) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }
        return itemstack;
    }

    @Override
    public void onContainerClosed(PlayerEntity player) {
        super.onContainerClosed(player);
        this.data.closeInventory(player);
    }
}
