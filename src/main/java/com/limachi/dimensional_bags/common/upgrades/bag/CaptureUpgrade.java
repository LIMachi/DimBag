package com.limachi.dimensional_bags.common.upgrades.bag;

import com.limachi.dimensional_bags.lib.ConfigManager.Config;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.SettingsData;
import com.limachi.dimensional_bags.common.upgrades.BaseUpgradeBag;
import com.limachi.dimensional_bags.common.bag.modes.ModeManager;
import com.limachi.dimensional_bags.common.upgrades.BagUpgradeManager;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
@StaticInit
public class CaptureUpgrade extends BaseUpgradeBag<CaptureUpgrade> {

    public static final String NAME = "capture_upgrade";

    @Config(cmt = "can this upgrade be installed")
    public static boolean ACTIVE = true;

    static {
        BagUpgradeManager.registerUpgrade(NAME, CaptureUpgrade::new);
    }

    @Override
    public void initSettings(SettingsData.SettingsReader settingsReader) {}

    public CaptureUpgrade() { super(DimBag.DEFAULT_PROPERTIES); }

    @Override
    public boolean canBeInstalled() { return ACTIVE; }

    @Override
    public String upgradeName() { return NAME; }

    @Override
    public int installUpgrade(BagUpgradeManager manager, int qty) {
        ModeManager.execute(manager.getbagId(), mm->mm.installMode("Capture"));
        return qty;
    }
}
