package com.limachi.dimensional_bags.common.data.inventory;

import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.data.container.BaseContainer;
import com.limachi.dimensional_bags.common.upgradesManager.UpgradeManager;
import net.minecraft.entity.player.PlayerEntity;

public class UpgradesInventory extends BaseInventory {

    EyeData data;

    public UpgradesInventory(EyeData data) {
        super(UpgradeManager.upgradesCount(), 2, 9/*, null*/);
        this.data = data;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        this.data.markDirty();
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) { return true; }

    @Override
    public void clear() {}
}
