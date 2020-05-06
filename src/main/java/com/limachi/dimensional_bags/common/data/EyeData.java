package com.limachi.dimensional_bags.common.data;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.EventManager;
import com.limachi.dimensional_bags.common.dimension.BagRiftDimension;
import com.limachi.dimensional_bags.common.inventory.BagInventory;
import com.limachi.dimensional_bags.common.inventory.UpgradeInventory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.UUID;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;
import static com.limachi.dimensional_bags.common.upgradeManager.UpgradeManager.COLUMNS;
import static com.limachi.dimensional_bags.common.upgradeManager.UpgradeManager.RADIUS;
import static com.limachi.dimensional_bags.common.upgradeManager.UpgradeManager.ROWS;

public class EyeData extends WorldSavedData { //TODO: make EyeData a WorldSavedData (and change DimBagData to only hold global data, like id's), and no longer use the manager, the eye will manage itself (the get function will take an id in adition to the additional server), no longer use the DimensionSavedDataManager#getOrCreate, make the getter use get and return an error if not present, use the setter to create a new eye
    private int id;
    private UUID ownerUUID;
    private String ownerName;
    private WeakReference<ServerPlayerEntity> owner; //cache for the player referenced by uuid
    private WeakReference<Entity> entity; //cache for the entity that currently hold the bag (can be the bag itself, in entity or itementity form)
    private DimensionType tpDimension;
    private BlockPos tpPosition;
    private BagInventory inventory;
    private UpgradeInventory upgrades;
    private final DimBagData globalData;

    public EyeData(@Nullable ServerPlayerEntity player, int id) {
        super(MOD_ID + "_eye_" + id);
        this.globalData = DimBagData.get(DimBag.getServer(player != null ? player.world : null));
        this.id = id;
        if (player != null) {
            this.ownerUUID = player.getUniqueID();
            this.ownerName = player.getName().getFormattedText();
        } else {
            this.ownerUUID = new UUID(0, 0); //null UUID
            this.ownerName = "Invalid Player";
        }
        this.owner = new WeakReference<>(player);
        this.entity = new WeakReference<>(player);
        this.tpDimension = DimensionType.OVERWORLD;
        this.tpPosition = globalData.getOverWorld().getSpawnPoint(); //default position for tp the spawn, until the actual position is updated by the item/entity version of the bag
        this.upgrades = new UpgradeInventory(this);
        this.inventory = new BagInventory(this);
    }

    public static EyeData get(@Nullable MinecraftServer server, int id) { //this getter does not create the eye, call DimBagData#newEye instead
        if (server == null)
            server = DimBag.getServer(null); //overkill security
        return server.getWorld(DimensionType.OVERWORLD).getSavedData().get(() -> new EyeData(null, id), MOD_ID + "_eye_" + id);
    }

    public static BlockPos getEyePos(int id) { return new BlockPos(8 + ((id - 1) << 10), 128, 8); } //each eye is 1024 blocks appart, so for the maximum size of a room (radius 126, 255 blocks diameter), there is at least 32 chunks (32*16=512 blocks) between each room

    public static EyeData getEyeData(World world, BlockPos pos, boolean eye) {
        if (world.isRemote || world.dimension.getType() != BagRiftDimension.getDimensionType()) return null;
        int x = pos.getX() - 8; //since all rooms are offset by 8 blocks (so the center of a room is approximately the center of a chunk), we offset back X and Z by 8
        int z = pos.getZ() - 8;
        if (eye) {
            if ((x & 1023) == 0 && pos.getY() == 128 && z == 0) //x & 1023 is the same as x % 1024 (basic binary arithmetic)
                return EyeData.get(world.getServer(),(x >> 10) + 1); //x >> 10 is the same as x / 1024
        }
        else if (((x + 128) & 1023) <= 256) { //we add 128, so the range [-128, 128], becomes [0, 256], the maximum size of a room + extra
            int id = ((x + 128) >> 10) + 1; //since the block is now in a range [0, 256] + unknown * 1024, we cam divide by 1024 and the [0, 256] part will be discarded by the int precision, also, using a shift instead of a division
            EyeData data = EyeData.get(world.getServer(), id);
            int radius = data.getupgrades().getStackInSlot(RADIUS).getCount(); //FIXME: use actual radius instead of the upgrade count
            if (((x + radius) & 1023) <= radius << 1 && pos.getY() >= 128 - radius && pos.getY() <= 128 + radius && z >= -radius && z <= radius) //run another test, but this time with the actual size of the room FIXME: for now, incompatible with more rooms on the Z axis
                return data;
        }
        return null;
    }

    public int getRows() {
        return this.upgrades.getStackInSlot(ROWS).getCount();
    }

    public int getColumns() {
        return this.upgrades.getStackInSlot(COLUMNS).getCount();
    }

    public final int getId() { return this.id; }

    public final ServerPlayerEntity getOwnerPlayer() {
        ServerPlayerEntity player = owner.get();
        if (player == null) {
            player = DimBag.getServer(null).getPlayerList().getPlayerByUUID(ownerUUID);
            if (player != null)
                owner = new WeakReference<>(player);
        }
        return player;
    }

    public final Entity getUser() { //try to get the user from the cache, or the owner from the cache, or the owner from the server, might return null if nobody is using the bag and the owner is not online
        return entity.get();
    }

    /*
    public final Entity getBagEntity() { //try to get the entity representing the bag (itemstack/player/bag entity) //for now, only get the player using the bag
        if (user.get() != null)
            return user.get();
        return null;
    }
    */

    public String getOwnerName() { return ownerName; }

    public void setUser(Entity user) {
//        if (player != null && user.get() != player)
//            DimBag.LOGGER.info("Player: " + player.getName().getFormattedText() + " is now using bag: " + id);
        entity = new WeakReference<>(user);
    }

    public final BagInventory getInventory() { return this.inventory; }
    public final UpgradeInventory getupgrades() { return this.upgrades; }

    public void tpBack(Entity entity) { //teleport an entity to the location targeted by the bag
        BagRiftDimension.teleportEntity(entity, tpDimension, tpPosition);
    }

    public void tpIn(Entity entity) { //teleport an entity to the eye of the bag
        BagRiftDimension.teleportEntity(entity, BagRiftDimension.getDimensionType(), new BlockPos(1024 * (id - 1) + 8, 129, 8));
    }

    public void updateBagPosition(BlockPos newPos, DimensionType newDim) {
        this.tpPosition = newPos;
        this.tpDimension = newDim;
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        DimBag.LOGGER.info("Updating " + getName());
        nbt.putInt("Id", this.id);
        nbt.putUniqueId("Owner", this.ownerUUID);
        nbt.putString("OwnerName", this.ownerName);
        nbt.putInt("Dim", this.tpDimension.getId());
        nbt.putInt("X", this.tpPosition.getX());
        nbt.putInt("Y", this.tpPosition.getY());
        nbt.putInt("Z", this.tpPosition.getZ());
        nbt.put("Inventory", this.inventory.write(new CompoundNBT()));
        nbt.put("Upgrades", this.upgrades.write(new CompoundNBT()));
        return nbt;
    }

    @Override
    public void read(CompoundNBT nbt) {
        DimBag.LOGGER.info("Loadding " + getName());
        this.id = nbt.getInt("Id");
        this.ownerUUID = nbt.getUniqueId("Owner");
        this.ownerName = nbt.getString("OwnerName");
        this.owner = new WeakReference<>(DimBag.getServer(null).getPlayerList().getPlayerByUUID(this.ownerUUID));
        this.tpDimension = DimensionType.getById(nbt.getInt("Dim"));
        this.tpPosition = new BlockPos(nbt.getInt("X"), nbt.getInt("Y"), nbt.getInt("Z"));
        this.inventory.read(nbt.getCompound("Inventory"));
        this.upgrades.read(nbt.getCompound("Upgrades"));
    }
}
