package com.limachi.dimensional_bags.common.container.slot;

import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.upgradeManager.UpgradeManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class UpgradeConsumerSlot extends SlotItemHandler {

    private int prevStackSize;
    private EyeData data;

    public UpgradeConsumerSlot(IItemHandler inventory, int index, int xPosition, int yPosition, EyeData data) {
        super(inventory, index, xPosition, yPosition);
        this.data = data;
        this.prevStackSize = inventory.getStackInSlot(index).getCount();
    }

    @Override
    public int getSlotStackLimit() { return 127; } //maximum signed byte, fall back if getItemStackLimit isn't called

    @Override
    public int getItemStackLimit(@Nonnull ItemStack stack) {
        int id = UpgradeManager.getIdByStack(stack);
        if (id == -1 || id != this.getSlotIndex()) return 0;
        return UpgradeManager.getLimit(id);
    }

    @Override
    public void onSlotChanged() {
        if (data != null) {
            ItemStack stack = getStack();
            int newStackSize = stack.getCount();
            if (newStackSize != prevStackSize) {
                UpgradeManager.applyUpgrade(UpgradeManager.getIdByStack(stack), prevStackSize, newStackSize, data);
                this.prevStackSize = newStackSize;
            }
        }
    }

    @Override
    public boolean isItemValid(ItemStack stack) { return getItemStackLimit(stack) > 0; }
}
