package com.limachi.dimensional_bags.common.upgradeManager.upgrades;

import com.limachi.dimensional_bags.common.WorldUtils;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.upgradeManager.Upgrade;

public class RadiusUpgrade extends Upgrade {

    public RadiusUpgrade() { super("radius", true, 3, 31, 3, 127); }

    @Override
    protected void applyUpgrade(EyeData data) {
        int radius = data.getRadius();
        for (int i = 0; i < data.roomCount(); ++i)
            WorldUtils.buildRoom(WorldUtils.getRiftWorld(), EyeData.getEyePos(data.getId()).add(0, 0, i << 10), radius + 1, radius);
        data.setRadius(radius + 1);
        data.markDirty();
    }
}
