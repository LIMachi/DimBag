package com.limachi.dimensional_bags;

import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.items.GhostBag;
import com.limachi.dimensional_bags.common.items.IDimBagCommonItem;
import com.limachi.dimensional_bags.common.network.PacketHandler;
import com.limachi.dimensional_bags.common.network.packets.ChangeModeRequest;
import com.limachi.dimensional_bags.common.network.packets.KeyStateMsg;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
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

    @SubscribeEvent
    public static void onScrollInput(InputEvent.MouseScrollEvent event) {
        PlayerEntity player = DimBag.getPlayer();
        if (player != null && KeyBindings.BAG_KEY.getState(player)) {
            int slot = IDimBagCommonItem.getFirstValidItemFromPlayer(player, Item.class, x->(x.getItem() instanceof Bag || x.getItem() instanceof GhostBag) && Bag.getEyeId(x) > 0);
            if (slot != -1) {
                PacketHandler.toServer(new ChangeModeRequest(Bag.getEyeId(IDimBagCommonItem.getItemFromPlayer(player, slot)), event.getScrollDelta() > 0));
                event.setCanceled(true);
            }
        }
    }

    public enum KeyBindings {
        BAG_KEY(true, ()->()->new KeyBinding("key.open_gui", KeyConflictContext.IN_GAME, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_I, KEY_CATEGORY)),
        SNEAK_KEY(false, ()->()->Minecraft.getInstance().gameSettings.keyBindSneak),
        SPRINT_KEY(false, ()->()->Minecraft.getInstance().gameSettings.keyBindSprint),
        JUMP_KEY(false, ()->()->Minecraft.getInstance().gameSettings.keyBindJump),
        FORWARD_KEY(false, ()->()->Minecraft.getInstance().gameSettings.keyBindForward),
        BACK_KEY(false, ()->()->Minecraft.getInstance().gameSettings.keyBindBack),
        RIGHT_KEY(false, ()->()->Minecraft.getInstance().gameSettings.keyBindRight),
        LEFT_KEY(false, ()->()->Minecraft.getInstance().gameSettings.keyBindLeft),
        USE_KEY(false, ()->()->Minecraft.getInstance().gameSettings.keyBindUseItem),
        ATTACK_KEY(false, ()->()->Minecraft.getInstance().gameSettings.keyBindAttack),
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
                    ()-> keybinding::isKeyDown,
                    ()->()->player != null && playerKeyStateMap.getOrDefault(player.getUniqueID(), new boolean[KeyBindings.values().length])[this.ordinal()]);
        }

        public void forceKeyState(PlayerEntity player, boolean state) {
            DimBag.runLogicalSide(player != null ? player.world : null, ()->()->{
                local_key_map[this.ordinal()] = state;
                KeyBindings.values()[this.ordinal()].keybinding.setPressed(state);
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
            if (/*TRACKED_KEYBINDS[i].getKeyConflictContext().isActive() &&*/ mouse ? KeyBindings.values()[i].getKeybinding().matchesMouseKey(key) : KeyBindings.values()[i].getKeybinding().matchesKey(key, scan)) {
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
        boolean[] previousKeys = playerKeyStateMap.getOrDefault(player.getUniqueID(), new boolean[KEY_BIND_COUNT]);
        boolean[] keys = previousKeys.clone();
        keys[key] = state;
        playerKeyStateMap.put(player.getUniqueID(), keys);
        MinecraftForge.EVENT_BUS.post(new KeyMapChangedEvent(player, keys, previousKeys));
    }
}
