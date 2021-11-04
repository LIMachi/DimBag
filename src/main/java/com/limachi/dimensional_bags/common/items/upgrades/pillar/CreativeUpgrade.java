package com.limachi.dimensional_bags.common.items.upgrades.pillar;

import com.limachi.dimensional_bags.ConfigManager;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.inventory.PillarInventory;
import com.limachi.dimensional_bags.common.items.upgrades.BaseUpgradeInventory;

@StaticInit
public class CreativeUpgrade extends BaseUpgradeInventory {
    public static final String NAME = "pillar_creative_upgrade";

    @ConfigManager.Config(cmt = "can this upgrade be installed")
    public static boolean ACTIVE = true;

    static {
        Registries.registerItem(NAME, CreativeUpgrade::new);
    }

    @Override
    public boolean canBeInstalled() { return ACTIVE; }
    @Override
    public String upgradeName() { return NAME; }

    public CreativeUpgrade() {
        super(UpgradeTarget.PILLAR, DEFAULT_PROPS.stacksTo(1));
        isVoid = true;
    }

    @Override
    public boolean applySequentialUpgrades(int count, Object target) {
        PillarInventory pillar = (PillarInventory)target;
        pillar.setLockState(true);
        pillar.setCreativeStack();
        return true;
    }
}
