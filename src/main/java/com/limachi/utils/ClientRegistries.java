package com.limachi.utils;

import com.limachi.dim_bag.layers.BagLayer;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class ClientRegistries {
    protected static final HashMap<RegistryObject<?>, RenderType> RENDER_LAYERS = new HashMap<>();
    protected static final HashMap<RegistryObject<Block>, BlockColor> BLOCK_COLORS = new HashMap<>();
    protected static final HashMap<RegistryObject<Item>, ItemColor> ITEM_COLORS = new HashMap<>();
    protected static final HashMap<ModelLayerLocation, Supplier<LayerDefinition>> LAYER_DEFINITIONS = new HashMap<>();
    protected static final ArrayList<OpaqueMenuScreenRegistry<?, ?>> MENU_SCREEN = new ArrayList<>();
    protected static final ArrayList<OpaqueBERRegistry<?>> BER = new ArrayList<>();
    protected static final ArrayList<OpaqueEntityRendererRegistry<?>> ER = new ArrayList<>();

    protected record OpaqueBERRegistry<T extends BlockEntity>(RegistryObject<BlockEntityType<T>> gbe, BlockEntityRendererProvider<T> gr) {
        void register(EntityRenderersEvent.RegisterRenderers event) { event.registerBlockEntityRenderer(gbe.get(), gr); }
    }

    protected record OpaqueEntityRendererRegistry<T extends Entity>(RegistryObject<EntityType<T>> ge, EntityRendererProvider<T> g) {
        void register(EntityRenderersEvent.RegisterRenderers event) { event.registerEntityRenderer(ge.get(), g); }
    }

    protected record OpaqueMenuScreenRegistry<M extends AbstractContainerMenu, S extends Screen & MenuAccess<M>>(RegistryObject<MenuType<M>> menu, MenuScreens.ScreenConstructor<M, S> builder) {
        void register() { MenuScreens.register(menu.get(), builder); }
    }

    public static <M extends AbstractContainerMenu, S extends Screen & MenuAccess<M>> void menuScreen(RegistryObject<MenuType<M>> menu, MenuScreens.ScreenConstructor<M, S> builder) { MENU_SCREEN.add(new OpaqueMenuScreenRegistry<>(menu, builder)); }
    public static <T extends BlockEntity> void setBer(RegistryObject<BlockEntityType<T>> getBe, BlockEntityRendererProvider<T> getRenderer) { BER.add(new OpaqueBERRegistry<>(getBe, getRenderer)); }
    public static <T extends Entity> void setEntityRenderer(RegistryObject<EntityType<T>> getBe, EntityRendererProvider<T> getRenderer) { ER.add(new OpaqueEntityRendererRegistry<>(getBe, getRenderer)); }

    protected static final HashMap<RegistryObject<MenuType<?>>, MenuScreens.ScreenConstructor<?, ?>> CONTAINER_SCREENS = new HashMap<>();

    public static void setRenderLayer(RegistryObject<Block> rb, RenderType type) { RENDER_LAYERS.put(rb, type); }
    public static void setColor(RegistryObject<Block> rb, BlockColor color) { BLOCK_COLORS.put(rb, color); }
    public static void setTranslucent(RegistryObject<Block> rb) { setRenderLayer(rb, RenderType.translucent()); }
    public static void setCutout(RegistryObject<Block> rb) { setRenderLayer(rb, RenderType.cutout()); }
    public static void setLayerDefinition(ModelLayerLocation location, Supplier<LayerDefinition> layerDef) { LAYER_DEFINITIONS.put(location, layerDef); }

    @SubscribeEvent
    static void clientSetup(final FMLClientSetupEvent event)
    {
        for (Map.Entry<RegistryObject<?>, RenderType> entry : RENDER_LAYERS.entrySet()) {
            Object o = entry.getKey().get();
            if (o instanceof Block)
                ItemBlockRenderTypes.setRenderLayer((Block)o, entry.getValue());
            if (o instanceof Fluid)
                ItemBlockRenderTypes.setRenderLayer((Fluid)o, entry.getValue());
        }
        for (OpaqueMenuScreenRegistry<?, ?> entry : MENU_SCREEN) entry.register();
    }

    @SubscribeEvent
    static void registerBlockColor(ColorHandlerEvent.Block event) {
        BlockColors blockcolors = event.getBlockColors();
        for (Map.Entry<RegistryObject<Block>, BlockColor> entry : BLOCK_COLORS.entrySet()) blockcolors.register(entry.getValue(), entry.getKey().get());
    }

    @SubscribeEvent
    static void registerBlockColor(ColorHandlerEvent.Item event) {
        ItemColors blockcolors = event.getItemColors();
        for (Map.Entry<RegistryObject<Item>, ItemColor> entry : ITEM_COLORS.entrySet()) blockcolors.register(entry.getValue(), entry.getKey().get());
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface RegisterSkin {
    }

    private static final ArrayList<Constructor<?>> SKINS = new ArrayList<>();
    private static void discoverRegisterSkin(String modId) {
        for (ModAnnotation a : ModAnnotation.iterModAnnotations(modId, RegisterSkin.class))
            SKINS.add(a.getAnnotatedClassConstructor(PlayerRenderer.class, EntityModelSet.class));
    }

    @SubscribeEvent
    static void registerLayersRenderers(EntityRenderersEvent.AddLayers event) {
        EntityModelSet models = event.getEntityModels();
        for (String rp : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(rp);
            if (renderer != null) {
//                for (Constructor<?> c : SKINS) {
//                    try {
//                        renderer.addLayer((RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>)c.newInstance(renderer, models));
//                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
//                        e.printStackTrace();
//                    }
//                }
            }
        }
    }

    @SubscribeEvent
    static void registerEntityLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        for (Map.Entry<ModelLayerLocation, Supplier<LayerDefinition>> e : LAYER_DEFINITIONS.entrySet()) event.registerLayerDefinition(e.getKey(), e.getValue());
    }

    @SubscribeEvent
    static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        for (OpaqueEntityRendererRegistry<?> er : ER) er.register(event);
        for (OpaqueBERRegistry<?> ber : BER) ber.register(event);
    }

    public static void register(String modId) {
        discoverRegisterSkin(modId);
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.register(ClientRegistries.class);
    }
}
