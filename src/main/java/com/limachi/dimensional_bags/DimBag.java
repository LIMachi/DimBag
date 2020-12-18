package com.limachi.dimensional_bags;

import com.google.common.reflect.Reflection;
import com.limachi.dimensional_bags.common.Config;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.settings.KeyConflictContext;
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
import static com.limachi.ducky_library.KeyMapController.KEY_CATEGORY;

import com.limachi.ducky_library.KeyMapController;
import org.lwjgl.glfw.GLFW;

@Mod(MOD_ID)
public class DimBag {

    public static KeyMapController.KeyBindingEntry BAG_KEY = KeyMapController.KeyBindingEntry.create(true, ()->()->new KeyBinding("key.open_gui", KeyConflictContext.IN_GAME, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_I, KEY_CATEGORY));
    public static KeyMapController.KeyBindingEntry SNEAK_KEY = KeyMapController.KeyBindingEntry.create(false, ()->()->Minecraft.getInstance().gameSettings.keyBindSneak);
    public static KeyMapController.KeyBindingEntry JUMP_KEY = KeyMapController.KeyBindingEntry.create(false, ()->()->Minecraft.getInstance().gameSettings.keyBindJump);
    public static KeyMapController.KeyBindingEntry FORWARD_KEY = KeyMapController.KeyBindingEntry.create(false, ()->()->Minecraft.getInstance().gameSettings.keyBindForward);
    public static KeyMapController.KeyBindingEntry BACK_KEY = KeyMapController.KeyBindingEntry.create(false, ()->()->Minecraft.getInstance().gameSettings.keyBindBack);
    public static KeyMapController.KeyBindingEntry RIGHT_KEY = KeyMapController.KeyBindingEntry.create(false, ()->()->Minecraft.getInstance().gameSettings.keyBindRight);
    public static KeyMapController.KeyBindingEntry LEFT_KEY = KeyMapController.KeyBindingEntry.create(false, ()->()->Minecraft.getInstance().gameSettings.keyBindLeft);
    public static KeyMapController.KeyBindingEntry USE_KEY = KeyMapController.KeyBindingEntry.create(false, ()->()->Minecraft.getInstance().gameSettings.keyBindUseItem);
    public static KeyMapController.KeyBindingEntry ATTACK_KEY = KeyMapController.KeyBindingEntry.create(false, ()->()->Minecraft.getInstance().gameSettings.keyBindAttack);

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

    public static void breakPoint() {
        LOGGER.error("breakpoint reached, please debug this error ASAP");
        int noop = 0; //put a breakpoint there
    }
}
