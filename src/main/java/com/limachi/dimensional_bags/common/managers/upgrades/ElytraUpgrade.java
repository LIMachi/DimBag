package com.limachi.dimensional_bags.common.managers.upgrades;

import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.managers.Upgrade;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.world.World;

public class ElytraUpgrade extends Upgrade {

    public ElytraUpgrade() { super("elytra", true, 0, 1, 0, 1); }

    @Override
    public ActionResultType upgradeCrafted(EyeData data, ItemStack stack, World world, Entity crafter) {
        incrementInt(data, "Count", 1);
        stack.getTag().putBoolean("ElytraAttached", true);
        data.modeManager().installMode("Elytra");
        data.markDirty();
        return ActionResultType.SUCCESS;
    }
}
