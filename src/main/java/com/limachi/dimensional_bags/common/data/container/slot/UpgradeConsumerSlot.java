package com.limachi.dimensional_bags.common.data.container.slot;

import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.upgradesManager.UpgradeManager;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class UpgradeConsumerSlot extends BaseSlot { //upgradesManager put in this slot will be removed from the game and stored in the eye definitely (the mod use this method as crafting the bag item with an upgrade would require to validate the craft on the server with methods that don't give a simple access to the server class)

    private int prevStackSize;

    public UpgradeConsumerSlot(IInventory inventory, int index, int xPosition, int yPosition, EyeData data) {
        super(inventory, index, xPosition, yPosition, true, false, data);
        this.prevStackSize = inventory.getStackInSlot(index).getCount();
    }

    @Override
    public int getSlotStackLimit() {
        return 127;
    } //by default, the maximum (the actual limit is calculated per item)

    @Override
    public int getItemStackLimit(ItemStack stack) {
        int id = UpgradeManager.getIdByStack(stack);
        if (id == -1) return 0; //not an upgrade
        if (id != this.getSlotIndex()) return 0; //invalid upgrade slot
        return UpgradeManager.getLimit(id);
    }

    @Override
    public void onSlotChanged() {
        ItemStack stack = this.getStack();
        int newStackSize = stack.getCount();
        if (newStackSize != this.prevStackSize) {
            UpgradeManager.applyUpgrade(UpgradeManager.getIdByStack(stack), this.prevStackSize, newStackSize, data);
            this.prevStackSize = newStackSize;
        }
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return getItemStackLimit(stack) > 0;
    }
}
