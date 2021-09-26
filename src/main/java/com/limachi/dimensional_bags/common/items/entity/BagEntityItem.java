package com.limachi.dimensional_bags.common.items.entity;

import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.HolderData;
import com.limachi.dimensional_bags.common.data.IEyeIdHolder;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.items.upgrades.EnticingUpgrade;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.limachi.dimensional_bags.StaticInit;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.Iterator;

@Mod.EventBusSubscriber
@StaticInit
public class BagEntityItem extends ItemEntity implements IEyeIdHolder {

    public static final String NAME = "bag_item";

    static {
        Registries.registerEntityType(NAME, ()->EntityType.Builder.<BagEntityItem>of(BagEntityItem::new, EntityClassification.MISC).sized(0.25F, 0.25F).build("bag_item"));
    }

    public BagEntityItem(EntityType<? extends ItemEntity> type, World world) { super(type, world); }

    public BagEntityItem(World worldIn, double x, double y, double z, ItemStack stack) {
        super(Registries.getEntityType(NAME), worldIn);
        this.setPos(x, y, z);
        this.setItem(stack);
        setInvulnerable(true);
        lifespan = Integer.MAX_VALUE;
        setPickUpDelay(10);
    }

    @Override
    public IPacket<?> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }

    @Override
    public boolean hurt(DamageSource source, float amount) { return false; }

    @Override
    protected void outOfWorld() {}

    @Override
    public boolean ignoreExplosion() { return true; }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void specialVillagerDeathEvent(LivingDropsEvent event) { //make sure the bag is not lost by a villager
        if (event.getEntity() instanceof AbstractVillagerEntity) {
            AbstractVillagerEntity villager = (AbstractVillagerEntity)event.getEntity();
            Inventory inv = villager.getInventory();
            boolean foundBag = false;
            for (int i = 0; i < inv.getContainerSize(); ++i)
                if (inv.getItem(i).getItem() instanceof Bag) {
                    foundBag = true;
                    break;
                }
            if (foundBag) {
                for (int i = 0; i < inv.getContainerSize(); ++i) {
                    ItemStack t = inv.getItem(i);
                    if (!t.isEmpty())
                        event.getDrops().add(villager.spawnAtLocation(t));
                }
                inv.clearContent();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void patchLivingDropEventForFoxesPartOne(LivingDeathEvent event) {
        if (event.getEntity() instanceof FoxEntity && event.getEntity().level instanceof ServerWorld) {
            FoxEntity fox = (FoxEntity) event.getEntity();
            if (!fox.removed && !fox.getMainHandItem().isEmpty()) {
                fox.getPersistentData().put("FoxEntity#spawnDrops", fox.getMainHandItem().save(new CompoundNBT()));
                fox.setItemInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void patchLivingDropEventForFoxesPartTwo(LivingDropsEvent event) {
        if (event.getEntity() instanceof FoxEntity && event.getEntity().level instanceof ServerWorld) {
            ItemStack mh = ItemStack.of(event.getEntity().getPersistentData().getCompound("FoxEntity#spawnDrops"));
            if (!mh.isEmpty()) event.getDrops().add(event.getEntity().spawnAtLocation(mh));
        }
    }

    //FIXME: should be moved to the upgrade and in the upgrade tick
    public static void enticingUpgradeBehavior(int id, BagEntityItem self) {
        if (EnticingUpgrade.getInstance(EnticingUpgrade.NAME).isActive(id))
            for (MobEntity entity : self.level.getEntitiesOfClass(MobEntity.class, self.getBoundingBox().inflate(0.5))) { //code to force pickup of bag by unwilling mob entities
                Iterator<ItemStack> it = entity.getHandSlots().iterator();
                if (entity instanceof AbstractVillagerEntity && !entity.removed) {
                    ItemStack t = self.getItem().copy();
                    ItemStack r = ((AbstractVillagerEntity)entity).getInventory().addItem(t);
                    if (r.isEmpty()) {
                        entity.setCanPickUpLoot(true);
                        entity.requiresCustomPersistence(); //the entity will no longer be able to despawn
                        entity.onItemPickup(self);
                        self.remove();
                        break;
                    }
                } else if (!entity.removed && it.hasNext() && (it.next().isEmpty() || it.hasNext())) { //found entity with at least one hand for the bag
                    EquipmentSlotType hand = entity.getMainHandItem().isEmpty() ? EquipmentSlotType.MAINHAND : EquipmentSlotType.OFFHAND;
                    if (hand == EquipmentSlotType.OFFHAND && !entity.getOffhandItem().isEmpty())
                        entity.spawnAtLocation(entity.getOffhandItem());
                    entity.setItemSlot(hand, self.getItem());
                    entity.setDropChance(hand, 2.f);
                    entity.setCanPickUpLoot(true);
                    entity.requiresCustomPersistence(); //the entity will no longer be able to despawn
                    entity.onItemPickup(self);
                    self.remove();
                    break;
                }
            }
    }

    @Override
    public void tick() {
        HolderData.tickEntity(this);
        super.tick();
    }

    public boolean attackEntityFrom(DamageSource source, float amount) {
        return false;
    }

    @Override
    public int getEyeId() {
        return Bag.getEyeId(getItem());
    }
}
