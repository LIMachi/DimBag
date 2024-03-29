package com.limachi.dimensional_bags.common.managers.upgrades;

import com.limachi.dimensional_bags.common.data.EyeDataMK2.TankData;
import com.limachi.dimensional_bags.common.inventory.Tank;
import com.limachi.dimensional_bags.common.managers.Upgrade;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import net.minecraft.item.ItemStack;

public class TankUpgrade extends Upgrade {

    public TankUpgrade() { super("tank", true, 0, 4, 0, 16); }

    @Override
    public void installUpgrade(int eyeId, ItemStack stack, int amount, boolean preview) {
        if (!preview)
            TankData.execute(eyeId, tankData -> tankData.attachTank(new Tank(UpgradeManager.getUpgrade("upgrade_tank_capacity").getCount(UpgradeManager.getInstance(eyeId)) * 1000), (byte) (TankData.CANINPUT | TankData.CANOUTPUT)));
    }
}
