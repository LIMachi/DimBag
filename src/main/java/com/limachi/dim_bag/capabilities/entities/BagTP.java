package com.limachi.dim_bag.capabilities.entities;

import com.limachi.lim_lib.World;
import com.limachi.lim_lib.capabilities.ICopyCapOnDeath;
import com.limachi.lim_lib.registries.annotations.RegisterCapability;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;

public class BagTP implements INBTSerializable<CompoundTag>, ICopyCapOnDeath<BagTP> {
    @RegisterCapability(targets = {Entity.class}, name = "teleport")
    public static final CapabilityToken<BagTP> TOKEN = new CapabilityToken<>(){};

    protected final HashMap<Integer, BlockPos> enterPosition = new HashMap<>();
    protected final HashMap<Integer, Pair<ResourceKey<Level>, BlockPos>> leavePosition = new HashMap<>();

    @Override
    public void copy(BagTP other) {
        enterPosition.clear();
        enterPosition.putAll(other.enterPosition);
        leavePosition.clear();
        leavePosition.putAll(other.leavePosition);
    }

    public BlockPos getEnterPos(int id) { return enterPosition.get(id); }
    public Pair<Level, BlockPos> getLeavePos(int id) {
        Pair<ResourceKey<Level>, BlockPos> p = leavePosition.get(id);
        if (p != null)
            return new Pair<>(World.getLevel(p.getFirst()), p.getSecond());
        return null; //FIXME
    }

    public void setEnterPos(int id, BlockPos pos) { enterPosition.put(id, pos); }
    public void setLeavePos(int id, ResourceKey<Level> dim, BlockPos pos) { leavePosition.put(id, new Pair<>(dim, pos)); }

    public void clearEnterPos(int id) { enterPosition.remove(id); }
    public void clearLeavePos(int id) { leavePosition.remove(id); }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag out = new CompoundTag();
        ListTag enter = new ListTag();
        for (Map.Entry<Integer, BlockPos> e : enterPosition.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putInt("BagId", e.getKey());
            entry.putLong("Pos", e.getValue().asLong());
            enter.add(entry);
        }
        out.put("Enter", enter);
        ListTag leave = new ListTag();
        for (Map.Entry<Integer, Pair<ResourceKey<Level>, BlockPos>> e : leavePosition.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putInt("BagId", e.getKey());
            entry.putString("Dim", e.getValue().getFirst().location().toString());
            entry.putLong("Pos", e.getValue().getSecond().asLong());
            leave.add(entry);
        }
        out.put("Leave", leave);
        return out;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        enterPosition.clear();
        ListTag enter = nbt.getList("Enter", Tag.TAG_COMPOUND);
        for (int i = 0; i < enter.size(); ++i) {
            CompoundTag entry = enter.getCompound(i);
            enterPosition.put(entry.getInt("BagId"), BlockPos.of(entry.getLong("Pos")));
        }
        leavePosition.clear();
        ListTag leave = nbt.getList("Leave", Tag.TAG_COMPOUND);
        for (int i = 0; i < leave.size(); ++i) {
            CompoundTag entry = leave.getCompound(i);
            leavePosition.put(entry.getInt("BagId"), new Pair<>(ResourceKey.create(Registries.DIMENSION, new ResourceLocation(entry.getString("Dim"))), BlockPos.of(entry.getLong("Pos"))));
        }
    }
}