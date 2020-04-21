package com.limachi.dimensional_bags.common.data;

import com.limachi.dimensional_bags.DimensionalBagsMod;
import com.limachi.dimensional_bags.common.network.DimBagDataSyncPacket;
import com.limachi.dimensional_bags.common.network.DimBagDataSyncRequestPacket;
import com.limachi.dimensional_bags.common.network.PacketHandler;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.WorldSavedData;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import static com.limachi.dimensional_bags.DimensionalBagsMod.MOD_ID;

//the big problem with this is that client can't get the data directly, might have to do an entire packet system for the client to get the world data from the server
public class DimBagData extends WorldSavedData { //since this mod used shared data on multiple levels, it has been decided that only id are stored in items/entities/tileentities and the actual data is obtained through the world shared data

    public enum Side {
        CLIENT,
        SERVER
    }

    private int lastId;
    private ArrayList<EyeData> eyes;
    private Side side;

    public DimBagData(String s, Side side) { super(s); lastId = 0; eyes = new ArrayList<>(); this.side = side;}

    static public DimBagData get(MinecraftServer server) {
        if (server != null)
            return server.getWorld(DimensionType.OVERWORLD).getSavedData().getOrCreate(() -> new DimBagData(MOD_ID, Side.SERVER), MOD_ID); //use overworld as default storage for data as it is the world that is guaranteed to exist (if I used the dim_bag_rift dimension, it would only work once the world is loaded)
        if (DimensionalBagsMod.client_side_mirror == null)
        {
            PacketHandler.toServer(new DimBagDataSyncRequestPacket());
            while (DimensionalBagsMod.client_side_mirror == null); //if the packet is handled by this thread, no luck, we're stuck
        }
        return DimensionalBagsMod.client_side_mirror;
    }

    public ArrayList<EyeData> dirtyEyes() {
        ArrayList<EyeData> out = new ArrayList<>();
        for (int i = 0; i < this.eyes.size(); ++i)
            if (this.eyes.get(i).dirty)
                out.add(this.eyes.get(i));
        return out;
    }

    public EyeData newEye() {
        int r = ++this.lastId;
        if (r - 1 >= this.eyes.size())
            for (int i = 0; i <= r; ++i) //I'm not overkille, I promise :p
                this.eyes.add(new EyeData(0));
        eyes.set(r - 1, new EyeData(r));
        this.update();
        return (eyes.get(r - 1));
    }

    public int getLastId() { return this.lastId; }

    public void loadChangesFromPacket(DimBagDataSyncPacket pack) {
        this.lastId = pack.lastId;
        if (this.eyes.size() < this.lastId)
            while (this.eyes.size() <= this.lastId)
                this.eyes.add(new EyeData(0));
        for (int i = 0; i < pack.eyes.size(); ++i) {
            EyeData td = pack.eyes.get(i);
            this.eyes.set(td.getId().getId() - 1, td);
        }
        this.update();
    }

    public void update() {
        this.markDirty();
        DimBagDataSyncPacket pack = new DimBagDataSyncPacket(this);
        if (this.side == Side.SERVER)
            PacketHandler.toClients(pack);
        else
            PacketHandler.toServer(pack);
        for (int i = 0; i < this.eyes.size(); ++i) //unset dirty flag for eyes
            this.eyes.get(i).dirty = false;
    }

    @Override
    public void read(CompoundNBT compound) {
        this.lastId = compound.getInt("lastId");
        this.eyes = new ArrayList<>();
        CompoundNBT lnbt = compound.getCompound("eyes");
        for (Integer i = 1; i < this.lastId; ++i) {
            EyeData td = new EyeData(i);
            td.read(lnbt.getCompound(i.toString()));
            this.eyes.add(i - 1, td);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putInt("lastId", lastId);
        CompoundNBT lnbt = new CompoundNBT();
        for (Integer i = 1; i < this.lastId; ++i) {
            CompoundNBT td = new CompoundNBT();
            this.eyes.get(i - 1).write(td);
            lnbt.put(i.toString(), td);
        }
        compound.put("eyes", lnbt);
        return compound;
    }

    public EyeData getEyeData(int id) {
        if (id > lastId || id < 1)
            throw new ArrayIndexOutOfBoundsException();
        return this.eyes.get(id - 1);
    }

    public void toBytes(PacketBuffer buff) {
        buff.writeInt(this.lastId);
        for (int i = 1; i < this.lastId; ++i)
            eyes.get(i - 1).toBytes(buff);
    }

    public void readBytes(PacketBuffer buff) {
        this.lastId = buff.readInt();
        this.eyes = new ArrayList<>(this.lastId);
        for (int i = 1; i < this.lastId; ++i) {
            EyeData td = new EyeData(i);
            td.readBytes(buff);
            eyes.set(i - 1, td);
        }
    }
}
