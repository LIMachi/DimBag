package com.limachi.dimensional_bags;

import com.limachi.dimensional_bags.common.network.PacketHandler;
import com.limachi.dimensional_bags.common.network.packets.SyncKeyMapMsg;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.DistExecutor;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KeyMapController { //FIXME: freeze after joining back a world
    public static final String KEY_CATEGORY = "Dimensional Bags";

    public static final int BAG_ACTION_KEY = 0;
    public static final int CROUCH_KEY = 1;
    public final static int KEY_BIND_COUNT = 2;
    public static final int NON_VANILLA_KEY_BIND_COUNT = 1;

    public final static KeyBinding[] TRACKED_KEYBINDS = DistExecutor.callWhenOn(Dist.CLIENT, ()->()->new KeyBinding[]{ //only initialized physical client side (but will only be used on the logical client side)
            new KeyBinding("key.open_gui", KeyConflictContext.IN_GAME, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_I, KEY_CATEGORY),
            Minecraft.getInstance().gameSettings.keyBindSneak
    });

    private static Map<UUID, boolean[]> playerKeyStateMap = new HashMap<>(); //only used server side
    private static boolean[] local_key_map = new boolean[KEY_BIND_COUNT]; //only used client side

    public static boolean getKey(PlayerEntity player, int keymapId) {
//        if (player instanceof ServerPlayerEntity)
//            return playerKeyStateMap.getOrDefault(player.getUniqueID(), new boolean[KEY_BIND_COUNT])[keymapId];
        return DimBag.runLogicalSide(null,
                ()->()->TRACKED_KEYBINDS[keymapId].isKeyDown(),
                ()->()->player != null && playerKeyStateMap.getOrDefault(player.getUniqueID(), new boolean[KEY_BIND_COUNT])[keymapId]);
    }

    @OnlyIn(Dist.CLIENT)
    public static void syncKeyMap(int key, int scan, boolean mouse, boolean state) {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player == null)
            return;
        for (int i = 0; i < KEY_BIND_COUNT; ++i) {
            if (/*TRACKED_KEYBINDS[i].getKeyConflictContext().isActive() &&*/ mouse ? TRACKED_KEYBINDS[i].matchesMouseKey(key) : TRACKED_KEYBINDS[i].matchesKey(key, scan)) {
                if (state != local_key_map[i]) {
                    local_key_map[i] = state;
                    PacketHandler.toServer(new SyncKeyMapMsg(/*player.getUniqueID(),*/ local_key_map));
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

    public static void syncKeyMapMsg(ServerPlayerEntity player, boolean[] keys) {
        boolean[] previousKeys = playerKeyStateMap.getOrDefault(player.getUniqueID(), new boolean[KEY_BIND_COUNT]);
        playerKeyStateMap.put(player.getUniqueID(), keys);
//        PlayerEntity player = DimBag.getServer().getPlayerList().getPlayerByUUID(playerId);
//        if (player != null)
            MinecraftForge.EVENT_BUS.post(new KeyMapChangedEvent(player, keys, previousKeys));
    }
}
