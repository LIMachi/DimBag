package com.limachi.dimensional_bags.common.upgradeManager.upgrades;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.WorldUtils;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.upgradeManager.Upgrade;

public class RadiusUpgrade extends Upgrade {

    public RadiusUpgrade() { super("radius", true, 3, 31, 3, 127); }

    @Override
    protected void applyUpgrade(int countBefore, int countAfter, EyeData data) {
        WorldUtils.buildRoom(WorldUtils.getRiftWorld(), EyeData.getEyePos(data.getId()), countAfter, countBefore);
        data.markDirty();
    }
}
