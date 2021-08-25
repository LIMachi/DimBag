package com.limachi.dimensional_bags.common.data.EyeDataMK2;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.inventory.EntityInventoryProxy;
import com.limachi.dimensional_bags.utils.UUIDUtils;
import com.limachi.dimensional_bags.utils.WorldUtils;
import com.limachi.dimensional_bags.common.data.IEyeIdHolder;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;

import java.lang.ref.WeakReference;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class HolderData extends WorldSavedDataManager.EyeWorldSavedData {

    private EntityInventoryProxy invProxy = new EntityInventoryProxy(null);
    private WeakReference<Entity> holderRef = new WeakReference<>(null);
    private UUID id = UUIDUtils.NULL_UUID;
    private String name = "";
    private Vector3d lastKnownPosition = null;
    private RegistryKey<World> lastKnownDimension = null;

    public HolderData(String suffix, int id, boolean client) {
        super(suffix, id, client, false);
    }

    public void setHolder(Entity entity) {
        boolean dirty = false;
        if (entity != null && getEyeId() != SubRoomsManager.getEyeId(entity.level, entity.blockPosition(), false) && (!entity.position().equals(lastKnownPosition) || !entity.level.dimension().equals(lastKnownDimension))) {
            lastKnownPosition = entity.position();
            lastKnownDimension = entity.level.dimension();
            dirty = true;
        }
        if (holderRef.get() != entity) {
            holderRef = new WeakReference<>(entity);
            invProxy.setEntity(entity);
            if (entity != null) {
                name = entity.getDisplayName().getString();
                id = entity.getUUID();
            } else {
                name = "";
                id = UUIDUtils.NULL_UUID;
            }
            dirty = true;
        }
        if (dirty)
            setDirty();
    }

    public RegistryKey<World> getLastKnownDimension() { return lastKnownDimension; }

    public Vector3d getLastKnownPosition() { return lastKnownPosition; }

    public void tpToHolder(Entity entity) {
        WorldUtils.teleportEntity(entity, lastKnownDimension, lastKnownPosition);
    }

    /**
     * @return if available (in loadded chunk and not removed), return the last known holder of this eye
     */
    public Entity getEntity() {
        Entity entity = holderRef.get();
        if (entity != null && !entity.removed)
            return entity;
        return null;
    }

    public EntityInventoryProxy getEntityInventory() { return invProxy; }

    /**
     * @return if available (not removed from the chunk), return the last known holder of this eye, will load the chunk to refresh the reference of the holder if needed
     */
    public Entity getEntityForceLoad() {
        if (id.equals(UUIDUtils.NULL_UUID)) return null;
        Entity entity = holderRef.get();
        if (entity != null && !entity.removed)
            return entity;
        if (lastKnownPosition != null && lastKnownDimension != null) {
            ServerWorld world = WorldUtils.getWorld(DimBag.getServer(), lastKnownDimension);
            if (world != null) {
                entity = WorldUtils.getEntityByUUIDInChunk((Chunk) world.getChunk(new BlockPos(lastKnownPosition)), id);
                if (entity != null && !entity.removed) {
                    holderRef = new WeakReference<>(entity);
                    invProxy.setEntity(entity);
                    name = entity.getDisplayName().getString();
                    lastKnownPosition = entity.position();
                }
            }
        }
        return null;
    }

    /**
     * @return true if the entity holding the bag is itself (either as an item on the ground or the bag entity itself)
     */
    public boolean isHolderItself() {
        if (getEyeId() != 0) {
            Entity entity = getEntity();
            if (entity == null) return false;
            return entity instanceof IEyeIdHolder && ((IEyeIdHolder)entity).getEyeId() == getEyeId();
        }
        return false;
    }

    /**
     * @return the name of the holder, the entity isn't required to be loaded/valid, return "" as an invalid/unset holder
     */
    public String getEntityName() { return name; }

    /**
     * @return the uuid of the holder, the entity isn't required to be loaded/valid, return UUIDUtils.NULL_UUID as an invalid/unset holder (aka new UUID(0,0))
     */
    public UUID getEntityUUID() { return id; }

    @Override
    public void load(CompoundNBT nbt) {
        id = nbt.getUUID("Id");
        name = nbt.getString("Name");
        lastKnownDimension = WorldUtils.stringToWorldRK(nbt.getString("Dimension"));
        lastKnownPosition = new Vector3d(nbt.getDouble("X"), nbt.getDouble("Y"), nbt.getDouble("Z"));
        holderRef = new WeakReference<>(null);
        invProxy.setEntity(null);
        if (!id.equals(UUIDUtils.NULL_UUID) && lastKnownPosition != null && lastKnownDimension != null)
            getEntityForceLoad();
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        nbt.putUUID("Id", id);
        nbt.putString("Name", name);
        if (lastKnownPosition != null) {
            nbt.putDouble("X", lastKnownPosition.x);
            nbt.putDouble("Y", lastKnownPosition.y);
            nbt.putDouble("Z", lastKnownPosition.z);
        }
        if (lastKnownDimension != null)
            nbt.putString("Dimension", WorldUtils.worldRKToString(lastKnownDimension));
        return nbt;
    }

    static public HolderData getInstance(int id) { return WorldSavedDataManager.getInstance(HolderData.class, id); }

    static public <T> T execute(int id, Function<HolderData, T> executable, T onErrorReturn) { return WorldSavedDataManager.execute(HolderData.class, id, executable, onErrorReturn); }

    static public boolean execute(int id, Consumer<HolderData> executable) { return WorldSavedDataManager.execute(HolderData.class, id, data->{executable.accept(data); return true;}, false); }
}
