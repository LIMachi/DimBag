package com.limachi.dimensional_bags;

import com.limachi.dimensional_bags.client.entity.layer.BagLayer;
import com.limachi.dimensional_bags.client.entity.model.BagEntityModel;
import com.limachi.dimensional_bags.common.IMC.curios.Curios;
import com.limachi.dimensional_bags.common.config.DimBagConfig;
import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.limachi.dimensional_bags.common.init.Registries;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.proxy.Client;
import com.limachi.dimensional_bags.proxy.ICommonProxy;
import com.limachi.dimensional_bags.proxy.Server;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

import static com.limachi.dimensional_bags.common.init.Registries.BAG_ITEM;

@Mod("dim_bag")
public class DimensionalBagsMod
{
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "dim_bag";
    public static DimensionalBagsMod instance;
    public static final ICommonProxy PROXY = DistExecutor.runForDist(() -> Client::new, ()-> Server::new);

    public DimensionalBagsMod() {
        final IEventBus meb = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, DimBagConfig.SPEC);
        Registries.registerAll();
        instance = this;
        MinecraftForge.EVENT_BUS.register(this);
        meb.addListener(this::registerModels);
        meb.addListener(this::onCommonSetup);
        meb.addListener(this::onClientSetup);
        meb.addListener(this::onEnqueueIMC);
    }

    public void registerModels(ModelRegistryEvent event) { PROXY.registerModels(event); }

    @SubscribeEvent //temporary trick
    public void onSpawnEntity(final EntityJoinWorldEvent event) {
        Entity ent = event.getEntity();
        if (ent instanceof BagEntity) {
            DimensionalBagsMod.LOGGER.info("detected new bag entity");
            ((BagEntity) ent).setNoAI(true);
            ent.setInvulnerable(true);
        }
    }

    @SubscribeEvent
    public void onItemToss(ItemTossEvent event) { //might move this to when the item-entity hits the ground instead (but this one has the advantage to detect that it was a player who droped the item)
        ItemStack is = event.getEntityItem().getItem();
        World world = event.getPlayer().getEntityWorld();
//        if (!world.isRemote()) return;
        if (is.getItem() instanceof Bag) { //catch when a bag is thrown in the world by a player and make a new bag entity
            LOGGER.info("bag was droped");
            int id;
            try {
                id = is.getTag().getInt("ID");
            } catch (NullPointerException e) {
                id = -1;
            }
            BagEntity new_bag = new BagEntity(Registries.BAG_ENTITY.get(), world);
            new_bag.getPersistentData().putInt("ID", id);
            ItemEntity ie = event.getEntityItem();
            new_bag.setPosition(ie.getPosX(), ie.getPosY(), ie.getPosZ());
            world.addEntity(new_bag);
            event.setCanceled(true); //since the event has already been fired, the item is now gone (in the limbo between inventory and the world, never to return)
        }
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        if (/*!event.getPlayer().getEntityWorld().isRemote() &&*/ event.getTarget() instanceof BagEntity) { //detect that a bag was punshed by a player, will try to give the player back the bag (in curios or hand, otherwise prevent the punch)
            LOGGER.info("bag is attacked!");
            event.setCanceled(true);
            PlayerEntity player = event.getPlayer();
            int slot;
            if (Curios.INSTANCE.getStack(player, Curios.BACKPACK_SLOT_ID, 0).getItem() == Items.AIR) {
                slot = 0;
            } else if (player.getHeldItemMainhand().getItem() == Items.AIR) {
                slot = 1;
            } else if (player.getHeldItemOffhand().getItem() == Items.AIR) {
                slot = 2;
            } else return;
            ItemStack new_bag = new ItemStack(Registries.BAG_ITEM.get());
            int id = ((BagEntity)event.getTarget()).getPersistentData().getInt("ID");
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt("ID", id);
            new_bag.setTag(nbt);
            if (slot != 0)
                player.setHeldItem(slot == 1 ? Hand.MAIN_HAND : Hand.OFF_HAND, new_bag);
            else
                Curios.INSTANCE.setStack(player, Curios.BACKPACK_SLOT_ID, 0, new_bag);
            ((BagEntity) event.getTarget()).setHealth(0);
        }
    }

    public void onCommonSetup(FMLCommonSetupEvent event) {}

    public void onClientSetup(FMLClientSetupEvent event) { PROXY.onClientSetup(event); }

    public void onEnqueueIMC(InterModEnqueueEvent event) {
        LOGGER.info("IMC event caught");
        ModList ML = ModList.get();
        if (ML != null && ML.getModContainerById("curios").isPresent()) {
            LOGGER.info("connecting to curios");
            Curios.INSTANCE.registerBagSlot();
        }
    }

    public static class ItemGroup extends net.minecraft.item.ItemGroup {

        public static final ItemGroup instance = new ItemGroup(net.minecraft.item.ItemGroup.GROUPS.length, "tab_dim_bag");

        private ItemGroup(int index, String label) {
            super(index, label);
        }

        @Override
        public ItemStack createIcon() {
            return new ItemStack(BAG_ITEM.get());
        }
    }
}
