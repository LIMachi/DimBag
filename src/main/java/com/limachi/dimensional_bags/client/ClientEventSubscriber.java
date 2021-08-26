package com.limachi.dimensional_bags.client;

import com.limachi.dimensional_bags.KeyMapController;
import com.limachi.dimensional_bags.client.entity.layer.BagLayer;
import com.limachi.dimensional_bags.client.entity.render.BagEntityRender;
import com.limachi.dimensional_bags.client.itemEntity.EntityItemRenderer;
import com.limachi.dimensional_bags.client.render.screen.*;
import com.limachi.dimensional_bags.client.render.tileEntity.Fountain;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.blocks.Cloud;
import com.limachi.dimensional_bags.common.container.*;
import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.items.GhostBag;
import com.limachi.dimensional_bags.common.items.entity.BagEntityItem;
import com.limachi.dimensional_bags.common.tileentities.FountainTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Map;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventSubscriber {

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        ClientRegistry.bindTileEntityRenderer(Registries.getBlockEntityType(FountainTileEntity.NAME), Fountain::new);
        ScreenManager.register(Registries.getContainerType(UserPillarContainer.NAME), UserPillarGUI::new);
        ScreenManager.register(Registries.getContainerType(SettingsContainer.NAME), SettingsScreen::new);
        ScreenManager.register(Registries.getContainerType(PillarContainer.NAME), SimpleContainerScreen<PillarContainer>::new);
        ScreenManager.register(Registries.getContainerType(FountainContainer.NAME), SimpleContainerScreen<FountainContainer>::new);

        RenderingRegistry.registerEntityRenderingHandler(Registries.getEntityType(BagEntityItem.NAME), EntityItemRenderer::new);

        ScreenManager.register(Registries.getContainerType(ClientSideOnlyScreenHandler.NAME), ClientSideOnlyScreenHandler::new);

        RenderTypeLookup.setRenderLayer(Cloud.INSTANCE.get(), RenderType.translucent());

        KeyMapController.KeyBindings.registerKeybindings();

        ItemModelsProperties.register(Registries.getItem(Bag.NAME), new ResourceLocation(MOD_ID, "bag_mode_property"), Bag::getModeProperty);
        ItemModelsProperties.register(Registries.getItem(GhostBag.NAME), new ResourceLocation(MOD_ID, "bag_mode_property"), GhostBag::getModeProperty);

        Map<String, PlayerRenderer> skin = Minecraft.getInstance().getEntityRenderDispatcher().getSkinMap();
        for (String key : skin.keySet()) {
            PlayerRenderer renderer = skin.get(key);
            renderer.addLayer(new BagLayer<>(renderer, new BipedModel(0.5F), new BipedModel(1.0F)));
        }
    }

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(Registries.getEntityType(BagEntity.NAME), BagEntityRender::new);
    }
}
