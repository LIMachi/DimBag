package com.limachi.dimensional_bags.common.data.EyeDataMK2;

import com.limachi.dimensional_bags.CuriosIntegration;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.data.DimBagData;
import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.limachi.dimensional_bags.common.inventory.EntityInventoryProxy;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.items.GhostBag;
import com.limachi.dimensional_bags.common.items.entity.BagEntityItem;
import com.limachi.dimensional_bags.common.items.upgrades.bag.ParadoxUpgrade;
import com.limachi.dimensional_bags.common.managers.ModeManager;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import com.limachi.dimensional_bags.utils.UUIDUtils;
import com.limachi.dimensional_bags.utils.WorldUtils;
import com.limachi.dimensional_bags.common.data.IEyeIdHolder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

@Mod.EventBusSubscriber
public class HolderData extends WorldSavedDataManager.EyeWorldSavedData {

    private EntityInventoryProxy invProxy = new EntityInventoryProxy(null);
    private WeakReference<Entity> holderRef = new WeakReference<>(null);
    private UUID id = UUIDUtils.NULL_UUID;
    private String name = "";
    private Vector3d lastKnownPosition = null;
    private RegistryKey<World> lastKnownDimension = null;

    public HolderData(String suffix, int id, boolean client) {
        super(suffix, id, client, false);
    }

    private static HashMap<Integer, ArrayList<Entity>> tickingHolders = new HashMap<>();

    /**
     * get a value describing how much this entity should be able to keep it's bag, order as follows:
     * owner
     * main holder
     * bag entity
     * non bag item or proxy holder
     * bag item
     * proxy/other
     */
    private static int keepValue(Entity e, HolderData hd, OwnerData od) {
        if (e.equals(od.getPlayer())) return 5;
        if (e.equals(hd.getEntity())) return 4;
        if (e instanceof BagEntity) return 3;
        if (!(e instanceof FakePlayer || e instanceof BagEntityItem)) return 2;
        if (e instanceof BagEntityItem) return 1;
        return 0;
    }

    /**
     * only called once per bag id and per tick, holder is the 'best' valid holder that used the bag this tick (other holders will see their inventory or themselves cleared)
     */
    private static void tickBag(Entity holder, int id, HolderData data, DimBagData dbd) {
        boolean isBagItself = holder instanceof BagEntityItem || holder instanceof BagEntity;

        if (!holder.level.isClientSide) {
            if (!(holder instanceof FakePlayer))
                data.setHolder(holder);
            if (holder.blockPosition().getY() < 1 && (isBagItself || false/*protect against void*/)) //FIXME: cleaner version
                holder.setPos(holder.blockPosition().getX(), 1, holder.blockPosition().getZ());
        }

        if (!(holder instanceof PlayerEntity) && dbd != null && holder.level instanceof ServerWorld) { //FIXME: should use a cleaner way of maintaining chunk loaded
            dbd.chunkloadder.unloadChunk(id);
            dbd.chunkloadder.loadChunk((ServerWorld) holder.level, holder.blockPosition(), id);
            dbd.setDirty();
        }

        ModeManager.execute(id, modeManager -> modeManager.inventoryTick(holder.level, holder));
        UpgradeManager.execute(id, upgradeManager -> upgradeManager.inventoryTick(holder.level, holder));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void tickBagsThenCleanDuplicateBags(TickEvent.ServerTickEvent event) {
        DimBagData dbd = DimBagData.get();
        for (Map.Entry<Integer, ArrayList<Entity>> e : tickingHolders.entrySet()) {
            int id = e.getKey();
            ArrayList<Entity> el = e.getValue();
            HolderData hd = HolderData.getInstance(id);
            if (el.size() <= 1) {
                if (el.size() == 1)
                    tickBag(el.get(0), id, hd, dbd);
                el.clear();
                continue;
            }
            OwnerData od = OwnerData.getInstance(id);
            Entity best = el.get(0);
            int k = keepValue(best, hd, od);
            for (Entity h : el) {
                int tk = keepValue(h, hd, od);
                if (tk > k) {
                    best = h;
                    k = tk;
                }
            }
            tickBag(best, id, hd, dbd);
            for (Entity h : el) { //FIXME: sometimes trigger a concurrent change, need to investigate
                if (h instanceof FakePlayer) continue;
                AtomicBoolean keep = new AtomicBoolean(h.equals(best));
                if (h instanceof BagEntityItem || h instanceof BagEntity) {
                    if (keep.get()) continue;
                    h.remove();
                } else CuriosIntegration.searchItem(h, Bag.class, stack->!(stack.getItem() instanceof GhostBag) && Bag.getEyeId(stack) == id, true).forEach(p->{
                    if (keep.get()) { //if this is the best entity, skip the first instance of bag
                        keep.set(false);
                        return;
                    }
                    p.set(ItemStack.EMPTY);
                });
            }
        }
        tickingHolders.clear();
    }

    private static void addTickingBag(Entity holder, int id, @Nullable CuriosIntegration.ProxySlotModifier slot) {
        //handles invalid id/creating new id
        if (id <= 0) {
            if (holder instanceof ServerPlayerEntity && slot != null) {
                ItemStack b = slot.get();
                id = DimBagData.get().newEye((ServerPlayerEntity) holder, b);
                slot.set(b);
            }
            else
                return;
        }

        //handles invalid tick position (bag ticking inside itself without correct rights)
        if (!(holder instanceof ItemEntity || holder instanceof BagEntity) && holder instanceof LivingEntity && holder.level.dimension().equals(WorldUtils.DimBagRiftKey) && !(holder instanceof PlayerEntity && (((PlayerEntity)holder).isCreative() || ((PlayerEntity)holder).isSpectator()))) {
            SubRoomsManager srm = SubRoomsManager.getInstance(id);
            if (srm != null) {
                int hid = SubRoomsManager.getEyeId(holder.level, holder.blockPosition(), false);
                if (hid == id && !ParadoxUpgrade.getInstance(ParadoxUpgrade.NAME).isActive(id)) {//paradox upgrade not active and bag ticking inside itself, we unequip and remove bags (teleport them outside)
                    Bag.unequipBags((LivingEntity) holder, id, null, null).forEach(b -> srm.leaveBag(b, false, null, null, holder.getPersistentData().getCompound(DimBag.MOD_ID).getCompound(Integer.toString(hid)).getCompound("proxy")));
                    return;
                }
            }
        }

        //store this entity for tick processing
        ArrayList<Entity> t = tickingHolders.computeIfAbsent(id, i->new ArrayList<>());
        t.add(holder);
    }

    public static void tickBagWithFakePlayer(int id, World world) {
        addTickingBag(Bag.getFakePlayer(id, (ServerWorld) world).get(), id, null);
    }

    /**
     * called by any living entity (via event) and by the bag item
     */
    public static void tickEntity(Entity entity) {
        if (entity.removed) return;
        if (entity instanceof BagEntityItem || entity instanceof BagEntity)
            addTickingBag(entity, entity instanceof BagEntityItem ? Bag.getEyeId(((BagEntityItem) entity).getItem()) : ((BagEntity) entity).getEyeId(), null);
        else if (entity instanceof LivingEntity)
            CuriosIntegration.searchItem(entity, Bag.class, stack -> !(stack.getItem() instanceof GhostBag), true).forEach(p->addTickingBag(entity, Bag.getEyeId(p.get()), p));
    }

    @SubscribeEvent
    public static void tickLivingEntity(LivingEvent.LivingUpdateEvent event) {
        Entity entity = event.getEntity();
        if (entity.level.dimension().equals(WorldUtils.DimBagRiftKey) && !(entity instanceof ServerPlayerEntity && (((ServerPlayerEntity)entity).isCreative() || ((ServerPlayerEntity)entity).isSpectator()))) { //fix position if the entity is not a creative or spectator player inside the bag dimension
            if (SubRoomsManager.getEyeId(entity.level, entity.blockPosition(), false) == 0) { //invalid position (outside a bag)
                int t = SubRoomsManager.getClosestBag(entity.level, entity.blockPosition());
                if (t == 0) {
                    if (entity.getPersistentData().contains("PKBDTP")) {
                        CompoundNBT nbt = entity.getPersistentData().getCompound("PKBDTP");
                        entity.getPersistentData().remove("PKBDTP");
                        entity = WorldUtils.teleportEntity(entity, WorldUtils.stringToWorldRK(nbt.getString("D")), nbt.getInt("X"), nbt.getInt("Y"), nbt.getInt("Z"));
                    }
                } else {
                    Entity finalEntity = entity;
                    SubRoomsManager.execute(t, srm->srm.enterBag(finalEntity, false, true, true, false, false));
                }
            }
        }
        tickEntity(entity);
    }

    private void setHolder(Entity entity) { //TODO: add check for invalid entering of bag while equipped
        boolean dirty = false;
        if (entity != null && getEyeId() != SubRoomsManager.getEyeId(entity.level, entity.blockPosition(), false) && (!entity.position().equals(lastKnownPosition) || !entity.level.dimension().equals(lastKnownDimension))) {
            lastKnownPosition = entity.position();
            lastKnownDimension = entity.level.dimension();
            dirty = true;
        }
        if (holderRef.get() != entity) {
            holderRef = new WeakReference<>(entity);
            invProxy.setEntity(entity);
            if (entity != null) {
                name = entity.getDisplayName().getString();
                id = entity.getUUID();
            } else {
                name = "";
                id = UUIDUtils.NULL_UUID;
            }
            dirty = true;
        }
        if (dirty)
            setDirty();
    }

    public RegistryKey<World> getLastKnownDimension() { return lastKnownDimension; }

    public Vector3d getLastKnownPosition() { return lastKnownPosition; }

    public Entity tpToHolder(Entity entity) {
        return WorldUtils.teleportEntity(entity, lastKnownDimension, lastKnownPosition);
    }

    /**
     * @return if available (in loadded chunk and not removed), return the last known holder of this eye
     */
    public Entity getEntity() {
        Entity entity = holderRef.get();
        if (entity != null && !entity.removed)
            return entity;
        return null;
    }

    public EntityInventoryProxy getEntityInventory() { return invProxy; }

    /**
     * @return if available (not removed from the chunk), return the last known holder of this eye, will load the chunk to refresh the reference of the holder if needed
     */
    public Entity getEntityForceLoad() {
        if (id.equals(UUIDUtils.NULL_UUID)) return null;
        Entity entity = holderRef.get();
        if (entity != null && !entity.removed)
            return entity;
        if (lastKnownPosition != null && lastKnownDimension != null) {
            ServerWorld world = WorldUtils.getWorld(DimBag.getServer(), lastKnownDimension);
            if (world != null) {
                entity = WorldUtils.getEntityByUUIDInChunk((Chunk) world.getChunk(new BlockPos(lastKnownPosition)), id);
                if (entity != null && !entity.removed) {
                    holderRef = new WeakReference<>(entity);
                    invProxy.setEntity(entity);
                    name = entity.getDisplayName().getString();
                    lastKnownPosition = entity.position();
                }
            }
        }
        return null;
    }

    /**
     * @return true if the entity holding the bag is itself (either as an item on the ground or the bag entity itself)
     */
    public boolean isHolderItself() {
        if (getEyeId() != 0) {
            Entity entity = getEntity();
            if (entity == null) return false;
            return entity instanceof IEyeIdHolder && ((IEyeIdHolder)entity).getEyeId() == getEyeId();
        }
        return false;
    }

    /**
     * @return the name of the holder, the entity isn't required to be loaded/valid, return "" as an invalid/unset holder
     */
    public String getEntityName() { return name; }

    /**
     * @return the uuid of the holder, the entity isn't required to be loaded/valid, return UUIDUtils.NULL_UUID as an invalid/unset holder (aka new UUID(0,0))
     */
    public UUID getEntityUUID() { return id; }

    @Override
    public void load(CompoundNBT nbt) {
        id = nbt.getUUID("Id");
        name = nbt.getString("Name");
        lastKnownDimension = WorldUtils.stringToWorldRK(nbt.getString("Dimension"));
        lastKnownPosition = new Vector3d(nbt.getDouble("X"), nbt.getDouble("Y"), nbt.getDouble("Z"));
        holderRef = new WeakReference<>(null);
        invProxy.setEntity(null);
        if (!id.equals(UUIDUtils.NULL_UUID) && lastKnownPosition != null && lastKnownDimension != null)
            getEntityForceLoad();
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        nbt.putUUID("Id", id);
        nbt.putString("Name", name);
        if (lastKnownPosition != null) {
            nbt.putDouble("X", lastKnownPosition.x);
            nbt.putDouble("Y", lastKnownPosition.y);
            nbt.putDouble("Z", lastKnownPosition.z);
        }
        if (lastKnownDimension != null)
            nbt.putString("Dimension", WorldUtils.worldRKToString(lastKnownDimension));
        return nbt;
    }

    static public HolderData getInstance(int id) { return WorldSavedDataManager.getInstance(HolderData.class, id); }

    static public <T> T execute(int id, Function<HolderData, T> executable, T onErrorReturn) { return WorldSavedDataManager.execute(HolderData.class, id, executable, onErrorReturn); }

    static public boolean execute(int id, Consumer<HolderData> executable) { return WorldSavedDataManager.execute(HolderData.class, id, data->{executable.accept(data); return true;}, false); }
}
