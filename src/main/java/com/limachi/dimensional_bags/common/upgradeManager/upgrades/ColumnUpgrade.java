package com.limachi.dimensional_bags.common.upgradeManager.upgrades;

import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.upgradeManager.Upgrade;

public class ColumnUpgrade extends Upgrade {

    public ColumnUpgrade() { super("column", true, 9, 12, 1, 18); }

    @Override
    protected void applyUpgrade(EyeData data) {
        int rows = data.getRows();
        int columns = data.getColumns();
        int size = data.getInventory().getSlots();
        data.getInventory().resizeInventory(size + 3, rows, columns + 1, rows, columns);
        data.setColumns(columns + 1);
        data.markDirty();
    }
}
