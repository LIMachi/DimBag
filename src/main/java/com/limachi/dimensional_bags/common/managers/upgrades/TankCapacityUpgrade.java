package com.limachi.dimensional_bags.common.managers.upgrades;

import com.limachi.dimensional_bags.common.data.EyeDataMK2.TankData;
import com.limachi.dimensional_bags.common.managers.Upgrade;
import net.minecraft.item.ItemStack;

public class TankCapacityUpgrade extends Upgrade {

    public TankCapacityUpgrade() { super("tank_capacity", true, 1, 256, 1, 4096); }

    @Override
    public void installUpgrade(int eyeId, ItemStack stack, int amount, boolean preview) {
        if (!preview)
            TankData.execute(eyeId, tankData -> tankData.increaseCapacity(1000));
    }
}
