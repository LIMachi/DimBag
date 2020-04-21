package com.limachi.dimensional_bags.common.network;

import com.limachi.dimensional_bags.DimensionalBagsMod;
import com.limachi.dimensional_bags.common.data.DimBagData;
import com.limachi.dimensional_bags.common.data.EyeData;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.function.Supplier;

import static com.limachi.dimensional_bags.DimensionalBagsMod.MOD_ID;

public class DimBagDataSyncPacket implements IBasePacket {

    public int lastId;
    public ArrayList<EyeData> eyes;

    public DimBagDataSyncPacket() {}

    public DimBagDataSyncPacket(DimBagData data) { //prepare a new message based on modifications found
        this.lastId = data.getLastId();
        this.eyes = data.dirtyEyes();
    }

    public static DimBagDataSyncPacket fromBytes(PacketBuffer buff) { //called to decode a message
        DimBagDataSyncPacket out = new DimBagDataSyncPacket();
        out.lastId = buff.readInt();
        int c = buff.readInt();
        out.eyes = new ArrayList<EyeData>();
        for (int i = 0; i < c; ++i) {
            EyeData td = new EyeData(0); //id will be overwrite by the readBytes method
            td.readBytes(buff);
            out.eyes.add(td);
        }
        return out;
    }

    @Override
    public void toBytes(PacketBuffer buff) { //called to encode a message
        buff.writeInt(this.lastId);
        buff.writeInt(this.eyes.size());
        for (int i = 0; i < this.eyes.size(); ++i)
            this.eyes.get(i).toBytes(buff);
    }

    static void enqueue(DimBagDataSyncPacket pack, Supplier<NetworkEvent.Context> ctxs) {
        NetworkEvent.Context ctx = ctxs.get();
        PacketHandler.Target t = PacketHandler.target(ctx);
        if (t == PacketHandler.Target.CLIENT) //receptionned client side
            ctx.enqueueWork(() -> {
                if (DimensionalBagsMod.client_side_mirror == null)
                    DimensionalBagsMod.client_side_mirror = new DimBagData(MOD_ID, DimBagData.Side.CLIENT);
                DimensionalBagsMod.client_side_mirror.loadChangesFromPacket(pack);
            });
        if (t == PacketHandler.Target.SERVER) //receptionned server side
            ctx.enqueueWork(() -> {
                if (ctx.getSender() == null) return; //should NOT happen since this message was received server side
                DimBagData data = DimBagData.get(ctx.getSender().server);
                data.loadChangesFromPacket(pack);
            });
        ctx.setPacketHandled(true);
    }
}
