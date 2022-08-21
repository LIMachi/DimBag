package com.limachi.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class Sides {
    public static final Logger LOGGER = LogManager.getLogger();
    public static boolean isLogicalClient() { return EffectiveSide.get().isClient(); }
    public static boolean isPhysicalClient() { return FMLEnvironment.dist.isClient(); }
    public static Player getPlayer() { return DistExecutor.unsafeRunForDist(()->()->Minecraft.getInstance().player, ()->()-> {Log.warn("trying to retrieve a client player server side!", 1); return null;}); }
    public static List<? extends Player> getPlayers() { return DistExecutor.safeRunForDist(()->()-> isLogicalClient() ? Collections.singletonList(Minecraft.getInstance().player) : getServer().getPlayerList().getPlayers(), ()->()->getServer().getPlayerList().getPlayers()); }
    public static MinecraftServer getServer() { return ServerLifecycleHooks.getCurrentServer(); }
    public static <T> T logicalSideRun(Supplier<Callable<T>> client, Supplier<Callable<T>> server) {
        if (isLogicalClient()) return DistExecutor.unsafeCallWhenOn(Dist.CLIENT, client);
        try {
            return server.get().call();
        } catch (Exception e) {
            return null;
        }
    }
}
