package com.limachi.dimensional_bags.common.upgradesManager.upgrades;

import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.upgradesManager.Upgrade;

public class RowUpgrade extends Upgrade {

    public RowUpgrade() { super("row", true, 3, 9, 1, 14); }

    @Override
    protected void applyUpgrade(int countBefore, int countAfter, EyeData data) {
        if (countAfter > countBefore) {
//            int delta = countAfter - countBefore;
            int size = data.getColumns() * countAfter;
            data.items.resizeInventory(size, countAfter, data.getColumns());
        }
    }
}
