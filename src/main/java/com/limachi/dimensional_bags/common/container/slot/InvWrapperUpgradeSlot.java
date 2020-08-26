package com.limachi.dimensional_bags.common.container.slot;

import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.inventory.Wrapper;
import com.limachi.dimensional_bags.common.upgradeManager.UpgradeManager;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class InvWrapperUpgradeSlot extends InvWrapperSlot {

    private EyeData data;
    private int prevStackSize;

    public InvWrapperUpgradeSlot(Wrapper inv, int index, int x, int y, EyeData data) {
        super(inv, index, x, y);
        this.data = data;
        this.prevStackSize = inv.getStackInSlot(index).getCount();
    }

    @Override
    public void onSlotChanged() {
        if (data != null) {
            ItemStack stack = getStack();
            int newStackSize = stack.getCount();
            if (newStackSize != prevStackSize) {
//                UpgradeManager.applyUpgrade(UpgradeManager.getIdByStack(stack), prevStackSize, newStackSize, data);
                this.prevStackSize = newStackSize;
            }
        }
    }

    @Override
    public int getItemStackLimit(@Nonnull ItemStack stack) {
        int id = UpgradeManager.getIdByStack(stack);
        if (id == -1 || id != this.getSlotIndex()) return 0;
        return UpgradeManager.getLimit(id);
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) { return getItemStackLimit(stack) > 0; }
}
