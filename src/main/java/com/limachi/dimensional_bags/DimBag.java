package com.limachi.dimensional_bags;

import com.limachi.dimensional_bags.common.Config;
import com.limachi.dimensional_bags.common.Registries;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

@Mod(MOD_ID)
public class DimBag {
    public static final String MOD_ID = "dim_bag";
    public static final Logger LOGGER = LogManager.getLogger();
    public static DimBag INSTANCE;
    public static final ItemGroup ITEM_GROUP = new ItemGroup(ItemGroup.GROUPS.length, "tab_" + MOD_ID) {
        @Override
        public ItemStack createIcon() { return new ItemStack(Items.ITEM_FRAME); } //FIXME: change the item used as icon
    };

    public DimBag() {
        INSTANCE = this;
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.getSpec());
        Registries.registerAll(eventBus);
    }

    public static boolean isServer(@Nullable World world) {
        if (world != null)
            return !world.isRemote();
        return EffectiveSide.get() == LogicalSide.SERVER;
    }

    public static MinecraftServer getServer(@Nullable World world) {
        if (world != null && world.getServer() != null)
            return world.getServer();
        return ServerLifecycleHooks.getCurrentServer();
    }
}
