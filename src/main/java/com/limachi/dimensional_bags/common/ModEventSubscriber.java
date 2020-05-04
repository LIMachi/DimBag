package com.limachi.dimensional_bags.common;

import com.limachi.dimensional_bags.common.dimension.BagRiftDimension;
import net.minecraftforge.common.ModDimension;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventSubscriber {

    @ObjectHolder(BagRiftDimension.REG_ID)
    public static final ModDimension bagDimension = null;

    @SubscribeEvent
    public static void onDimensionRegistry(RegistryEvent.Register<ModDimension> event) {
        event.getRegistry().register(new BagRiftDimension.BagRiftModDimension().setRegistryName(BagRiftDimension.REG_ID));
    }
}
