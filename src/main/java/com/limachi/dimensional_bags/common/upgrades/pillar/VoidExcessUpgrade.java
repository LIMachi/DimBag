package com.limachi.dimensional_bags.common.upgrades.pillar;

import com.limachi.dimensional_bags.lib.ConfigManager;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.references.Registries;
import com.limachi.dimensional_bags.common.upgrades.BaseUpgradeInventory;

@StaticInit
public class VoidExcessUpgrade extends BaseUpgradeInventory {
    public static final String NAME = "pillar_void_excess_upgrade";

    @ConfigManager.Config(cmt = "can this upgrade be installed")
    public static boolean ACTIVE = true;

    static {
        Registries.registerItem(NAME, VoidExcessUpgrade::new);
    }

    @Override
    public boolean canBeInstalled() { return ACTIVE; }
    @Override
    public String upgradeName() { return NAME; }

    public VoidExcessUpgrade() {
        super(UpgradeTarget.PILLAR, DEFAULT_PROPS.stacksTo(1));
        isVoid = true;
    }

    @Override
    public boolean applySequentialUpgrades(int count, Object target) { return true; }
}
