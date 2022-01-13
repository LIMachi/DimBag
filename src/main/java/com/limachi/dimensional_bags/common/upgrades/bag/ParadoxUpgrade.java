package com.limachi.dimensional_bags.common.upgrades.bag;

import com.limachi.dimensional_bags.lib.ConfigManager.Config;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.upgrades.BaseUpgradeBag;
import com.limachi.dimensional_bags.common.upgrades.BagUpgradeManager;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
@StaticInit
public class ParadoxUpgrade extends BaseUpgradeBag<ParadoxUpgrade> {

    public static final String NAME = "paradox_upgrade";

    @Config(cmt = "can this upgrade be installed")
    public static boolean ACTIVE = true;

    static {
        BagUpgradeManager.registerUpgrade(NAME, ParadoxUpgrade::new);
    }

    public ParadoxUpgrade() { super(DimBag.DEFAULT_PROPERTIES); }

    @Override
    public boolean canBeInstalled() { return ACTIVE; }

    @Override
    public String upgradeName() { return NAME; }
}
