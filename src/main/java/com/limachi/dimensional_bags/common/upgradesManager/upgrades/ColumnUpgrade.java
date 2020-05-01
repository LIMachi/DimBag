package com.limachi.dimensional_bags.common.upgradesManager.upgrades;

import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.upgradesManager.Upgrade;

public class ColumnUpgrade extends Upgrade {

    public ColumnUpgrade() { super("column", true, 9, 18, 1, 35); }

    @Override
    protected void applyUpgrade(int countBefore, int countAfter, EyeData data) {
        if (countAfter > countBefore) {
//            int delta = countAfter - countBefore;
            int size = data.getRows() * countAfter;
            data.items.resizeInventory(size, data.getRows(), countAfter);
        }
    }
}
