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
    public ArrayList<EyeData> dirtyEyes;

    public DimBagDataSyncPacket() {}

    public DimBagDataSyncPacket(DimBagData data) { //prepare a new message based on modifications found
        this.lastId = data.getLastId();
        this.dirtyEyes = data.dirtyEyes();
    }

    public static DimBagDataSyncPacket fromBytes(PacketBuffer buff) { //called to decode a message
        DimBagDataSyncPacket out = new DimBagDataSyncPacket();
        out.lastId = buff.readInt();
        int c = buff.readInt();
        out.dirtyEyes = new ArrayList<EyeData>();
        for (int i = 0; i < c; ++i)
            out.dirtyEyes.add(new EyeData(buff)); //those eye aren't attached to the dataManager (DimBagData)
        return out;
    }

    @Override
    public void toBytes(PacketBuffer buff) { //called to encode a message
        buff.writeInt(this.lastId);
        buff.writeInt(this.dirtyEyes.size());
        for (int i = 0; i < this.dirtyEyes.size(); ++i)
            this.dirtyEyes.get(i).toBytes(buff);
    }

    static void enqueue(DimBagDataSyncPacket pack, Supplier<NetworkEvent.Context> ctxs) {
        NetworkEvent.Context ctx = ctxs.get();
        PacketHandler.Target t = PacketHandler.target(ctx);
        if (t == PacketHandler.Target.CLIENT) //receptionned client side
            ctx.enqueueWork(() -> {
                if (DimensionalBagsMod.instance.client_side_mirror == null)
                    DimensionalBagsMod.instance.client_side_mirror = new DimBagData(MOD_ID);
                DimensionalBagsMod.instance.client_side_mirror.loadChangesFromPacket(pack);
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
