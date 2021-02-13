package com.limachi.dimensional_bags.common.data.EyeDataMK2;

import com.limachi.dimensional_bags.CuriosIntegration;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.NBTUtils;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.WorldUtils;
import com.limachi.dimensional_bags.common.blocks.Tunnel;
import com.limachi.dimensional_bags.common.blocks.Wall;
import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.limachi.dimensional_bags.common.inventory.Helper;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import com.limachi.dimensional_bags.common.tileentities.PadTileEntity;
import javafx.util.Pair;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
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
    private HashMap<Vector3i, Integer> subRoomsToId = new HashMap<>();
    private ArrayList<Vector3i> idToSubRooms = new ArrayList<>();
    private HashSet<Vector3i> activePads = new HashSet<>();
    private int radius;

    public static final int ROOM_SPACING = 1024;
    public static final int ROOM_MAX_SIZE = 256;
    public static final int ROOM_OFFSET_X = 8;
    public static final int ROOM_OFFSET_Z = 8;
    public static final int ROOM_CENTER_Y = 128;

    public SubRoomsManager(String suffix, int id, boolean client) {
        super(suffix, id, client);
        subRoomsToId.put(new Vector3i(0, 0, 0), 0);
        idToSubRooms.add(0, new Vector3i(0, 0, 0));
        radius = UpgradeManager.getUpgrade("upgrade_radius").getStart();
    }

    public void activatePad(BlockPos pos) {
        activePads.add(pos);
        markDirty();
    }

    public void deactivatePad(BlockPos pos) {
        activePads.remove(pos);
        markDirty();
    }

    /*
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
            SubRoomsManager manager = getInstance(id);
            if (manager != null) {
                int radius = manager.getRadius();
                if (((x + radius) & 1023) <= radius << 1 && pos.getY() >= 128 - radius && pos.getY() <= 128 + radius && ((z + radius) & 1023) <= radius << 1) //now that we know the radius, we can quickly test the room on the X and Z axis
                    return Optional.of(new Pair<>(id, (z + 128) >> 10)); //same trick as for the id, but rooms start at 0 (0 = center room, where the eye resides)
            }
        }
        return Optional.empty();
    }
     */

    public static Optional<Pair<Integer, Integer>> getRoomIds(World world, BlockPos pos, boolean eye) {
        if (world.isRemote || world.getDimensionKey() != WorldUtils.DimBagRiftKey) return Optional.empty();
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
                int radius = manager.getRadius();
                if ((x + radius) % ROOM_SPACING <= radius * 2 && pos.getY() >= half_room - radius && pos.getY() <= half_room + radius && (z + radius) % ROOM_SPACING <= radius * 2)
                    return Optional.of(new Pair<>(id, (z + half_room) / ROOM_SPACING));
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
    public boolean enterBag(Entity entity, boolean checkForBag, boolean checkForPads, boolean defaultToPreviousPos) {
        //if check for bag is true, might have to remove the bag from the inventory and drop it as an entity (if the inseption uprage ins't installed)
        //pad first
        //previous known position second
        //default eye tp last
        if (checkForBag) {
//            int slot = Bag.getBagSlot(entity, getEyeId());
            List<CuriosIntegration.ProxyItemStackModifier> res = CuriosIntegration.searchItem(entity, Bag.class, o->Bag.getEyeId(o) == getEyeId(), true);
            if (!res.isEmpty()) {
                Optional<CompoundNBT> n = UpgradeManager.isUpgradeInstalled(getEyeId(), "inception"); //FIXME: change this string to a real upgrade id
                if (!n.isPresent()) { //this bag is not inception compatible, we should remove the item and spawn the bag entity
//                    if (entity instanceof PlayerEntity) {
//                        if (slot == 38)
//                            BagEntity.spawn(entity.world, entity.getPosition(), Bag.unequipBagOnChestSlot((PlayerEntity) entity));
//                        else {
//                            BagEntity.spawn(entity.world, entity.getPosition(), ((PlayerEntity) entity).inventory.getStackInSlot(slot));
//                            ((PlayerEntity) entity).inventory.setInventorySlotContents(slot, ItemStack.EMPTY);
//                        }
//                    } else {
//                        EquipmentSlotType s = Helper.slotTypeFromIndex(slot);
//                        if (s != null) {
//                            BagEntity.spawn(entity.world, entity.getPosition(), Helper.getItemStack(s, entity));
//                            entity.setItemStackToSlot(s, ItemStack.EMPTY);
//                        }
//                    }
                    for (CuriosIntegration.ProxyItemStackModifier p : res) {
                        BagEntity.spawn(entity.world, entity.getPosition(), p.get());
                        p.set(ItemStack.EMPTY);
                    }
                }
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
        WorldUtils.teleportEntity(entity, WorldUtils.DimBagRiftKey, arrival);
        return true;
    }

    public boolean enterBag(Entity entity) {
        return enterBag(entity, true, true, true);
    }

//    protected void tpIn(Entity entity) { //teleport an entity to the eye of the bag, no check for pads, previous know position or whatnot
//        WorldUtils.teleportEntity(entity, WorldUtils.DimBagRiftKey, new BlockPos(ROOM_SPACING * (getEyeId() - 1) + ROOM_OFFSET_X, ROOM_CENTER_Y + 1, ROOM_OFFSET_Z));
//    }

    private static BlockPos calculateOutput(BlockPos tunnelIn, BlockPos roomCenter, Direction direction, int deltaRoom) {
        return new BlockPos(tunnelIn.getX() + (-Math.abs(direction.getXOffset()) * 2 * (tunnelIn.getX() - roomCenter.getX())),
                tunnelIn.getY() + (-Math.abs(direction.getYOffset()) * 2 * (tunnelIn.getY() - roomCenter.getY())),
                tunnelIn.getZ() + (-Math.abs(direction.getZOffset()) * 2 * (tunnelIn.getZ() - roomCenter.getZ())) + deltaRoom * ROOM_SPACING);
    }

    private Direction wall(BlockPos pos, int room) { //test if a position is IN a wall, and if true, return the direction of the wall (north, south, east, west, up, down; null if not a wall)
        int x = pos.getX() -(getEyeId() - 1) * ROOM_SPACING;
        if (x == ROOM_OFFSET_X + radius) return Direction.byLong(1, 0, 0);
        if (x == ROOM_OFFSET_X - radius) return Direction.byLong(-1, 0, 0);
        int y = pos.getY();
        if (y == ROOM_CENTER_Y + radius) return Direction.byLong(0, 1, 0);
        if (y == ROOM_CENTER_Y - radius) return Direction.byLong(0, -1, 0);
        int z = pos.getZ() -room * ROOM_SPACING;
        if (z == ROOM_OFFSET_Z + radius) return Direction.byLong(0, 0, 1);
        if (z == ROOM_OFFSET_Z - radius) return Direction.byLong(0, 0, -1);
        return null;
    }

    public static BlockPos getEyePos(int id) { return new BlockPos(ROOM_OFFSET_X + ((id - 1) * ROOM_SPACING), ROOM_CENTER_Y, ROOM_OFFSET_Z); }

    public int createSubRoom(World world, BlockPos targetRoom) {
        Integer room = subRoomsToId.get(targetRoom);
        if (room == null) { //test if the room exists, do nothing with the value
            room = idToSubRooms.size();
            subRoomsToId.put(targetRoom, room); //create the double link
            idToSubRooms.add(room, targetRoom);
            WorldUtils.buildRoom(world, getEyePos(getEyeId()).add(0, 0, room * ROOM_SPACING), radius, 0); //build the room
            markDirty();
        }
        return room;
    }

    public static void tunnel(ServerWorld world, BlockPos tunnel, Entity entity, boolean create, boolean destroy) { //create mode: build (if needed) a room and place a portal, !create mode: teleport the entity to the next portal
        Optional<Pair<Integer, Integer>> req = getRoomIds(world, tunnel, false);
        if (!req.isPresent()) return;
        int id = req.get().getKey();
        int room = req.get().getValue();
        SubRoomsManager data = getInstance(id);
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
        BlockPos output = calculateOutput(tunnel, getEyePos(id).add(0, 0, room * ROOM_SPACING), wall, targetRoomId - room); //calculate the position of the output portal
        if (create)
            world.setBlockState(output, Registries.getBlock(Tunnel.NAME).getDefaultState()); //put the tunnel on the targeted room
        else if (destroy)
            world.setBlockState(output, Registries.getBlock(Wall.NAME).getDefaultState()); //destroy the tunnel
        else
            WorldUtils.teleportEntity(entity, world.getDimensionKey(), wall == Direction.DOWN ? output.offset(wall, 2) : output.offset(wall)); //teleport the entity next to the portal
    }

    public int getRadius() { return radius; }

    public void changeRadius(int newRadius) {
        for (int i = 0; i < idToSubRooms.size(); ++i)
            WorldUtils.buildRoom(WorldUtils.getRiftWorld(), getEyePos(getEyeId()).add(0, 0, i * ROOM_SPACING), newRadius, radius);
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
        activePads = new HashSet<>();
        for (int i = 0; i < listSubRooms.size(); ++i) {
            CompoundNBT entry = listSubRooms.getCompound(i);
            Vector3i room = new Vector3i(entry.getInt("X"), entry.getInt("Y"), entry.getInt("Z"));
            idToSubRooms.add(i, room);
            subRoomsToId.put(room, i);
        }
        for (INBT sn : nbt.getList("ActivePads", 10))
            activePads.add(new Vector3i(((CompoundNBT)sn).getInt("X"), ((CompoundNBT)sn).getInt("Y"), ((CompoundNBT)sn).getInt("Z")));
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
