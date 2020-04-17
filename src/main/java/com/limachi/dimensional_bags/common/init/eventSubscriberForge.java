package com.limachi.dimensional_bags.common.init;

import com.limachi.dimensional_bags.common.dimensions.BagModDimension;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.world.RegisterDimensionsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.limachi.dimensional_bags.DimensionalBagsMod.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class eventSubscriberForge {
    public static final ResourceLocation DIM_TYPE_RL = new ResourceLocation(MOD_ID, BagModDimension.STR_ID);

    @SubscribeEvent
    public static void onRegisterDimensionEvent(RegisterDimensionsEvent event) {
        if (DimensionType.byName(DIM_TYPE_RL) == null) {
            DimensionManager.registerDimension(DIM_TYPE_RL, eventSubscriberMod.bagDimension, null, true);
        }
    }
}
