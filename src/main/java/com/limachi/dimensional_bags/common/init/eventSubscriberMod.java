package com.limachi.dimensional_bags.common.init;

import com.limachi.dimensional_bags.common.dimensions.BagModDimension;
import net.minecraftforge.common.ModDimension;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

import static com.limachi.dimensional_bags.DimensionalBagsMod.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class eventSubscriberMod {
    @ObjectHolder(BagModDimension.REG_ID)
    public static final ModDimension bagDimension = null;

    @SubscribeEvent
    public static void onDimensionRegistryEvent(RegistryEvent.Register<ModDimension> event) {
        event.getRegistry().register(new BagModDimension().setRegistryName(BagModDimension.REG_ID));
    }
}
