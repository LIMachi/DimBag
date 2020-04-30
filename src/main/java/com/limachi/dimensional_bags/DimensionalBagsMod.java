package com.limachi.dimensional_bags;

import com.google.common.reflect.Reflection;
import com.limachi.dimensional_bags.compat.Curios;
import com.limachi.dimensional_bags.common.config.DimBagConfig;
import com.limachi.dimensional_bags.common.data.DimBagData;
import com.limachi.dimensional_bags.common.init.Registries;
import com.limachi.dimensional_bags.common.network.PacketHandler;
import com.limachi.dimensional_bags.proxy.Client;
import com.limachi.dimensional_bags.proxy.ICommonProxy;
import com.limachi.dimensional_bags.proxy.Server;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
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

import static com.limachi.dimensional_bags.common.init.Registries.BAG_ITEM;

@Mod("dim_bag")
public class DimensionalBagsMod
{
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "dim_bag";
    public static DimensionalBagsMod instance;
    public static final ICommonProxy PROXY = DistExecutor.runForDist(() -> Client::new, ()-> Server::new);

    public DimBagData client_side_mirror = null; //copy of the overworld-saved data on the server, server should send packets with the necessary data to populate this copy, should be updaed upon player connecting

    public DimensionalBagsMod() {
        Reflection.initialize(PacketHandler.class); //force initialization of the class statics ASAP
        final IEventBus meb = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, DimBagConfig.getSpec());
        Registries.registerAll();
        instance = this;
        MinecraftForge.EVENT_BUS.register(this);
        meb.addListener(this::registerModels);
        meb.addListener(this::onCommonSetup);
        meb.addListener(this::onClientSetup);
        meb.addListener(this::onEnqueueIMC);
    }

    public void registerModels(ModelRegistryEvent event) { PROXY.registerModels(event); }

    public void onCommonSetup(FMLCommonSetupEvent event) {}

    public void onClientSetup(FMLClientSetupEvent event) { PROXY.onClientSetup(event); }

    public void onEnqueueIMC(InterModEnqueueEvent event) {
        ModList ML = ModList.get();
        if (ML != null && ML.getModContainerById("curios").isPresent()) {
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
