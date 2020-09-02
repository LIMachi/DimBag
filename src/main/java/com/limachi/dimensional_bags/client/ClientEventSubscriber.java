package com.limachi.dimensional_bags.client;

import com.limachi.dimensional_bags.client.entity.render.BagEntityRender;
import com.limachi.dimensional_bags.client.itemEntity.EntityItemRenderer;
import com.limachi.dimensional_bags.client.screen.InventoryGUI;
import com.limachi.dimensional_bags.client.screen.PlayerInterfaceGUI;
import com.limachi.dimensional_bags.common.Registries;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventSubscriber {

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        ScreenManager.registerFactory(Registries.BAG_CONTAINER.get(), InventoryGUI::new);
        ScreenManager.registerFactory(Registries.PLAYER_CONTAINER.get(), PlayerInterfaceGUI::new);
        RenderingRegistry.registerEntityRenderingHandler(Registries.BAG_ITEM_ENTITY.get(), EntityItemRenderer::new);
        RenderTypeLookup.setRenderLayer(Registries.CLOUD_BLOCK.get(), RenderType.getTranslucent());
        ClientRegistry.registerKeyBinding(openGuiKey);
    }

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(Registries.BAG_ENTITY.get(), BagEntityRender::new);
    }

    public static final String KEY_CATEGORY = "Dimensional Bags";
    public static KeyBinding openGuiKey = new KeyBinding("key.open_gui", KeyConflictContext.IN_GAME, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_I, KEY_CATEGORY);


}
