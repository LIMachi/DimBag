package com.limachi.dimensional_bags.common.managers.upgrades;

import com.limachi.dimensional_bags.common.managers.ModeManager;
import com.limachi.dimensional_bags.common.managers.Upgrade;
import net.minecraft.item.ItemStack;

public class ElytraUpgrade extends Upgrade {

    public ElytraUpgrade() { super("elytra", true, 0, 1, 0, 1); }

    @Override
    public void installUpgrade(int eyeId, ItemStack stack, int amount, boolean preview) {
        ModeManager.getMode("Elytra").installMode(eyeId, stack, preview);
    }
}
