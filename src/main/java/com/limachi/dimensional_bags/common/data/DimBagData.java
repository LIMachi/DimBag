package com.limachi.dimensional_bags.common.data;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.WorldUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nullable;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;
import static com.limachi.dimensional_bags.common.upgradeManager.UpgradeManager.RADIUS;

public class DimBagData extends WorldSavedData { //server side only, client side only has acces to copies of inventories or other data sync through packets (vanilla, forge or modded ones)
    private int lastId;
    private final Chunkloadder chunkloadder;

    DimBagData() {
        super(MOD_ID);
        this.lastId = 0;
        this.chunkloadder = new Chunkloadder();
    }

    static public DimBagData get(@Nullable MinecraftServer server) {
        if (server == null)
            server = DimBag.getServer(null);
        return server.getWorld(WorldUtils.DimOverworldKey).getSavedData().getOrCreate(DimBagData::new, MOD_ID);
    }

    public int getLastId() { return lastId; }

    public EyeData newEye(ServerPlayerEntity player) {
        int id = ++this.lastId;
        EyeData data = new EyeData(player, id);
        WorldUtils.buildRoom(WorldUtils.getRiftWorld(), EyeData.getEyePos(data.getId()), data.getRadius(), 0);
        data.markDirty();
        WorldUtils.getOverWorld().getSavedData().set(data);
        this.markDirty();
        return data;
    }

    public void loadChunk(MinecraftServer server, RegistryKey<World> worldRK, /*int dimId,*/ int x, int z, int by) {
        this.chunkloadder.loadChunk(server.getWorld(worldRK), x, z, by);
        this.markDirty();
    }

    public void unloadChunk(MinecraftServer server, int by) {
        this.chunkloadder.unloadChunk(server, by);
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
