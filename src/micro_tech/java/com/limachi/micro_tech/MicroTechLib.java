package com.limachi.micro_tech;

import com.google.common.reflect.Reflection;
//import com.limachi.dimensional_bags.common.Config;
//import com.limachi.dimensional_bags.common.Registries;
//import com.limachi.dimensional_bags.common.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
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

@Mod(MicroTechLib.MOD_ID)
public class MicroTechLib {
    public static final String MOD_ID = "micro_tech";
    public static final Logger LOGGER = LogManager.getLogger();
    public static MicroTechLib INSTANCE;

    public MicroTechLib() {
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
        return runLogicalSide(null, ()->()-> Minecraft.getInstance().player, ()->()->null);
    }

    /** try to get the current server we are connected on, return null if we aren't connected (hangoing in main menu for example) */
    public static MinecraftServer getServer() { return ServerLifecycleHooks.getCurrentServer(); }
}
