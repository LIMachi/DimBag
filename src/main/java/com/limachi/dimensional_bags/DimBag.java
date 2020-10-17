package com.limachi.dimensional_bags;

import com.google.common.reflect.Reflection;
import com.limachi.dimensional_bags.common.Config;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
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

import java.util.concurrent.Callable;
import java.util.function.Supplier;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

@Mod(MOD_ID)
public class DimBag {

    public static final String MOD_ID = "dim_bag";
    public static final Logger LOGGER = LogManager.getLogger();
    public static DimBag INSTANCE;
    public static final ItemGroup ITEM_GROUP = new ItemGroup(ItemGroup.GROUPS.length, "tab_" + MOD_ID) {
        @Override
        public ItemStack createIcon() { return new ItemStack(Registries.BAG_ITEM.get()); }
    };

    public DimBag() {
        Reflection.initialize(PacketHandler.class);
        INSTANCE = this;
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.getSpec());
        Registries.registerAll(eventBus);
    }

    /** try by all means to know if the current invocation is on a logical client or logical server */
    public static boolean isServer(@Nullable World world) {
        if (world != null)
            return !world.isRemote();
        return EffectiveSide.get() == LogicalSide.SERVER;
    }

    /** execute the first wrapped callable only on logical client + physical client, and the second wrapped callable on logical server (any physical side) */
    public static <T> T runLogicalSide(@Nullable World world, Supplier<Callable<T>> client, Supplier<Callable<T>> server) {
        if (isServer(world))
            try {
                return server.get().call();
            } catch (Exception e) { return null; }
        else
            return DistExecutor.callWhenOn(Dist.CLIENT, client);
    }

    /** get the local minecraft player (only on client logical and physical side, returns null otherwise) */
    public static PlayerEntity getPlayer() {
        return runLogicalSide(null, ()->()->Minecraft.getInstance().player, ()->()->null);
    }

    /** try to get the current server we are connected on, return null if we aren't connected (hanging in main menu for example) */
    public static MinecraftServer getServer() { return ServerLifecycleHooks.getCurrentServer(); }

    /**
     * will run the given runnable in X ticks (on the client/server thread depending on witch thread called this method)
     */
    public static <T> void delayedTask(int ticksToWait, Runnable run) {
        runLogicalSide(null,
                ()->()->{com.limachi.dimensional_bags.client.EventManager.delayedTask(ticksToWait, run); return null;},
                ()->()->{com.limachi.dimensional_bags.common.EventManager.delayedTask(ticksToWait, run); return null;});
    }
}
