package com.limachi.dimensional_bags.common.data;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.dimension.BagRiftDimension;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nullable;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

public class DimBagData extends WorldSavedData { //server side only, client side only has acces to copies of inventories or other data sync through packets (vanilla, forge or modded ones)
    private int lastId;
    private final ServerWorld riftWorld;
    private final ServerWorld overWorld;

    DimBagData() {
        super(MOD_ID);
        this.lastId = 0;
        MinecraftServer server = DimBag.getServer(null);
        this.riftWorld = server.getWorld(BagRiftDimension.getDimensionType());
        this.overWorld = server.getWorld(DimensionType.OVERWORLD);
    }

    public ServerWorld getRiftWorld() { return this.riftWorld; }
    public ServerWorld getOverWorld() { return this.overWorld; }

    static public DimBagData get(@Nullable MinecraftServer server) {
        if (server == null)
            server = DimBag.getServer(null);
        return server.getWorld(DimensionType.OVERWORLD).getSavedData().getOrCreate(DimBagData::new, MOD_ID);
    }

    public int getLastId() { return lastId; }

    public EyeData newEye(ServerPlayerEntity player) {
        int id = ++this.lastId;
        EyeData data = new EyeData(player, id);
        data.markDirty();
        overWorld.getSavedData().set(data);
        this.markDirty();
        return data;
    }

    @Override
    public void read(CompoundNBT compound) {
        DimBag.LOGGER.info("Loadding global data");
        this.lastId = compound.getInt("lastId");
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        DimBag.LOGGER.info("Updating global data");
        compound.putInt("lastId", lastId);
        return compound;
    }
}
