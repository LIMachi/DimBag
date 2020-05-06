package com.limachi.dimensional_bags.common.upgradeManager.upgrades;

import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.upgradeManager.Upgrade;

public class RadiusUpgrade extends Upgrade {

    public RadiusUpgrade() { super("radius", true, 3, 31, 2, 126); }

    @Override
    protected void applyUpgrade(int countBefore, int countAfter, EyeData data) {

    }

    @Override
    public String getDescription() {
        return "increase the size of the current room (main room if applied to the bag directly) by 1 block in all directions";
    }
}
