package com.limachi.utils;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class ModBase {

    public static final String COMMON_ID = "limachi_mods";

    public static final HashMap<String, ModBase> INSTANCES = new HashMap<>();
    protected CreativeModeTab tab = CreativeModeTab.TAB_MISC;

    public ModBase(@Nonnull String modId, @Nonnull String name) {
        Registries.annotations(modId);
        StaticInitializer.initialize();
        Registries.register(modId);
        DistExecutor.unsafeCallWhenOn(Dist.CLIENT, ()->()->{ ClientRegistries.register(modId); return true; });
        MinecraftForge.EVENT_BUS.register(this);
        Configs.register(modId, name);
        Network.register(modId);
        SaveData.annotations(modId);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(CuriosIntegration::enqueueIMC);
        INSTANCES.put(modId, this);
    }

    public CreativeModeTab tab() { return tab; }
    public Item.Properties defaultProps() { return new Item.Properties().tab(tab); }

    public void createTab(String modId, Supplier<? extends Item> item) {
        tab = new CreativeModeTab("tab_" + modId) {
            @Override
            @Nonnull
            public ItemStack makeIcon() {
                return new ItemStack(item.get());
            }
        };
    }
}
