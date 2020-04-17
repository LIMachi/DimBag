package com.limachi.dimensional_bags.common.dimensions;

import com.limachi.dimensional_bags.DimensionalBagsMod;
import com.limachi.dimensional_bags.common.dimensions.BagDimension;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.storage.WorldSavedData;

import static com.limachi.dimensional_bags.DimensionalBagsMod.MOD_ID;

class SavedData extends WorldSavedData {

    private int lastId;
    private static MinecraftServer sServer;

    private SavedData(String s, MinecraftServer server) {
        super(s);
        DimensionalBagsMod.LOGGER.info("construction called for " + this);
        sServer = server;
    }

    static public SavedData get(MinecraftServer server) {
        return server.getWorld(BagDimension.sType).getSavedData().getOrCreate(() -> new SavedData(MOD_ID, server), MOD_ID);
    }

    int getIdCount() { return lastId; }

    int nextId() {
        int r = lastId++;
        DimensionalBagsMod.LOGGER.info("current last id: " + getIdCount() + "for " + this);
        update();
        return r;
    }

    public void update() //FIXME: since write didn't seem to be called at the right time, force the save to be right after markDirty (surely a problem of lifetime/instancing), might lag the game if called too often (IO)
    {
        super.markDirty();
        sServer.getWorld(BagDimension.sType).getSavedData().save();
    }

    @Override
    public void read(CompoundNBT compound) {
        lastId = compound.getInt("lastId");
        DimensionalBagsMod.LOGGER.info("**************** WSD read was called, lastId: " + lastId);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        DimensionalBagsMod.LOGGER.info("**************** WSD write was called, lastId: " + lastId);
        compound.putInt("lastId", lastId);
        return compound;
    }
}