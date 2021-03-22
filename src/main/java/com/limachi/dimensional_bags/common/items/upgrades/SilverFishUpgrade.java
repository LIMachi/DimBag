package com.limachi.dimensional_bags.common.items.upgrades;

/*
@StaticInit
public class SilverFishUpgrade extends BaseUpgrade {

    public static final String NAME = "silver_fish_upgrade";

    @Config.Boolean(def = true, cmt = "can this upgrade be installed")
    public static boolean ACTIVE;

    static { Registries.registerItem(NAME, SilverFishUpgrade::new); }

    public SilverFishUpgrade() { super(DimBag.DEFAULT_PROPERTIES); }

    @Override
    public boolean canBeInstalled() { return ACTIVE; }

    @Override
    public int getItemStackLimit(ItemStack stack) { return 1; }

    @Override
    public String upgradeName() { return NAME; }

    @Override
    public int installUpgrade(int eyeId, int qty) {
        if (ACTIVE && !UpgradeManager.isUpgradeInstalled(eyeId, NAME).isPresent())
            return qty - 1;
        return qty;
    }
}
*/