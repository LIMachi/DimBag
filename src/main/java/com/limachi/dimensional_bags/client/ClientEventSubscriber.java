package com.limachi.dimensional_bags.client;

import com.limachi.dimensional_bags.KeyMapController;
import com.limachi.dimensional_bags.client.entity.layer.BagLayer;
import com.limachi.dimensional_bags.client.entity.render.BagEntityRender;
import com.limachi.dimensional_bags.client.itemEntity.EntityItemRenderer;
import com.limachi.dimensional_bags.client.render.screen.*;
import com.limachi.dimensional_bags.client.render.tileEntity.TankTileEntityRenderer;
import com.limachi.dimensional_bags.client.render.tileEntity.SlotTileEntityRenderer;
import com.limachi.dimensional_bags.common.bagDimensionOnly.bagSlot.SlotTileEntity;
import com.limachi.dimensional_bags.common.references.Registries;
import com.limachi.dimensional_bags.common.bagDimensionOnly.bagTank.TankContainer;
import com.limachi.dimensional_bags.common.bagDimensionOnly.bagUserBlock.UserBlockContainer;
import com.limachi.dimensional_bags.lib.common.container.*;
import com.limachi.dimensional_bags.common.bag.BagEntity;
import com.limachi.dimensional_bags.common.bag.BagItem;
import com.limachi.dimensional_bags.common.bag.GhostBagItem;
import com.limachi.dimensional_bags.common.bag.BagEntityItem;
import com.limachi.dimensional_bags.common.bagDimensionOnly.bagSlot.SlotSettingsContainer;
import com.limachi.dimensional_bags.common.bagDimensionOnly.bagSlot.SlotContainer;
import com.limachi.dimensional_bags.common.bagDimensionOnly.bagTank.TankTileEntity;
import net.minecraft.block.Block;
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
        ClientRegistry.bindTileEntityRenderer(Registries.getBlockEntityType(TankTileEntity.NAME), TankTileEntityRenderer::new);
        ClientRegistry.bindTileEntityRenderer(Registries.getBlockEntityType(SlotTileEntity.NAME), SlotTileEntityRenderer::new);

        ScreenManager.register(Registries.getContainerType(UserBlockContainer.NAME), UserPillarGUI::new);
        ScreenManager.register(Registries.getContainerType(SettingsContainer.NAME), SettingsScreen::new);
        ScreenManager.register(Registries.getContainerType(SlotContainer.NAME), SimpleContainerScreen<SlotContainer>::new);
        ScreenManager.register(Registries.getContainerType(TankContainer.NAME), SimpleContainerScreen<TankContainer>::new);
        ScreenManager.register(Registries.getContainerType(SlotSettingsContainer.NAME), SimpleContainerScreen<SlotSettingsContainer>::new);

        RenderingRegistry.registerEntityRenderingHandler(Registries.getEntityType(BagEntityItem.NAME), EntityItemRenderer::new);

        ScreenManager.register(Registries.getContainerType(ClientSideOnlyScreenHandler.NAME), ClientSideOnlyScreenHandler::new);


        for (Block b : Registries.getBlocks())
            if (!b.defaultBlockState().canOcclude())
                RenderTypeLookup.setRenderLayer(b, RenderType.translucent());

        KeyMapController.KeyBindings.registerKeybindings();

        ItemModelsProperties.register(Registries.getItem(BagItem.NAME), new ResourceLocation(MOD_ID, "bag_mode_property"), BagItem::getModeProperty);
        ItemModelsProperties.register(Registries.getItem(GhostBagItem.NAME), new ResourceLocation(MOD_ID, "bag_mode_property"), GhostBagItem::getModeProperty);

        Map<String, PlayerRenderer> skin = Minecraft.getInstance().getEntityRenderDispatcher().getSkinMap();
        for (String key : skin.keySet()) {
            PlayerRenderer renderer = skin.get(key);
            renderer.addLayer(new BagLayer<>(renderer, new BipedModel(0.5F), new BipedModel(1.0F)));
        }

//        ModelLoaderRegistry.registerLoader(, );
    }

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(Registries.getEntityType(BagEntity.NAME), BagEntityRender::new);
    }
}
