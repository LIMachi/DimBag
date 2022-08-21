package com.limachi.dim_bag;

import com.limachi.dim_bag.layers.BagLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = DimBag.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Client {
    @SubscribeEvent
    static void registerLayersRenderers(EntityRenderersEvent.AddLayers event) {
        for (String rp : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(rp);
            if (renderer != null)
                renderer.addLayer(new BagLayer<>(renderer, event.getEntityModels()));
        }
    }
}
