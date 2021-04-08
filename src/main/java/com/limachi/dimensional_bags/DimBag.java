package com.limachi.dimensional_bags;

import com.google.common.reflect.Reflection;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.items.Bag;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

import javax.annotation.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

@Mod(MOD_ID)
public class DimBag {

    public static final String MOD_ID = "dim_bag";
    public static final Logger LOGGER = LogManager.getLogger();
    public static DimBag INSTANCE;
    public static final ItemGroup ITEM_GROUP = new ItemGroup(ItemGroup.GROUPS.length, "tab_" + MOD_ID) {
        @Override
        public ItemStack createIcon() { return new ItemStack(Registries.getItem(Bag.NAME)); }
    };

    public static final Item.Properties DEFAULT_PROPERTIES = new Item.Properties().group(DimBag.ITEM_GROUP);

    /**
     * will debug methods actually log something
     */
    public static final boolean DO_DEBUG = true;
    /**
     * how debug will be logged (LOGGER::info or LOGGER::debug)
     */
    public static final Consumer<String> DEBUG = LOGGER::info;

    @SuppressWarnings({"UnusedReturnValue", "unused"})
    public static <T> T debug(T v, String s) { if (DO_DEBUG) DEBUG.accept(Thread.currentThread().getStackTrace()[2].toString() + " V: " + v + " : "+ s); return v; }
    @SuppressWarnings({"UnusedReturnValue", "unused"})
    public static <T> T debug(T v) { if (DO_DEBUG) DEBUG.accept(Thread.currentThread().getStackTrace()[2].toString() + " V: " + v); return v; }
    @SuppressWarnings({"UnusedReturnValue", "unused"})
    public static <T> T debug(T v, int depth) { if (DO_DEBUG) DEBUG.accept(Thread.currentThread().getStackTrace()[2 + depth].toString() + " V: " + v); return v; }
    @SuppressWarnings({"UnusedReturnValue", "unused"})
    public static <T> T debug(T v, int depth, String s) { if (DO_DEBUG) DEBUG.accept(Thread.currentThread().getStackTrace()[2 + depth].toString() + " V: " + v + " : "+ s); return v; }

    static {
        Type type = Type.getType(StaticInit.class);
        for (ModFileScanData.AnnotationData data : ModList.get().getAllScanData().stream().map(ModFileScanData::getAnnotations).flatMap(Collection::stream).filter(a-> type.equals(a.getAnnotationType())).collect(Collectors.toList())) {
            try {
                Reflection.initialize(Class.forName(data.getClassType().getClassName()));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public DimBag() {
        INSTANCE = this;
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ConfigManager.create(MOD_ID, ModConfig.Type.COMMON, "dimensional_bags", new String[]{".common", ".items", ".data", ".EyeDataMK2"});
        Registries.registerAll(eventBus);
        eventBus.addListener(CuriosIntegration::enqueueIMC);
    }

    /** try by all means to know if the current invocation is on a logical client or logical server */
    public static boolean isServer(@Nullable World world) {
        if (world != null)
            return !world.isRemote();
        return EffectiveSide.get() == LogicalSide.SERVER;
    }

    public static CommandSource silentCommandSource() {
        MinecraftServer server = getServer();
        ServerWorld serverworld = server.func_241755_D_();
        return new CommandSource(server, Vector3d.copy(serverworld.getSpawnPoint()), Vector2f.ZERO, serverworld, 4, "DimBag Silent Command", new StringTextComponent("DimBag Silent Command"), server, null).withFeedbackDisabled();
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

    public static List<ServerPlayerEntity> getPlayers() {
        return runLogicalSide(null, ()->()->null, ()->()->getServer().getPlayerList().getPlayers());
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
