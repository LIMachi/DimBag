package com.limachi.dimensional_bags.common.data;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.WorldUtils;
import com.limachi.dimensional_bags.common.inventory.BagInventory;
import com.limachi.dimensional_bags.common.managers.ModeManager;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.*;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

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
    private final DimBagData globalData;
    private Map<BlockPos, Integer> subRoomsToId; //maps virtual room coordinates (x,y,z) to real coordinates (+/-z), room 0,0,0 is the main/center room
    private ArrayList<BlockPos> idToSubRooms; //inverse of the above map
    private CompoundNBT upgradesNBT; //private storage of the upgrades
    private ModeManager modeManager;

    public ModeManager modeManager() { return modeManager; }

    public int roomCount() { return idToSubRooms.size(); }

    public static void tunnel(World world, BlockPos tunnel, Entity entity, boolean create, boolean destroy) { //create mode: build (if needed) a room and place a portal, !create mode: teleport the entity to the next portal
        int[] req = getRoomIds(world, tunnel, false);
        if (req == null) return ;
        EyeData data = EyeData.get(world.getServer(), req[0]);
        BlockPos coord = data.idToSubRooms.get(req[1]); //virtual coordinates of the current room
        Direction wall = data.wall(tunnel, req[1]); //which wall the tunnel was placed on
        if (wall == null) return ;
        BlockPos targetRoom = new BlockPos(coord.getX() + wall.getXOffset(), coord.getY() + wall.getYOffset(), coord.getZ() + wall.getZOffset()); //virtual coordinates of the targeted room
        Integer targetRoomId;
        if (create)
            targetRoomId = data.createSubRoom(world, targetRoom); //build the room (if necessary)
        else
            targetRoomId = data.subRoomsToId.get(targetRoom);
        BlockPos output = calculateOutput(tunnel, getEyePos(req[0]).add(0, 0, req[1] << 10), wall, targetRoomId - req[1]); //calculate the position of the output portal
        if (create)
            world.setBlockState(output, Registries.TUNNEL_BLOCK.get().getDefaultState()); //put the tunnel on the targeted room
        else if (destroy)
            world.setBlockState(output, Registries.WALL_BLOCK.get().getDefaultState()); //destroy the tunnel
        else
            WorldUtils.teleportEntity(entity, WorldUtils.worldRKFromWorld(world), wall == Direction.DOWN ? output.offset(wall, 2) : output.offset(wall)); //teleport the entity next to the portal
    }

    private static BlockPos calculateOutput(BlockPos tunnelIn, BlockPos roomCenter, Direction direction, int deltaRoom) {
        return new BlockPos(tunnelIn.getX() + (-Math.abs(direction.getXOffset()) * 2 * (tunnelIn.getX() - roomCenter.getX())),
                            tunnelIn.getY() + (-Math.abs(direction.getYOffset()) * 2 * (tunnelIn.getY() - roomCenter.getY())),
                            tunnelIn.getZ() + (-Math.abs(direction.getZOffset()) * 2 * (tunnelIn.getZ() - roomCenter.getZ())) + deltaRoom * 1024);
    }

    public int createSubRoom(World world, BlockPos targetRoom) {
        Integer room = subRoomsToId.get(targetRoom);
        if (room == null) { //test if the room exists, do nothing with the value
            room = idToSubRooms.size();
            subRoomsToId.put(targetRoom, room); //create the double link
            idToSubRooms.add(room, targetRoom);
            WorldUtils.buildRoom(world, getEyePos(getId()).add(0, 0, room << 10), getRadius(), 0); //build the room
            this.markDirty();
        }
        return room;
    }

    private Direction wall(BlockPos pos, int room) { //test if a position is IN a wall, and if true, return the direction of the wall (north, south, east, west, up, down; null if not a wall)
        int radius = getRadius();
        int x = pos.getX() -(id - 1) * 1024;
        if (x == 8 + radius) return Direction.byLong(1, 0, 0);
        if (x == 8 - radius) return Direction.byLong(-1, 0, 0);
        int y = pos.getY();
        if (y == 128 + radius) return Direction.byLong(0, 1, 0);
        if (y == 128 - radius) return Direction.byLong(0, -1, 0);
        int z = pos.getZ() -room * 1024;
        if (z == 8 + radius) return Direction.byLong(0, 0, 1);
        if (z == 8 - radius) return Direction.byLong(0, 0, -1);
        return null;
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
        this.upgradesNBT = new CompoundNBT();
        this.modeManager = new ModeManager(this);
        UpgradeManager.startingUpgrades(this);
        this.inventory = new BagInventory(this);
        this.subRoomsToId = new HashMap<>();
        this.subRoomsToId.put(new BlockPos(0, 0, 0), 0);
        this.idToSubRooms = new ArrayList<>();
        this.idToSubRooms.add(0, new BlockPos(0, 0, 0));
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
        int[] req = getRoomIds(world, pos, eye);
        if (req == null) return null;
        return get(world.getServer(), req[0]);
    }

    public static int[] getRoomIds(World world, BlockPos pos, boolean eye) {
        if (world.isRemote || WorldUtils.worldRKFromWorld((ServerWorld)world) != WorldUtils.DimBagRiftKey) return null;
        int x = pos.getX() - 8; //since all rooms are offset by 8 blocks (so the center of a room is approximately the center of a chunk), we offset back X and Z by 8
        int z = pos.getZ() - 8;
        if (eye) {
            if ((x & 1023) == 0 && pos.getY() == 128 && z == 0) //x & 1023 is the same as x % 1024 (basic binary arithmetic)
                return new int[]{(x >> 10) + 1, 0}; //x >> 10 is the same as x / 1024
        }
        else if (((x + 128) & 1023) <= 256) { //we add 128, so the range [-128, 128], becomes [0, 256], the maximum size of a room + extra
            int id = ((x + 128) >> 10) + 1; //since the block is now in a range [0, 256] + unknown * 1024, we cam divide by 1024 and the [0, 256] part will be discarded by the int precision, also, using a shift instead of a division
            EyeData data = EyeData.get(world.getServer(), id);
            int radius = data.getRadius();
            if (((x + radius) & 1023) <= radius << 1 && pos.getY() >= 128 - radius && pos.getY() <= 128 + radius && ((z + radius) & 1023) <= radius << 1) //now that we know the radius, we can quickly test the room on the X and Z axis
                return new int[]{id, (z + 128) >> 10}; //same trick as for the id, but rooms start at 0 (0 = center room, where the eye resides)
        }
        return null;
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

//    public EyeData setRows(int rows) { this.upgrades[ROWS] = rows; return this; }
    public int getRows() { return UpgradeManager.getUpgrade("upgrade_row").getCount(this); }

//    public EyeData setColumns(int columns) { this.upgrades[COLUMNS] = columns; return this; }
    public int getColumns() { return UpgradeManager.getUpgrade("upgrade_column").getCount(this); }

    public int getRadius() { return UpgradeManager.getUpgrade("upgrade_radius").getCount(this); }
//    public EyeData setRadius(int radius) { this.upgrades[RADIUS] = radius; return this; }

    public CompoundNBT getUpgradesNBT() { return this.upgradesNBT; }
    public Set<String> getUpgrades() { return this.upgradesNBT.keySet(); }

    /*
    public int getUpgrade(int id) { return id < 0 || id >= UpgradeManager.upgradesCount() ? 0 : this.upgrades[id]; }
    public EyeData setUpgrade(int id, int value) {
        if (id >= 0 && id < UpgradeManager.upgradesCount())
            this.upgrades[id] = value;
        return this;
    }
     */

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
        entity = new WeakReference<>(user);
    }

    public boolean shouldCreateCloudInVoid() {
        return true; //FIXME: use and upgrade instead
    }

    public final BagInventory getInventory() { return this.inventory; }

    public void tpBack(Entity entity) { //teleport an entity to the location targeted by the bag
        WorldUtils.teleportEntity(entity, tpDimension, tpPosition);
    }

    public void tpIn(Entity entity) { //teleport an entity to the eye of the bag
        WorldUtils.teleportEntity(entity, WorldUtils.DimBagRiftKey, new BlockPos(1024 * (id - 1) + 8, 129, 8));
    }

    public void tpTunnel(Entity entity, BlockPos portalPos) { //teleport an entity to the next room, the position of the portal determine the destination
        tunnel(entity.world, portalPos, entity, false, false);
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
        nbt.put("Modes", modeManager.write(new CompoundNBT()));
        nbt.put("UpgradesData", this.upgradesNBT);
        ListNBT listSubRooms = new ListNBT();
        for (BlockPos li : idToSubRooms)
            listSubRooms.add(NBTUtil.writeBlockPos(li));
        nbt.put("SubRooms", listSubRooms);
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
        this.modeManager = new ModeManager(this);
        this.modeManager.read(nbt.getCompound("Modes"));
        this.upgradesNBT = nbt.getCompound("UpgradesData");
        ListNBT listSubRooms = nbt.getList("SubRooms", 10);
        this.idToSubRooms = new ArrayList<>();
        this.subRoomsToId = new HashMap<>();
        for (int i = 0; i < listSubRooms.size(); ++i) {
            BlockPos room = NBTUtil.readBlockPos(listSubRooms.getCompound(i));
            idToSubRooms.add(i, room);
            subRoomsToId.put(room, i);
        }
    }
}
