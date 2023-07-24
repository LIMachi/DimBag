package com.limachi.dim_bag.bag_data;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.bag_modes.ModesRegistry;
import com.limachi.dim_bag.bag_modules.TeleportModule;
import com.limachi.dim_bag.blocks.WallBlock;
import com.limachi.dim_bag.capabilities.entities.BagTP;
import com.limachi.dim_bag.entities.BagEntity;
import com.limachi.dim_bag.entities.BagItemEntity;
import com.limachi.dim_bag.items.BagItem;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.dim_bag.utils.SimpleTank;
import com.limachi.dim_bag.utils.Tags;
import com.limachi.lim_lib.World;
import com.limachi.lim_lib.capabilities.Cap;
import com.limachi.lim_lib.nbt.NBT;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import java.util.Optional;
import java.util.function.Consumer;

public class BagInstance {

    private final int bag;
    private ServerLevel bagLevel;
    private final HolderData holder;
    private final CompoundTag rawData;
    private BlockPos minWalls;
    private BlockPos maxWalls;
    private final SlotData slots;
    private final TankData tanks;

    public BagInstance(int id, CompoundTag data) {
        bag = id;
        rawData = data;
        holder = new HolderData(data.getCompound("holder"));
        if (!rawData.contains("modules"))
            rawData.put("modules", new CompoundTag());
        if (!rawData.contains("modes")) {
            rawData.put("modes", new ListTag());
            for (ModesRegistry.ModeEntry me : ModesRegistry.modesList)
                if (me.mode().autoInstall)
                    installMode(me.name());
        }
        if (!rawData.contains("room"))
            buildRoom();
        else {
            CompoundTag room = rawData.getCompound("room");
            if (room.contains("min_walls", Tag.TAG_LONG) && room.contains("max_walls", Tag.TAG_LONG)) {
                minWalls = BlockPos.of(room.getLong("min_walls"));
                maxWalls = BlockPos.of(room.getLong("max_walls"));
            } else
                buildRoom();
        }
        slots = new SlotData(id, Tags.getOrCreateList(rawData, "slots", ListTag::new));
        tanks = new TankData(id, Tags.getOrCreateList(rawData, "tanks", ListTag::new), ()->getModeData("Tank"));
    }

    private ServerLevel prepareBagLevel() {
        if (bagLevel == null)
            bagLevel = (ServerLevel)World.getLevel(DimBag.BAG_DIM);
        return bagLevel;
    }

    public long installedModesMask() {
        long mask = 0;
        for (Tag t : rawData.getList("modes", Tag.TAG_COMPOUND))
            if (t instanceof CompoundTag c && !c.getBoolean("disabled"))
                mask |= 1L << ModesRegistry.getModeIndex(c.getString("name"));
        return mask;
    }

    public CompoundTag unsafeRawAccess() { return rawData; }
    public Optional<CompoundTag> getModeData(String mode) {
        for (Tag t : rawData.getList("modes", Tag.TAG_COMPOUND))
            if (t instanceof CompoundTag c && c.getString("name").equals(mode))
                return Optional.of(c);
        return Optional.empty();
    }

    public LazyOptional<SlotData> slotsHandle() { return slots.getHandle(); }

    public LazyOptional<IItemHandler> slotHandle(BlockPos pos) { return slots.getSlotHandle(pos); }

    public Component getSlotLabel(BlockPos pos) { return slots.getSlotLabel(pos); }

    public void setSlotLabel(BlockPos pos, Component label) { slots.setSlotLabel(pos, label); }

    public LazyOptional<TankData> tanksHandle() { return tanks.getHandle(); }

    public LazyOptional<SimpleTank> tankHandle(BlockPos pos) { return tanks.getTankHandle(pos); }

    public Component getTankLabel(BlockPos pos) { return tanks.getTankLabel(pos); }

    public void setTankLabel(BlockPos pos, Component label) { tanks.setTankLabel(pos, label); }

    public void invalidate() {
        slots.invalidate();
        tanks.invalidate();
    }

    protected void buildRoom() {
        CompoundTag room = Tags.getOrCreateCompound(rawData, "room", CompoundTag::new);
        BlockPos center = BagsData.roomCenter(bag);
        minWalls = center.offset(-BagsData.DEFAULT_ROOM_RADIUS, -BagsData.DEFAULT_ROOM_RADIUS, -BagsData.DEFAULT_ROOM_RADIUS);
        maxWalls = center.offset(BagsData.DEFAULT_ROOM_RADIUS, BagsData.DEFAULT_ROOM_RADIUS, BagsData.DEFAULT_ROOM_RADIUS);
        BlockState wall = WallBlock.R_BLOCK.get().defaultBlockState();
        prepareBagLevel();
        for (int x = minWalls.getX(); x <= maxWalls.getX(); ++x) {
            for (int z = minWalls.getZ(); z <= maxWalls.getZ(); ++z) {
                BlockPos topPos = new BlockPos(x, maxWalls.getY(), z);
                BlockPos downPos = new BlockPos(x, minWalls.getY(), z);
                bagLevel.setBlockAndUpdate(topPos, wall);
                bagLevel.setBlockAndUpdate(downPos, wall);
            }
        }
        for (int x = minWalls.getX(); x <= maxWalls.getX(); ++x) {
            for (int y = minWalls.getY(); y <= maxWalls.getY(); ++y) {
                BlockPos topPos = new BlockPos(x, y, maxWalls.getZ());
                BlockPos downPos = new BlockPos(x, y, minWalls.getZ());
                bagLevel.setBlockAndUpdate(topPos, wall);
                bagLevel.setBlockAndUpdate(downPos, wall);
            }
        }
        for (int z = minWalls.getZ(); z <= maxWalls.getZ(); ++z) {
            for (int y = minWalls.getY(); y <= maxWalls.getY(); ++y) {
                BlockPos topPos = new BlockPos(maxWalls.getX(), y, z);
                BlockPos downPos = new BlockPos(minWalls.getX(), y, z);
                bagLevel.setBlockAndUpdate(topPos, wall);
                bagLevel.setBlockAndUpdate(downPos, wall);
            }
        }
        room.putLong("min_walls", minWalls.asLong());
        room.putLong("max_walls", maxWalls.asLong());
    }

    public int bagId() { return bag; }

    public void storeOn(CompoundTag instance) {
        instance.put("holder", holder.serialize());
        instance.put("slots", slots.serialize());
        instance.put("tanks", tanks.serialize());
    }

    public void setHolder(Entity entity) {
        boolean paradox = (entity.level().dimension().equals(DimBag.BAG_DIM) && isInRoom(entity.blockPosition()));
        if (paradox) {
            if (entity instanceof BagItemEntity || entity instanceof BagEntity) { //bags as item/entities should never be allowed to live inside a bag if not inside an inventory (as this would probably result in loss of bag access)
                leave(entity);
                return;
            }
            if (!isPresent("paradox")) {
                if (holder.level != null && holder.position != null)
                    BagItem.unequipBags(entity, bagId(), holder.level, holder.position);
                else
                    ; //FIXME: player spawn if entity is player? world spawn?
                return;
            }
            holder.paradox = true;
        } else {
            holder.paradox = false;
            holder.position = entity.blockPosition();
            holder.level = entity.level();
        }
        holder.entity = entity;
    }

    public Optional<Entity> getHolder(boolean nonParadoxOnly) {
        if (nonParadoxOnly && holder.paradox) return Optional.empty();
        return Optional.ofNullable(holder.entity);
    }

    public Optional<Pair<Level, BlockPos>> getHolderPosition(boolean nonParadoxOnly) {
        if (holder.paradox && !nonParadoxOnly && holder.entity != null)
                return Optional.of(new Pair<>(World.getLevel(DimBag.BAG_DIM), holder.entity.blockPosition()));
        if (holder.position != null && holder.level != null)
            return Optional.of(new Pair<>(holder.level, holder.position));
        return Optional.empty();
    }

    public boolean installMode(String name) {
        ListTag modes = rawData.getList("modes", Tag.TAG_COMPOUND);
        for (Tag tag : modes)
            if (tag instanceof CompoundTag entry && name.equals(entry.getString("name")))
                return false;
        CompoundTag entry = ModesRegistry.getMode(name).initialData();
        entry.putString("name", name);
        modes.add(entry);
        return true;
    }

    public void simpleInstall(String name, BlockPos pos) {
        ListTag modules = Tags.getOrCreateList(rawData.getCompound("modules"), name, ListTag::new);
        LongTag p = LongTag.valueOf(pos.asLong());
        if (!modules.contains(p))
            modules.add(p);
    }

    public void simpleUninstall(String name, BlockPos pos) {
        CompoundTag data = rawData.getCompound("modules");
        ListTag modules = data.getList(name, Tag.TAG_LONG);
        modules.remove(LongTag.valueOf(pos.asLong()));
        if (modules.isEmpty())
            data.remove(name);
    }

    public void compoundInstall(String name, BlockPos pos, CompoundTag data) {
        ListTag modules = Tags.getOrCreateList(rawData.getCompound("modules"), name, ListTag::new);
        long p = pos.asLong();
        boolean replace = false;
        for (Tag t : modules)
            if (t instanceof CompoundTag c && c.getLong("position") == p) {
                replace = true;
                NBT.clear(c);
                c.merge(data);
                c.putLong("position", p);
            }
        if (!replace) {
            CompoundTag insert = data.copy();
            insert.putLong("position", p);
            modules.add(insert);
        }
    }

    public CompoundTag compoundUninstall(String name, BlockPos pos) {
        CompoundTag out = new CompoundTag();
        CompoundTag data = rawData.getCompound("modules");
        ListTag modules = data.getList(name, Tag.TAG_COMPOUND);
        long p = pos.asLong();
        for (int i = 0; i < modules.size(); ++i) {
            CompoundTag entry = modules.getCompound(i);
            if (entry.getLong("position") == p) {
                entry.remove("position");
                out = entry;
                modules.remove(i);
                break;
            }
        }
        if (modules.isEmpty())
            data.remove(name);
        return out;
    }

    public CompoundTag getInstalledCompound(String name, BlockPos pos) {
        ListTag modules = rawData.getCompound("modules").getList(name, Tag.TAG_COMPOUND);
        long p = pos.asLong();
        for (int i = 0; i < modules.size(); ++i) {
            CompoundTag entry = modules.getCompound(i);
            if (entry.getLong("position") == p)
                return entry;
        }
        return new CompoundTag();
    }

    public boolean isPresent(String name) { return rawData.getCompound("modules").contains(name); }

    protected boolean inRange(int v, int min, int max) { return v >= min && v <= max; }

    public boolean isWall(BlockPos pos) {
        return (((pos.getX() == minWalls.getX() || pos.getX() == maxWalls.getX()) && inRange(pos.getY(), minWalls.getY(), maxWalls.getY()) && inRange(pos.getZ(), minWalls.getZ(), maxWalls.getZ())) ||
                ((pos.getY() == minWalls.getY() || pos.getY() == maxWalls.getY()) && inRange(pos.getX(), minWalls.getX(), maxWalls.getX()) && inRange(pos.getZ(), minWalls.getZ(), maxWalls.getZ())) ||
                ((pos.getZ() == minWalls.getZ() || pos.getZ() == maxWalls.getZ()) && inRange(pos.getY(), minWalls.getY(), maxWalls.getY()) && inRange(pos.getX(), minWalls.getX(), maxWalls.getX())));
    }

    public boolean isInRoom(BlockPos pos) {
        return inRange(pos.getX(), minWalls.getX(), maxWalls.getX()) && inRange(pos.getY(), minWalls.getY(), maxWalls.getY()) && inRange(pos.getZ(), minWalls.getZ(), maxWalls.getZ());
    }

    private Direction wallDirection(BlockPos wallPos) {
        if (wallPos.getX() == minWalls.getX()) return Direction.WEST;
        if (wallPos.getX() == maxWalls.getX()) return Direction.EAST;
        if (wallPos.getY() == minWalls.getY()) return Direction.DOWN;
        if (wallPos.getY() == maxWalls.getY()) return Direction.UP;
        if (wallPos.getZ() == minWalls.getZ()) return Direction.NORTH;
        if (wallPos.getZ() == maxWalls.getZ()) return Direction.SOUTH;
        return null;
    }

    private void iterateWall(Direction wall, int offset, Consumer<BlockPos> run) {
        BlockPos start = switch (wall) {
            case UP -> new BlockPos(minWalls.getX() - offset, maxWalls.getY(), minWalls.getZ() - offset);
            case DOWN -> new BlockPos(minWalls.getX() - offset, minWalls.getY(), minWalls.getZ() - offset);
            case NORTH -> new BlockPos(minWalls.getX() - offset, minWalls.getY() - offset, minWalls.getZ());
            case SOUTH -> new BlockPos(minWalls.getX() - offset, minWalls.getY() - offset, maxWalls.getZ());
            case EAST -> new BlockPos(maxWalls.getX(), minWalls.getY() - offset, minWalls.getZ() - offset);
            case WEST -> new BlockPos(minWalls.getX(), minWalls.getY() - offset, minWalls.getZ() - offset);
        };
        BlockPos end = switch (wall) {
            case UP -> new BlockPos(maxWalls.getX() + offset, maxWalls.getY(), maxWalls.getZ() + offset);
            case DOWN -> new BlockPos(maxWalls.getX() + offset, minWalls.getY(), maxWalls.getZ() + offset);
            case NORTH -> new BlockPos(maxWalls.getX() + offset, maxWalls.getY() + offset, minWalls.getZ());
            case SOUTH -> new BlockPos(maxWalls.getX() + offset, maxWalls.getY() + offset, maxWalls.getZ());
            case EAST -> new BlockPos(maxWalls.getX(), maxWalls.getY() + offset, maxWalls.getZ() + offset);
            case WEST -> new BlockPos(minWalls.getX(), maxWalls.getY() + offset, maxWalls.getZ() + offset);
        };
        for (int x = start.getX(); x <= end.getX(); ++x)
            for (int y = start.getY(); y <= end.getY(); ++y)
                for (int z = start.getZ(); z <= end.getZ(); ++z)
                    run.accept(new BlockPos(x, y, z));
    }

    public boolean pushWall(BlockPos wallPos) {
        if (!isWall(wallPos)) return false;
        Direction pushDirection = wallDirection(wallPos);
        if (pushDirection == null) return false;
        BlockPos center = BagsData.roomCenter(bag);
        BlockPos delta;
        if (pushDirection.getAxisDirection() == Direction.AxisDirection.POSITIVE)
            delta = maxWalls.relative(pushDirection).subtract(center);
        else
            delta = center.subtract(minWalls.relative(pushDirection));
        if (delta.getX() > BagsData.MAXIMUM_ROOM_RADIUS || delta.getY() > BagsData.MAXIMUM_ROOM_RADIUS || delta.getZ() > BagsData.MAXIMUM_ROOM_RADIUS) //should probably use a check for maximum world size (Y) to uncap the hard 126 block limit
            return false;
        if (prepareBagLevel() == null)
            return false;
        BlockState air = Blocks.AIR.defaultBlockState();
        iterateWall(pushDirection, 0, p->bagLevel.setBlockAndUpdate(p.relative(pushDirection), bagLevel.getBlockState(p)));
        iterateWall(pushDirection, -1, p->bagLevel.setBlockAndUpdate(p, air));
        if (pushDirection.getAxisDirection() == Direction.AxisDirection.POSITIVE)
            maxWalls = maxWalls.relative(pushDirection);
        else
            minWalls = minWalls.relative(pushDirection);
        CompoundTag room = Tags.getOrCreateCompound(rawData, "room", CompoundTag::new);
        room.putLong("min_walls", minWalls.asLong());
        room.putLong("max_walls", maxWalls.asLong());
        return true;
    }

    public void temporaryChunkLoad() {
        prepareBagLevel();
        for (int x = minWalls.getX(); x < maxWalls.getX(); x += 16)
            for (int z = minWalls.getZ(); z < maxWalls.getZ(); z += 16)
                World.temporaryChunkLoad(bagLevel, new BlockPos(x, 128, z));
    }

    public Entity enter(Entity entity, boolean proxy) {
        BlockPos destination = BagsData.roomCenter(bag);
        BlockPos test = TeleportModule.getDestination(this, entity).orElse(null);
        if (test == null && !(entity instanceof Player)) return null;
        if (test != null)
            destination = test;
        else {
            test = ((Optional<BlockPos>)Cap.run(entity, BagTP.TOKEN, c -> Optional.of(c.getEnterPos(bag)), Optional.empty())).orElse(null);
            if (test != null && isInRoom(test) && !isWall(test))
                destination = test;
        }
        if (proxy)
            Cap.run(entity, BagTP.TOKEN, c -> c.setLeavePos(bag, entity.level().dimension(), entity.blockPosition()));
        return World.teleportEntity(entity, DimBag.BAG_DIM, destination);
    }

    public Entity leave(Entity entity) {
        Optional<Pair<Level, BlockPos>> out = entity.getCapability(CapabilityManager.get(BagTP.TOKEN)).resolve().map(c -> {
            Pair<Level, BlockPos> t = c.getLeavePos(bag);
            c.setEnterPos(bag, entity.blockPosition());
            c.clearLeavePos(bag);
            return t;
        });
        if (out.isEmpty())
            out = getHolderPosition(true);
        if (out.isPresent())
            return World.teleportEntity(entity, out.get().getFirst().dimension(), out.get().getSecond());
        return entity;
    }
}
