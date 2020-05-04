package com.limachi.dimensional_bags.common.data;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.dimension.BagRiftDimension;
import com.limachi.dimensional_bags.common.inventory.BagInventory;
import com.limachi.dimensional_bags.common.inventory.UpgradeInventory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nullable;
import java.util.UUID;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;
import static com.limachi.dimensional_bags.common.upgradeManager.UpgradeManager.COLUMNS;
import static com.limachi.dimensional_bags.common.upgradeManager.UpgradeManager.ROWS;

public class EyeData extends WorldSavedData { //TODO: make EyeData a WorldSavedData (and change DimBagData to only hold global data, like id's), and no longer use the manager, the eye will manage itself (the get function will take an id in adition to the additional server), no longer use the DimensionSavedDataManager#getOrCreate, make the getter use get and return an error if not present, use the setter to create a new eye
    private int id;
    private UUID playerUUID;
    @Nullable
    private ServerPlayerEntity player; //might be null if the player is not online
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
            this.player = player;
            this.playerUUID = player.getUniqueID();
        } else {
            this.player = null;
            this.playerUUID = new UUID(0, 0); //null UUID
        }
        this.tpDimension = DimensionType.OVERWORLD;
        this.tpPosition = globalData.getOverWorld().getSpawnPoint(); //default position for tp the spawn, until the actual position is updated by the item/entity version of the bag
        this.upgrades = new UpgradeInventory(this);
        this.inventory = new BagInventory(this);
    }

    public static EyeData get(MinecraftServer server, int id) { //this getter does not create the eye, call DimBagData#newEye instead
        if (server == null)
            server = DimBag.getServer(null); //overkill security
        return server.getWorld(DimensionType.OVERWORLD).getSavedData().get(() -> new EyeData(null, id), MOD_ID + "_eye_" + id);
    }

    public int getRows() {
        return this.upgrades.getStackInSlot(ROWS).getCount();
    }

    public int getColumns() {
        return this.upgrades.getStackInSlot(COLUMNS).getCount();
    }

    public final int getId() { return this.id; }
    public final ServerPlayerEntity getPlayer() { return this.player; }
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
        nbt.putUniqueId("Owner", this.playerUUID);
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
        this.playerUUID = nbt.getUniqueId("Owner");
        DimBag.getServer(null).getPlayerList().getPlayerByUUID(this.playerUUID);
        this.tpDimension = DimensionType.getById(nbt.getInt("Dim"));
        this.tpPosition = new BlockPos(nbt.getInt("X"), nbt.getInt("Y"), nbt.getInt("Z"));
        this.inventory.read(nbt.getCompound("Inventory"));
        this.upgrades.read(nbt.getCompound("Upgrades"));
    }
}
