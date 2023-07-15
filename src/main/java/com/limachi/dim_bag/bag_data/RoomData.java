package com.limachi.dim_bag.bag_data;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.blocks.WallBlock;
import com.limachi.dim_bag.capabilities.entities.BagTP;
import com.limachi.lim_lib.World;
import com.limachi.lim_lib.capabilities.Cap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.CapabilityManager;

/*
public class RoomData extends BaseData {

    public RoomData(int bag) { super(bag, "room"); }

//    public static RoomData getRoom(BlockPos pos) {
//        int id = (pos.getX() / ROOM_SPACING) + 1;
//        if (id > 0 && id <= AllBagsData.maxBagId()) {
//            RoomData out = new RoomData(id);
//            if (out.isInRoom(pos))
//                return out;
//        }
//        return null;
//    }

    public static int getRoomId(BlockPos pos) {
        RoomData room = getRoom(pos);
        if (room != null)
            return room.id;
        return 0;
    }

    public static int getBag(BlockPos pos) {
        int id = (pos.getX() / ROOM_SPACING) + 1;
        if (id > 0 && id <= AllBagsData.maxBagId() && new RoomData(id).isInRoom(pos))
            return id;
        return 0;
    }

    private BlockPos minWalls = null;
    private BlockPos maxWalls = null;

    private void load() {
        minWalls = BlockPos.of(data().getLong("min_walls"));
        maxWalls = BlockPos.of(data().getLong("max_walls"));
    }

    protected boolean inRange(int v, int min, int max) { return v >= min && v <= max; }

    public boolean isWall(BlockPos pos) {
        load();
        return ((pos.getX() == minWalls.getX() || pos.getX() == maxWalls.getX()) && inRange(pos.getY(), minWalls.getY(), maxWalls.getY()) && inRange(pos.getZ(), minWalls.getZ(), maxWalls.getZ())) ||
                ((pos.getY() == minWalls.getY() || pos.getY() == maxWalls.getY()) && inRange(pos.getX(), minWalls.getX(), maxWalls.getX()) && inRange(pos.getZ(), minWalls.getZ(), maxWalls.getZ())) ||
                ((pos.getZ() == minWalls.getZ() || pos.getZ() == maxWalls.getZ()) && inRange(pos.getY(), minWalls.getY(), maxWalls.getY()) && inRange(pos.getX(), minWalls.getX(), maxWalls.getX()));
    }

    public boolean isInRoom(BlockPos pos) {
        load();
        return inRange(pos.getX(), minWalls.getX(), maxWalls.getX()) && inRange(pos.getY(), minWalls.getY(), maxWalls.getY()) && inRange(pos.getZ(), minWalls.getZ(), maxWalls.getZ());
    }

    public static BlockPos roomCenter(int id) { return new BlockPos(8 + (id - 1) * ROOM_SPACING, 128, 8); }

    public void initRoomData() {
        BlockPos center = roomCenter(getId());
        minWalls = center.offset(-DEFAULT_ROOM_RADIUS, -DEFAULT_ROOM_RADIUS, -DEFAULT_ROOM_RADIUS);
        maxWalls = center.offset(DEFAULT_ROOM_RADIUS, DEFAULT_ROOM_RADIUS, DEFAULT_ROOM_RADIUS);
        data().putLong("min_walls", minWalls.asLong());
        data().putLong("max_walls", maxWalls.asLong());
    }

    public void temporaryChunkLoad() {
        if (World.getLevel(DimBag.BAG_DIM) instanceof ServerLevel level) {
            load();
            for (int x = minWalls.getX(); x < maxWalls.getX(); x += 16)
                for (int z = minWalls.getZ(); z < maxWalls.getZ(); z += 16)
                    World.temporaryChunkLoad(level, new BlockPos(x, 128, z));
        }
    }

    public void build() {
        if (World.getLevel(DimBag.BAG_DIM) instanceof ServerLevel level) {
            load();
            BlockState wall = WallBlock.R_BLOCK.get().defaultBlockState();
            for (int x = minWalls.getX(); x <= maxWalls.getX(); ++x) {
                for (int z = minWalls.getZ(); z <= maxWalls.getZ(); ++z) {
                    BlockPos topPos = new BlockPos(x, maxWalls.getY(), z);
                    BlockPos downPos = new BlockPos(x, minWalls.getY(), z);
                    level.setBlockAndUpdate(topPos, wall);
                    level.setBlockAndUpdate(downPos, wall);
                }
            }
            for (int x = minWalls.getX(); x <= maxWalls.getX(); ++x) {
                for (int y = minWalls.getY(); y <= maxWalls.getY(); ++y) {
                    BlockPos topPos = new BlockPos(x, y, maxWalls.getZ());
                    BlockPos downPos = new BlockPos(x, y, minWalls.getZ());
                    level.setBlockAndUpdate(topPos, wall);
                    level.setBlockAndUpdate(downPos, wall);
                }
            }
            for (int z = minWalls.getZ(); z <= maxWalls.getZ(); ++z) {
                for (int y = minWalls.getY(); y <= maxWalls.getY(); ++y) {
                    BlockPos topPos = new BlockPos(maxWalls.getX(), y, z);
                    BlockPos downPos = new BlockPos(minWalls.getX(), y, z);
                    level.setBlockAndUpdate(topPos, wall);
                    level.setBlockAndUpdate(downPos, wall);
                }
            }
        }
    }

    public Entity enter(Entity e, boolean proxyPos) {
        BlockPos destination = roomCenter(getId());
        final BlockPos[] test = { Teleporters.getDestination(getId(), e) };
        if (test[0] != null)
            destination = test[0];
        else {
            Cap.run(e, BagTP.TOKEN, c->test[0] = c.getEnterPos(getId()));
            if (test[0] != null && isInRoom(test[0]) && !isWall(test[0]))
                destination = test[0];
        }
        if (proxyPos)
            Cap.run(e, BagTP.TOKEN, c->c.setLeavePos(getId(), e.level().dimension(), e.blockPosition()));
        return World.teleportEntity(e, DimBag.BAG_DIM, destination);
    }

    public Entity leave(Entity e) {
        Pair<Level, BlockPos> out = e.getCapability(CapabilityManager.get(BagTP.TOKEN)).resolve().map(c->{Pair<Level, BlockPos> t = c.getLeavePos(getId()); c.setEnterPos(getId(), e.blockPosition()); c.clearLeavePos(getId()); return t;}).orElse(null);
        if (out == null)
            out = new HolderData(getId()).getHolderPosition(true);
        if (out != null)
            return World.teleportEntity(e, out.getFirst().dimension(), out.getSecond());
        return e;
    }
}
*/