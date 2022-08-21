package com.limachi.utils;

import com.limachi.utils.messages.KeyStateMsg;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class KeyMapController {

    public static final ArrayList<GlobalKeyBinding> KEY_BINDINGS = new ArrayList<>();

    public static GlobalKeyBinding SNEAK = registerVanilla("key.categories.movement:key.sneak", ()->()->Minecraft.getInstance().options.keyShift);
    public static GlobalKeyBinding SPRINT = registerVanilla("key.categories.movement:key.sprint", ()->()->Minecraft.getInstance().options.keySprint);
    public static GlobalKeyBinding JUMP = registerVanilla("key.categories.movement:key.jump", ()->()->Minecraft.getInstance().options.keyJump);
    public static GlobalKeyBinding FORWARD = registerVanilla("key.categories.movement:key.forward", ()->()->Minecraft.getInstance().options.keyUp);
    public static GlobalKeyBinding BACK = registerVanilla("key.categories.movement:key.back", ()->()->Minecraft.getInstance().options.keyDown);
    public static GlobalKeyBinding RIGHT = registerVanilla("key.categories.movement:key.right", ()->()->Minecraft.getInstance().options.keyRight);
    public static GlobalKeyBinding LEFT = registerVanilla("key.categories.movement:key.left", ()->()->Minecraft.getInstance().options.keyLeft);
    public static GlobalKeyBinding USE = registerVanilla("key.categories.gameplay:key.use", ()->()->Minecraft.getInstance().options.keyUse);
    public static GlobalKeyBinding ATTACK = registerVanilla("key.categories.gameplay:key.attack", ()->()->Minecraft.getInstance().options.keyAttack);

    protected static GlobalKeyBinding registerVanilla(String key, Supplier<Callable<KeyMapping>> ref) {
        GlobalKeyBinding out = new GlobalKeyBinding(key, false, ref);
        KEY_BINDINGS.add(out);
        return out;
    }

    private static final HashMap<UUID, boolean[]> playerKeyStateMap = new HashMap<>(); //only used server side
    private static boolean[] local_key_map = null; //only used client side

    private static boolean[] getLocalKeyMap() {
        if (local_key_map == null)
            local_key_map = new boolean[KEY_BINDINGS.size()];
        return local_key_map;
    }

    public static GlobalKeyBinding registerKeyBind(String nameLangKey, int key, String categoryLangKey) {
        return registerKeyBind(nameLangKey, KeyConflictContext.UNIVERSAL, KeyModifier.NONE, false, key, categoryLangKey);
    }
    public static GlobalKeyBinding registerMouseKeyBind(String nameLangKey, int button, String categoryLangKey) {
        return registerKeyBind(nameLangKey, KeyConflictContext.UNIVERSAL, KeyModifier.NONE, true, button, categoryLangKey);
    }
    public static GlobalKeyBinding registerMouseKeyBind(String nameLangKey, IKeyConflictContext conflict, int button, String categoryLangKey) {
        return registerKeyBind(nameLangKey, conflict, KeyModifier.NONE, true, button, categoryLangKey);
    }
    public static GlobalKeyBinding registerKeyBind(String nameLangKey, IKeyConflictContext conflict, KeyModifier modifier, boolean mouse, int input, String categoryLangKey) {
        GlobalKeyBinding out = new GlobalKeyBinding(categoryLangKey + ":" + nameLangKey, true, ()->()->new KeyMapping(nameLangKey, conflict, modifier, (mouse ? InputConstants.Type.MOUSE : InputConstants.Type.KEYSYM).getOrCreate(input), categoryLangKey));
        KEY_BINDINGS.add(out);
        return out;
    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseInputEvent event) {
        KeyMapController.syncKeyMap(event.getButton(), 0, true, event.getAction() == GLFW.GLFW_PRESS || event.getAction() == GLFW.GLFW_REPEAT);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        KeyMapController.syncKeyMap(event.getKey(), event.getScanCode(), false, event.getAction() == GLFW.GLFW_PRESS || event.getAction() == GLFW.GLFW_REPEAT);
    }

    @OnlyIn(Dist.CLIENT)
    public static void syncKeyMap(int key, int scan, boolean mouse, boolean state) {
        Player player = Minecraft.getInstance().player;
        if (player == null)
            return;
        for (int i = 0; i < KEY_BINDINGS.size(); ++i) {
            if (mouse ? KEY_BINDINGS.get(i).getKeybinding().matchesMouse(key) : KEY_BINDINGS.get(i).getKeybinding().matches(key, scan)) {
                if (state != getLocalKeyMap()[i]) {
                    getLocalKeyMap()[i] = state;
                    Network.toServer(ModBase.COMMON_ID, new KeyStateMsg(i, state));
                }
                return;
            }
        }
    }

    public static class GlobalKeyBinding {
        final public String name;
        final private Object keybinding;
        private int id = -1;

        public GlobalKeyBinding(String name, boolean needRegister, Supplier<Callable<KeyMapping>> keybinding) {
            this.name = name;
            this.keybinding = DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> {
                KeyMapping k = keybinding.get().call();
                if (needRegister)
                    ClientRegistry.registerKeyBinding(k);
                return k;
            });
        }

        public int getId() {
            if (id == -1) {
                for (int i = 0; i < KEY_BINDINGS.size(); ++i)
                    if (KEY_BINDINGS.get(i).name.equals(name))
                        id = i;
            }
            return id;
        }

        @OnlyIn(Dist.CLIENT)
        public KeyMapping getKeybinding() { return (KeyMapping)keybinding; }

        public boolean getState(Player player) {
            return DistExecutor.unsafeRunForDist(
                    ()->((KeyMapping)keybinding)::isDown,
                    ()->()->player != null && playerKeyStateMap.getOrDefault(player.getUUID(), new boolean[KEY_BINDINGS.size()])[getId()]);
        }

        public void forceKeyState(Player player, boolean state) {
            DistExecutor.unsafeRunForDist(()->()->{
                getLocalKeyMap()[getId()] = state;
                ((KeyMapping)KEY_BINDINGS.get(getId()).keybinding).setDown(state);
                return null;
            }, ()->()->{
                Network.toClient(ModBase.COMMON_ID, (ServerPlayer)player, new KeyStateMsg(getId(), state));
                return null;
            });
        }
    }

    public static class KeyMapChangedEvent extends Event {
        private final Player player;
        private final boolean[] keys;
        private final boolean[] previousKeys;

        KeyMapChangedEvent(Player player, boolean[] keys, boolean[] previousKeys) {
            this.player = player;
            this.keys = keys;
            this.previousKeys = previousKeys;
        }

        public Player getPlayer() { return player; }

        public boolean[] getKeys() { return keys; }

        public boolean[] getPreviousKeys() { return previousKeys; }

        public boolean[] getChangedKeys() {
            boolean[] out = new boolean[Math.max(keys.length, previousKeys.length)];
            for (int i = 0; i < out.length; ++i)
                out[i] = (i < keys.length && keys[i]) != (i < previousKeys.length && previousKeys[i]);
            return out;
        }
    }

    public static void syncKeyMapServer(Player player, int key, boolean state) {
        boolean[] keys = playerKeyStateMap.computeIfAbsent(player.getUUID(), p->new boolean[KEY_BINDINGS.size()]);
        boolean[] previousKeys = keys.clone();
        keys[key] = state;
        MinecraftForge.EVENT_BUS.post(new KeyMapChangedEvent(player, keys, previousKeys));
    }
}
