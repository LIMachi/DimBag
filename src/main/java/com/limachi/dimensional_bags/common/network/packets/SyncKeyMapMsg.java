package com.limachi.dimensional_bags.common.network.packets;

import com.limachi.dimensional_bags.KeyMapController;
import com.limachi.dimensional_bags.common.network.PacketHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SyncKeyMapMsg {

    UUID playerId;
    boolean[] keys;

    public SyncKeyMapMsg(UUID player, boolean[] keys) {
        this.playerId = player;
        this.keys = keys;
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeUniqueId(playerId);
        buffer.writeByte(keys.length);
        for (boolean key : keys) buffer.writeBoolean(key);
    }

    public static SyncKeyMapMsg fromBytes(PacketBuffer buffer) {
        UUID id = buffer.readUniqueId();
        boolean[] keys = new boolean[buffer.readByte()];
        for (int i = 0; i < keys.length; ++i)
            keys[i] = buffer.readBoolean();
        return new SyncKeyMapMsg(id, keys);
    }

    public static void enqueue(SyncKeyMapMsg pack, Supplier<NetworkEvent.Context> ctxs) {
        NetworkEvent.Context ctx = ctxs.get();
        PacketHandler.Target t = PacketHandler.target(ctx);
        if (t == PacketHandler.Target.SERVER)
            ctx.enqueueWork(() -> KeyMapController.syncKeyMapMsg(pack.playerId, pack.keys));
        ctx.setPacketHandled(true);
    }
}
