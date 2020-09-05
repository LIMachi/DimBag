package com.limachi.dimensional_bags.client;

import com.limachi.dimensional_bags.KeyMapController;
import com.limachi.dimensional_bags.client.entity.layer.BagLayer;
import com.limachi.dimensional_bags.client.entity.layer.ElytraOnBagLayer;
import com.limachi.dimensional_bags.client.entity.model.BagLayerModel;
import com.limachi.dimensional_bags.client.entity.render.BagEntityRender;
import com.limachi.dimensional_bags.client.itemEntity.EntityItemRenderer;
import com.limachi.dimensional_bags.client.screen.InventoryGUI;
import com.limachi.dimensional_bags.client.screen.PlayerInterfaceGUI;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.items.Bag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.entity.PlayerRenderer;
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
        ScreenManager.registerFactory(Registries.BAG_CONTAINER.get(), InventoryGUI::new);
        ScreenManager.registerFactory(Registries.PLAYER_CONTAINER.get(), PlayerInterfaceGUI::new);
        RenderingRegistry.registerEntityRenderingHandler(Registries.BAG_ITEM_ENTITY.get(), EntityItemRenderer::new);
        RenderTypeLookup.setRenderLayer(Registries.CLOUD_BLOCK.get(), RenderType.getTranslucent());
        for (int i = 0; i < KeyMapController.NON_VANILLA_KEY_BIND_COUNT; ++i)
            ClientRegistry.registerKeyBinding(KeyMapController.TRACKED_KEYBINDS[i]);
        ItemModelsProperties.func_239418_a_(Registries.BAG_ITEM.get(), new ResourceLocation(MOD_ID, "bag_mode_property"), Bag::getModeProperty);
        Map<String, PlayerRenderer> skin = Minecraft.getInstance().getRenderManager().getSkinMap();
        PlayerRenderer defaultSkin = skin.get("default");
        PlayerRenderer slimSkin = skin.get("slim");
        defaultSkin.addLayer(new BagLayer<>(defaultSkin, new BagLayerModel(false)));
        defaultSkin.addLayer(new ElytraOnBagLayer<>(defaultSkin));
        slimSkin.addLayer(new BagLayer<>(slimSkin, new BagLayerModel(false)));
        slimSkin.addLayer(new ElytraOnBagLayer<>(slimSkin));
    }

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(Registries.BAG_ENTITY.get(), BagEntityRender::new);
    }
}
