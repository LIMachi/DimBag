package com.limachi.dimensional_bags.client;

import com.limachi.dimensional_bags.client.entity.render.BagEntityRender;
import com.limachi.dimensional_bags.client.itemEntity.EntityItemRenderer;
import com.limachi.dimensional_bags.client.screen.InventoryGUI;
import com.limachi.dimensional_bags.client.screen.PlayerInterfaceGUI;
import com.limachi.dimensional_bags.client.screen.UpgradeGUI;
import com.limachi.dimensional_bags.common.Registries;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventSubscriber {

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        ScreenManager.registerFactory(Registries.BAG_CONTAINER.get(), InventoryGUI::new);
        ScreenManager.registerFactory(Registries.PLAYER_CONTAINER.get(), PlayerInterfaceGUI::new);
        ScreenManager.registerFactory(Registries.UPGRADE_CONTAINER.get(), UpgradeGUI::new);
        RenderingRegistry.registerEntityRenderingHandler(Registries.BAG_ITEM_ENTITY.get(), EntityItemRenderer::new);
    }

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(Registries.BAG_ENTITY.get(), BagEntityRender::new);
    }
}
