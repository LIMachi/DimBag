package com.limachi.dimensional_bags.client;

import com.limachi.dimensional_bags.KeyMapController;
import com.limachi.dimensional_bags.client.entity.layer.BagLayer;
import com.limachi.dimensional_bags.client.entity.render.BagEntityRender;
import com.limachi.dimensional_bags.client.itemEntity.EntityItemRenderer;
import com.limachi.dimensional_bags.client.render.screen.*;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.blocks.Cloud;
import com.limachi.dimensional_bags.common.container.*;
import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.items.GhostBag;
import com.limachi.dimensional_bags.common.items.entity.BagEntityItem;
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
        ScreenManager.registerFactory(Registries.getContainerType(BagContainer.NAME), InventoryGUI::new);
        ScreenManager.registerFactory(Registries.getContainerType(WrappedPlayerInventoryContainer.NAME), PlayerInterfaceGUI::new);
        ScreenManager.registerFactory(Registries.getContainerType(BrainContainer.NAME), BrainGUI::new);
        ScreenManager.registerFactory(Registries.getContainerType(GhostHandContainer.NAME), GhostHandGUI::new);
        ScreenManager.registerFactory(Registries.getContainerType(SettingsContainer.NAME), SettingsGUI::new);

        RenderingRegistry.registerEntityRenderingHandler(Registries.getEntityType(BagEntityItem.NAME), EntityItemRenderer::new);

        RenderTypeLookup.setRenderLayer(Registries.getBlock(Cloud.NAME), RenderType.getTranslucent());

        KeyMapController.KeyBindings.registerKeybindings();

        ItemModelsProperties.registerProperty(Registries.getItem(Bag.NAME), new ResourceLocation(MOD_ID, "bag_mode_property"), Bag::getModeProperty);
        ItemModelsProperties.registerProperty(Registries.getItem(GhostBag.NAME), new ResourceLocation(MOD_ID, "bag_mode_property"), GhostBag::getModeProperty);

        Map<String, PlayerRenderer> skin = Minecraft.getInstance().getRenderManager().getSkinMap();
        for (String key : skin.keySet()) {
            PlayerRenderer renderer = skin.get(key);
            renderer.addLayer(new BagLayer<>(renderer, new BipedModel(0.5F), new BipedModel(1.0F)));
        }

        Map<EntityType<?>, EntityRenderer<?>> renderers = Minecraft.getInstance().getRenderManager().renderers;
    }

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(Registries.getEntityType(BagEntity.NAME), BagEntityRender::new);
    }
}
