package com.limachi.dimensional_bags.common.data.EyeDataMK2;

import com.limachi.dimensional_bags.CuriosIntegration;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.ConfigManager.Config;
import com.limachi.dimensional_bags.common.blocks.BagGateway;
import com.limachi.dimensional_bags.common.blocks.Wall;
import com.limachi.dimensional_bags.utils.NBTUtils;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.utils.WorldUtils;
import com.limachi.dimensional_bags.common.blocks.Tunnel;
import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import com.limachi.dimensional_bags.common.tileentities.PadTileEntity;
import javafx.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
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

    @Config(min = "3", max = "126", cmt = "default radius of a main room inside a bag, this includes the walls")
    public static int DEFAULT_RADIUS = 3;

    @Config(min = "2", max = "126", cmt = "default radius of a sub room (created with a tunnel), this includes the walls")
    public static int DEFAULT_RADIUS_SUB_ROOMS = 2;

    @Config(min = "3", max = "126", cmt = "maximum size of a room/subroom (how far can a wall be pushed), this includes walls")
    public static int MAX_RADIUS = 15;

    public static final int ROOM_SPACING = 1024;
    public static final int ROOM_MAX_SIZE = 256;
    public static final int HALF_ROOM = ROOM_MAX_SIZE / 2;
    public static final int ROOM_OFFSET_X = 8;
    public static final int ROOM_OFFSET_Z = 8;
    public static final int ROOM_CENTER_Y = 128;

    private HashMap<Vector3i, Integer> posToSubRoomId = new HashMap<>();
    private int selectedPad = -1;
    private ArrayList<Vector3i> activePads = new ArrayList<>();
    private ArrayList<SubRoomData> subRooms = new ArrayList<>();

    public static class SubRoomData {
        public Vector3i pos;

        public BlockPos center;

        public int upWall; // + Y
        public int downWall; // - Y
        public int northWall; // - Z
        public int southWall; // + Z
        public int eastWall; // + X
        public int westWall; // - X

        //corners are generated for a traversal using only positive increment in X, Y and Z

        //lowest corner on all axis, including offset
        public BlockPos getWallCorner1(Direction dir, int depthOffset, int areaOffset) {
            switch (dir) {
                case UP: return new BlockPos(center.getX() - westWall - areaOffset, center.getY() + upWall + depthOffset, center.getZ() - northWall - areaOffset);
                case DOWN: return new BlockPos(center.getX() - westWall - areaOffset, center.getY() - downWall - depthOffset, center.getZ() - northWall - areaOffset);
                case NORTH: return new BlockPos(center.getX() - westWall - areaOffset, center.getY() - downWall - areaOffset, center.getZ() - northWall - depthOffset);
                case SOUTH: return new BlockPos(center.getX() - westWall - areaOffset, center.getY() - downWall - areaOffset, center.getZ() + southWall + depthOffset);
                case EAST: return new BlockPos(center.getX() + eastWall + depthOffset, center.getY() - downWall - areaOffset, center.getZ() - northWall - areaOffset);
                case WEST: return new BlockPos(center.getX() - westWall - depthOffset, center.getY() - downWall - areaOffset, center.getZ() - northWall - areaOffset);
            }
            return new BlockPos(0, 0, 0);
        }

        //highest corner on all axis, including offset
        public BlockPos getWallCorner2(Direction dir, int depthOffset, int areaOffset) {
            switch (dir) {
                case UP: return new BlockPos(center.getX() + eastWall + areaOffset, center.getY() + upWall + depthOffset, center.getZ() + southWall + areaOffset);
                case DOWN: return new BlockPos(center.getX() + eastWall + areaOffset, center.getY() - downWall - depthOffset, center.getZ() + southWall + areaOffset);
                case NORTH: return new BlockPos(center.getX() + eastWall + areaOffset, center.getY() + upWall + areaOffset, center.getZ() - northWall - depthOffset);
                case SOUTH: return new BlockPos(center.getX() + eastWall + areaOffset, center.getY() + upWall + areaOffset, center.getZ() + southWall + depthOffset);
                case EAST: return new BlockPos(center.getX() + eastWall + depthOffset, center.getY() + upWall + areaOffset, center.getZ() + southWall + areaOffset);
                case WEST: return new BlockPos(center.getX() - westWall - depthOffset, center.getY() + upWall + areaOffset, center.getZ() + southWall + areaOffset);
            }
            return new BlockPos(0, 0, 0);
        }

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

        public boolean isInGateway(BlockPos pos) {
            if (isInsideOrWall(pos)) return false;
            return pos.getX() >= center.getX() - westWall - 1
                    && pos.getX() <= center.getX() + eastWall + 1
                    && pos.getY() >= center.getY() - downWall - 1
                    && pos.getY() <= center.getY() + upWall + 1
                    && pos.getZ() >= center.getZ() - northWall - 1
                    && pos.getZ() <= center.getZ() + southWall + 1;
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
        if (activePads.size() == 1) //first active pad
            selectedPad = 0;
        markDirty();
    }

    public void deactivatePad(BlockPos pos) {
        boolean reselect = selectedPad != -1 && activePads.get(selectedPad).equals(pos);
        activePads.remove(pos);
        if (reselect)
            selectOtherPad(true);
        else
            markDirty();
    }

    public PadTileEntity getSelectedPad() {
        if (selectedPad == -1) return null;
        World w = WorldUtils.getRiftWorld();
        if (w == null) return null;
        TileEntity te = w.getTileEntity(new BlockPos(activePads.get(selectedPad)));
        if (!(te instanceof PadTileEntity)) return null;
        return (PadTileEntity)te;
    }

    public void selectOtherPad(boolean previous) {
        if (activePads.isEmpty()) selectedPad = -1;
        else {
            selectedPad += (previous ? -1 : 1);
            if (selectedPad < 0)
                selectedPad = activePads.size() - 1;
            if (selectedPad >= activePads.size())
                selectedPad = 0;
        }
        markDirty();
    }

    public int getWallOffset(int room, Direction wall) {
        return subRooms.get(room).getWallOffset(wall);
    }

    public boolean isWall(int room, BlockPos pos) { return subRooms.get(room).isInWall(pos); }

    public boolean isGateway(int room, BlockPos pos) { return subRooms.get(room).isInGateway(pos); }

    public static boolean isWall(World world, BlockPos pos) {
        Optional<Pair<Integer, Integer>> r = SubRoomsManager.getRoomIds(world, pos, false, false);
        return r.isPresent() && SubRoomsManager.getInstance(r.get().getKey()).isWall(r.get().getValue(), pos);
    }

    public static boolean isGateway(World world, BlockPos pos) {
        Optional<Pair<Integer, Integer>> r = SubRoomsManager.getRoomIds(world, pos, false, false);
        return r.isPresent() && SubRoomsManager.getInstance(r.get().getKey()).isGateway(r.get().getValue(), pos);
    }

    public boolean isInRoom(BlockPos pos, int room) {
        int x = pos.getX() - ROOM_OFFSET_X;
        if ((x + HALF_ROOM) / ROOM_SPACING + 1 != getEyeId()) return false;
        int z = pos.getZ() - ROOM_OFFSET_Z;
        int guess = (z + HALF_ROOM) / ROOM_SPACING;
        if (guess >= subRooms.size() || (room >= 0 && room != guess)) return false;
        return subRooms.get(guess).isInsideOrWall(pos);
    }

    public static Optional<Pair<Integer, Integer>> getRoomIds(World world, BlockPos pos, boolean eye, boolean includeOutside) {
        if (world.getDimensionKey() != WorldUtils.DimBagRiftKey) return Optional.empty();
        int x = pos.getX() - ROOM_OFFSET_X;
        int z = pos.getZ() - ROOM_OFFSET_Z;
        if (eye) {
            if ((x % ROOM_SPACING) == 0 && pos.getY() == ROOM_CENTER_Y && z == 0)
                return Optional.of(new Pair<>(x / ROOM_SPACING + 1, 0));
        } else if (((x + HALF_ROOM) % ROOM_SPACING) <= ROOM_MAX_SIZE) {
            int id = (x + HALF_ROOM) / ROOM_SPACING + 1;
            SubRoomsManager manager = getInstance(id);
            if (manager != null) {
                int guess = (z + HALF_ROOM) / ROOM_SPACING;
                if (guess >= manager.subRooms.size()) return Optional.empty();
                if (includeOutside || manager.subRooms.get(guess).isInsideOrWall(pos))
                    return Optional.of(new Pair<>(id, guess));
            }
        }
        return Optional.empty();
    }

    public void tpTunnel(Entity entity, BlockPos portalPos) { //teleport an entity to the next room, the position of the portal determine the destination
        tunnel((ServerWorld)entity.world, portalPos, entity, false, false, null);
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
    public Entity enterBag(Entity entity, boolean checkForBag, boolean checkForPads, boolean defaultToPreviousPos, boolean requirePad) {
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
            if (w != null)
                for (Vector3i pos : activePads) {
                    TileEntity te = w.getTileEntity(new BlockPos(pos));
                    if (!(te instanceof PadTileEntity)) {
                        deactivatePad(new BlockPos(pos));
                        continue;
                    }
                    if (((PadTileEntity)te).isValidEntity(entity)) { //should take this chance to clear invalid pads
                        arrival = new BlockPos(pos).up();
                        break;
                    }
                }
            if (requirePad)
                return arrival != null ? WorldUtils.teleportEntity(entity, WorldUtils.DimBagRiftKey, arrival) : null;
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
        if (entity instanceof MobEntity)
            ((MobEntity)entity).enablePersistence();
        return enterBag(entity, true, true, true, false);
    }

    public Entity captureEntity(Entity entity) {
        if (entity instanceof MobEntity)
            ((MobEntity)entity).enablePersistence();
        return enterBag(entity, true, true, false, true);
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
            WorldUtils.buildRoom(world, center, DEFAULT_RADIUS_SUB_ROOMS); //build the room
            markDirty();
        }
        return room;
    }

    //get all wall blocks positions that need an overlay (telling where the next room is to place tunnels)
    public static List<Pair<BlockPos, Boolean>> collectPlacerOverlays(ClientPlayerEntity player, ItemStack tunnel) {
        List<Pair<BlockPos, Boolean>> out = new ArrayList<>();
        Pair<Integer, Integer> pr = SubRoomsManager.getRoomIds(player.world, player.getPosition(), false, false).orElse(new Pair<>(0, 0));
        if (pr.getKey() == 0) return out;
        SubRoomsManager data = SubRoomsManager.getInstance(pr.getKey());
        if (data == null) return out;
        Vector3i coord = data.subRooms.get(pr.getValue()).pos; //virtual coordinates of the current room
        SubRoomData srdr = data.subRooms.get(pr.getValue());
        CompoundNBT nbt = tunnel.getTag();
        int nid = NBTUtils.getOrDefault(nbt, "Eye", -1);
        int room1 = NBTUtils.getOrDefault(nbt, "Room1", -1);
        int room2 = NBTUtils.getOrDefault(nbt, "Room2", -1);
        boolean forceFalse = (nid != -1 && room1 != -1 && room2 != -1 && (nid != pr.getKey() || !(room1 == pr.getValue() || room2 == pr.getValue())));
        for (int x = player.getPosition().getX() - 6; x <= player.getPosition().getX() + 6; ++x)
            for (int y = player.getPosition().getY() - 5; y <= player.getPosition().getY() + 8; ++y)
                for (int z = player.getPosition().getZ() - 6; z <= player.getPosition().getZ() + 6; ++z) {
                    BlockPos tested = new BlockPos(x, y, z);
                    if (srdr.isInWall(tested) && !(player.world.getBlockState(tested).getBlock() instanceof Tunnel)) {
                        boolean valid = false;
                        if (!forceFalse) {
                            Direction dir = data.wall(tested, pr.getValue()); //which wall the tunnel was placed on
                            if (dir != null) {
                                BlockPos targetRoom = new BlockPos(coord.getX() + dir.getXOffset(), coord.getY() + dir.getYOffset(), coord.getZ() + dir.getZOffset()); //virtual coordinates of the targeted room
                                Integer targetRoomId = data.posToSubRoomId.get(targetRoom);
                                SubRoomData targetSrd;
                                if (targetRoomId == null) {
                                    targetRoomId = data.subRooms.size();
                                    BlockPos center = getEyePos(pr.getKey()).add(0, 0, ROOM_SPACING * targetRoomId);
                                    targetSrd = new SubRoomData().setCenter(center).setPos(targetRoom).setRadius(DEFAULT_RADIUS_SUB_ROOMS, null);
                                } else
                                    targetSrd = data.subRooms.get(targetRoomId);
                                if (!(room1 != -1 && room2 != -1 && !(room1 == targetRoomId || room2 == targetRoomId)))
                                    if (targetSrd.isOnlyInWall(calculateOutput(tested, srdr, dir, targetRoomId - pr.getValue(), targetSrd), dir.getOpposite()))
                                        valid = true;
                            }
                        }
                        out.add(new Pair<>(new BlockPos(x, y, z), valid));
                    }
                }
        return out;
    }

    public static boolean tunnel(ServerWorld world, BlockPos tunnel, Entity entity, boolean create, boolean destroy, @Nullable CompoundNBT nbt) { //create mode: build (if needed) a room and place a portal, !create mode: teleport the entity to the next portal
        Optional<Pair<Integer, Integer>> req = getRoomIds(world, tunnel, false, false);
        if (!req.isPresent()) return false;
        int id = req.get().getKey();
        int room = req.get().getValue();
        int nid = NBTUtils.getOrDefault(nbt, "Eye", -1);
        int room1 = NBTUtils.getOrDefault(nbt, "Room1", -1);
        int room2 = NBTUtils.getOrDefault(nbt, "Room2", -1);
        if (nid != -1 && room1 != -1 && room2 != -1 && (nid != id || !(room1 == room || room2 == room))) return false; //nid, room1, room2 are valid, but either the bag or the current room is invalid
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
        if (room1 != -1 && room2 != -1 && !(room1 == targetRoomId || room2 == targetRoomId)) return false; //room1, room2 are valid, but the target room is invalid
        SubRoomData targetSrd = data.subRooms.get(targetRoomId);
        BlockPos output = calculateOutput(tunnel, data.subRooms.get(room), wall, targetRoomId - room, targetSrd); //calculate the position of the output portal
        if (!targetSrd.isOnlyInWall(output, wall.getOpposite()))
            return false;
        if (nbt != null) {
            nbt.putInt("Eye", id);
            nbt.putInt("Room1", room);
            nbt.putInt("Room2", targetRoomId);
        }
        if (create)
            WorldUtils.replaceBlockAndGiveBack(output, Registries.getBlock(Tunnel.NAME), (PlayerEntity)entity);
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

    public static void iterateBlockPos(BlockPos start, BlockPos end, Consumer<BlockPos> consumer) {
        for (int x = start.getX(); x <= end.getX(); ++x)
            for (int y = start.getY(); y <= end.getY(); ++y)
                for (int z = start.getZ(); z <= end.getZ(); ++z)
                    consumer.accept(new BlockPos(x, y, z));
    }

    public static boolean pushWall(ServerWorld world, BlockPos pos) {
        Optional<Pair<Integer, Integer>> req = getRoomIds(world, pos, false, false);
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
        BlockState airState = Blocks.AIR.getDefaultState();
        BlockState gatewayState = Registries.getBlock(BagGateway.NAME).getDefaultState();
        /**first we clone walls*/
        iterateBlockPos(srd.getWallCorner1(wall, 0, 0),srd.getWallCorner2(wall, 0, 0), p->world.setBlockState(p.add(wall.getDirectionVec()), world.getBlockState(p)));
        /**second we carve inside the old walls*/
        iterateBlockPos(srd.getWallCorner1(wall, 0, -1),srd.getWallCorner2(wall, 0, -1), p->world.setBlockState(p, airState));
        /**third we add the new gateways*/
        iterateBlockPos(srd.getWallCorner1(wall, 2, 1),srd.getWallCorner2(wall, 2, 1), p->world.setBlockState(p, gatewayState));
        srd.setWallOffset(wall, off + 1);
        data.markDirty();
        return true;
    }

    public static int getEyeId(World world, BlockPos pos, boolean eye) {
        return getRoomIds(world, pos, eye, false).orElse(new Pair<>(0, 0)).getKey();
    }

    @Override
    public void read(CompoundNBT nbt) {
        ListNBT listSubRooms = nbt.getList("SubRooms", 10);
        subRooms = new ArrayList<>();
        posToSubRoomId = new HashMap<>();
        activePads = new ArrayList<>();
        selectedPad = nbt.getInt("SelectedPad");
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
        nbt.putInt("SelectedPad", selectedPad);
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
