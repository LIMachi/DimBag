package com.limachi.dim_bag.save_datas;

import com.limachi.dim_bag.DimBag;
//import com.limachi.dim_bag.bag_data.HolderData;
import com.limachi.dim_bag.blocks.WallBlock;
import com.limachi.dim_bag.capabilities.entities.BagTP;
import com.limachi.lim_lib.Configs;
import com.limachi.lim_lib.World;
import com.limachi.lim_lib.capabilities.Cap;
import com.limachi.lim_lib.saveData.AbstractSyncSaveData;
import com.limachi.lim_lib.saveData.RegisterSaveData;
import com.limachi.lim_lib.saveData.SaveDataManager;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.CapabilityManager;
import org.jetbrains.annotations.NotNull;

/*
@RegisterSaveData
public class BagRoom extends AbstractSyncSaveData {

    @Configs.Config(cmt = "Initial size of a new bag (in blocks, including walls)")
    public static int DEFAULT_ROOM_RADIUS = 3;

    @Configs.Config(cmt = "Blocks between each room centers. CHANGING THIS WILL CORRUPT EXISTING WORLDS!")
    public static int ROOM_SPACING = 1024;

    @Configs.Config(min = "3", max = "126", cmt = "Maximum size of a bag (in blocks, including walls)")
    public static int MAXIMUM_ROOM_RADIUS = 64;

    private BlockPos minWalls;
    private BlockPos maxWalls;
    private int bagId = 0;

    public BagRoom(String name) { super(name); }

    protected boolean inRange(int v, int min, int max) { return v >= min && v <= max; }

    public boolean isWall(BlockPos pos) {
        return ((pos.getX() == minWalls.getX() || pos.getX() == maxWalls.getX()) && inRange(pos.getY(), minWalls.getY(), maxWalls.getY()) && inRange(pos.getZ(), minWalls.getZ(), maxWalls.getZ())) ||
                ((pos.getY() == minWalls.getY() || pos.getY() == maxWalls.getY()) && inRange(pos.getX(), minWalls.getX(), maxWalls.getX()) && inRange(pos.getZ(), minWalls.getZ(), maxWalls.getZ())) ||
                ((pos.getZ() == minWalls.getZ() || pos.getZ() == maxWalls.getZ()) && inRange(pos.getY(), minWalls.getY(), maxWalls.getY()) && inRange(pos.getX(), minWalls.getX(), maxWalls.getX()));
    }

    public boolean isInRoom(BlockPos pos) {
        return inRange(pos.getX(), minWalls.getX(), maxWalls.getX()) && inRange(pos.getY(), minWalls.getY(), maxWalls.getY()) && inRange(pos.getZ(), minWalls.getZ(), maxWalls.getZ());
    }

    public static BlockPos roomCenter(int id) { return new BlockPos(8 + (id - 1) * ROOM_SPACING, 128, 8); }

    public void initRoomData() {
        BlockPos center = roomCenter(bagId);
        minWalls = center.offset(-DEFAULT_ROOM_RADIUS, -DEFAULT_ROOM_RADIUS, -DEFAULT_ROOM_RADIUS);
        maxWalls = center.offset(DEFAULT_ROOM_RADIUS, DEFAULT_ROOM_RADIUS, DEFAULT_ROOM_RADIUS);
        setDirty();
    }

    public static BagRoom getRoom(BlockPos byBlock) {
        int tryId = (byBlock.getX() / ROOM_SPACING) + 1;
        BagRoom out = tryGetRoom(tryId);
        return out != null && out.isInRoom(byBlock) ? out : null;
    }

    public static void temporaryChunkLoad(int bag) {
        BagRoom room = BagRoom.tryGetRoom(bag);
        Level level = World.getLevel(DimBag.BAG_DIM);
        if (room != null && level != null)
            for (int x = room.minWalls.getX(); x < room.maxWalls.getX(); x += 16)
                for (int z = room.minWalls.getZ(); z < room.maxWalls.getZ(); z += 16)
                    World.temporaryChunkLoad(level, new BlockPos(x, 128, z));
    }

    public static int getRoomId(BlockPos pos) {
        BagRoom tmp = getRoom(pos);
        return tmp != null ? tmp.bagId : 0;
    }

    public static BagRoom getRoom(int byId) {
        BagRoom out = SaveDataManager.getInstance("bag_room:" + byId, Level.OVERWORLD);
        if (out != null && out.bagId != byId)
            out.bagId = byId;
        return out;
    }

    public static BagRoom tryGetRoom(int id) {
        BagRoom out = SaveDataManager.getInstance("bag_room:" + id, Level.OVERWORLD, true);
        if (out != null)
            out.bagId = id;
        return out;
    }

    @NotNull
    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        compoundTag.putLong("minWalls", minWalls.asLong());
        compoundTag.putLong("maxWalls", maxWalls.asLong());
        return compoundTag;
    }

    @Override
    public void load(CompoundTag compoundTag) {
        minWalls = BlockPos.of(compoundTag.getLong("minWalls"));
        maxWalls = BlockPos.of(compoundTag.getLong("maxWalls"));
    }

    public BlockPos getMinWalls() { return minWalls; }

    public BlockPos getMaxWalls() { return maxWalls; }

    public int getBagId() { return bagId; }

    public void build() {
        Level level = World.getLevel(DimBag.BAG_DIM);
        BlockState wall = WallBlock.R_BLOCK.get().defaultBlockState();
        for (int x = minWalls.getX(); x <= maxWalls.getX(); ++x) {
            for (int z =  minWalls.getZ(); z <=  maxWalls.getZ(); ++z) {
                BlockPos topPos = new BlockPos(x, maxWalls.getY(), z);
                BlockPos downPos = new BlockPos(x, minWalls.getY(), z);
                level.setBlockAndUpdate(topPos, wall);
                level.setBlockAndUpdate(downPos, wall);
            }
        }
        for (int x = minWalls.getX(); x <= maxWalls.getX(); ++x) {
            for (int y =  minWalls.getY(); y <=  maxWalls.getY(); ++y) {
                BlockPos topPos = new BlockPos(x, y, maxWalls.getZ());
                BlockPos downPos = new BlockPos(x, y, minWalls.getZ());
                level.setBlockAndUpdate(topPos, wall);
                level.setBlockAndUpdate(downPos, wall);
            }
        }
        for (int z = minWalls.getZ(); z <= maxWalls.getZ(); ++z) {
            for (int y =  minWalls.getY(); y <=  maxWalls.getY(); ++y) {
                BlockPos topPos = new BlockPos(maxWalls.getX(), y, z);
                BlockPos downPos = new BlockPos(minWalls.getX(), y, z);
                level.setBlockAndUpdate(topPos, wall);
                level.setBlockAndUpdate(downPos, wall);
            }
        }
    }

    public Entity enter(Entity e, boolean proxyPos) {
        BlockPos destination = roomCenter(bagId);
        final BlockPos[] test = { Teleporters.getDestination(bagId, e) };
        if (test[0] != null)
            destination = test[0];
        else {
            Cap.run(e, BagTP.TOKEN, c->test[0] = c.getEnterPos(bagId));
            if (test[0] != null && isInRoom(test[0]) && !isWall(test[0]))
                destination = test[0];
        }
        if (proxyPos)
            Cap.run(e, BagTP.TOKEN, c->c.setLeavePos(bagId, e.level().dimension(), e.blockPosition()));
        return World.teleportEntity(e, DimBag.BAG_DIM, destination);
    }

    public Entity leave(Entity e) {
        Pair<Level, BlockPos> out = e.getCapability(CapabilityManager.get(BagTP.TOKEN)).resolve().map(c->{Pair<Level, BlockPos> t = c.getLeavePos(bagId); c.setEnterPos(bagId, e.blockPosition()); c.clearLeavePos(bagId); return t;}).orElse(null);
        if (out == null)
            out = new HolderData(getBagId()).getHolderPosition(true);
        if (out != null)
            return World.teleportEntity(e, out.getFirst().dimension(), out.getSecond());
        return e;
    }
}
*/