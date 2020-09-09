package com.limachi.dimensional_bags.common.managers.upgrades;

import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.inventory.Tank;
import com.limachi.dimensional_bags.common.managers.Upgrade;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.world.World;

import static com.limachi.dimensional_bags.common.inventory.Wrapper.IORights.CANINPUT;
import static com.limachi.dimensional_bags.common.inventory.Wrapper.IORights.CANOUTPUT;

public class TankUpgrade extends Upgrade {

    public TankUpgrade() { super("tank", true, 0, 4, 0, 16); }

    @Override
    public ActionResultType upgradeCrafted(EyeData data, ItemStack stack, World world, Entity crafter) {
        incrementInt(data, "Count", 1);
        data.modeManager().installMode("Tank");
        data.getTank().attachTank(new Tank(UpgradeManager.getUpgrade( "upgrade_tank_capacity").getCount(data) * 1000), (byte)(CANINPUT | CANOUTPUT));
        data.markDirty();
        return ActionResultType.SUCCESS;
    }
}
