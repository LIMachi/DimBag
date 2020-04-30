package com.limachi.dimensional_bags.common.data.container;

import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.data.container.slot.UpgradeConsumerSlot;
import com.limachi.dimensional_bags.common.init.Registries;
import com.limachi.dimensional_bags.common.references.GUIs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import static com.limachi.dimensional_bags.common.references.GUIs.ScreenParts.*;

public class DimBagContainer extends Container {

    private final EyeData data;

    public DimBagContainer(int windowId, PlayerInventory inventory, EyeData data) {
        super(Registries.BAG_CONTAINER.get(), windowId);
        this.data = data;
        this.addSlots(inventory);
    }

    public DimBagContainer(int windowId, PlayerInventory inventory, PacketBuffer buff) {
        this(windowId, inventory, new EyeData(buff));
    }

    public int getRows() { return this.data.getRows(); }
    public int getColumns() { return this.data.getColumns(); }

    private void addSlots(PlayerInventory inventory) {
        int sx = GUIs.BagScreen.calculateShiftLeft(data.getColumns());
        int sy = GUIs.BagScreen.calculateYSize(data.getRows());
        int dx = PART_SIZE_X + 1 + (sx < 0 ? -sx * SLOT_SIZE_X : 0);
        int dy = PART_SIZE_Y * 2 + 1;
        for (int y = 0; y < this.data.getRows(); ++y)
            for (int x = 0; x < this.data.getColumns(); ++x)
                this.addSlot(new Slot(this.data.items, x + y * data.getColumns(), dx + SLOT_SIZE_X * x, dy + SLOT_SIZE_Y * y));
        dx = PART_SIZE_X + 1 + (sx > 0 ? sx * SLOT_SIZE_X : 0);
        dy = sy - PLAYER_INVENTORY_Y + PLAYER_INVENTORY_FIRST_SLOT_Y + 1;
        for (int y = 0; y < 3; ++y)
            for (int x = 0; x < 9; ++x)
                this.addSlot(new Slot(inventory, x + y * 9 + 9, dx + SLOT_SIZE_X * x, dy + SLOT_SIZE_Y * y));
        dy = sy - PLAYER_INVENTORY_Y + PLAYER_BELT_FIRST_SLOT_Y + 1;
        for (int x = 0; x < 9; ++x)
            this.addSlot(new Slot(inventory, x, dx + SLOT_SIZE_X * x, dy));
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) { //FIXME: for now, set it to true, will have to implement logic later
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);

        if (slot != null && slot.getHasStack() && !(slot instanceof UpgradeConsumerSlot)) {
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
        this.data.items.closeInventory(player);
    }
}
