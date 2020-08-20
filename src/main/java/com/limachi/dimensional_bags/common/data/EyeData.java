package com.limachi.dimensional_bags.common.data;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.WorldUtils;
import com.limachi.dimensional_bags.common.inventory.BagInventory;
import com.limachi.dimensional_bags.common.inventory.UpgradeInventory;
import com.limachi.dimensional_bags.common.inventory.Wrapper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.List;
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
    private List<WeakReference<ServerPlayerEntity>> listeners; //list of player currently accessing a gui of the eye/bag
    private RegistryKey<World> tpDimension;
    private BlockPos tpPosition;
    private BagInventory inventory;
    private Wrapper upgrades;
    private final DimBagData globalData;

    public List<ServerPlayerEntity> collectListeners(ContainerType type) { //get all the players currently using this kind of container
        List<ServerPlayerEntity> out = NonNullList.create();
        for (WeakReference<ServerPlayerEntity> ref : listeners) {
            ServerPlayerEntity player = ref.get();
            if (player == null)
                listeners.remove(ref);
            else if (player.openContainer.getType() == type)
                out.add(player);
        }
        return out;
    }

    public void addListener(ServerPlayerEntity player) {
        if (player == null) return;
        for (WeakReference<ServerPlayerEntity> ref : listeners) {
            if (ref.get() == null)
                listeners.remove(ref);
            if (ref.get() == player)
                return;
        }
        listeners.add(new WeakReference<>(player));
    }

    public void removeListener(ServerPlayerEntity player) {
        if (player == null) return;
        for (WeakReference<ServerPlayerEntity> ref : listeners) {
            if (ref.get() == null)
                listeners.remove(ref);
            if (ref.get() == player) {
                listeners.remove(ref);
                return;
            }
        }
    }

    public EyeData(@Nullable ServerPlayerEntity player, int id) {
        super(MOD_ID + "_eye_" + id);
        this.globalData = DimBagData.get(DimBag.getServer(player != null ? player.world : null));
        this.id = id;
        if (player != null) {
            this.ownerUUID = player.getUniqueID();
            this.ownerName = player.getName().getString();
        } else {
            this.ownerUUID = new UUID(0, 0); //null UUID
            this.ownerName = "Invalid Player";
        }
        this.owner = new WeakReference<>(player);
        this.entity = new WeakReference<>(player);
        this.tpDimension = WorldUtils.DimOverworldKey;
        this.tpPosition = WorldUtils.getOverWorld().func_241135_u_(); //TODO: replace mapping, should be the call to get the default spawn
//        this.tpDimension = player.getServer().getWorld(DimensionType.OVERWORLD)//DimensionType.OVERWORLD//DimensionType.OVERWORLD;
//        this.tpPosition = globalData.getOverWorld(); //default position for tp the spawn, until the actual position is updated by the item/entity version of the bag (or the holder of the bag)
        this.upgrades = new UpgradeInventory(this);
        this.inventory = new BagInventory(this);
    }

    public static EyeData get(@Nullable MinecraftServer server, int id) { //this getter does not create the eye, call DimBagData#newEye instead
        if (server == null)
            server = DimBag.getServer(null); //overkill security
        if (server == null)
            return null; //overkill security
        return server.getWorld(World.field_234918_g_).getSavedData().get(() -> new EyeData(null, id), MOD_ID + "_eye_" + id);
    }

    public static BlockPos getEyePos(int id) { return new BlockPos(8 + ((id - 1) << 10), 128, 8); } //each eye is 1024 blocks appart, so for the maximum size of a room (radius 127, 255 blocks diameter), there is at least 32 chunks (32*16=512 blocks) between each room

    public BlockPos getEyePos() {
        return getEyePos(this.id);
    }

    public static EyeData getEyeData(World world, BlockPos pos, boolean eye) {
        if (world.isRemote || WorldUtils.worldRKFromWorld((ServerWorld)world) != WorldUtils.DimBagRiftKey) return null;
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

    public static int getEyeId(World world, BlockPos pos) {
        EyeData data = getEyeData(world, pos, false);
        if (data == null)
            return 0;
        return data.getId();
    }

    public /*PlayerInvWrapper*/PlayerInventory getPlayerInventory() {
        Entity try1 = entity.get();
        if (try1 instanceof ServerPlayerEntity) {
//            userInventory.resyncPlayerInventory(((ServerPlayerEntity) try1).inventory);
            return /*new PlayerInvWrapper(*/((ServerPlayerEntity) try1).inventory/*, ioRights)*/;
        }
        ServerPlayerEntity try2 = getOwnerPlayer();
        if (try2 != null) {
//            userInventory.resyncPlayerInventory(try2.inventory);
            return /*new PlayerInvWrapper(*/try2.inventory/*, ioRights)*/;
        }
        return null;
    }

    /*
    public void openInvetoryGUI(ServerPlayerEntity player) {
        NetworkHooks.openGui(player, new INamedContainerProvider() {
            @Override
            public ITextComponent getDisplayName() {
                return new TranslationTextComponent("inventory.bag.name");
            }

            @Nullable
            @Override
            public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                return new BagContainer(windowId, player, this);
            }
        }, inventory::toBytes); //should change the call to toBytes
    }
    */

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
    public final Wrapper getupgrades() { return this.upgrades; }

    public void tpBack(Entity entity) { //teleport an entity to the location targeted by the bag
        WorldUtils.teleportEntity(entity, tpDimension, tpPosition);
    }

    public void tpIn(Entity entity) { //teleport an entity to the eye of the bag
        WorldUtils.teleportEntity(entity, WorldUtils.DimBagRiftKey, new BlockPos(1024 * (id - 1) + 8, 129, 8));
    }

    public void tpTunnel(Entity entity, BlockPos portalPos) { //teleport an entity to the next room, the position of the portal determine the destination

    }

    public void updateBagPosition(BlockPos newPos, ServerWorld newDim) {
        this.tpPosition = newPos;
        this.tpDimension = WorldUtils.worldRKFromWorld(newDim);
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        DimBag.LOGGER.info("Updating " + getName());
        nbt.putInt("Id", this.id);
        nbt.putUniqueId("Owner", this.ownerUUID);
        nbt.putString("OwnerName", this.ownerName);
        nbt.putString("Dim", WorldUtils.worldRKToString(this.tpDimension));
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
        this.tpDimension = WorldUtils.stringToWorldRK(nbt.getString("Dim"));
        this.tpPosition = new BlockPos(nbt.getInt("X"), nbt.getInt("Y"), nbt.getInt("Z"));
        this.inventory.read(nbt.getCompound("Inventory"));
        this.upgrades.read(nbt.getCompound("Upgrades"));
    }
}
