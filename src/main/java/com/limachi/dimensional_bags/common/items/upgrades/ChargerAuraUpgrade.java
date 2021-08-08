package com.limachi.dimensional_bags.common.items.upgrades;

import com.limachi.dimensional_bags.ConfigManager.Config;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ActionResultType;
import net.minecraft.world.World;

@StaticInit
public class ChargerAuraUpgrade extends BaseUpgrade {

    public static final String NAME = "charger_aura_upgrade";

    @Config(cmt = "can this upgrade be installed")
    public static boolean ACTIVE = true;

    static {
        UpgradeManager.registerUpgrade(NAME, ChargerAuraUpgrade::new);
    }

    public ChargerAuraUpgrade() { super(DimBag.DEFAULT_PROPERTIES); }

    @Override
    public boolean canBeInstalled() { return ACTIVE; }

    @Override
    public String upgradeName() { return NAME; }

    @Override
    public int installUpgrade(UpgradeManager manager, int qty) { return qty; }

    @Override
    public ActionResultType upgradeEntityTick(int eyeId, World world, Entity entity) {
        //should find any chargeable item in the entity inventory and try to charge them by using this bag energy crystals
        return ActionResultType.SUCCESS;
    }
}
