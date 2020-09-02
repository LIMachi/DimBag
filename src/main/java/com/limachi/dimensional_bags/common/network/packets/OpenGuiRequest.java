package com.limachi.dimensional_bags.common.network.packets;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.network.Network;
import com.limachi.dimensional_bags.common.network.PacketHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class OpenGuiRequest {

    UUID id;
    int bagId;

    private OpenGuiRequest(UUID player, int bagId) {
        this.id = player;
        this.bagId = bagId;
    }

    public OpenGuiRequest(PlayerEntity player, int bagId) {
        this.id = player.getUniqueID();
        this.bagId = bagId;
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeUniqueId(id);
        buffer.writeInt(bagId);
    }

    public static OpenGuiRequest fromBytes(PacketBuffer buffer) {
        UUID id = buffer.readUniqueId();
        int bagId = buffer.readInt();
        return new OpenGuiRequest(id, bagId);
    }

    public static void enqueue(OpenGuiRequest pack, Supplier<NetworkEvent.Context> ctxs) {
        NetworkEvent.Context ctx = ctxs.get();
        PacketHandler.Target t = PacketHandler.target(ctx);
        if (t == PacketHandler.Target.SERVER)
            ctx.enqueueWork(() -> {
                MinecraftServer server = DimBag.getServer(null);
                Network.openEyeInventory(server.getPlayerList().getPlayerByUUID(pack.id), EyeData.get(server, pack.bagId));
            });
        ctx.setPacketHandled(true);
    }
}
