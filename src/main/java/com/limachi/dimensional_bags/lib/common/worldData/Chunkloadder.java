package com.limachi.dimensional_bags.lib.common.worldData;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.lib.utils.WorldUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Chunkloadder {

    private static class CLEntry {
        public int x;
        public int y;
        public String dimReg;

        public CLEntry(ServerWorld world1, int x1, int y1) {
            x = x1;
            y = y1;
            dimReg = WorldUtils.worldRKToString(world1.dimension());
        }

        public CLEntry(String dim, int x1, int y1) {
            x = x1;
            y = y1;
            dimReg = dim;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CLEntry clEntry = (CLEntry) o;
            return x == clEntry.x && y == clEntry.y && Objects.equals(dimReg, clEntry.dimReg);
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, dimReg);
        }
    }

    private Map<Integer, CLEntry> map = new HashMap<>(); //which bag is in which chunk (forcing it to load)
    private Map<CLEntry, Integer> arc = new HashMap<>(); //how many bags are in each loaded chunk (once at zero, chunk should be unloaded)
    private ArrayList<CLEntry> list = new ArrayList<>(); //which chunk should be loadded no matter what

    public void loadChunk(ServerWorld world, BlockPos pos, int by) {
        loadChunk(world, pos.getX() >> 4, pos.getZ() >> 4, by);
    }

    public void loadChunk(ServerWorld world, int x, int z, int by) { //'by' is the bag id that is forcing the chunk to be loadded, since the mod is made with the idea that only one item/entity can exist at a time with the same id, we consider that only one chunk can be loadded by a single item/entity
//        int cx = x >> 4;
//        int cy = z >> 4;
        CLEntry entry = new CLEntry(world, x, z);
        Integer r = arc.getOrDefault(entry, 0);
        if (r == 0) //this is the first id that tried to load this chunk, and thus the chunk must be loaded
            world.setChunkForced(x, z, true);
        arc.put(entry, r + 1);
        if (by != 0) {
            CLEntry mapEntry = map.get(by); //if possible, get the previous chunk loaded by this id
            if (mapEntry != null && !mapEntry.equals(entry)) { //the bag is not in the same chunk as before, should try an unload
                Integer r1 = arc.getOrDefault(mapEntry, 0);
                if (r1 <= 1) //this was the last id loading this chunk, so it is time to unload it
                    WorldUtils.getWorld(DimBag.getServer(), mapEntry.dimReg).setChunkForced(mapEntry.x, mapEntry.y, false);
                if (r1 <= 1)
                    arc.remove(mapEntry);
                else
                    arc.put(mapEntry, r1 - 1);
            }
            map.put(by, entry); //override the position of the id
        } else if (!list.contains(entry))
            list.add(entry);
    }

    public void unloadChunk(int by) { //this id is now unloaded/missing, if possible try to unload the chunk it was in
        if (by != 0) {
            CLEntry entry = map.get(by);
            if (entry == null) return;
            map.remove(by);
            int r = arc.getOrDefault(entry, 0);
            if (r <= 1) {
                WorldUtils.getWorld(DimBag.getServer(), entry.dimReg).setChunkForced(entry.x, entry.y, false);
                arc.remove(entry);
            } else
                arc.put(entry, r - 1);
        }
    }

    public void unloadChunk(ServerWorld world, int x, int z) {
        CLEntry entry = new CLEntry(world, x >> 4, z >> 4);
        if (list.contains(entry)) {
            list.remove(entry);
            int c = arc.getOrDefault(entry, 0);
            if (c <= 1) {
                WorldUtils.getWorld(DimBag.getServer(), entry.dimReg).setChunkForced(entry.x, entry.y, false);
                arc.remove(entry);
            } else
                arc.put(entry, c - 1);
        }
    }

    public void reloadAll() {
        arc = new HashMap<>(); //reset the counters
        for (Map.Entry<Integer, CLEntry> me: map.entrySet()) {
            CLEntry entry = me.getValue();
            int r = arc.getOrDefault(entry, 0);
            if (r == 0)
                WorldUtils.getWorld(DimBag.getServer(), entry.dimReg).setChunkForced(entry.x, entry.y, true); //FIXME: invalid entry
            arc.put(entry, r + 1);
        }
        for (int i = 0; i < list.size(); ++i) {
            CLEntry entry = list.get(i);
            int r = arc.getOrDefault(entry, 0);
            if (r == 0)
                WorldUtils.getWorld(DimBag.getServer(), entry.dimReg).setChunkForced(entry.x, entry.y, true);
            arc.put(entry, r + 1);
        }
    }

    public void read(CompoundNBT compound) {
        ListNBT cl = compound.getList("ChunkLoadedByBag", 10);
        for (int i = 0; i < cl.size(); ++i) {
            CompoundNBT e = (CompoundNBT)cl.get(i);
            map.put(e.getInt("Id"), new CLEntry(e.getString("Dim"), e.getInt("X"), e.getInt("Y")));
        }
        cl = compound.getList("ChunkLoaded", 10);
        for (int i = 0; i < cl.size(); ++i) {
            CompoundNBT e = (CompoundNBT)cl.get(i);
            list.add(new CLEntry(e.getString("Dim"), e.getInt("X"), e.getInt("Y")));
        }
        reloadAll();
    }

    public CompoundNBT write(CompoundNBT compound) {
        ListNBT cl = new ListNBT();
        for (Map.Entry<Integer, CLEntry> me: map.entrySet()) {
            CompoundNBT e = new CompoundNBT();
            e.putInt("Id", me.getKey());
            CLEntry entry = me.getValue();
            e.putInt("X", entry.x);
            e.putInt("Y", entry.y);
            e.putString("Dim", entry.dimReg);
            cl.add(e);
        }
        compound.put("ChunkLoadedByBag", cl);
        cl = new ListNBT();
        for (CLEntry entry : list) {
            CompoundNBT e = new CompoundNBT();
            e.putInt("X", entry.x);
            e.putInt("Y", entry.y);
            e.putString("Dim", entry.dimReg);
            cl.add(e);
        }
        compound.put("ChunkLoaded", cl);
        return compound;
    }
}
