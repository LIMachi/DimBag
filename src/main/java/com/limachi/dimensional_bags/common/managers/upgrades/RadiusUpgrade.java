package com.limachi.dimensional_bags.common.managers.upgrades;

import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.managers.Upgrade;
import net.minecraft.item.ItemStack;

public class RadiusUpgrade extends Upgrade {

    public RadiusUpgrade() { super("radius", true, 3, 31, 3, 127); }

    @Override
    public void installUpgrade(int eyeId, ItemStack stack, int amount, boolean simulate) {
        if (!simulate)
            SubRoomsManager.execute(eyeId, subRoomsManager -> subRoomsManager.changeRadius(subRoomsManager.getRadius() + amount));
    }
}
