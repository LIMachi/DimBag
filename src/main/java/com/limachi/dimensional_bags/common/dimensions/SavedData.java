package com.limachi.dimensional_bags.common.dimensions;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.WorldSavedData;

import static com.limachi.dimensional_bags.DimensionalBagsMod.MOD_ID;

class SavedData extends WorldSavedData {

    private int lastId;

    public SavedData(String s) {
        super(s);
    }

    static public SavedData get(MinecraftServer server) {
        return server.getWorld(DimensionType.OVERWORLD).getSavedData().getOrCreate(() -> new SavedData(MOD_ID), MOD_ID); //use overworld as default storage for data as it is the world that is guaranteed to exist (if I used the dim_bag_rift dimension, it would only work once the world is loaded)
    }

    int getIdCount() { return lastId; }

    int nextId() {
        int r = lastId++;
        markDirty();
        return r;
    }

    @Override
    public void read(CompoundNBT compound) {
        lastId = compound.getInt("lastId");
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putInt("lastId", lastId);
        return compound;
    }
}