package com.limachi.dimensional_bags.client;

import com.limachi.dimensional_bags.client.tile_entity.BagEyeBackGroundTERenderer;
import com.limachi.dimensional_bags.common.init.Registries;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static com.limachi.dimensional_bags.DimensionalBagsMod.MOD_ID;
import static com.limachi.dimensional_bags.common.init.Registries.TET_BAG_EYE;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class eventSubscriber {

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        ClientRegistry.bindTileEntityRenderer(TET_BAG_EYE, BagEyeBackGroundTERenderer::new);
    }
}
