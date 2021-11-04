package com.limachi.dimensional_bags.common.items.upgrades.pillar;

import com.limachi.dimensional_bags.ConfigManager;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.inventory.PillarInventory;
import com.limachi.dimensional_bags.common.items.upgrades.BaseUpgradeInventory;

@StaticInit
public class AddStacksUpgrade extends BaseUpgradeInventory {
    public static final String NAME = "pillar_add_stacks_upgrade";

    @ConfigManager.Config(cmt = "can this upgrade be installed")
    public static boolean ACTIVE = true;

    static {
        Registries.registerItem(NAME, AddStacksUpgrade::new);
    }

    public AddStacksUpgrade() { super(UpgradeTarget.PILLAR, DEFAULT_PROPS); }

    @Override
    public boolean canBeInstalled() { return ACTIVE; }
    @Override
    public String upgradeName() { return NAME; }

    @Override
    public boolean applySequentialUpgrades(int count, Object target) {
        PillarInventory pillar = (PillarInventory)target;
        return pillar.addSize(count, true);
    }
}
