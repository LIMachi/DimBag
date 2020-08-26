package com.limachi.dimensional_bags.common.upgradeManager.upgrades;

import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.upgradeManager.Upgrade;

public class RowUpgrade extends Upgrade {

    public RowUpgrade() { super("row", true, 3, 9, 1, 12); }

    @Override
    protected void applyUpgrade(EyeData data) {
        int rows = data.getRows();
        int columns = data.getColumns();
        int size = data.getInventory().getSlots();
        data.getInventory().resizeInventory(size + 3, rows + 1, columns, rows, columns);
        data.setRows(rows + 1);
        data.markDirty();
    }
}
