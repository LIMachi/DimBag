package com.limachi.dimensional_bags.common.data;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.ClientDataManager;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.OwnerData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SettingsData;
import com.limachi.dimensional_bags.common.managers.ModeManager;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import com.limachi.dimensional_bags.utils.WorldUtils;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nullable;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

public class DimBagData extends WorldSavedData { //server side only, client side only has acces to copies of inventories or other data sync through packets (vanilla, forge or modded ones)
    private int lastId = 0;
    private final Chunkloadder chunkloadder = new Chunkloadder();

    DimBagData() { super(MOD_ID); }

    static public DimBagData get(@Nullable MinecraftServer server) {
        if (server == null)
            server = DimBag.getServer();
        return server.getWorld(World.OVERWORLD).getSavedData().getOrCreate(DimBagData::new, MOD_ID);
    }

    public int getLastId() { return lastId; }

    public int newEye(ServerPlayerEntity player, ItemStack bag) {
        int id = ++lastId;
        SubRoomsManager roomsManager = SubRoomsManager.getInstance(id);
        if (roomsManager == null) {
            --lastId;
            return 0;
        }
        World world = WorldUtils.getRiftWorld();
        if (world instanceof ServerWorld) {
            BlockPos eyePos = SubRoomsManager.getEyePos(id);
            WorldUtils.buildRoom(world, eyePos, SubRoomsManager.DEFAULT_RADIUS);
            roomsManager.markDirty();
            chunkloadder.loadChunk((ServerWorld) world, eyePos.getX(), eyePos.getZ(), 0);
        }
        SettingsData.getInstance(id).initDefaultSettings();
        ModeManager.getInstance(id).markDirty();
        OwnerData ownerData = OwnerData.getInstance(id);
        if (ownerData != null) {
            ownerData.setPlayer(player);
            new ClientDataManager(id, UpgradeManager.getInstance(id), ModeManager.getInstance(id), ownerData).store(bag);
        }
        markDirty();
        return id;
    }

    public void loadChunk(ServerWorld world, int x, int z, int by) {
        this.chunkloadder.loadChunk(world, x, z, by);
        this.markDirty();
    }

    public void unloadChunk(MinecraftServer server, int by) {
        this.chunkloadder.unloadChunk(by);
        this.markDirty();
    }

    public void unloadChunk(ServerWorld world, int x, int z) {
        this.chunkloadder.unloadChunk(world, x, z);
        this.markDirty();
    }

    @Override
    public void read(CompoundNBT compound) {
        DimBag.LOGGER.info("Loadding global data");
        this.lastId = compound.getInt("lastId");
        this.chunkloadder.read(compound);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        DimBag.LOGGER.info("Updating global data");
        compound.putInt("lastId", lastId);
        this.chunkloadder.write(compound);
        return compound;
    }
}
