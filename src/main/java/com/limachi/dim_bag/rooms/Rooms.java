package com.limachi.dim_bag.rooms;

import com.limachi.dim_bag.Constants;
import com.limachi.dim_bag.saveData.BagIdManager;
import com.limachi.lim_lib.Configs;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.Optional;

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

    private static final Pair<Integer, Integer> INVALID_ROOM_PAIR = new Pair<>(0, 0);

    public static int getbagId(Level level, BlockPos pos, boolean eye) {
        return getRoomIds(level, pos, eye, false).orElse(INVALID_ROOM_PAIR).getFirst();
    }

    public static int getClosestBag(Level level, BlockPos pos) {
        int close = getRoomIds(level, pos, false, true).orElse(INVALID_ROOM_PAIR).getFirst();
        int last = BagIdManager.getLastId();
        return last < close ? last : close;
    }
}
