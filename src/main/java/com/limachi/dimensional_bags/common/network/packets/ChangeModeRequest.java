package com.limachi.dimensional_bags.common.network.packets;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.network.PacketHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class ChangeModeRequest {

    UUID id;
    int slot;
    boolean up;

    private ChangeModeRequest(UUID player, int slot, boolean up) {
        this.id = player;
        this.slot = slot;
        this.up = up;
    }

    public ChangeModeRequest(PlayerEntity player, int slot, boolean up) {
        this.id = player.getUniqueID();
        this.slot = slot;
        this.up = up;
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeUniqueId(id);
        buffer.writeInt(slot);
        buffer.writeBoolean(up);
    }

    public static ChangeModeRequest fromBytes(PacketBuffer buffer) {
        UUID id = buffer.readUniqueId();
        int slot = buffer.readInt();
        boolean up = buffer.readBoolean();
        return new ChangeModeRequest(id, slot, up);
    }

    public static void enqueue(ChangeModeRequest pack, Supplier<NetworkEvent.Context> ctxs) {
        NetworkEvent.Context ctx = ctxs.get();
        PacketHandler.Target t = PacketHandler.target(ctx);
        if (t == PacketHandler.Target.SERVER)
            ctx.enqueueWork(() -> {
                Bag.changeModeRequest(DimBag.getServer(null).getPlayerList().getPlayerByUUID(pack.id), pack.slot, pack.up);
            });
        ctx.setPacketHandled(true);
    }
}
