package com.limachi.dimensional_bags.common.data.inventory;

import com.limachi.dimensional_bags.common.data.EyeData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class BagInventory extends BaseInventory {

    EyeData data;

    public BagInventory(EyeData data) {
        super(data.getRows() * data.getColumns());
        this.data = data;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        this.data.markDirty();
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
        return !data.upgrades.isDirty(); //close if another player is changing the upgrades?
    }

    @Override
    public void clear() {}
}
