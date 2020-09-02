package com.limachi.dimensional_bags.common.managers.upgrades;

import com.limachi.dimensional_bags.common.WorldUtils;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.managers.Upgrade;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.world.World;

public class RadiusUpgrade extends Upgrade {

    public RadiusUpgrade() { super("radius", true, 3, 31, 3, 127); }

    @Override
    public ActionResultType upgradeCrafted(EyeData data, ItemStack stack, World world, Entity crafter) {
        int radius = data.getRadius();
        for (int i = 0; i < data.roomCount(); ++i)
            WorldUtils.buildRoom(WorldUtils.getRiftWorld(), EyeData.getEyePos(data.getId()).add(0, 0, i << 10), radius + 1, radius);
        incrementInt(data, "Count", 1);
        data.markDirty();
        return ActionResultType.SUCCESS;
    }
}
