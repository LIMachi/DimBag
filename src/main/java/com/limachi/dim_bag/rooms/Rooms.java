package com.limachi.dim_bag.rooms;

import com.limachi.dim_bag.Constants;
import com.limachi.dim_bag.blocks.WallBlock;
import com.limachi.dim_bag.saveData.BagIdManager;
import com.limachi.lim_lib.Configs;
import com.limachi.lim_lib.Log;
import com.limachi.lim_lib.World;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;
import java.util.function.Predicate;

public class Rooms {
    @Configs.Config(min = "3", max = "126", cmt = "default radius of the main room inside a bag, this includes the walls")
    public static int DEFAULT_RADIUS = 3;
    @Configs.Config(min = "2", max = "126", cmt = "default radius of a sub room (created with a tunnel), this includes the walls")
    public static int DEFAULT_RADIUS_SUB_ROOMS = 2;
    @Configs.Config(min = "3", max = "126", cmt = "maximum size of a room/subroom (how far can walls be pushed), this includes walls")
    public static int MAX_RADIUS = 15;

    public static final int ROOM_SPACING = 1024;
    public static final int ROOM_MAX_SIZE = 256;
    public static final int HALF_ROOM = ROOM_MAX_SIZE / 2;
    public static final int ROOM_OFFSET_X = 8;
    public static final int ROOM_OFFSET_Z = 8;
    public static final int ROOM_CENTER_Y = 128;

    public static boolean validateRoomOwnership(Player player, int id, int room) {
        Log.warn("need implementation");
        return true; //FIXME
    }

    public static Optional<Pair<Integer, Integer>> getRoomIds(Level world, BlockPos pos, boolean eye, boolean includeOutside) {
        if (world == null || pos == null || world.dimension() != Constants.BAG_DIM) return Optional.empty();
        int x = pos.getX() - ROOM_OFFSET_X;
        int z = pos.getZ() - ROOM_OFFSET_Z;
        int id = (x + HALF_ROOM) / ROOM_SPACING + 1;
        int guess = (z + HALF_ROOM) / ROOM_SPACING;
        if (includeOutside)
            return Optional.of(new Pair<>(id < 1 ? 1 : id, guess < 0 ? 0 : guess));
        if (eye) {
            if ((x % ROOM_SPACING) == 0 && pos.getY() == ROOM_CENTER_Y && z == 0)
                return Optional.of(new Pair<>(x / ROOM_SPACING + 1, 0));
        } else if (((x + HALF_ROOM) % ROOM_SPACING) <= ROOM_MAX_SIZE) {
            RoomsData manager = RoomsData.getInstance(id);
            if (manager != null) {
                if (guess >= manager.getSubRooms().size()) return Optional.empty();
                if (manager.getSubRooms().get(guess).isInsideOrWall(pos))
                    return Optional.of(new Pair<>(id, guess));
            }
        }
        return Optional.empty();
    }

    public static BlockPos getRoomCenter(int id, int subRoom) {
        return new BlockPos(ROOM_OFFSET_X + (id - 1) * ROOM_SPACING, ROOM_CENTER_Y, ROOM_OFFSET_Z + subRoom * ROOM_SPACING);
    }

    private static final Pair<Integer, Integer> INVALID_ROOM_PAIR = new Pair<>(0, 0);

    public static int getbagId(Level level, BlockPos pos, boolean eye) {
        return getRoomIds(level, pos, eye, false).orElse(INVALID_ROOM_PAIR).getFirst();
    }

    public static int getClosestBag(Level level, BlockPos pos) {
        int close = getRoomIds(level, pos, false, true).orElse(INVALID_ROOM_PAIR).getFirst();
        int last = BagIdManager.getLastId();
        return last < close ? last : close;
    }

    public static void buildRoom(Level level, SubRoom room, int slots, int tanks, int batteries) {
        BlockState wall = WallBlock.R_BLOCK.get().defaultBlockState();
        for (int x = room.center.getX() - room.west; x <= room.center.getX() + room.east; ++x)
            for (int y = room.center.getY() - room.down; y <= room.center.getY() + room.up; ++y)
                for (int z = room.center.getZ() - room.north; z <= room.center.getZ() + room.south; ++z) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (room.isInWall(pos))
                        level.setBlock(pos, wall, 2);
                    else {
                        if (slots > 0) {
                            //FIXME
                            --slots;
                        } else if (tanks > 0) {
                            //FIXME
                            --tanks;
                        } else if (batteries > 0) {
                            //FIXME
                            --batteries;
                        }
                    }
                }
    }

    /**
     * search the next valid coordinates to place a module inside the main room of the bag (for now not compatible with subrooms)
     * return null if no space is left
     */
    public static BlockPos getNewModulePlacement(int id) {
        SubRoom room = new SubRoom(id, 5, 0, new Vec3i(0, 0, 0)); //FIXME
        Level level = World.getLevel(Constants.BAG_DIM);
        for (Direction wall : Direction.values()) {
            BlockPos p = spiralScan(level, room, wall);
            if (p != null)
                return p;
        }
        return null;
    }

    private static BlockPos spiralScan(Level level, SubRoom room, Direction wall) {
        BlockPos p = room.center.relative(wall, room.getWallOffset(wall) - 1);
        Direction d = wall.getAxis() == Direction.Axis.Y ? Direction.NORTH : Direction.UP;
        int limit = 1;
        int step = 0;
        boolean increase = false;
        while (!room.isInWall(p.relative(d))) {
            if (level.getBlockState(p).is(Blocks.AIR)) return p;
            p = p.relative(d);
            if (room.isInWall(p.relative(d))) return null;
            if (++step >= limit) {
                step = 0;
                if (!(increase ^= true))
                    ++limit;
                d = d.getClockWise(wall.getAxis());
            }
        }
        return null;
    }
}
