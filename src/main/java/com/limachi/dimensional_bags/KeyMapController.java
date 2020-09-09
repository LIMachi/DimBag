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

public class KeyMapController {
    public static final String KEY_CATEGORY = "Dimensional Bags";

    public static final int BAG_ACTION_KEY = 0;
    public static final int CROUCH_KEY = 1;
    public final static int KEY_BIND_COUNT = 2;
    public static final int NON_VANILLA_KEY_BIND_COUNT = 1;

    public final static KeyBinding[] TRACKED_KEYBINDS = DistExecutor.callWhenOn(Dist.CLIENT, ()->()->new KeyBinding[]{ //only initialized client side
            new KeyBinding("key.open_gui", KeyConflictContext.IN_GAME, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_I, KEY_CATEGORY),
            Minecraft.getInstance().gameSettings.keyBindSneak
    });

    private static Map<UUID, boolean[]> playerKeyStateMap = new HashMap<>(); //only used server side
    private static boolean[] local_key_map = new boolean[KEY_BIND_COUNT]; //only used client side

    public static boolean getKey(PlayerEntity player, int keymapId) {
        if (player instanceof ServerPlayerEntity)
            return playerKeyStateMap.getOrDefault(player.getUniqueID(), new boolean[KEY_BIND_COUNT])[keymapId];
        return DistExecutor.runForDist(
                ()->()->TRACKED_KEYBINDS[keymapId].isKeyDown(),
                ()->()-> player != null && playerKeyStateMap.getOrDefault(player.getUniqueID(), new boolean[KEY_BIND_COUNT])[keymapId]);
    }

    @OnlyIn(Dist.CLIENT)
    public static void syncKeyMap() {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player == null)
            return;
        boolean[] newKeyState = new boolean[KEY_BIND_COUNT];
        boolean shouldUpdate = false;
        for (int i = 0; i < KEY_BIND_COUNT; ++i) {
            newKeyState[i] = TRACKED_KEYBINDS[i].isKeyDown();
            if (newKeyState[i] != local_key_map[i])
                shouldUpdate = true;
        }
        if (shouldUpdate) {
            local_key_map = newKeyState;
            PacketHandler.toServer(new SyncKeyMapMsg(player.getUniqueID(), local_key_map));
        }
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
                    PacketHandler.toServer(new SyncKeyMapMsg(player.getUniqueID(), local_key_map));
                }
                return;
            }
        }
    }

    public static class KeyMapChangedEvent extends Event {
        private PlayerEntity player;
        private boolean[] keys;

        KeyMapChangedEvent(PlayerEntity player, boolean[] keys) {
            this.player = player;
            this.keys = keys;
        }

        public PlayerEntity getPlayer() { return player; }

        public boolean[] getKeys() { return keys; }
    }

    public static void syncKeyMapMsg(UUID playerId, boolean[] keys) {
        playerKeyStateMap.put(playerId, keys);
        MinecraftForge.EVENT_BUS.post(new KeyMapChangedEvent(DimBag.getServer(null).getPlayerList().getPlayerByUUID(playerId), keys));
    }
}
