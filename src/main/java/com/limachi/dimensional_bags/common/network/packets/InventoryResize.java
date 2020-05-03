package com.limachi.dimensional_bags.common.network.packets;

import com.limachi.dimensional_bags.DimensionalBagsMod;
import com.limachi.dimensional_bags.common.data.DimBagData;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.network.IBasePacket;
import com.limachi.dimensional_bags.common.network.PacketHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.function.Supplier;

import static com.limachi.dimensional_bags.DimensionalBagsMod.MOD_ID;

public class InventoryResize implements IBasePacket {

    private int id;
    private int size;
    private int rows;
    private int colums;

    public InventoryResize() {}

    public InventoryResize(int eyeId, int size, int rows, int columns) {
        this.id = eyeId;
        this.size = size;
        this.rows = rows;
        this.colums = columns;
    }

    public static InventoryResize fromBytes(PacketBuffer buff) { //called to decode a message
        InventoryResize out = new InventoryResize();
        out.id = buff.readInt();
        out.size = buff.readInt();
        out.rows = buff.readInt();
        out.colums = buff.readInt();
        return out;
    }

    @Override
    public void toBytes(PacketBuffer buff) { //called to encode a message
        buff.writeInt(this.id);
        buff.writeInt(this.size);
        buff.writeInt(this.rows);
        buff.writeInt(this.colums);
    }

    public static void enqueue(InventoryResize pack, Supplier<NetworkEvent.Context> ctxs) {
        NetworkEvent.Context ctx = ctxs.get();
        PacketHandler.Target t = PacketHandler.target(ctx);
        if (t == PacketHandler.Target.CLIENT) //receptionned client side
            ctx.enqueueWork(() -> {
                /*
                if (DimensionalBagsMod.instance.client_side_mirror == null)
                    DimensionalBagsMod.instance.client_side_mirror = new DimBagData(MOD_ID);
                DimensionalBagsMod.instance.client_side_mirror.loadChangesFromPacket(pack);
                */
            });
        ctx.setPacketHandled(true);
    }
}
