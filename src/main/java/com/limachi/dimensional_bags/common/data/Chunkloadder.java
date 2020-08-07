package com.limachi.dimensional_bags.common.data;

import com.limachi.dimensional_bags.DimBag;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import org.omg.IOP.ENCODING_CDR_ENCAPS;

import java.util.HashMap;
import java.util.Map;

public class Chunkloadder {

    private class CLEntry {
        public int x;
        public int y;
        public int dim;

        public CLEntry(ServerWorld world1, int x1, int y1) {
            x = x1;
            y = y1;
            dim = world1.getDimension().getType().getId();
        }

        public CLEntry(int dim1, int x1, int y1) {
            x = x1;
            y = y1;
            dim = dim1;
        }

        public boolean equal(ServerWorld world1, int x1, int y1) {
            return dim == world1.getDimension().getType().getId() && x == x1 && y == y1;
        }
    }

    private Map<Integer, CLEntry> map = new HashMap<>(); //which bag is in which chunk (forcing it to load)
    private Map<CLEntry, Integer> arc = new HashMap<>(); //how many bags are in each loaded chunk (once at zero, chunk should be unloaded)

    public void loadChunk(ServerWorld world, int x, int z, int by) { //'by' is the bag id that is forcing the chunk to be loadded, since the mod is made with the idea that only one item/entity can exist at a time with the same id, we consider that only one chunk can be loadded by a single item/entity
        int cx = x >> 4;
        int cy = z >> 4;
        CLEntry entry = new CLEntry(world, cx, cy);
        Integer r = arc.getOrDefault(entry, 0);
        if (r == 0) //this is the first id that tried to load this chunk, and thus the chunk must be loaded
            world.forceChunk(cx, cy, true);
        arc.put(entry, r + 1);
        CLEntry mapEntry = map.get(by); //if possible, get the previous chunk loaded by this id
        if (mapEntry != null && !entry.equal(world, cx, cy)) { //the bag is not in the same chunk as before, should try an unload
            Integer r1 = arc.getOrDefault(mapEntry, 0);
            if (r1 <= 1) //this was the last id loading this chunk, so it is time to unload it
                DimBag.getServer(world).getWorld(DimensionType.getById(mapEntry.dim)).forceChunk(mapEntry.x, mapEntry.y, false);
            if (r1 <= 1)
                arc.remove(mapEntry);
            else
                arc.put(mapEntry, r1 - 1);
        }
        map.put(by, entry); //override the position of the id
    }

    public void unloadChunk(MinecraftServer server, int by) { //this id is now unloaded/missing, if possible try to unload the chunk it was in
        CLEntry entry = map.get(by);
        if (entry == null) return;
        map.remove(by);
        int r = arc.getOrDefault(entry, 0);
        if (r <= 1) {
            server.getWorld(DimensionType.getById(entry.dim)).forceChunk(entry.x, entry.y, false);
            arc.remove(entry);
        } else
            arc.put(entry, r - 1);
    }

    public void reloadAll() {
        arc = new HashMap<>(); //reset the counters
        for (Map.Entry<Integer, CLEntry> me: map.entrySet()) {
            CLEntry entry = me.getValue();
            int r = arc.getOrDefault(entry, 0);
            if (r == 0)
                DimBag.getServer(null).getWorld(DimensionType.getById(entry.dim)).forceChunk(entry.x, entry.y, true);
            arc.put(entry, r + 1);
        }
    }

    public void read(CompoundNBT compound) {
        ListNBT cl = compound.getList("ChunkLoaded", 10);
        for (int i = 0; i < cl.size(); ++i) {
            CompoundNBT e = (CompoundNBT)cl.get(i);
            map.put(e.getInt("Id"), new CLEntry(e.getInt("Dim"), e.getInt("X"), e.getInt("Y")));
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
            e.putInt("Dim", entry.dim);
            cl.add(e);
        }
        compound.put("ChunkLoaded", cl);
        return compound;
    }
}
