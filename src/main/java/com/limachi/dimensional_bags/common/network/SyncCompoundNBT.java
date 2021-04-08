package com.limachi.dimensional_bags.common.network;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.utils.NBTUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.UUID;

/**
 * self contained client-server nbt synchronization
 * with this class you can easily create a CompoundNBT that will be sync either manually or via client/server ticks, using a UUID
 *
 * WE STRONGLY SUGGEST THAT ID IS STATIC FINAL (to make sure the same id is used on client and server)
 *
 * something like this:
 *   class MyCoolClass {
 *       public static final SyncCompoundNBT sc = SyncCompoundNBT.create(UUID.fromString("02462109-5a0e-41c2-a614-175257e69cde"), true, true);
 *   }
 *
 * for containers, you might want to look at SimpleContainer
 */

@Mod.EventBusSubscriber
public class SyncCompoundNBT {
    private static final HashMap<UUID, SyncCompoundNBT> SYNC_MAP = new HashMap<>();

    @SubscribeEvent
    public static void onTickS(TickEvent.ServerTickEvent event) {
        for (SyncCompoundNBT t : SYNC_MAP.values())
            if (t.autoUpdate)
                t.update();
    }

    @SubscribeEvent
    public static void onTickC(TickEvent.ClientTickEvent event) {
        for (SyncCompoundNBT t : SYNC_MAP.values())
            if (t.autoUpdate)
                t.update();
    }

    private final UUID id;
    private CompoundNBT data;
    private CompoundNBT prevData;
    private final boolean isClient;
    private final boolean syncUp;
    private final boolean autoUpdate;

    public static class SCNBTC extends PacketHandler.Message {
        private final UUID id;
        private final boolean isClient;
        private final boolean syncUp;
        private final boolean autoUpdate;

        public SCNBTC(UUID id, boolean isClient, boolean syncUp, boolean autoUpdate) {
            this.id = id;
            this.isClient = isClient;
            this.syncUp = syncUp;
            this.autoUpdate = autoUpdate;
        }

        public SCNBTC(PacketBuffer buffer) {
            id = buffer.readUniqueId();
            isClient = buffer.readBoolean();
            syncUp = buffer.readBoolean();
            this.autoUpdate =buffer.readBoolean();
        }

        @Override
        public void toBytes(PacketBuffer buffer) {
            buffer.writeUniqueId(id);
            buffer.writeBoolean(isClient);
            buffer.writeBoolean(syncUp);
            buffer.writeBoolean(autoUpdate);
        }

        @Override
        public void serverWork(ServerPlayerEntity player) { SYNC_MAP.put(id, new SyncCompoundNBT(id, isClient, syncUp, autoUpdate)); }

        @Override
        public void clientWork() { SYNC_MAP.put(id, new SyncCompoundNBT(id, isClient, syncUp, autoUpdate)); }
    }

    public static class SCNBTD extends PacketHandler.Message {
        private final UUID id;

        public SCNBTD(UUID id) { this.id = id; }

        public SCNBTD(PacketBuffer buffer) { id = buffer.readUniqueId(); }

        @Override
        public void toBytes(PacketBuffer buffer) { buffer.writeUniqueId(id); }

        @Override
        public void serverWork(ServerPlayerEntity player) { SYNC_MAP.remove(id); }

        @Override
        public void clientWork() { SYNC_MAP.remove(id); }
    }

    public static class SCNBTU extends PacketHandler.Message {
        private final UUID id;
        private final CompoundNBT diff;

        public SCNBTU(UUID id, CompoundNBT diff) { this.id = id; this.diff = diff; }

        public SCNBTU(PacketBuffer buffer) { id = buffer.readUniqueId(); diff = buffer.readCompoundTag(); }

        @Override
        public void toBytes(PacketBuffer buffer) { buffer.writeUniqueId(id); buffer.writeCompoundTag(diff); }

        @Override
        public void serverWork(ServerPlayerEntity player) { applyDiff(id, diff); }

        @Override
        public void clientWork() { applyDiff(id, diff); }
    }

    private static void applyDiff(UUID id, CompoundNBT diff) {
        SyncCompoundNBT f = SYNC_MAP.get(id);
        NBTUtils.applyDiff(f.data, diff);
        f.prevData = f.data.copy();
    }

    public void update() {
        CompoundNBT diff = NBTUtils.extractDiff(data, prevData);
        if (!diff.isEmpty()) {
            SCNBTU p = new SCNBTU(id, diff);
            if (!isClient)
                PacketHandler.toClients(p);
            else if (syncUp)
                PacketHandler.toServer(p);
        }
    }

    public static SyncCompoundNBT create(UUID id, boolean syncUp, boolean autoUpdate) {
        boolean isClient = !DimBag.isServer(null);
        SyncCompoundNBT r = new SyncCompoundNBT(id, isClient, syncUp, autoUpdate);
        SYNC_MAP.put(id, r);
        SCNBTC p = new SCNBTC(id, !isClient, syncUp, autoUpdate);
        if (isClient)
            PacketHandler.toServer(p);
        else
            PacketHandler.toClients(p);
        return r;
    }

    public void destroy() {
        SYNC_MAP.remove(id);
        SCNBTD p = new SCNBTD(id);
        if (isClient)
            PacketHandler.toServer(p);
        else
            PacketHandler.toClients(p);
    }

    public static SyncCompoundNBT get(UUID id) { return SYNC_MAP.get(id); }

    private SyncCompoundNBT(UUID id, boolean isClient, boolean syncUp, boolean autoUpdate) {
        this.id = id;
        data = new CompoundNBT();
        prevData = new CompoundNBT();
        this.isClient = isClient;
        this.syncUp = syncUp;
        this.autoUpdate = autoUpdate;
    }

    public CompoundNBT getData() { return data; }
}
