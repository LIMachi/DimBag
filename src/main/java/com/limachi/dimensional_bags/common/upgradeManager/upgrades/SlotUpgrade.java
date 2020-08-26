package com.limachi.dimensional_bags.common.upgradeManager.upgrades;

import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.upgradeManager.Upgrade;

public class SlotUpgrade extends Upgrade {

    private final int qty;

    public SlotUpgrade(int qty) { super(qty == 1 ? "slot" : "slot_x" + qty, true, 0, calcQuantity(12, 9), 0, calcQuantity(18, 12)); this.qty = qty; }

    private static int calcQuantity(int col, int row) {
        int r = col * row - col * 3 - row * 3;
        return r > 0 ? r : 0;
    }

    @Override
    protected void applyUpgrade(EyeData data) {
        int rows = data.getRows();
        int columns = data.getColumns();
        int size = data.getInventory().getSlots();
        data.getInventory().resizeInventory(size + qty, rows, columns, rows, columns);
        data.markDirty();
    }
}
