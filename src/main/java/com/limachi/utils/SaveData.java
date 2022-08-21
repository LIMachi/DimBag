package com.limachi.utils;

import com.limachi.utils.messages.SaveDataSyncMsg;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber
public class SaveData {

    public enum Sync {
        SERVER_ONLY, //this data is only to be stored and accessed by the server
        SERVER_TO_CLIENT, //this data is sync from the server to the client, allowing both to read but only the server can write
        BOTH_WAY //the server writes to the client but the client can modify it too
    }
    protected static HashMap<String, Pair<Class<? extends SyncSaveData>, Sync>> SAVE_DATAS = new HashMap<>();
    protected static final HashMap<String, SyncSaveData> CLIENT_INSTANCES = new HashMap<>();

    public static void register(String name, Sync sync, Class<? extends SyncSaveData> dataClass) {
        SAVE_DATAS.put(name, new Pair<>(dataClass, sync));
    }

    public @interface RegisterSaveData {
        String name() default "";
        Sync sync() default Sync.SERVER_ONLY;
    }

    public static void annotations(String modId) {
        for (ModAnnotation a : ModAnnotation.iterModAnnotations(modId, RegisterSaveData.class)) {
            String name = a.getData("name", "");
            if (name.equals(""))
                name = Strings.camel_to_snake(Strings.get_file('.', a.getAnnotatedClass().getCanonicalName())).replace("_save_data", "").replace("_data", "");
            register(name, a.getData("sync", Sync.SERVER_ONLY), (Class<? extends SyncSaveData>)a.getAnnotatedClass());
        }
    }

    public static class SyncSaveData extends SavedData {
        public final String name;
        public final Sync sync;
        private int lastMarkDirty = 0;
        private final boolean isClient = Sides.isLogicalClient();
        private CompoundTag prevState;

        public SyncSaveData(String name, Sync sync) {
            this.name = name;
            this.sync = sync;
        }

        @Override
        public void setDirty() {
            super.setDirty();
            if (lastMarkDirty < Events.tick) {
                lastMarkDirty = Events.tick;
                if (isClient && sync == Sync.BOTH_WAY)
                    Events.delayedTask(1, ()->Network.toServer(ModBase.COMMON_ID, pack(prevState == null)));
                else if (!isClient && sync == Sync.SERVER_TO_CLIENT)
                    Events.delayedTask(1, ()->Network.toClients(ModBase.COMMON_ID, pack(prevState == null)));
            }
        }

        private SaveDataSyncMsg pack(boolean send_all) {
            CompoundTag ser = save(new CompoundTag());
            SaveDataSyncMsg out;
            if (!send_all) {
                CompoundTag diff = NBT.extractDiff(ser, prevState);
                if (diff.isEmpty()) return null;
                if (diff.toString().length() < prevState.toString().length())
                    out = new SaveDataSyncMsg(name, true, diff);
                else
                    out = new SaveDataSyncMsg(name, false, ser);
            } else
                out = new SaveDataSyncMsg(name, false, ser);
            prevState = ser;
            return out;
        }

        @Override
        public @NotNull CompoundTag save(@NotNull CompoundTag nbt) { return nbt; };

        public void load(CompoundTag nbt) {};

        public void applyDiff(CompoundTag nbt) {
            load(NBT.applyDiff(save(new CompoundTag()), nbt));
            setDirty(false);
        }
    }

    public static void serverUpdate(Player player, String name, CompoundTag nbt, boolean isDiff) {
        execute(name, s->{
            if (isDiff)
                s.applyDiff(nbt);
            else
                s.load(nbt);
            if (!s.isClient)
                s.setDirty();
        });
    }

    public static void clientUpdate(String name, CompoundTag nbt, boolean isDiff) {
        SyncSaveData d = CLIENT_INSTANCES.computeIfAbsent(name, s -> {
            if (!SAVE_DATAS.containsKey(name)) return null;
            Class<? extends SyncSaveData> type = SAVE_DATAS.get(name).getFirst();
            try {
                return type.getConstructor(String.class, Sync.class).newInstance(name, SAVE_DATAS.get(name).getSecond());
            } catch (Exception e) {
                return null;
            }
        });
        if (d != null) {
            if (isDiff)
                d.applyDiff(nbt);
            else
                d.load(nbt);
        }
    }

    public static <T extends SyncSaveData> T getInstance(String name) {
        if (Sides.isLogicalClient()) return (T) CLIENT_INSTANCES.get(name);
        Class<T> clazz = (Class<T>)SAVE_DATAS.get(name).getFirst();
        Sync sync = SAVE_DATAS.get(name).getSecond();
        if (clazz != null) {
            ServerLevel overworld = (ServerLevel) World.overworld();
            if (overworld != null) {
                Supplier<T> supp = () -> {
                    try {
                        return clazz.getConstructor(String.class, Sync.class).newInstance(name, sync);
                    } catch (Exception e) {
                        return null;
                    }
                };
                return overworld.getDataStorage().computeIfAbsent(nbt -> {
                    T t = supp.get();
                    t.load(nbt);
                    return t;
                }, supp, name);
            }
        }
        return null;
    }

    public static <T, S extends SyncSaveData> T execute(String name, Function<S, T> exec, Supplier<T> onError) {
        S instance = getInstance(name);
        if (instance != null)
            return exec.apply(instance);
        return onError.get();
    }

    public static <S extends SyncSaveData> void execute(String name, Consumer<S> exec) {
        S instance = getInstance(name);
        if (instance != null)
            exec.accept(instance);
    }

    @SubscribeEvent
    public static void onPlayerLoginEvent(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getPlayer().level.isClientSide()) {
            for (String k : SAVE_DATAS.keySet()) {
                SyncSaveData d = getInstance(k);
                if (d != null)
                    Network.toClient(ModBase.COMMON_ID, (ServerPlayer) event.getPlayer(), d.pack(true));
            }
        } else
            CLIENT_INSTANCES.clear();
    }

    @SubscribeEvent
    public static void onPlayerLogoutEvent(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getPlayer().level.isClientSide())
            CLIENT_INSTANCES.clear();
    }
}
