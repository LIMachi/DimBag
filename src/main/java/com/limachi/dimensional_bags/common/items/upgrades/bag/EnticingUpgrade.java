package com.limachi.dimensional_bags.common.items.upgrades.bag;

import com.limachi.dimensional_bags.ConfigManager.Config;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.items.entity.BagEntityItem;
import com.limachi.dimensional_bags.common.items.upgrades.BaseUpgradeBag;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;

import java.util.Iterator;

@Mod.EventBusSubscriber
@StaticInit
public class EnticingUpgrade extends BaseUpgradeBag<EnticingUpgrade> {

    public static final String NAME = "enticing_upgrade";

    @Config(cmt = "can this upgrade be installed")
    public static boolean ACTIVE = true;

    static {
        UpgradeManager.registerUpgrade(NAME, EnticingUpgrade::new);
    }

    @Override
    public ActionResultType upgradeEntityTick(int eyeId, World world, Entity entity) {
        if (entity instanceof BagEntityItem && !world.isClientSide && EnticingUpgrade.getInstance(EnticingUpgrade.NAME).isActive(eyeId)) {
            BagEntityItem self = (BagEntityItem)entity;
            for (MobEntity e : self.level.getEntitiesOfClass(MobEntity.class, self.getBoundingBox().inflate(0.5))) { //code to force pickup of bag by unwilling mob entities
                Iterator<ItemStack> it = e.getHandSlots().iterator();
                if (e instanceof AbstractVillagerEntity && !e.removed) {
                    ItemStack t = self.getItem().copy();
                    ItemStack r = ((AbstractVillagerEntity)e).getInventory().addItem(t);
                    if (r.isEmpty()) {
                        e.setCanPickUpLoot(true);
                        e.requiresCustomPersistence(); //the entity will no longer be able to despawn
                        e.onItemPickup(self);
                        self.remove();
                        break;
                    }
                } else if (!e.removed && it.hasNext() && (it.next().isEmpty() || it.hasNext())) { //found entity with at least one hand for the bag
                    EquipmentSlotType hand = e.getMainHandItem().isEmpty() ? EquipmentSlotType.MAINHAND : EquipmentSlotType.OFFHAND;
                    if (hand == EquipmentSlotType.OFFHAND && !e.getOffhandItem().isEmpty())
                        e.spawnAtLocation(e.getOffhandItem());
                    e.setItemSlot(hand, self.getItem());
                    e.setDropChance(hand, 2.f);
                    e.setCanPickUpLoot(true);
                    e.requiresCustomPersistence(); //the entity will no longer be able to despawn
                    e.onItemPickup(self);
                    self.remove();
                    break;
                }
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    public EnticingUpgrade() { super(DimBag.DEFAULT_PROPERTIES); }

    @Override
    public boolean canBeInstalled() { return ACTIVE; }

    @Override
    public String upgradeName() { return NAME; }
}
