package com.limachi.dim_bag.entities;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.items.BagItem;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.lim_lib.World;
import com.limachi.lim_lib.registries.ClientRegistries;
import com.limachi.lim_lib.registries.StaticInitClient;
import com.limachi.lim_lib.registries.annotations.RegisterEntity;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = DimBag.MOD_ID)
public class BagItemEntity extends ItemEntity {

    @RegisterEntity(width = 0.25f, height = 0.25f)
    public static RegistryObject<EntityType<ItemEntity>> R_TYPE;

    @OnlyIn(Dist.CLIENT)
    @StaticInitClient
    public static void registerRenderer() { ClientRegistries.setEntityRenderer(R_TYPE, ItemEntityRenderer::new); }

    public BagItemEntity(EntityType<? extends ItemEntity> type, Level level) { super(type, level); }

    public BagItemEntity(Level level, double x, double y, double z, ItemStack stack) {
        this(level, x, y, z, stack, level.random.nextDouble() * 0.2D - 0.1D, 0.2D, level.random.nextDouble() * 0.2D - 0.1D);
    }

    public BagItemEntity(Level level, double x, double y, double z, ItemStack stack, double vx, double vy, double vz) {
        this(R_TYPE.get(), level);
        this.setPos(x, y, z);
        this.setDeltaMovement(vx, vy, vz);
        this.setItem(stack);
        lifespan = (stack.getItem() == null ? 6000 : stack.getEntityLifespan(level));
        setPickUpDelay(40);
        setUnlimitedLifetime();
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }

    @Override
    public boolean ignoreExplosion() { return true; }

    @Override
    public boolean hurt(DamageSource source, float amount) { return false; }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void specialVillagerDeathEvent(LivingDropsEvent event) { //make sure the bag is not lost by a villager
        if (event.getEntity() instanceof AbstractVillager) {
            AbstractVillager villager = (AbstractVillager)event.getEntity();
            SimpleContainer inv = villager.getInventory();
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

    public int getBagId() { return BagItem.getBagId(getItem()); }

    protected int loadCoolDown = 0;
    protected BlockPos lastPos = blockPosition();

    @Override
    public void tick() {
        if (!level().isClientSide && (--loadCoolDown <= 0 || !lastPos.equals(blockPosition()))) {
            World.temporaryChunkLoad(level(), blockPosition());
            loadCoolDown = 200;
            lastPos = blockPosition();
            BagsData.runOnBag(getBagId(), b->{
                b.setHolder(this);
                b.temporaryChunkLoad();
            });
        }
        super.tick();
    }
}
