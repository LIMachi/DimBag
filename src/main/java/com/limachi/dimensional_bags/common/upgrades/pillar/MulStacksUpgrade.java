package com.limachi.dimensional_bags.common.upgrades.pillar;

import com.limachi.dimensional_bags.lib.ConfigManager;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.references.Registries;
import com.limachi.dimensional_bags.common.bagDimensionOnly.bagSlot.SlotInventory;
import com.limachi.dimensional_bags.common.upgrades.BaseUpgradeInventory;

@StaticInit
public class MulStacksUpgrade extends BaseUpgradeInventory {
    public static final String NAME = "pillar_mul_stacks_upgrade";

    @ConfigManager.Config(cmt = "can this upgrade be installed")
    public static boolean ACTIVE = true;

    static {
        Registries.registerItem(NAME, MulStacksUpgrade::new);
    }

    @Override
    public boolean canBeInstalled() { return ACTIVE; }
    @Override
    public String upgradeName() { return NAME; }

    public MulStacksUpgrade() { super(UpgradeTarget.PILLAR, DEFAULT_PROPS); }

    @Override
    public boolean applySequentialUpgrades(int count, Object target) {
        SlotInventory pillar = (SlotInventory)target;
        return pillar.mulSize(count, true);
    }
}
