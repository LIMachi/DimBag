package com.limachi.dimensional_bags.common.data;

import com.limachi.dimensional_bags.DimensionalBagsMod;
import com.limachi.dimensional_bags.common.blocks.BagEye;
import com.limachi.dimensional_bags.common.dimensions.BagDimension;
import com.limachi.dimensional_bags.common.init.Registries;
import com.limachi.dimensional_bags.common.network.DimBagDataSyncPacket;
import com.limachi.dimensional_bags.common.network.DimBagDataSyncRequestPacket;
import com.limachi.dimensional_bags.common.network.PacketHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

import java.util.ArrayList;

import static com.limachi.dimensional_bags.DimensionalBagsMod.MOD_ID;

//the big problem with this is that client can't get the data directly, might have to do an entire packet system for the client to get the world data from the server
public class DimBagData extends WorldSavedData { //since this mod used shared data on multiple levels, it has been decided that only id are stored in items/entities/tileentities and the actual data is obtained through the world shared data

    public enum Side {
        CLIENT,
        SERVER
    }

    private int lastId;
    private ArrayList<EyeData> eyes;

    public Side side;

    public ArrayList<EyeData> getEyes() { return this.eyes; }

    public DimBagData(String s, Side side) { super(s); lastId = 0; eyes = new ArrayList<>(); this.side = side; DimensionalBagsMod.LOGGER.warn("new DimBagData? I hope this is a new world");}

    static public DimBagData get(MinecraftServer server) {
        if (server != null)
            return server.getWorld(DimensionType.OVERWORLD).getSavedData().getOrCreate(() -> new DimBagData(MOD_ID, Side.SERVER), MOD_ID); //use overworld as default storage for data as it is the world that is guaranteed to exist (if I used the dim_bag_rift dimension, it would only work once the world is loaded)
        if (DimensionalBagsMod.instance.client_side_mirror == null)
        {
            PacketHandler.toServer(new DimBagDataSyncRequestPacket());
            while (DimensionalBagsMod.instance.client_side_mirror == null); //if the packet is handled by this thread, no luck, we're stuck
        }
        return DimensionalBagsMod.instance.client_side_mirror;
    }

    public ArrayList<EyeData> dirtyEyes() {
        ArrayList<EyeData> out = new ArrayList<>();
        for (int i = 0; i < this.eyes.size(); ++i)
            if (this.eyes.get(i).dirty)
                out.add(this.eyes.get(i));
        return out;
    }

    public EyeData newEye(PlayerEntity player) {
        if (player.world.isRemote()) return null; //should not be called client side
        int r = ++this.lastId;
        ServerWorld dim = BagDimension.get(player.getServer());
        dim.setBlockState(new BlockPos(8 + (r - 1) * 1024, 128, 8), Registries.BAG_EYE_BLOCK.get().getDefaultState()); //new eye
        if (r - 1 >= this.eyes.size())
            for (int i = 0; i <= r; ++i) //I'm not overkille, I promise :p
                this.eyes.add(new EyeData(this));
        eyes.set(r - 1, new EyeData(this, player, r));
        this.update(true);
        return (eyes.get(r - 1));
    }

    public int getLastId() { return this.lastId; }

    public void loadChangesFromPacket(DimBagDataSyncPacket pack) {
        this.lastId = pack.lastId;
        if (this.eyes.size() < this.lastId)
            while (this.eyes.size() <= this.lastId)
                this.eyes.add(new EyeData(this));
        for (int i = 0; i < pack.dirtyEyes.size(); ++i) {
            EyeData td = pack.dirtyEyes.get(i);
            td.attachDataManager(this);
            this.eyes.set(td.getId().getId() - 1, td);
        }
        this.update(false);
    }

    public void update(boolean sync_to_server) {
        this.markDirty();
        DimBagDataSyncPacket pack = new DimBagDataSyncPacket(this);
        DimensionalBagsMod.LOGGER.info("update data for " + this.side);
        if (this.side == Side.SERVER)
            PacketHandler.toClients(pack);
        else if (sync_to_server)
            PacketHandler.toServer(pack);
        for (int i = 0; i < this.eyes.size(); ++i) //unset dirty flag for eyes
            this.eyes.get(i).dirty = false;
    }

    @Override
    public void read(CompoundNBT compound) {
        this.lastId = compound.getInt("lastId");
        this.eyes = new ArrayList<>();
        CompoundNBT lnbt = compound.getCompound("eyes");
        for (Integer i = 1; i <= this.lastId; ++i) {
            EyeData td = new EyeData(this);
            td.read(lnbt.getCompound(i.toString()));
            this.eyes.add(i - 1, td);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putInt("lastId", lastId);
        CompoundNBT lnbt = new CompoundNBT();
        for (Integer i = 1; i <= this.lastId; ++i) {
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
            EyeData td = new EyeData(this);
            td.readBytes(buff);
            eyes.set(i - 1, td);
        }
    }
}
