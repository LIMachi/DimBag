package com.limachi.dimensional_bags.common.upgradeManager.upgrades;

import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.upgradeManager.Upgrade;

public class ColumnUpgrade extends Upgrade {

    public ColumnUpgrade() { super("column", true, 9, 18, 1, 35); }

    @Override
    protected void applyUpgrade(int countBefore, int countAfter, EyeData data) {
        if (countAfter > countBefore) {
            int rows = data.getRows();
            int size = rows * countAfter;
            data.getInventory().resizeInventory(size, rows, countAfter, rows, data.getColumns());
            data.markDirty();
        }
    }
}
