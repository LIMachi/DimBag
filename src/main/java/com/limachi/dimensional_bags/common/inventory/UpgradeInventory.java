package com.limachi.dimensional_bags.common.inventory;

import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.upgradeManager.UpgradeManager;

public class UpgradeInventory extends Wrapper {
    public UpgradeInventory(EyeData data) {
        super(UpgradeManager.upgradesCount(), data::markDirty);
        for (int i = 0; i < this.IO.length; ++i) {
            this.inv.setInventorySlotContents(i, UpgradeManager.defaultStack(i));
            this.IO[i] = UpgradeManager.defaultRights(i);
        }
    }
}