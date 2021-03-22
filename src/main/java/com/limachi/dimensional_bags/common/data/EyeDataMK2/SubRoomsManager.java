package com.limachi.dimensional_bags.common.data.EyeDataMK2;

import com.limachi.dimensional_bags.CuriosIntegration;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.ConfigManager.Config;
import com.limachi.dimensional_bags.common.NBTUtils;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.WorldUtils;
import com.limachi.dimensional_bags.common.blocks.Tunnel;
import com.limachi.dimensional_bags.common.blocks.Wall;
import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import com.limachi.dimensional_bags.common.tileentities.PadTileEntity;
import javafx.util.Pair;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class SubRoomsManager extends WorldSavedDataManager.EyeWorldSavedData {

    @Config(min = "3", max = "127", cmt = "default radius of a main room inside a bag, this includes the walls")
    public static int DEFAULT_RADIUS = 3;

    @Config(min = "2", max = "127", cmt = "default radius of a sub room (created with a tunnel), this includes the walls")
    public static int DEFAULT_RADIUS_SUB_ROOMS = 2;

    @Config(min = "3", max = "127", cmt = "maximum size of a room/subroom (how far can a wall be pushed), this includes walls")
    public static int MAX_RADIUS = 15;

    public static final int ROOM_SPACING = 1024;
    public static final int ROOM_MAX_SIZE = 256;
    public static final int ROOM_OFFSET_X = 8;
    public static final int ROOM_OFFSET_Z = 8;
    public static final int ROOM_CENTER_Y = 128;

    private HashMap<Vector3i, Integer> posToSubRoomId = new HashMap<>();
    private HashSet<Vector3i> activePads = new HashSet<>();
    private ArrayList<SubRoomData> subRooms = new ArrayList<>();

    public static class SubRoomData {
        public Vector3i pos;

        public BlockPos center;

        public int upWall;
        public int downWall;
        public int northWall;
        public int southWall;
        public int eastWall;
        public int westWall;

        public boolean isInsideOrWall(BlockPos pos) {
            return pos.getX() >= center.getX() - westWall
                    && pos.getX() <= center.getX() + eastWall
                    && pos.getY() >= center.getY() - downWall
                    && pos.getY() <= center.getY() + upWall
                    && pos.getZ() >= center.getZ() - northWall
                    && pos.getZ() <= center.getZ() + southWall;
        }

        public boolean isInside(BlockPos pos) {
            return pos.getX() > center.getX() - westWall
                && pos.getX() < center.getX() + eastWall
                && pos.getY() > center.getY() - downWall
                && pos.getY() < center.getY() + upWall
                && pos.getZ() > center.getZ() - northWall
                && pos.getZ() < center.getZ() + southWall;
        }

        public boolean isInWall(BlockPos pos) {
            if (!isInsideOrWall(pos)) return false;
            return pos.getX() == center.getX() - westWall
                    || pos.getX() == center.getX() + eastWall
                    || pos.getY() == center.getY() - downWall
                    || pos.getY() == center.getY() + upWall
                    || pos.getZ() == center.getZ() - northWall
                    || pos.getZ() == center.getZ() + southWall;
        }

        public boolean isInWall(BlockPos pos, Direction wall) {
            if (!isInsideOrWall(pos)) return false;
            switch (wall) {
                case UP: return pos.getY() == center.getY() + upWall;
                case DOWN: return pos.getY() == center.getY() - downWall;
                case NORTH: return pos.getZ() == center.getZ() - northWall;
                case SOUTH: return pos.getZ() == center.getZ() + southWall;
                case EAST: return pos.getX() == center.getX() + eastWall;
                case WEST: return pos.getX() == center.getX() - westWall;
            }
            return false;
        }

        public boolean isOnlyInWall(BlockPos pos, Direction wall) {
            if (!isInsideOrWall(pos)) return false;
            for (Direction test : Direction.values())
                if ((test == wall) != isInWall(pos, test))
                    return false;
            return true;
        }

        public SubRoomData setPos(Vector3i pos) {
            this.pos = pos;
            return this;
        }

        public SubRoomData setCenter(BlockPos center) {
            this.center = center;
            return this;
        }

        public SubRoomData setRadius(int r, @Nullable Direction dir) {
            if (dir == null || dir == Direction.UP)
                upWall = r;
            if (dir == null || dir == Direction.DOWN)
                downWall = r;
            if (dir == null || dir == Direction.NORTH)
                northWall = r;
            if (dir == null || dir == Direction.SOUTH)
                southWall = r;
            if (dir == null || dir == Direction.EAST)
                eastWall = r;
            if (dir == null || dir == Direction.WEST)
                westWall = r;
            return this;
        }

        public int getWallOffset(Direction dir) {
            switch (dir) {
                case UP: return upWall;
                case DOWN: return downWall;
                case NORTH: return northWall;
                case SOUTH: return southWall;
                case EAST: return eastWall;
                case WEST: return westWall;
            }
            return 0;
        }

        public void setWallOffset(Direction dir, int newVal) {
            switch (dir) {
                case UP: upWall = newVal; return;
                case DOWN: downWall = newVal; return;
                case NORTH: northWall = newVal; return;
                case SOUTH: southWall = newVal; return;
                case EAST: eastWall = newVal; return;
                case WEST: westWall = newVal;
            }
        }

        public static SubRoomData fromNBT(CompoundNBT nbt) {
            SubRoomData out = new SubRoomData();
            out.upWall = nbt.getInt("Up");
            out.downWall = nbt.getInt("Down");
            out.northWall = nbt.getInt("North");
            out.southWall = nbt.getInt("South");
            out.eastWall = nbt.getInt("East");
            out.westWall = nbt.getInt("West");
            out.pos = new Vector3i(nbt.getInt("X"), nbt.getInt("Y"), nbt.getInt("Z"));
            out.center = new BlockPos(nbt.getInt("CenterX"), nbt.getInt("CenterY"), nbt.getInt("CenterZ"));
            return out;
        }

        public CompoundNBT toNBT() {
            CompoundNBT out = new CompoundNBT();
            out.putInt("Up", upWall);
            out.putInt("Down", downWall);
            out.putInt("North", northWall);
            out.putInt("South", southWall);
            out.putInt("East", eastWall);
            out.putInt("West", westWall);
            out.putInt("X", pos.getX());
            out.putInt("Y", pos.getY());
            out.putInt("Z", pos.getZ());
            out.putInt("CenterX", center.getX());
            out.putInt("CenterY", center.getY());
            out.putInt("CenterZ", center.getZ());
            return out;
        }
    }

    public SubRoomsManager(String suffix, int id, boolean client) {
        super(suffix, id, client, false);
        posToSubRoomId.put(new Vector3i(0, 0, 0), 0);
        subRooms.add(0, new SubRoomData().setPos(new Vector3i(0, 0, 0)).setCenter(getEyePos(id)).setRadius(DEFAULT_RADIUS, null));
    }

    public void activatePad(BlockPos pos) {
        activePads.add(pos);
        markDirty();
    }

    public void deactivatePad(BlockPos pos) {
        activePads.remove(pos);
        markDirty();
    }

    public int getWallOffset(int room, Direction wall) {
        return subRooms.get(room).getWallOffset(wall);
    }

    public static Optional<Pair<Integer, Integer>> getRoomIds(World world, BlockPos pos, boolean eye) {
        if (world.getDimensionKey() != WorldUtils.DimBagRiftKey) return Optional.empty();
        int x = pos.getX() - ROOM_OFFSET_X;
        int z = pos.getZ() - ROOM_OFFSET_Z;
        int half_room = ROOM_MAX_SIZE / 2;
        if (eye) {
            if ((x % ROOM_SPACING) == 0 && pos.getY() == ROOM_CENTER_Y && z == 0)
                return Optional.of(new Pair<>(x / ROOM_SPACING + 1, 0));
        } else if (((x + half_room) % ROOM_SPACING) <= ROOM_MAX_SIZE) {
            int id = (x + half_room) / ROOM_SPACING + 1;
            SubRoomsManager manager = getInstance(id);
            if (manager != null) {
                int guess = (z + half_room) / ROOM_SPACING;
                if (guess >= manager.subRooms.size()) return Optional.empty();
                if (manager.subRooms.get(guess).isInsideOrWall(pos))
                    return Optional.of(new Pair<>(id, guess));
            }
        }
        return Optional.empty();
    }

    public void tpTunnel(Entity entity, BlockPos portalPos) { //teleport an entity to the next room, the position of the portal determine the destination
        tunnel((ServerWorld)entity.world, portalPos, entity, false, false);
    }

    /**
     * function to tp an entity outside the bag (includes an option to immediately reequip the bag and remove the bag entity)
     * @param entity
     * @param reequipIfAble
     * @return
     */
    public boolean leaveBag(Entity entity, boolean reequipIfAble, @Nullable BlockPos pos, @Nullable RegistryKey<World> world) {
        //store the position inside the entity
        //tp out
        //if reequipIfAble is true, try to reequip the bag to the entity and remove the bag entity
        CompoundNBT nbt = NBTUtils.ensurePathExistence(entity.getPersistentData(), DimBag.MOD_ID, Integer.toString(getEyeId()), "previousPosition");
        nbt.putInt("X", entity.getPosition().getX());
        nbt.putInt("Y", entity.getPosition().getY());
        nbt.putInt("Z", entity.getPosition().getZ());
        if (pos == null || world == null)
            HolderData.execute(getEyeId(), holderData -> holderData.tpToHolder(entity));
        else
            WorldUtils.teleportEntity(entity, world, pos);
        //TODO: reequip code
        return true;
    }

    /**
     * function to tp an entity inside the bag (includes check for previous position and pads)
     * @param entity
     * @param checkForBag
     * @return true if the function succeeded
     */
    public Entity enterBag(Entity entity, boolean checkForBag, boolean checkForPads, boolean defaultToPreviousPos) {
        //if check for bag is true, might have to remove the bag from the inventory and drop it as an entity (if the inseption uprage ins't installed)
        //pad first
        //previous known position second
        //default eye tp last
        if (checkForBag && !UpgradeManager.isUpgradeInstalled(getEyeId(), "inception").isPresent()) {
            List<CuriosIntegration.ProxyItemStackModifier> res = CuriosIntegration.searchItem(entity, Bag.class, o->Bag.getEyeId(o) == getEyeId(), true);
            for (CuriosIntegration.ProxyItemStackModifier p : res) {
                BagEntity.spawn(entity.world, entity.getPosition(), p.get());
                p.set(ItemStack.EMPTY);
            }
        }
        BlockPos arrival = null;
        if (checkForPads) { //request the active pads for this entity, if found change the tp destination
            //TODO
            World w = WorldUtils.getRiftWorld();
            for (Vector3i pos : activePads) {
                TileEntity te = w.getTileEntity(new BlockPos(pos));
                if (!(te instanceof PadTileEntity)) {
                    deactivatePad(new BlockPos(pos));
                    continue;
                }
                if (((PadTileEntity)te).isValidEntity(entity)) { //should take this chance to clear invalid pads
                    arrival = new BlockPos(pos).add(0, 1, 0);
                    break;
                }
            }
        }
        if (arrival == null && defaultToPreviousPos) { //request the last known position of this entity inside this bag
            CompoundNBT bnbt = entity.getPersistentData().getCompound(DimBag.MOD_ID).getCompound(Integer.toString(getEyeId())).getCompound("previousPosition");
            if (!bnbt.isEmpty())
                arrival = new BlockPos(bnbt.getInt("X"), bnbt.getInt("Y"), bnbt.getInt("Z"));
        }
        if (arrival == null) { //default position if all previous position weren't found
            arrival = getEyePos(getEyeId()).add(0, 1, 0);
        }
        return WorldUtils.teleportEntity(entity, WorldUtils.DimBagRiftKey, arrival);
    }

    public Entity enterBag(Entity entity) {
        return enterBag(entity, true, true, true);
    }

    private static BlockPos calculateOutput(BlockPos tunnelIn, SubRoomData srdFrom, Direction direction, int deltaRoom, SubRoomData srdTarget) {
        switch (direction) {
            case UP: return new BlockPos(tunnelIn.getX(), ROOM_CENTER_Y - srdTarget.downWall, tunnelIn.getZ() + deltaRoom * ROOM_SPACING);
            case DOWN: return new BlockPos(tunnelIn.getX(), ROOM_CENTER_Y + srdTarget.upWall, tunnelIn.getZ() + deltaRoom * ROOM_SPACING);
            case NORTH: return new BlockPos(tunnelIn.getX(), tunnelIn.getY(), tunnelIn.getZ() + srdFrom.northWall + srdTarget.southWall + deltaRoom * ROOM_SPACING);
            case SOUTH: return new BlockPos(tunnelIn.getX(), tunnelIn.getY(), tunnelIn.getZ() - srdFrom.southWall - srdTarget.northWall + deltaRoom * ROOM_SPACING);
            case EAST: return new BlockPos(tunnelIn.getX() - srdFrom.eastWall - srdTarget.westWall, tunnelIn.getY(), tunnelIn.getZ() + deltaRoom * ROOM_SPACING);
            case WEST: return new BlockPos(tunnelIn.getX() + srdFrom.westWall + srdTarget.eastWall, tunnelIn.getY(), tunnelIn.getZ() + deltaRoom * ROOM_SPACING);
        }
        return BlockPos.ZERO;
    }

    private Direction wall(BlockPos pos, int room) { //test if a position is IN a wall, and if true, return the direction of the wall (north, south, east, west, up, down; null if not a wall)
        int x = pos.getX() -(getEyeId() - 1) * ROOM_SPACING;
        if (x == ROOM_OFFSET_X + getWallOffset(room, Direction.EAST)) return Direction.byLong(1, 0, 0);
        if (x == ROOM_OFFSET_X - getWallOffset(room, Direction.WEST)) return Direction.byLong(-1, 0, 0);
        int y = pos.getY();
        if (y == ROOM_CENTER_Y + getWallOffset(room, Direction.UP)) return Direction.byLong(0, 1, 0);
        if (y == ROOM_CENTER_Y - getWallOffset(room, Direction.DOWN)) return Direction.byLong(0, -1, 0);
        int z = pos.getZ() -room * ROOM_SPACING;
        if (z == ROOM_OFFSET_Z + getWallOffset(room, Direction.SOUTH)) return Direction.byLong(0, 0, 1);
        if (z == ROOM_OFFSET_Z - getWallOffset(room, Direction.NORTH)) return Direction.byLong(0, 0, -1);
        return null;
    }

    public static BlockPos getEyePos(int id) { return new BlockPos(ROOM_OFFSET_X + ((id - 1) * ROOM_SPACING), ROOM_CENTER_Y, ROOM_OFFSET_Z); }

    public int createSubRoom(World world, BlockPos targetRoom) {
        Integer room = posToSubRoomId.get(targetRoom);
        if (room == null) { //test if the room exists, do nothing with the value
            room = subRooms.size();
            posToSubRoomId.put(targetRoom, room); //create the double link
            BlockPos center = getEyePos(getEyeId()).add(0, 0, ROOM_SPACING * room);
            subRooms.add(room, new SubRoomData().setCenter(center).setPos(targetRoom).setRadius(DEFAULT_RADIUS_SUB_ROOMS, null));
            WorldUtils.buildRoom(world, center, DEFAULT_RADIUS_SUB_ROOMS, 0); //build the room
            markDirty();
        }
        return room;
    }

    public static boolean tunnel(ServerWorld world, BlockPos tunnel, Entity entity, boolean create, boolean destroy) { //create mode: build (if needed) a room and place a portal, !create mode: teleport the entity to the next portal
        Optional<Pair<Integer, Integer>> req = getRoomIds(world, tunnel, false);
        if (!req.isPresent()) return false;
        int id = req.get().getKey();
        int room = req.get().getValue();
        SubRoomsManager data = getInstance(id);
        if (data == null) return false;
        Vector3i coord = data.subRooms.get(room).pos; //virtual coordinates of the current room
        Direction wall = data.wall(tunnel, room); //which wall the tunnel was placed on
        if (wall == null) return false;
        BlockPos targetRoom = new BlockPos(coord.getX() + wall.getXOffset(), coord.getY() + wall.getYOffset(), coord.getZ() + wall.getZOffset()); //virtual coordinates of the targeted room
        Integer targetRoomId;
        if (create)
            targetRoomId = data.createSubRoom(world, targetRoom); //build the room (if necessary)
        else
            targetRoomId = data.posToSubRoomId.get(targetRoom);
        if (targetRoomId == null)
            return false;
        SubRoomData targetSrd = data.subRooms.get(targetRoomId);
        BlockPos output = calculateOutput(tunnel, data.subRooms.get(room), wall, targetRoomId - room, targetSrd); //calculate the position of the output portal
        if (!targetSrd.isOnlyInWall(output, wall.getOpposite()))
            return false;
        if (create)
            world.setBlockState(output, Registries.getBlock(Tunnel.NAME).getDefaultState()); //put the tunnel on the targeted room
        else if (destroy)
            world.setBlockState(output, Registries.getBlock(Wall.NAME).getDefaultState()); //destroy the tunnel
        else {
            output = output.offset(wall);
            if (entity instanceof PlayerEntity && world.getBlockState(output.offset(Direction.DOWN)).getBlock() == Blocks.AIR) //special code to try to keep the tunnel at eye level of a player on teleport
                output = output.offset(Direction.DOWN);
            WorldUtils.teleportEntity(entity, world.getDimensionKey(), output); //teleport the entity next to the portal
        }
        return true;
    }

    public static boolean pushWall(ServerWorld world, BlockPos pos) {
        Optional<Pair<Integer, Integer>> req = getRoomIds(world, pos, false);
        if (!req.isPresent()) return false;
        int id = req.get().getKey();
        SubRoomsManager data = getInstance(id);
        if (data == null) return false;
        int room = req.get().getValue();
        Direction wall = data.wall(pos, room);
        if (wall == null) return false;
        SubRoomData srd = data.subRooms.get(room);
        int off = srd.getWallOffset(wall);
        if (off >= MAX_RADIUS) return false;
        WorldUtils.pushWall(world, pos, wall);
        srd.setWallOffset(wall, off + 1);
        data.markDirty();
        return true;
    }

    public static int getEyeId(World world, BlockPos pos, boolean eye) {
        return getRoomIds(world, pos, eye).orElse(new Pair<>(0, 0)).getKey();
    }

    @Override
    public void read(CompoundNBT nbt) {
        ListNBT listSubRooms = nbt.getList("SubRooms", 10);
        subRooms = new ArrayList<>();
        posToSubRoomId = new HashMap<>();
        activePads = new HashSet<>();
        for (int i = 0; i < listSubRooms.size(); ++i) {
            CompoundNBT entry = listSubRooms.getCompound(i);
            subRooms.add(i, SubRoomData.fromNBT(entry));
            posToSubRoomId.put(subRooms.get(i).pos, i);
        }
        for (INBT sn : nbt.getList("ActivePads", 10))
            activePads.add(new Vector3i(((CompoundNBT)sn).getInt("X"), ((CompoundNBT)sn).getInt("Y"), ((CompoundNBT)sn).getInt("Z")));
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        ListNBT listSubRooms = new ListNBT();
        for (SubRoomData li : subRooms)
            listSubRooms.add(li.toNBT());
        nbt.put("SubRooms", listSubRooms);
        ListNBT pads = new ListNBT();
        for (Vector3i pad : activePads) {
            CompoundNBT entry = new CompoundNBT();
            entry.putInt("X", pad.getX());
            entry.putInt("Y", pad.getY());
            entry.putInt("Z", pad.getZ());
            pads.add(entry);
        }
        nbt.put("ActivePads", pads);
        return nbt;
    }

    static public SubRoomsManager getInstance(int id) {
        return WorldSavedDataManager.getInstance(SubRoomsManager.class, null, id);
    }

    static public <T> T execute(int id, Function<SubRoomsManager, T> executable, T onErrorReturn) {
        return WorldSavedDataManager.execute(SubRoomsManager.class, null, id, executable, onErrorReturn);
    }

    static public boolean execute(int id, Consumer<SubRoomsManager> executable) {
        return WorldSavedDataManager.execute(SubRoomsManager.class, null, id, data->{executable.accept(data); return true;}, false);
    }
}
