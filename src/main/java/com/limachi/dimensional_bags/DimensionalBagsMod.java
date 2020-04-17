package com.limachi.dimensional_bags;

import com.limachi.dimensional_bags.client.entity.layer.BagLayer;
import com.limachi.dimensional_bags.client.entity.model.BagEntityModel;
import com.limachi.dimensional_bags.common.IMC.curios.Curios;
import com.limachi.dimensional_bags.common.config.DimBagConfig;
import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.limachi.dimensional_bags.common.init.Registries;
import com.limachi.dimensional_bags.proxy.Client;
import com.limachi.dimensional_bags.proxy.ICommonProxy;
import com.limachi.dimensional_bags.proxy.Server;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
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
    public void onCommonSetup(FMLCommonSetupEvent event) {

    }

    @SubscribeEvent
    public void onClientSetup(FMLClientSetupEvent event) { PROXY.onClientSetup(event); }

    @SubscribeEvent
    public void onEnqueueIMC(InterModEnqueueEvent event) {
        ModList ML = ModList.get();
        if (ML != null && ML.getModContainerById("curios").isPresent())
            Curios.registerBagSlot();
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
