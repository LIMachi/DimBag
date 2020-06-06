package com.limachi.dimensional_bags.common.inventory;

import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.upgradeManager.UpgradeManager;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

/*
public class UpgradeInventory extends BaseInventory {

    public UpgradeInventory(EyeData data) {
        super(UpgradeManager.upgradesCount(), 2, 9, data::markDirty);
        for (int i = 0; i < this.items.length; ++i)
            items[i] = UpgradeManager.defaultStackAccessor(i);
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("inventory.upgrades.name");
    }
}*/

public class UpgradeInventory extends Wrapper {
    public UpgradeInventory(EyeData data) {
        super(UpgradeManager.upgradesCount(), data::markDirty);
        for (int i = 0; i < this.IO.length; ++i) {
            this.inv.setInventorySlotContents(i, UpgradeManager.defaultStack(i));
            this.IO[i] = UpgradeManager.defaultRights(i);
        }
    }
}