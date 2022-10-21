package com.limachi.dim_bag.rooms;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class SubRoom {
    Vec3i pos;
    BlockPos center;
    int up;
    int down;
    int north;
    int south;
    int east;
    int west;

    public SubRoom(int id, int radius, int subRoom, Vec3i pos) {
        center = Rooms.getRoomCenter(id, subRoom);
        this.pos = pos;
        up = radius;
        down = radius;
        north = radius;
        south = radius;
        east = radius;
        west = radius;
    }

    public SubRoom(Vec3i pos, BlockPos center, int up, int down, int north, int south, int east, int west) {
        this.center = center;
        this.pos = pos;
        this.up = up;
        this.down = down;
        this.north = north;
        this.south = south;
        this.east = east;
        this.west = west;
    }

    public Set<ChunkPos> getComposingChunks() {
        HashSet<ChunkPos> out = new HashSet<>();
        for (int x = (center.getX() - west) >> 4; x <= (int)Math.ceil((center.getX() + east) / 16.); ++x)
            for (int z = (center.getZ() - north) >> 4; z <= (int)Math.ceil((center.getZ() + south) / 16.); ++z)
                out.add(new ChunkPos(x, z));
        return out;
    }

    AABB AABB() { return new AABB(center.getX() - west, center.getY() - down, center.getZ() - north, center.getX() + east, center.getY() + up, center.getZ() + south); }

    BlockPos getWallCorner1(Direction dir, int depth, int area) {
        switch (dir) {
            case UP:    return new BlockPos(center.getX() - west - area,  center.getY() + up + depth,   center.getZ() - north - area);
            case DOWN:  return new BlockPos(center.getX() - west - area,  center.getY() - down - depth, center.getZ() - north - area);
            case NORTH: return new BlockPos(center.getX() - west - area,  center.getY() - down - area,  center.getZ() - north - depth);
            case SOUTH: return new BlockPos(center.getX() - west - area,  center.getY() - down - area,  center.getZ() + south + depth);
            case EAST:  return new BlockPos(center.getX() + east + depth, center.getY() - down - area,  center.getZ() - north - area);
            case WEST:  return new BlockPos(center.getX() - west - depth, center.getY() - down - area,  center.getZ() - north - area);
        }
        return new BlockPos(0, -1, 0);
    }

    public BlockPos getWallCorner2(Direction dir, int depth, int area) {
        switch (dir) {
            case UP:    return new BlockPos(center.getX() + east + area,  center.getY() + up + depth,   center.getZ() + south + area);
            case DOWN:  return new BlockPos(center.getX() + east + area,  center.getY() - down - depth, center.getZ() + south + area);
            case NORTH: return new BlockPos(center.getX() + east + area,  center.getY() + up + area,    center.getZ() - north - depth);
            case SOUTH: return new BlockPos(center.getX() + east + area,  center.getY() + up + area,    center.getZ() + south + depth);
            case EAST:  return new BlockPos(center.getX() + east + depth, center.getY() + up + area,    center.getZ() + south + area);
            case WEST:  return new BlockPos(center.getX() - west - depth, center.getY() + up + area,    center.getZ() + south + area);
        }
        return new BlockPos(0, -1, 0);
    }

    public boolean isInsideOrWall(BlockPos pos) {
        return pos.getX() >= center.getX() - west
                && pos.getX() <= center.getX() + east
                && pos.getY() >= center.getY() - down
                && pos.getY() <= center.getY() + up
                && pos.getZ() >= center.getZ() - north
                && pos.getZ() <= center.getZ() + south;
    }

    public boolean isInside(BlockPos pos) {
        return pos.getX() > center.getX() - west
                && pos.getX() < center.getX() + east
                && pos.getY() > center.getY() - down
                && pos.getY() < center.getY() + up
                && pos.getZ() > center.getZ() - north
                && pos.getZ() < center.getZ() + south;
    }

    public boolean isInWall(BlockPos pos) {
        if (!isInsideOrWall(pos)) return false;
        return pos.getX() == center.getX() - west
                || pos.getX() == center.getX() + east
                || pos.getY() == center.getY() - down
                || pos.getY() == center.getY() + up
                || pos.getZ() == center.getZ() - north
                || pos.getZ() == center.getZ() + south;
    }

    public boolean isInWall(BlockPos pos, Direction wall) {
        if (!isInsideOrWall(pos)) return false;
        switch (wall) {
            case UP: return pos.getY() == center.getY() + up;
            case DOWN: return pos.getY() == center.getY() - down;
            case NORTH: return pos.getZ() == center.getZ() - north;
            case SOUTH: return pos.getZ() == center.getZ() + south;
            case EAST: return pos.getX() == center.getX() + east;
            case WEST: return pos.getX() == center.getX() - west;
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

    public SubRoom setPos(Vec3i pos) { this.pos = pos; return this; }

    public SubRoom setCenter(BlockPos center) { this.center = center; return this; }

    public SubRoom setRadius(int r, @Nullable Direction dir) {
        if (dir == null || dir == Direction.UP) up = r;
        if (dir == null || dir == Direction.DOWN) down = r;
        if (dir == null || dir == Direction.NORTH) north = r;
        if (dir == null || dir == Direction.SOUTH) south = r;
        if (dir == null || dir == Direction.EAST) east = r;
        if (dir == null || dir == Direction.WEST) west = r;
        return this;
    }

    public int getWallOffset(Direction dir) {
        switch (dir) {
            case UP: return up;
            case DOWN: return down;
            case NORTH: return north;
            case SOUTH: return south;
            case EAST: return east;
            case WEST: return west;
        }
        return 0;
    }

    public void setWallOffset(Direction dir, int newVal) {
        switch (dir) {
            case UP: up = newVal; return;
            case DOWN: down = newVal; return;
            case NORTH: north = newVal; return;
            case SOUTH: south = newVal; return;
            case EAST: east = newVal; return;
            case WEST: west = newVal;
        }
    }

    public static SubRoom fromNBT(CompoundTag nbt) {
        Vec3i pos = new Vec3i(nbt.getInt("X"), nbt.getInt("Y"), nbt.getInt("Z"));
        BlockPos center = new BlockPos(nbt.getInt("CenterX"), nbt.getInt("CenterY"), nbt.getInt("CenterZ"));
        return new SubRoom(pos, center, nbt.getInt("Up"), nbt.getInt("Down"), nbt.getInt("North"), nbt.getInt("South"), nbt.getInt("East"), nbt.getInt("West"));
    }

    public CompoundTag toNBT() {
        CompoundTag out = new CompoundTag();
        out.putInt("Up", up);
        out.putInt("Down", down);
        out.putInt("North", north);
        out.putInt("South", south);
        out.putInt("East", east);
        out.putInt("West", west);
        out.putInt("X", pos.getX());
        out.putInt("Y", pos.getY());
        out.putInt("Z", pos.getZ());
        out.putInt("CenterX", center.getX());
        out.putInt("CenterY", center.getY());
        out.putInt("CenterZ", center.getZ());
        return out;
    }
}
