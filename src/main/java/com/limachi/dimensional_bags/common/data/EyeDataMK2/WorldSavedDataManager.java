package com.limachi.dimensional_bags.common.data.EyeDataMK2;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.EventManager;
import com.limachi.dimensional_bags.utils.NBTUtils;
import com.limachi.dimensional_bags.utils.TextUtils;
import com.limachi.dimensional_bags.utils.WorldUtils;
import com.limachi.dimensional_bags.common.data.DimBagData;
import com.limachi.dimensional_bags.common.managers.ModeManager;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import com.limachi.dimensional_bags.common.network.PacketHandler;
import com.limachi.dimensional_bags.common.network.packets.WorldSavedDataSyncMsg;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.function.Function;

@Mod.EventBusSubscriber
public class WorldSavedDataManager {
    public static HashMap<Class<? extends EyeWorldSavedData>, String> TYPE_TO_SUFFIX = new HashMap<>();
    public static HashMap<String, Class<? extends EyeWorldSavedData>> SUFFIX_TO_TYPE = new HashMap<>();

    static {
        register("energy_data", EnergyData.class);
        register("holder_data", HolderData.class);
        register("inventory_data", InventoryData.class);
        register("owner_data", OwnerData.class);
        register("sub_rooms_manager", SubRoomsManager.class);
        register("tank_data", TankData.class);
        register("upgrade_manager", UpgradeManager.class);
        register("mode_manager", ModeManager.class);
        register("settings_manager", SettingsData.class);
    }

    public abstract static class EyeWorldSavedData extends WorldSavedData {
        public final boolean isClient;
        public final boolean syncUp;
        private final int id;
        private final String suffix;
        private CompoundNBT prevState;
        int lastMarkDirty = 0;

        public static String nameGenerator(String suffix, int id) {
            return DimBag.MOD_ID + "_eye_" + id + "_" + suffix;
        }

        public String getSuffix() { return suffix; }

        public int getEyeId() { return id; }

        public EyeWorldSavedData(String suffix, int id, boolean isClient, boolean syncUp) {
            super(nameGenerator(suffix, id));
            this.id = id;
            this.suffix = suffix;
            this.isClient = isClient;
            this.syncUp = syncUp;
            this.prevState = null;
        }

        @Override
        public void save(File fileIn) { super.save(DimBag.debug(fileIn, " Saving data")); }

        @Override
        public void setDirty() {
            super.setDirty();
            if (lastMarkDirty != EventManager.tick && (!isClient || syncUp)) {
                if (!isClient)
                    EventManager.delayedTask(1, ()->PacketHandler.toClients(preparePacket(prevState == null)));
                else
                    EventManager.delayedTask(1, ()->PacketHandler.toServer(preparePacket(prevState == null)));
            }
            lastMarkDirty = EventManager.tick;
        }

        public WorldSavedDataSyncMsg preparePacket(boolean send_all) {
            CompoundNBT ser = serializeNBT();
            WorldSavedDataSyncMsg out;
            if (!send_all) {
                CompoundNBT diff = NBTUtils.extractDiff(ser, prevState);
                if (diff.isEmpty()) return null;
                if (diff.toString().length() < prevState.toString().length())
                    out = new WorldSavedDataSyncMsg(getSuffix(), getEyeId(), diff, true);
                else
                    out = new WorldSavedDataSyncMsg(getSuffix(), getEyeId(), ser, false);
            }
            else
                out = new WorldSavedDataSyncMsg(getSuffix(), getEyeId(), ser, false);
            prevState = ser;
            return out;
        }

        public void applyDiff(CompoundNBT nbt) {
            deserializeNBT(NBTUtils.applyDiff(serializeNBT(), nbt));
            setDirty(false);
        }
    }

    protected static final HashMap<String, EyeWorldSavedData> clientSideReflexion = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerLoginEvent(PlayerEvent.PlayerLoggedInEvent event) {
        if (DimBag.isServer(event.getPlayer().level)) {
            /*should send the current state of all the worldsaveddate to the client*/
            for (int eye = 0; eye < DimBagData.get().getLastId(); ++eye)
                for (HashMap.Entry<Class<? extends EyeWorldSavedData>, String> entry: TYPE_TO_SUFFIX.entrySet()) {
                    EyeWorldSavedData data = getInstance(entry.getKey(), eye + 1);
                    if (data != null)
                        PacketHandler.toClient((ServerPlayerEntity)event.getPlayer(), data.preparePacket(true));
                }
        } else
            clientSideReflexion.clear();
    }

    public static void clientUpdate(String suffix, int id, CompoundNBT nbt, boolean isDiff) {
        EyeWorldSavedData ewsd = clientSideReflexion.get(EyeWorldSavedData.nameGenerator(suffix, id));
        if (ewsd == null) {
            Class<? extends EyeWorldSavedData> type = null;
            for (Class<? extends EyeWorldSavedData> test : TYPE_TO_SUFFIX.keySet())
                if (TYPE_TO_SUFFIX.get(test).equals(suffix))
                    type = test;
            if (type == null) return;
            Constructor<? extends EyeWorldSavedData> constructor = null;
            try {
                constructor = type.getConstructor(String.class, int.class, boolean.class);
                ewsd = constructor.newInstance(suffix, id, true);
            } catch (Exception e) {
                return;
            }
        }
        if (isDiff)
            ewsd.applyDiff(nbt);
        else
            ewsd.load(nbt);
        clientSideReflexion.put(EyeWorldSavedData.nameGenerator(suffix, id), ewsd);
    }

    public static void serverUpdate(PlayerEntity player, String suffix, int id, CompoundNBT nbt, boolean isDiff) {
        EyeWorldSavedData ewsd = getInstance(SUFFIX_TO_TYPE.get(suffix), id);
        if (ewsd != null) {
            if (isDiff)
                ewsd.applyDiff(nbt);
            else
                ewsd.load(nbt);
            if (!ewsd.isClient)
                ewsd.setDirty();
        }
    }

    @SubscribeEvent
    public static void onPlayerLogoutEvent(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!DimBag.isServer(event.getPlayer().level))
            clientSideReflexion.clear();
    }

    public static void register(String suffix, Class<? extends EyeWorldSavedData> type) {
        TYPE_TO_SUFFIX.put(type, suffix);
        SUFFIX_TO_TYPE.put(suffix, type);
    }

    public static IFormattableTextComponent prettyDebug(int id) {
        final CompoundNBT EC = new CompoundNBT();
        IFormattableTextComponent out = new StringTextComponent(SettingsData.execute(id, SettingsData::getBagName, "<unnamed bag with id " + id + ">")).append("\n------------------------------------------\n");
        for (Class<? extends EyeWorldSavedData> t : TYPE_TO_SUFFIX.keySet())
            out.append(TYPE_TO_SUFFIX.get(t) + ": ").append(TextUtils.prettyNBT(execute(t, id, WorldSavedData::serializeNBT, EC))).append("\n");
        return out.append("------------------------------------------\n");
    }

    public static void populateAllById(int id) {
        for (Class<? extends EyeWorldSavedData> t : TYPE_TO_SUFFIX.keySet())
            execute(t, id, r->{r.setDirty(); return null;}, null);
    }

    public static <T extends EyeWorldSavedData> T getInstance(Class<T> type, int id) {
        if (id <= 0) return null;
        String suffix = TYPE_TO_SUFFIX.get(type);
        if (DimBag.isServer(null)) {
            ServerWorld world = (ServerWorld)WorldUtils.getOverWorld();
            if (world != null) {
                if (suffix != null && suffix.length() != 0)
                    return world.getDataStorage().computeIfAbsent(() -> {
                        try {
                            return type.getConstructor(String.class, int.class, boolean.class).newInstance(suffix, id, false);
                        } catch (Exception e) {
                            return null;
                        }
                    }, EyeWorldSavedData.nameGenerator(suffix, id));
            }
        } else if (suffix != null && suffix.length() != 0)
            return (T)clientSideReflexion.get(EyeWorldSavedData.nameGenerator(suffix, id));
        return null;
    }

    public static <T extends Object, S extends EyeWorldSavedData> T execute(Class<S> type, int id, Function<S, T> executable, T onErrorReturn) {
        if (type != null && id > 0 && executable != null) {
            S instance = getInstance(type, id);
            if (instance != null)
                return executable.apply(instance);
        }
        return onErrorReturn;
    }
}
