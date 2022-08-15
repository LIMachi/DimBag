package com.limachi.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public class Sides {
    public static final Logger LOGGER = LogManager.getLogger();
    public static boolean isClient() { return FMLEnvironment.dist.isClient(); }
    public static Player getPlayer() { return DistExecutor.safeRunForDist(()->()-> Minecraft.getInstance().player, ()->()-> {Log.warn("trying to retrieve a client player server side!", 1); return null;}); }
    public static List<? extends Player> getPlayers() { return DistExecutor.safeRunForDist(()->()-> Collections.singletonList(Minecraft.getInstance().player), ()->()-> ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()); }
    public static MinecraftServer getServer() { return ServerLifecycleHooks.getCurrentServer(); }
}
