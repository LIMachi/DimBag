package com.limachi.dimensional_bags.common.data.EyeDataMK2;

import com.google.common.collect.Range;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.WorldUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nullable;

public class IDManager extends WorldSavedData {

    private int id = 0;

    public IDManager() {
        super(DimBag.MOD_ID + "_id_manager");
    }

    public Range<Integer> currentlyGivenIds() { return Range.openClosed(0, id); }

    public int lastId() { return id; }

    public int newId() { markDirty(); return ++id; }

    @Override
    public void read(CompoundNBT nbt) {
        id = nbt.getInt("Id");
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        nbt.putInt("Id", id);
        return nbt;
    }

    static public IDManager getInstance(@Nullable ServerWorld world) {
        if (world == null)
            world = WorldUtils.getOverWorld();
        if (world != null)
            return world.getSavedData().getOrCreate(IDManager::new, DimBag.MOD_ID + "_id_manager");
        return null;
    }
}
