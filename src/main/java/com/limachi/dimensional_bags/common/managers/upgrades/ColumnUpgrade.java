package com.limachi.dimensional_bags.common.managers.upgrades;

import com.limachi.dimensional_bags.common.data.EyeDataMK2.InventoryData;
import com.limachi.dimensional_bags.common.managers.Upgrade;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import net.minecraft.item.ItemStack;

public class ColumnUpgrade extends Upgrade {

    public ColumnUpgrade() { super("column", true, 9, 12, 1, 18); }

    @Override
    public void installUpgrade(int eyeId, ItemStack stack, int amount, boolean simulate) {
        if (!simulate) {
            InventoryData data = InventoryData.getInstance(null, eyeId);
            int rows = data.getRows();
            int columns = data.getColumns();
            int size = data.getInventory().getSlots();
            data.getInventory().resizeInventory(size + 3 * amount, rows, columns + amount, rows, columns);
        }
        UpgradeManager.installUpgrade("upgrade_slot", stack, 3 * amount, simulate);
    }
}
