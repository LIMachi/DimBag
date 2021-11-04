package com.limachi.dimensional_bags.common.items.upgrades.bag;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.ConfigManager.Config;
import com.limachi.dimensional_bags.common.EventManager;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SettingsData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.items.upgrades.BaseUpgradeBag;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
@StaticInit
public class TurtleUpgrade extends BaseUpgradeBag<TurtleUpgrade> {

    public static final String NAME = "turtle_upgrade";

    @Config(cmt = "can this upgrade be installed")
    public static boolean ACTIVE = true;

    @Config(cmt = "single blows are divided by this mush to calculate if you should survive a hit (does not affect Threshold)")
    public static int GRACE_FACTOR = 4;

    static {
        UpgradeManager.registerUpgrade(NAME, TurtleUpgrade::new);
    }

    @Override
    public void initSettings(SettingsData.SettingsReader settingsReader) {
        super.initSettings(settingsReader);
        settingsReader.integer("Threshold", 5, 2, 20, true);
    }

    public TurtleUpgrade() { super(DimBag.DEFAULT_PROPERTIES); }

    @Override
    public boolean canBeInstalled() { return ACTIVE; }

    @Override
    public String upgradeName() { return NAME; }

    @SubscribeEvent
    public static void turtleUpgrade(LivingDamageEvent event) { //protect and tp the player in the bag if it would take damage setting it's health below a threshold
        LivingEntity entity = event.getEntityLiving();
        if (!entity.isAlive() || event.getAmount() <= 0) return;
        int id = Bag.getBag(entity, 0, true, false);
        if (id == 0) return;
        TurtleUpgrade turtleUpgrade = getInstance(NAME);
        if (!turtleUpgrade.isActive(id)) return;
        int limit = turtleUpgrade.getSetting(id, "Threshold");
        if (entity.getHealth() - event.getAmount() <= limit && entity.getHealth() - event.getAmount() / GRACE_FACTOR > 0) {
            event.setAmount(entity.getHealth() - limit);
            EventManager.delayedTask(0, ()->{
                LivingEntity eff = (LivingEntity) SubRoomsManager.execute(id, sm->sm.enterBag(entity), entity);
                if (eff != null) {
                    eff.addEffect(new EffectInstance(Effects.DIG_SLOWDOWN, 600, 2, false, false, false));
                    eff.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE, 200, 2, false, false, false));
                }});
        }
    }
}
