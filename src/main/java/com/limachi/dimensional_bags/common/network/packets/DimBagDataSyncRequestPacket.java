package com.limachi.dimensional_bags.common.network.packets;

import com.limachi.dimensional_bags.common.data.DimBagData;
import com.limachi.dimensional_bags.common.network.IBasePacket;
import com.limachi.dimensional_bags.common.network.PacketHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class DimBagDataSyncRequestPacket implements IBasePacket {
    public static DimBagDataSyncRequestPacket fromBytes(PacketBuffer buff) {return new DimBagDataSyncRequestPacket();}
    public void toBytes(PacketBuffer buff) {}
    public static void enqueue(DimBagDataSyncRequestPacket pack, Supplier<NetworkEvent.Context> ctxs) {
        NetworkEvent.Context ctx = ctxs.get();
        PacketHandler.Target t = PacketHandler.target(ctx);
        if (t == PacketHandler.Target.SERVER) //receptionned server side
            ctx.enqueueWork(() -> {
                DimBagData data = DimBagData.get(ctx.getSender().server);
                DimBagDataSyncPacket ackPack = new DimBagDataSyncPacket(data);
                ackPack.dirtyEyes = data.getEyes(); //make sur all eyes are loadded
                PacketHandler.toClient(ackPack, ctx.getSender());
            });
        ctx.setPacketHandled(true);
    }
}
