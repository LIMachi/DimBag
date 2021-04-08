package com.limachi.dimensional_bags.common.items.upgrades;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.ConfigManager.Config;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
@StaticInit
public class DeathHopperUpgrade extends BaseUpgrade {

    public static final String NAME = "death_hopper_upgrade";

    @Config(cmt = "can this upgrade be installed")
    public static boolean ACTIVE = true;

    @Config(cmt = "does this upgrade apply an effect to items so they dont despawn over time")
    public static boolean ITEMS_DO_NOT_DESPAWN = true;

    static {
        UpgradeManager.registerUpgrade(NAME, DeathHopperUpgrade::new);
    }

    public DeathHopperUpgrade() { super(DimBag.DEFAULT_PROPERTIES); }

    @Override
    public boolean canBeInstalled() { return ACTIVE; }

    @Override
    public String upgradeName() { return NAME; }

    @Override
    public int installUpgrade(UpgradeManager manager, int qty) { return qty; }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void deathHopperUpgrade(LivingDropsEvent event) { //collect the player item's on death and send them inside the bag (change the cooldown of the items so they don't despawn)
        if (event.getDrops().isEmpty()) return;
        SubRoomsManager sm = null;
        for (ItemEntity item : event.getDrops())
            if (item.getItem().getItem() instanceof Bag && Bag.getEyeId(item.getItem()) != 0) {
                int id = Bag.getEyeId(item.getItem());
                UpgradeManager up = UpgradeManager.getInstance(id);
                if (!up.getInstalledUpgrades().contains(NAME)) continue;
                sm = SubRoomsManager.getInstance(id);
                if (sm != null) {
                    BagEntity.spawn(event.getEntity().world, event.getEntity().getPosition(), item.getItem()).addPotionEffect(new EffectInstance(Effects.GLOWING, 6000, 1, false, false, false));
                    item.remove();
                    break;
                }
            }
        if (sm != null) {
            for (ItemEntity item : event.getDrops())
                if (!item.removed) {
                    if (ITEMS_DO_NOT_DESPAWN)
                        item.setNoDespawn();
                    sm.enterBag(item, false, true, false);
                }
            event.setCanceled(true);
        }
    }
}
