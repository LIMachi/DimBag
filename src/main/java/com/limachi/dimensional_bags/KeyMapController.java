package com.limachi.dimensional_bags;

import com.limachi.dimensional_bags.client.EventManager;
import com.limachi.dimensional_bags.common.bag.BagItem;
import com.limachi.dimensional_bags.common.bag.modes.ModeManager;
import com.limachi.dimensional_bags.lib.ConfigManager;
import com.limachi.dimensional_bags.lib.common.network.PacketHandler;
import com.limachi.dimensional_bags.lib.common.network.packets.ChangeModeRequest;
import com.limachi.dimensional_bags.lib.common.network.packets.KeyStateMsg;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class KeyMapController {

    public static final String KEY_CATEGORY = "Dimensional Bags";

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseInputEvent event) {
        KeyMapController.syncKeyMap(event.getButton(), 0, true, event.getAction() == GLFW.GLFW_PRESS || event.getAction() == GLFW.GLFW_REPEAT);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        KeyMapController.syncKeyMap(event.getKey(), event.getScanCode(), false, event.getAction() == GLFW.GLFW_PRESS || event.getAction() == GLFW.GLFW_REPEAT);
    }

    private static int scrollCoolDown = 0;

    @ConfigManager.Config(cmt = "how much ticks should pass before the next scroll event will be sent to the bag", min = "5", max = "20")
    public static final int SCROLL_COOLDOWN = 5;

    @SubscribeEvent
    public static void addScrollBehaviorToBag(InputEvent.MouseScrollEvent event) {
        PlayerEntity player = DimBag.getPlayer();
        ItemStack stack = player.getItemInHand(Hand.MAIN_HAND);
        if (!(stack.getItem() instanceof BagItem))
            stack = player.getItemInHand(Hand.OFF_HAND);
        if (stack.getItem() instanceof BagItem) {
            int eye = BagItem.getbagId(stack);
            boolean up = event.getScrollDelta() > 0;
            boolean trueRun = EventManager.getTick() - scrollCoolDown >= SCROLL_COOLDOWN; //prevent spam of scroll (only allow a scroll every SCROLL_COOLDOWN ticks)
            if (ModeManager.changeModeRequest(player, eye, up, !trueRun)) {
                if (trueRun) {
                    PacketHandler.toServer(new ChangeModeRequest(eye, up));
                    scrollCoolDown = EventManager.getTick();
                }
                event.setCanceled(true);
            }
        }
    }

    public enum KeyBindings {
        BAG_KEY(true, ()->()->new KeyBinding("key.action", KeyConflictContext.IN_GAME, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_I, KEY_CATEGORY)),
        SNEAK_KEY(false, ()->()->Minecraft.getInstance().options.keyShift),
        SPRINT_KEY(false, ()->()->Minecraft.getInstance().options.keySprint),
        JUMP_KEY(false, ()->()->Minecraft.getInstance().options.keyJump),
        FORWARD_KEY(false, ()->()->Minecraft.getInstance().options.keyUp),
        BACK_KEY(false, ()->()->Minecraft.getInstance().options.keyDown),
        RIGHT_KEY(false, ()->()->Minecraft.getInstance().options.keyRight),
        LEFT_KEY(false, ()->()->Minecraft.getInstance().options.keyLeft),
        USE_KEY(false, ()->()->Minecraft.getInstance().options.keyUse),
        ATTACK_KEY(false, ()->()->Minecraft.getInstance().options.keyAttack),
        ;

        static public void registerKeybindings() {
            for (KeyBindings key : KeyBindings.values())
                if (key.needRegister)
                    ClientRegistry.registerKeyBinding(key.keybinding);
        }

        final private boolean needRegister;
        final private KeyBinding keybinding;

        KeyBindings(boolean needRegister, Supplier<Callable<KeyBinding>> keybinding) {
            this.needRegister = needRegister;
            this.keybinding = DistExecutor.callWhenOn(Dist.CLIENT, keybinding);
        }

        public KeyBinding getKeybinding() { return keybinding; }

        public boolean getState(PlayerEntity player) {
            return DimBag.runLogicalSide(null,
                    ()-> keybinding::isDown,
                    ()->()->player != null && playerKeyStateMap.getOrDefault(player.getUUID(), new boolean[KeyBindings.values().length])[this.ordinal()]);
        }

        public void forceKeyState(PlayerEntity player, boolean state) {
            DimBag.runLogicalSide(player != null ? player.level : null, ()->()->{
                local_key_map[this.ordinal()] = state;
                KeyBindings.values()[this.ordinal()].keybinding.setDown(state);
                return null;
            }, ()->()->{
                PacketHandler.toClient((ServerPlayerEntity)player, new KeyStateMsg(this.ordinal(), state));
                return null;
            });
        }
    }

    public static final int KEY_BIND_COUNT = KeyBindings.values().length;

    private static Map<UUID, boolean[]> playerKeyStateMap = new HashMap<>(); //only used server side
    private static boolean[] local_key_map = new boolean[KEY_BIND_COUNT]; //only used client side

    @OnlyIn(Dist.CLIENT)
    public static void syncKeyMap(int key, int scan, boolean mouse, boolean state) {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player == null)
            return;
        for (int i = 0; i < KEY_BIND_COUNT; ++i) {
            if (/*TRACKED_KEYBINDS[i].getKeyConflictContext().isActive() &&*/ mouse ? KeyBindings.values()[i].getKeybinding().matchesMouse(key) : KeyBindings.values()[i].getKeybinding().matches(key, scan)) {
                if (state != local_key_map[i]) {
                    local_key_map[i] = state;
                    PacketHandler.toServer(new KeyStateMsg(i, state));
                }
                return;
            }
        }
    }

    public static class KeyMapChangedEvent extends Event {
        private PlayerEntity player;
        private boolean[] keys;
        private boolean[] previousKeys;

        KeyMapChangedEvent(PlayerEntity player, boolean[] keys, boolean[] previousKeys) {
            this.player = player;
            this.keys = keys;
            this.previousKeys = previousKeys;
        }

        public PlayerEntity getPlayer() { return player; }

        public boolean[] getKeys() { return keys; }

        public boolean[] getPreviousKeys() { return previousKeys; }

        public boolean[] getChangedKeys() {
            boolean[] out = new boolean[Math.max(keys.length, previousKeys.length)];
            for (int i = 0; i < out.length; ++i)
                out[i] = (i < keys.length && keys[i]) != (i < previousKeys.length && previousKeys[i]);
            return out;
        }
    }

    public static void syncKeyMapMsg(ServerPlayerEntity player, int key, boolean state) {
        boolean[] previousKeys = playerKeyStateMap.getOrDefault(player.getUUID(), new boolean[KEY_BIND_COUNT]);
        boolean[] keys = previousKeys.clone();
        keys[key] = state;
        playerKeyStateMap.put(player.getUUID(), keys);
        MinecraftForge.EVENT_BUS.post(new KeyMapChangedEvent(player, keys, previousKeys));
    }
}
