package com.limachi.dimensional_bags.common.items.upgrades;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.ConfigManager.Config;
import com.limachi.dimensional_bags.common.EventManager;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SettingsData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
@StaticInit
public class TurtleUpgrade extends BaseUpgrade {

    public static final String NAME = "turtle_upgrade";

    @Config(cmt = "can this upgrade be installed")
    public static boolean ACTIVE = true;

    @Config(cmt = "single blows are divided by this mush to calculate if you should survive a hit (does not affect Threshold)")
    public static int GRACE_FACTOR = 4;

    static {
        Registries.registerItem(NAME, TurtleUpgrade::new);
        UpgradeManager.registerUpgrade(NAME, new TurtleUpgrade());
    }

    @Override
    public void initSettings(SettingsData.Settings settings) {
        settings.integer("Threshold", 5, 2, 20, true);
    }

    public TurtleUpgrade() { super(DimBag.DEFAULT_PROPERTIES); }

    @Override
    public boolean canBeInstalled() { return ACTIVE; }

    @Override
    public String upgradeName() { return NAME; }

    @Override
    public int installUpgrade(UpgradeManager manager, int qty) { return qty; }

    @SubscribeEvent
    public static void turtleUpgrade(LivingDamageEvent event) { //protect and tp the player in the bag if it would take damage setting it's health below a threshold
        LivingEntity entity = event.getEntityLiving();
        if (!entity.isAlive() || event.getAmount() <= 0) return;
        int id = Bag.getBag(entity, 0);
        if (id == 0) return;
        BaseUpgrade turtleUpgrade = getInstance(NAME);
        if (!turtleUpgrade.isActive(id)) return;
        int limit = turtleUpgrade.getSetting(id, "Threshold");
        if (entity.getHealth() - event.getAmount() <= limit && entity.getHealth() - event.getAmount() / GRACE_FACTOR > 0) {
            event.setAmount(entity.getHealth() - limit);
            EventManager.delayedTask(0, ()->{
                LivingEntity eff = (LivingEntity) SubRoomsManager.execute(id, sm->sm.enterBag(entity), entity);
                if (eff != null) {
                    eff.addPotionEffect(new EffectInstance(Effects.MINING_FATIGUE, 600, 2, false, false, false));
                    eff.addPotionEffect(new EffectInstance(Effects.RESISTANCE, 200, 2, false, false, false));
                }});
        }
    }
}
