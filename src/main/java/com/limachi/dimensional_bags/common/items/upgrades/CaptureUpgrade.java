package com.limachi.dimensional_bags.common.items.upgrades;

import com.limachi.dimensional_bags.ConfigManager.Config;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SettingsData;
import com.limachi.dimensional_bags.common.managers.ModeManager;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
@StaticInit
public class CaptureUpgrade extends BaseUpgrade<CaptureUpgrade> {

    public static final String NAME = "capture_upgrade";

    @Config(cmt = "can this upgrade be installed")
    public static boolean ACTIVE = true;

    static {
        UpgradeManager.registerUpgrade(NAME, CaptureUpgrade::new);
    }

    @Override
    public void initSettings(SettingsData.SettingsReader settingsReader) {}

    public CaptureUpgrade() { super(DimBag.DEFAULT_PROPERTIES); }

    @Override
    public boolean canBeInstalled() { return ACTIVE; }

    @Override
    public String upgradeName() { return NAME; }

    @Override
    public int installUpgrade(UpgradeManager manager, int qty) {
        ModeManager.execute(manager.getEyeId(), mm->mm.installMode("Capture"));
        return qty;
    }
}
