package com.limachi.dimensional_bags.common.managers.upgrades;

import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.managers.Upgrade;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.world.World;

public class TankCapacityUpgrade extends Upgrade {

    public TankCapacityUpgrade() { super("tank_capacity", true, 1, 256, 1, 4096); }

    @Override
    public ActionResultType upgradeCrafted(EyeData data, ItemStack stack, World world, Entity crafter) {
        incrementInt(data, "Count", 1);
        data.getTank().increaseCapacity(1000);
        data.markDirty();
        return ActionResultType.SUCCESS;
    }
}
