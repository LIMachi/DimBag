package com.limachi.dimensional_bags.common.upgrades.bag;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.lib.ConfigManager.Config;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.bag.BagEntity;
import com.limachi.dimensional_bags.common.bag.BagItem;
import com.limachi.dimensional_bags.common.upgrades.BaseUpgradeBag;
import com.limachi.dimensional_bags.common.upgrades.BagUpgradeManager;
import com.limachi.dimensional_bags.lib.utils.WorldUtils;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
@StaticInit
public class DeathHopperUpgrade extends BaseUpgradeBag<DeathHopperUpgrade> {

    public static final String NAME = "death_hopper_upgrade";

    @Config(cmt = "can this upgrade be installed")
    public static boolean ACTIVE = true;

    @Config(cmt = "does this upgrade apply an effect to items so they dont despawn over time")
    public static boolean ITEMS_DO_NOT_DESPAWN = true;

    static {
        BagUpgradeManager.registerUpgrade(NAME, DeathHopperUpgrade::new);
    }

    public DeathHopperUpgrade() { super(DimBag.DEFAULT_PROPERTIES); }

    @Override
    public boolean canBeInstalled() { return ACTIVE; }

    @Override
    public String upgradeName() { return NAME; }

    public static ItemEntity setNeverDespawn(ItemEntity entity) {
        CompoundNBT t = new CompoundNBT();
        entity.save(t);
        t.putShort("Age", Short.MIN_VALUE); //when set to short min, the age will not tick up ItemEntity#tick():141
        t.putInt("Lifespan", Short.MIN_VALUE + 100); //but to make sure players can pickup the item, we also need to tamper the lifespan ItemEntity#playerTouch(PlayerEntity):325
        entity.load(t); //we reload the entity with the two changed tags
        return entity;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void deathHopperUpgrade(LivingDropsEvent event) { //collect the holder item's on death and send them inside the bag (change the cooldown of the items so they don't despawn)
        DimBag.LOGGER.info("entity drop: " + event.getDrops());
        if (event.getDrops().isEmpty()) return;
        int[] id = {0};
        event.getDrops().removeIf(item->{
            if (item.getItem().getItem() instanceof BagItem && BagItem.getbagId(item.getItem()) != 0) {
                id[0] = BagItem.getbagId(item.getItem());
                if (getInstance(NAME).isActive(id[0])) {
                    SubRoomsManager sm = SubRoomsManager.getInstance(id[0]);
                    if (sm != null) {
                        BagEntity.spawn(event.getEntity().level, event.getEntity().blockPosition(), item.getItem()).addEffect(new EffectInstance(Effects.GLOWING, 6000, 1, false, false, false));
                        item.remove();
                        return true;
                    }
                }
            }
            return false;
        });
        if (id[0] > 0 && getInstance(NAME).isActive(id[0])) {
            SubRoomsManager.execute(id[0], sm->event.getDrops().forEach(item->{
                if (!item.removed) {
                    if (ITEMS_DO_NOT_DESPAWN)
                        setNeverDespawn(item);
                    if (item.level.dimension().equals(WorldUtils.DimBagRiftKey))
                        item.level.addFreshEntity(item); //since the event will be cancelled and the teleport will not copy this entity because we are in the same dimension, we add it manually
                    sm.enterBag(item, false, true, false, false, false);
                }
            }));
            event.setCanceled(true);
        }
    }
}
