package com.limachi.dimensional_bags.common.data.EyeDataMK2;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.WorldUtils;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import javafx.util.Pair;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class SubRoomsManager extends WorldSavedDataManager.EyeWorldSavedData {
    private HashMap<Vector3i, Integer> subRoomsToId = new HashMap<>();
    private ArrayList<Vector3i> idToSubRooms = new ArrayList<>();
    private int radius;
//    private final int id;

    public SubRoomsManager(String suffix, int id, boolean client) {
        super(suffix, id, client);
//        super(DimBag.MOD_ID + "_eye_" + id + "_sub_rooms_manager");
//        this.id = id;
        subRoomsToId.put(new Vector3i(0, 0, 0), 0);
        idToSubRooms.add(0, new Vector3i(0, 0, 0));
        radius = UpgradeManager.getUpgrade("upgrade_radius").getStart();
    }

    public static Optional<Pair<Integer, Integer>> getRoomIds(World world, BlockPos pos, boolean eye) {
        if (world.isRemote || world.getDimensionKey() != WorldUtils.DimBagRiftKey) return Optional.empty();
        int x = pos.getX() - 8; //since all rooms are offset by 8 blocks (so the center of a room is approximately the center of a chunk), we offset back X and Z by 8
        int z = pos.getZ() - 8;
        if (eye) {
            if ((x & 1023) == 0 && pos.getY() == 128 && z == 0) //x & 1023 is the same as x % 1024 (basic binary arithmetic)
                return Optional.of(new Pair<>((x >> 10) + 1, 0)); //x >> 10 is the same as x / 1024
        }
        else if (((x + 128) & 1023) <= 256) { //we add 128, so the range [-128, 128], becomes [0, 256], the maximum size of a room + extra
            int id = ((x + 128) >> 10) + 1; //since the block is now in a range [0, 256] + unknown * 1024, we cam divide by 1024 and the [0, 256] part will be discarded by the int precision, also, using a shift instead of a division
            SubRoomsManager manager = getInstance(null, id);
            if (manager != null) {
                int radius = manager.getRadius();
                if (((x + radius) & 1023) <= radius << 1 && pos.getY() >= 128 - radius && pos.getY() <= 128 + radius && ((z + radius) & 1023) <= radius << 1) //now that we know the radius, we can quickly test the room on the X and Z axis
                    return Optional.of(new Pair<>(id, (z + 128) >> 10)); //same trick as for the id, but rooms start at 0 (0 = center room, where the eye resides)
        }
        }
        return Optional.empty();
    }

    public void tpTunnel(Entity entity, BlockPos portalPos) { //teleport an entity to the next room, the position of the portal determine the destination
        tunnel((ServerWorld)entity.world, portalPos, entity, false, false);
    }

    public void tpIn(Entity entity) { //teleport an entity to the eye of the bag
        WorldUtils.teleportEntity(entity, WorldUtils.DimBagRiftKey, new BlockPos(1024 * (getEyeId() - 1) + 8, 129, 8));
    }

    private static BlockPos calculateOutput(BlockPos tunnelIn, BlockPos roomCenter, Direction direction, int deltaRoom) {
        return new BlockPos(tunnelIn.getX() + (-Math.abs(direction.getXOffset()) * 2 * (tunnelIn.getX() - roomCenter.getX())),
                tunnelIn.getY() + (-Math.abs(direction.getYOffset()) * 2 * (tunnelIn.getY() - roomCenter.getY())),
                tunnelIn.getZ() + (-Math.abs(direction.getZOffset()) * 2 * (tunnelIn.getZ() - roomCenter.getZ())) + deltaRoom * 1024);
    }

    private Direction wall(BlockPos pos, int room) { //test if a position is IN a wall, and if true, return the direction of the wall (north, south, east, west, up, down; null if not a wall)
        int x = pos.getX() -(getEyeId() - 1) * 1024;
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

    public static BlockPos getEyePos(int id) { return new BlockPos(8 + ((id - 1) << 10), 128, 8); }

    public int createSubRoom(World world, BlockPos targetRoom) {
        Integer room = subRoomsToId.get(targetRoom);
        if (room == null) { //test if the room exists, do nothing with the value
            room = idToSubRooms.size();
            subRoomsToId.put(targetRoom, room); //create the double link
            idToSubRooms.add(room, targetRoom);
            WorldUtils.buildRoom(world, getEyePos(getEyeId()).add(0, 0, room << 10), radius, 0); //build the room
            markDirty();
        }
        return room;
    }

    public static void tunnel(ServerWorld world, BlockPos tunnel, Entity entity, boolean create, boolean destroy) { //create mode: build (if needed) a room and place a portal, !create mode: teleport the entity to the next portal
        Optional<Pair<Integer, Integer>> req = getRoomIds(world, tunnel, false);
        if (!req.isPresent()) return;
        int id = req.get().getKey();
        int room = req.get().getValue();
        SubRoomsManager data = getInstance(world, id);
        if (data == null) return;
        Vector3i coord = data.idToSubRooms.get(room); //virtual coordinates of the current room
        Direction wall = data.wall(tunnel, room); //which wall the tunnel was placed on
        if (wall == null) return ;
        BlockPos targetRoom = new BlockPos(coord.getX() + wall.getXOffset(), coord.getY() + wall.getYOffset(), coord.getZ() + wall.getZOffset()); //virtual coordinates of the targeted room
        Integer targetRoomId;
        if (create)
            targetRoomId = data.createSubRoom(world, targetRoom); //build the room (if necessary)
        else
            targetRoomId = data.subRoomsToId.get(targetRoom);
        BlockPos output = calculateOutput(tunnel, getEyePos(id).add(0, 0, room << 10), wall, targetRoomId - room); //calculate the position of the output portal
        if (create)
            world.setBlockState(output, Registries.TUNNEL_BLOCK.get().getDefaultState()); //put the tunnel on the targeted room
        else if (destroy)
            world.setBlockState(output, Registries.WALL_BLOCK.get().getDefaultState()); //destroy the tunnel
        else
            WorldUtils.teleportEntity(entity, world.getDimensionKey(), wall == Direction.DOWN ? output.offset(wall, 2) : output.offset(wall)); //teleport the entity next to the portal
    }

    public int getRadius() { return radius; }

    public void changeRadius(int newRadius) {
        for (int i = 0; i < idToSubRooms.size(); ++i)
            WorldUtils.buildRoom(WorldUtils.getRiftWorld(), getEyePos(getEyeId()).add(0, 0, i << 10), newRadius, radius);
        radius = newRadius;
        markDirty();
    }

    public static int getEyeId(World world, BlockPos pos, boolean eye) {
        return getRoomIds(world, pos, eye).orElse(new Pair<>(0, 0)).getKey();
    }

    @Override
    public void read(CompoundNBT nbt) {
        radius = nbt.getInt("Radius");
        ListNBT listSubRooms = nbt.getList("SubRooms", 10);
        idToSubRooms = new ArrayList<>();
        subRoomsToId = new HashMap<>();
        for (int i = 0; i < listSubRooms.size(); ++i) {
            CompoundNBT entry = listSubRooms.getCompound(i);
            Vector3i room = new Vector3i(entry.getInt("X"), entry.getInt("Y"), entry.getInt("Z"));
            idToSubRooms.add(i, room);
            subRoomsToId.put(room, i);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        nbt.putInt("Radius", radius);
        ListNBT listSubRooms = new ListNBT();
        for (Vector3i li : idToSubRooms) {
            CompoundNBT entry = new CompoundNBT();
            entry.putInt("X", li.getX());
            entry.putInt("Y", li.getY());
            entry.putInt("Z", li.getZ());
            listSubRooms.add(entry);
        }
        nbt.put("SubRooms", listSubRooms);
        return nbt;
    }

//    static public SubRoomsManager getInstance(@Nullable ServerWorld world, int id) {
//        if (id <= 0) return null;
//        if (world == null)
//            world = WorldUtils.getOverWorld();
//        if (world != null)
//            return world.getSavedData().getOrCreate(()->new SubRoomsManager(id), DimBag.MOD_ID + "_eye_" + id + "_sub_rooms_manager");
//        return null;
//    }

    static public SubRoomsManager getInstance(@Nullable ServerWorld world, int id) {
        return WorldSavedDataManager.getInstance(SubRoomsManager.class, world, id);
    }
}
