package com.limachi.dimensional_bags.common.bag;

import com.limachi.dimensional_bags.common.references.Registries;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.HolderData;
import com.limachi.dimensional_bags.lib.common.worldData.IBagIdHolder;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.passive.FoxEntity;
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

@Mod.EventBusSubscriber
@StaticInit
public class BagEntityItem extends ItemEntity implements IBagIdHolder {

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
                if (inv.getItem(i).getItem() instanceof BagItem) {
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

    @Override
    public void tick() {
        HolderData.tickEntity(this);
        super.tick();
    }

    public boolean attackEntityFrom(DamageSource source, float amount) {
        return false;
    }

    @Override
    public int getbagId() {
        return BagItem.getbagId(getItem());
    }
}
