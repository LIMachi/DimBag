package com.limachi.dimensional_bags.client;

import com.limachi.dimensional_bags.client.entity.layer.BagLayer;
import com.limachi.dimensional_bags.client.entity.render.BagEntityRender;
import com.limachi.dimensional_bags.client.itemEntity.EntityItemRenderer;
import com.limachi.dimensional_bags.client.render.screen.*;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.items.GhostBag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Map;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventSubscriber {

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        ScreenManager.registerFactory(Registries.BAG_CONTAINER.get(), InventoryGUI::new);
        ScreenManager.registerFactory(Registries.PLAYER_CONTAINER.get(), PlayerInterfaceGUI::new);
        ScreenManager.registerFactory(Registries.BRAIN_CONTAINER.get(), BrainGUI::new);
        ScreenManager.registerFactory(Registries.GHOST_HAND_CONTAINER.get(), GhostHandGUI::new);
        ScreenManager.registerFactory(Registries.SETTINGS_CONTAINER.get(), SettingsGUI::new);

        RenderingRegistry.registerEntityRenderingHandler(Registries.BAG_ITEM_ENTITY.get(), EntityItemRenderer::new);

        RenderTypeLookup.setRenderLayer(Registries.CLOUD_BLOCK.get(), RenderType.getTranslucent());

        ItemModelsProperties.registerProperty(Registries.BAG_ITEM.get(), new ResourceLocation(MOD_ID, "bag_mode_property"), Bag::getModeProperty);
        ItemModelsProperties.registerProperty(Registries.GHOST_BAG_ITEM.get(), new ResourceLocation(MOD_ID, "bag_mode_property"), GhostBag::getModeProperty);

        Map<String, PlayerRenderer> skin = Minecraft.getInstance().getRenderManager().getSkinMap();
        for (String key : skin.keySet()) {
            PlayerRenderer renderer = skin.get(key);
            renderer.addLayer(new BagLayer<>(renderer, new BipedModel(0.5F), new BipedModel(1.0F)));
        }

        Map<EntityType<?>, EntityRenderer<?>> renderers = Minecraft.getInstance().getRenderManager().renderers;
    }

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(Registries.BAG_ENTITY.get(), BagEntityRender::new);
    }
}
