package com.limachi.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class ClientRegistries {
    protected static final HashMap<RegistryObject<?>, RenderType> RENDER_LAYERS = new HashMap<>();
    protected static final HashMap<RegistryObject<Block>, BlockColor> BLOCK_COLORS = new HashMap<>();
    protected static final ArrayList<OpaqueMenuScreenRegistry<?, ?>> MENU_SCREEN = new ArrayList<>();

    protected record OpaqueMenuScreenRegistry<M extends AbstractContainerMenu, S extends Screen & MenuAccess<M>>(
            RegistryObject<MenuType<M>> menu,
            MenuScreens.ScreenConstructor<M, S> builder) {

        void register() { MenuScreens.register(menu.get(), builder); }
    }

    public static <M extends AbstractContainerMenu, S extends Screen & MenuAccess<M>> void menuScreen(RegistryObject<MenuType<M>> menu, MenuScreens.ScreenConstructor<M, S> builder) { MENU_SCREEN.add(new OpaqueMenuScreenRegistry<>(menu, builder)); }

    protected static final HashMap<RegistryObject<MenuType<?>>, MenuScreens.ScreenConstructor<?, ?>> CONTAINER_SCREENS = new HashMap<>();

    public static void setRenderLayer(RegistryObject<Block> rb, RenderType type) { RENDER_LAYERS.put(rb, type); }
    public static void setColor(RegistryObject<Block> rb, BlockColor color) { BLOCK_COLORS.put(rb, color); }
    public static void setTranslucent(RegistryObject<Block> rb) { setRenderLayer(rb, RenderType.translucent()); }
    public static void setCutout(RegistryObject<Block> rb) { setRenderLayer(rb, RenderType.cutout()); }

    static void clientSetup(final FMLClientSetupEvent event)
    {
        for (Map.Entry<RegistryObject<?>, RenderType> entry : RENDER_LAYERS.entrySet()) {
            Object o = entry.getKey().get();
            if (o instanceof Block)
                ItemBlockRenderTypes.setRenderLayer((Block)o, entry.getValue());
            if (o instanceof Fluid)
                ItemBlockRenderTypes.setRenderLayer((Fluid)o, entry.getValue());
        }

        BlockColors blockcolors = Minecraft.getInstance().getBlockColors();

        for (Map.Entry<RegistryObject<Block>, BlockColor> entry : BLOCK_COLORS.entrySet()) {
            blockcolors.register(entry.getValue(), entry.getKey().get());
        }

        for (OpaqueMenuScreenRegistry<?, ?> entry : MENU_SCREEN) {
            entry.register();
        }
    }
}
