package com.limachi.dimensional_bags.common.managers.upgrades;

import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.managers.Upgrade;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.world.World;

public class SlotUpgrade extends Upgrade {

    private final int qty;

    public SlotUpgrade(int qty) { super(qty == 1 ? "slot" : "slot_x" + qty, true, 6, calcQuantity(12, 9), 6, calcQuantity(18, 12)); this.qty = qty; this.canConfig = qty == 1; }

    private static int calcQuantity(int col, int row) {
        int r = col * row - col * 3 - row * 3;
        return r > 0 ? r : 0;
    }

    @Override
    public CompoundNBT getMemory(EyeData data) {
        if (qty != 1)
            return UpgradeManager.getUpgrade("upgrade_slot").getMemory(data);
        return super.getMemory(data);
    }

    @Override
    public void setMemory(EyeData data, CompoundNBT nbt) {
        if (qty != 1)
            UpgradeManager.getUpgrade("upgrade_slot").setMemory(data, nbt);
        else
            super.setMemory(data, nbt);
    }

    @Override
    public ActionResultType upgradeCrafted(EyeData data, ItemStack stack, World world, Entity crafter) {
        int rows = data.getRows();
        int columns = data.getColumns();
        int size = data.getInventory().getSlots();
        data.getInventory().resizeInventory(size + qty, rows, columns, rows, columns);
        incrementInt(data, "Count", qty);
        data.markDirty();
        return ActionResultType.SUCCESS;
    }
}
