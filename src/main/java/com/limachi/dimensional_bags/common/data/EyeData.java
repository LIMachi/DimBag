package com.limachi.dimensional_bags.common.data;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.NBTUtils;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.WorldUtils;
import com.limachi.dimensional_bags.common.inventory.BagInventory;
import com.limachi.dimensional_bags.common.inventory.MultyTank;
import com.limachi.dimensional_bags.common.managers.ModeManager;
import com.limachi.dimensional_bags.common.managers.Upgrade;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import javafx.util.Pair;
import net.minecraft.client.MainWindow;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.*;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

public class EyeData {}/*extends WorldSavedData { //TODO: make EyeData a WorldSavedData (and change DimBagData to only hold global data, like id's), and no longer use the manager, the eye will manage itself (the get function will take an id in adition to the additional server), no longer use the DimensionSavedDataManager#getOrCreate, make the getter use get and return an error if not present, use the setter to create a new eye
    public static final String ID_KEY = "Id";
    private int id;
    private UUID ownerUUID;
    private String ownerName;
    private WeakReference<ServerPlayerEntity> owner; //cache for the player referenced by uuid
    private WeakReference<Entity> entity; //cache for the entity that currently hold the bag (can be the bag itself, in entity or itementity form)
    private List<WeakReference<ServerPlayerEntity>> listeners; //list of player currently accessing a gui of the eye/bag
    private RegistryKey<World> tpDimension;
    private Vector3d tpPosition;
    private BagInventory inventory;
    private final DimBagData globalData;
    private Map<BlockPos, Integer> subRoomsToId; //maps virtual room coordinates (x,y,z) to real coordinates (+/-z), room 0,0,0 is the main/center room
    private ArrayList<BlockPos> idToSubRooms; //inverse of the above map
    private CompoundNBT upgradesNBT; //private storage of the upgrades
    private ModeManager modeManager;
    private MultyTank tanks;
    private EnergyStorage energyStorage;
    private ICapabilityProvider capabilityProvider;
    private static final ICapabilityProvider EMPTY_CAPABILITY_PROVIDER = new ICapabilityProvider() {
        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return LazyOptional.empty();
        }
    };

    public ItemStack asBagItem() {
        ItemStack out = new ItemStack(Registries.BAG_ITEM.get());
        NBTUtils.mergeNbt(out, this.write(new CompoundNBT()));
        return out;
    }

    public ModeManager modeManager() { return modeManager; }

    public boolean canEnter(Entity entity) { return true; } //TODO: use upgrades to change this
    public boolean canInvade(Entity entity) { return canEnter(entity); } //TODO: use upgrades to change this

    public int roomCount() { return idToSubRooms.size(); }

    public static void tunnel(World world, BlockPos tunnel, Entity entity, boolean create, boolean destroy) { //create mode: build (if needed) a room and place a portal, !create mode: teleport the entity to the next portal
        Optional<Pair<Integer, Integer>> req = getRoomIds(world, tunnel, false);
        if (!req.isPresent()) return;
        int id = req.get().getKey();
        int room = req.get().getValue();
        EyeData data = EyeData.get(id);
        if (data == null) return;
        BlockPos coord = data.idToSubRooms.get(room); //virtual coordinates of the current room
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

    private static BlockPos calculateOutput(BlockPos tunnelIn, BlockPos roomCenter, Direction direction, int deltaRoom) {
        return new BlockPos(tunnelIn.getX() + (-Math.abs(direction.getXOffset()) * 2 * (tunnelIn.getX() - roomCenter.getX())),
                            tunnelIn.getY() + (-Math.abs(direction.getYOffset()) * 2 * (tunnelIn.getY() - roomCenter.getY())),
                            tunnelIn.getZ() + (-Math.abs(direction.getZOffset()) * 2 * (tunnelIn.getZ() - roomCenter.getZ())) + deltaRoom * 1024);
    }

    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return LazyOptional.of(this::getInventory).cast();
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            return LazyOptional.of(this::getTank).cast();
        if (cap == CapabilityEnergy.ENERGY)
            return LazyOptional.of(this::getBattery).cast();
        return LazyOptional.empty();
    }

    public IEnergyStorage getBattery() { return energyStorage; }

    public void changeBatterySize(int newSize) {
        energyStorage = new EnergyStorage(newSize, newSize / 128, newSize / 128, energyStorage.getEnergyStored());
        markDirty();
    }

    public MultyTank getTank() { return tanks; }

    public ICapabilityProvider getCapabilityProvider() { return capabilityProvider; }

    public static ICapabilityProvider getCapabilityProvider(int id) {
        EyeData data = EyeData.get(id);
        if (data != null)
            return data.getCapabilityProvider();
        return EMPTY_CAPABILITY_PROVIDER;
    }

    public int createSubRoom(World world, BlockPos targetRoom) {
        Integer room = subRoomsToId.get(targetRoom);
        if (room == null) { //test if the room exists, do nothing with the value
            room = idToSubRooms.size();
            subRoomsToId.put(targetRoom, room); //create the double link
            idToSubRooms.add(room, targetRoom);
            WorldUtils.buildRoom(world, getEyePos(getId()).add(0, 0, room << 10), getRadius(), 0); //build the room
            this.markDirty();
        }
        return room;
    }

    private Direction wall(BlockPos pos, int room) { //test if a position is IN a wall, and if true, return the direction of the wall (north, south, east, west, up, down; null if not a wall)
        int radius = getRadius();
        int x = pos.getX() -(id - 1) * 1024;
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

    private static Vector3d PosToV3D(BlockPos pos) {
        return new Vector3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
    }

    public EyeData(@Nullable ServerPlayerEntity player, int id) {
        super(MOD_ID + "_eye_" + id);
        if (id > 0)
            this.globalData = DimBagData.get(DimBag.getServer());
        else
            this.globalData = null;
        this.id = id;
        if (player != null) {
            this.ownerUUID = player.getUniqueID();
            this.ownerName = player.getName().getString();
        } else {
            this.ownerUUID = new UUID(0, 0); //null UUID
            this.ownerName = "";
        }
        this.energyStorage = new EnergyStorage(0);
        this.owner = new WeakReference<>(player);
        this.entity = new WeakReference<>(player);
        this.tpDimension = World.OVERWORLD;
        ServerWorld overworld = WorldUtils.getOverWorld();
        this.tpPosition = overworld != null ? PosToV3D(overworld.getSpawnPoint()) : new Vector3d(0.5, 70, 0.5);
        this.upgradesNBT = new CompoundNBT();
        this.modeManager = new ModeManager(this);
        UpgradeManager.startingUpgrades(this);
        this.inventory = new BagInventory(this);
        this.tanks = new MultyTank();
        this.tanks.attachListener(this::markDirty);
        this.subRoomsToId = new HashMap<>();
        this.subRoomsToId.put(new BlockPos(0, 0, 0), 0);
        this.idToSubRooms = new ArrayList<>();
        this.idToSubRooms.add(0, new BlockPos(0, 0, 0));
        EyeData data = this;
        this.capabilityProvider = new ICapabilityProvider() {
            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
                return data.getCapability(cap, side);
            }
        };
    }

//    private static final HashMap<Integer, EyeData> cachedEyes = new HashMap(); //FIXME this cache should be emptied when leaving/entering a world

    public static EyeData get(int id) {
        if (id < 1) return null;
        EyeData data;
//        EyeData data = cachedEyes.get(id);
//        if (data != null)
//            return data;
        MinecraftServer server = DimBag.getServer();
        if (server == null) return null;
        ServerWorld world = server.getWorld(World.OVERWORLD);
        if (world == null) return null;
        data = world.getSavedData().get(() -> new EyeData(null, id), MOD_ID + "_eye_" + id);
//        if (data != null)
//            cachedEyes.put(id, data);
        return data;
    }

    public static BlockPos getEyePos(int id) { return new BlockPos(8 + ((id - 1) << 10), 128, 8); } //each eye is 1024 blocks appart, so for the maximum size of a room (radius 127, 255 blocks diameter), there is at least 32 chunks (32*16=512 blocks) between each room

    public BlockPos getEyePos() { return getEyePos(this.id); }

    public static EyeData getEyeData(World world, BlockPos pos, boolean eye) {
        return get(getRoomIds(world, pos, eye).orElse(new Pair<>(0, 0)).getKey());
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
            EyeData data = EyeData.get(id);
            int radius = data.getRadius();
            if (((x + radius) & 1023) <= radius << 1 && pos.getY() >= 128 - radius && pos.getY() <= 128 + radius && ((z + radius) & 1023) <= radius << 1) //now that we know the radius, we can quickly test the room on the X and Z axis
                return Optional.of(new Pair<>(id, (z + 128) >> 10)); //same trick as for the id, but rooms start at 0 (0 = center room, where the eye resides)
        }
        return Optional.empty();
    }

    @OnlyIn(Dist.CLIENT)
    public void onRenderHud(PlayerEntity player, MainWindow window, MatrixStack matrixStack, float partialTicks) {
        for (String upgrade : getUpgrades())
            UpgradeManager.getUpgrade(upgrade).onRenderHud(this, player, window, matrixStack, partialTicks);
        for (String mode : modeManager.getInstalledModes())
            ModeManager.getMode(mode).onRenderHud(this, player, window, matrixStack, partialTicks, mode.equals(modeManager.getSelectedMode()));
    }

    @OnlyIn(Dist.CLIENT)
    public <T extends LivingEntity> void onRenderEquippedBag(BipedModel<T> entityModel, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        for (String upgrade : getUpgrades())
            UpgradeManager.getUpgrade(upgrade).onRenderEquippedBag(this, entityModel, matrixStackIn, bufferIn, packedLightIn, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
        for (String mode : modeManager.getInstalledModes())
            ModeManager.getMode(mode).onRenderEquippedBag(this, mode.equals(modeManager.getSelectedMode()), entityModel, matrixStackIn, bufferIn, packedLightIn, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
    }

    public PlayerInventory getPlayerInventory() {
        Entity try1 = entity.get();
        if (try1 instanceof ServerPlayerEntity)
            return ((ServerPlayerEntity) try1).inventory;
        ServerPlayerEntity try2 = getOwnerPlayer();
        if (try2 != null)
            return try2.inventory;
        return null;
    }

    public int getRows() { return UpgradeManager.getUpgrade("upgrade_row").getCount(this); }

    public int getColumns() { return UpgradeManager.getUpgrade("upgrade_column").getCount(this); }

    public int getRadius() { return UpgradeManager.getUpgrade("upgrade_radius").getCount(this); }

    public CompoundNBT getUpgradesNBT() { return this.upgradesNBT; }
    public Set<String> getUpgrades() { return this.upgradesNBT.keySet(); }

    public final int getId() { return this.id; }

    public final ServerPlayerEntity getOwnerPlayer() {
        ServerPlayerEntity player = owner.get();
        if (player == null) {
            player = DimBag.getServer().getPlayerList().getPlayerByUUID(ownerUUID);
            if (player != null)
                owner = new WeakReference<>(player);
        }
        return player;
    }

    public final Entity getUser() { //try to get the user from the cache, or the owner from the cache, or the owner from the server, might return null if nobody is using the bag and the owner is not online
        return entity.get();
    }

    public String getOwnerName() { return ownerName; }

    public void setUser(Entity user) {
        entity = new WeakReference<>(user);
    }

    public boolean shouldCreateCloudInVoid() {
        return true; //FIXME: use and upgrade instead
    }

    public final BagInventory getInventory() { return this.inventory; }

    public void tpBack(Entity entity) { //teleport an entity to the location targeted by the bag
        WorldUtils.teleportEntity(entity, tpDimension, tpPosition);
    }

    public void tpIn(Entity entity) { //teleport an entity to the eye of the bag
        WorldUtils.teleportEntity(entity, WorldUtils.DimBagRiftKey, new BlockPos(1024 * (id - 1) + 8, 129, 8));
    }

    public void tpTunnel(Entity entity, BlockPos portalPos) { //teleport an entity to the next room, the position of the portal determine the destination
        tunnel(entity.world, portalPos, entity, false, false);
    }

    public void updateBagPosition(Vector3d newPos, ServerWorld newDim) {
        this.tpPosition = newPos;
        this.tpDimension = newDim.getDimensionKey();
    }

    public Vector3d getBagPosition() {
        return tpPosition;
    }

    public RegistryKey<World> getBagWorld() {
        return tpDimension;
    }

    public void mergeBag(ItemStack stack) {
        CompoundNBT nbt = NBTUtils.getNbt(stack);
        modeManager.selectMode(nbt.getString("Mode"));
        CompoundNBT upgradesData = nbt.getCompound("UpgradesData");
        for (String upgrade : upgradesData.keySet()) {
            int gq = upgradesNBT.getCompound(upgrade).getInt("Count");
            int iq = upgradesData.getCompound(upgrade).getInt("Count");
            if (iq > gq) {
                Upgrade u = UpgradeManager.getUpgrade(upgrade);
                for (int i = gq; i < iq; ++i)
                    u.upgradeCrafted(this, stack, null, null);
            }
        }
        NBTUtils.mergeNbt(stack, write(new CompoundNBT()));
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
//        final CompoundNBT[] out = {in};
//        CompoundNBT nbt = new CompoundNBT();
//        DimBag.LOGGER.info("Updating " + getName());
        nbt.putInt(ID_KEY, this.id);
        nbt.putUniqueId("Owner", this.ownerUUID);
        nbt.putString("OwnerName", this.ownerName);
        nbt.putString("Dim", WorldUtils.worldRKToString(this.tpDimension));
        nbt.putDouble("X", this.tpPosition.getX());
        nbt.putDouble("Y", this.tpPosition.getY());
        nbt.putDouble("Z", this.tpPosition.getZ());
        nbt.put("Inventory", this.inventory.write(new CompoundNBT()));
        nbt.put("Tanks", tanks.write(new CompoundNBT()));
        nbt.putInt("Energy", energyStorage.getEnergyStored());
        nbt.putInt("MaxEnergy", energyStorage.getMaxEnergyStored());
        nbt.put("Modes", modeManager.write(new CompoundNBT()));
        nbt.put("UpgradesData", this.upgradesNBT);
        ListNBT listSubRooms = new ListNBT();
        for (BlockPos li : idToSubRooms)
            listSubRooms.add(NBTUtil.writeBlockPos(li));
        nbt.put("SubRooms", listSubRooms);
//        NBTUtils.mergeNbt(this.getClass(), ()->out[0], nbt, w->out[0]=w);
        return nbt;
    }

    @Override
    public void read(CompoundNBT nbt) {
//        final CompoundNBT[] refIn = {in};
//        DimBag.LOGGER.info("Loadding " + getName());
//        CompoundNBT nbt = NBTUtils.getNbt(this.getClass(), ()->refIn[0], w->refIn[0]=w);
        this.id = nbt.getInt(ID_KEY);
        this.ownerName = nbt.getString("OwnerName");
        if (this.ownerName.length() == 0) {
            this.ownerUUID = new UUID(0, 0);
            this.owner = new WeakReference<>(null);
        }
        else {
            this.ownerUUID = nbt.getUniqueId("Owner");
            this.owner = new WeakReference<>(DimBag.getServer().getPlayerList().getPlayerByUUID(this.ownerUUID));
        }
        this.tpDimension = WorldUtils.stringToWorldRK(nbt.getString("Dim"));
        this.tpPosition = new Vector3d(nbt.getDouble("X"), nbt.getDouble("Y"), nbt.getDouble("Z"));
        this.inventory.read(nbt.getCompound("Inventory"));
        this.tanks.read(nbt.getCompound("Tanks"));
        int energyCapacity = nbt.getInt("MaxEnergy");
        this.energyStorage = new EnergyStorage(energyCapacity, energyCapacity / 128, energyCapacity / 128, nbt.getInt("Energy"));
        this.modeManager = new ModeManager(this);
        this.modeManager.read(nbt.getCompound("Modes"));
        this.upgradesNBT = nbt.getCompound("UpgradesData");
        ListNBT listSubRooms = nbt.getList("SubRooms", 10);
        this.idToSubRooms = new ArrayList<>();
        this.subRoomsToId = new HashMap<>();
        for (int i = 0; i < listSubRooms.size(); ++i) {
            BlockPos room = NBTUtil.readBlockPos(listSubRooms.getCompound(i));
            idToSubRooms.add(i, room);
            subRoomsToId.put(room, i);
        }
    }
}*/
