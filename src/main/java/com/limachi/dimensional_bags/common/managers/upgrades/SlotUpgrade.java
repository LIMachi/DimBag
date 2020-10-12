package com.limachi.dimensional_bags.common.managers.upgrades;

import com.limachi.dimensional_bags.common.data.EyeDataMK2.InventoryData;
import com.limachi.dimensional_bags.common.managers.Upgrade;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import java.util.HashMap;

public class SlotUpgrade extends Upgrade {

    private final int qty;

    public SlotUpgrade(int qty) { super(qty == 1 ? "slot" : "slot_x" + qty, true, 6, calcQuantity(12, 9), 6, calcQuantity(18, 12)); this.qty = qty; this.canConfig = qty == 1; }

    private static int calcQuantity(int col, int row) {
        int r = col * row - col * 3 - row * 3;
        return r > 0 ? r : 0;
    }

    @Override
    public void installUpgrade(int eyeId, ItemStack stack, int amount, boolean preview) {
        if (!preview) {
            InventoryData data = InventoryData.getInstance(null, eyeId);
            int rows = data.getRows();
            int columns = data.getColumns();
            int size = data.getInventory().getSlots();
            data.getInventory().resizeInventory(size + amount, rows, columns, rows, columns);
        }
    }

    @Override
    public int getCount(HashMap<String, Integer> map) {
        if (qty != 1)
            return map.get("upgrade_slot");
        return super.getCount(map);
    }

    @Override
    public void changeCount(HashMap<String, Integer> map, int count) {
        if (qty != 1)
            map.put("upgrade_slot", count);
        super.changeCount(map, count);
    }

    @Override
    public CompoundNBT getMemory(UpgradeManager manager) {
        if (qty != 1)
            return UpgradeManager.getUpgrade("upgrade_slot").getMemory(manager);
        return super.getMemory(manager);
    }

    @Override
    public void setMemory(UpgradeManager manager, CompoundNBT nbt) {
        if (qty != 1)
            UpgradeManager.getUpgrade("upgrade_slot").setMemory(manager, nbt);
        else
            super.setMemory(manager, nbt);
    }
}
