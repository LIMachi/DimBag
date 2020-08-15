package com.limachi.dimensional_bags.common.upgradeManager.upgrades;

import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.upgradeManager.Upgrade;

public class RowUpgrade extends Upgrade {

    public RowUpgrade() { super("row", true, 3, 9, 1, 14); }

    @Override
    protected void applyUpgrade(int countBefore, int countAfter, EyeData data) {
        if (countAfter > countBefore) {
            int columns = data.getColumns();
            int size = columns * countAfter;
            data.getInventory().resizeInventory(size, countAfter, columns, data.getRows(), columns);
            data.markDirty();
        }
    }
}
