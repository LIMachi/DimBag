package com.limachi.dimensional_bags.proxy;

import com.limachi.dimensional_bags.client.entity.layer.BagLayer;
import com.limachi.dimensional_bags.client.entity.model.BagLayerModel;
import com.limachi.dimensional_bags.client.entity.render.BagEntityRender;
import com.limachi.dimensional_bags.common.init.Registries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Map;

public class Client implements ICommonProxy {

    public void onClientSetup(FMLClientSetupEvent event) {
        Map<String, PlayerRenderer> skin = Minecraft.getInstance().getRenderManager().getSkinMap();
        PlayerRenderer defaultSkin = skin.get("default");
        defaultSkin.addLayer(new BagLayer<>(defaultSkin, new BagLayerModel<>(false)));
        PlayerRenderer slimSkin = skin.get("slim");
        slimSkin.addLayer(new BagLayer<>(slimSkin, new BagLayerModel<>(false)));
    }

    public void registerModels(ModelRegistryEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(Registries.BAG_ENTITY.get(), BagEntityRender::new);
    }
}
