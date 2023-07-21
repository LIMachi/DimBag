package com.limachi.dim_bag.save_datas;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.bag_data.BagInstance;
import com.limachi.lim_lib.Configs;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * did I just reinvent world capabilities? probably... but at least I understand the invalidation/rebuild process
 * (invalidators didn't seem to fire on level capabilities, resulting in garbage when switching between saves/servers)
 */
@Mod.EventBusSubscriber(modid = DimBag.MOD_ID)
public class BagsData extends SavedData {

    @Configs.Config(path = "rooms", cmt = "Initial size of a new bag (in blocks, including walls)")
    public static int DEFAULT_ROOM_RADIUS = 3;

    @Configs.Config(path = "rooms", cmt = "Blocks between each room centers. CHANGING THIS WILL CORRUPT EXISTING WORLDS!")
    public static int ROOM_SPACING = 1024;

    @Configs.Config(path = "rooms", min = "3", max = "126", cmt = "Maximum size of a bag (in blocks, including walls)")
    public static int MAXIMUM_ROOM_RADIUS = 64;

    private static BagsData INSTANCE = null;
    private static LinkedList<Runnable> INVALIDATORS = new LinkedList<>();

    ListTag raw;
    private final ArrayList<BagInstance> instances = new ArrayList<>();

    private static BagInstance roomAt(Level level, BlockPos pos) {
        if (level instanceof ServerLevel && level.dimension().equals(DimBag.BAG_DIM)) {
            int id = (pos.getX() / ROOM_SPACING) + 1;
            if (id > 0 && INSTANCE != null && id <= INSTANCE.instances.size()) {
                BagInstance bag = INSTANCE.instances.get(id - 1);
                if (bag.isInRoom(pos))
                    return bag;
            }
        }
        return null;
    }

    public static boolean isWall(Level level, BlockPos pos) {
        if (level instanceof ServerLevel && level.dimension().equals(DimBag.BAG_DIM)) {
            int id = (pos.getX() / ROOM_SPACING) + 1;
            if (id > 0 && INSTANCE != null && id <= INSTANCE.instances.size())
                return INSTANCE.instances.get(id - 1).isWall(pos);
        }
        return false;
    }

    public static BlockPos roomCenter(int id) { return new BlockPos(8 + (id - 1) * ROOM_SPACING, 128, 8); }

    /**
     * <pre>
     * Get a handle that will be valid for more than an instant, but require to be invalidated remotely
     * The invalidator CAN be null, but then you have to make sure to release the handle at the end of the calling function
     * example (99% of the usages will have this form):
     * {@code
     *      class tileEntityThing extends BlockEntity {
     *          private IBagInstance bag = null;
     *          private int bagId = 1;
     *
     *          ...
     *
     *          public IBagInstance getBag() {
     *              if (bag == null)
     *                  bag = BagsData.getBagHandle(bagId, ()->this.bag = null);
     *              return bag;
     *          }
     *      }
     * }
     * </pre>
     */
    public static BagInstance getBagHandle(int id, Runnable invalidator) {
        if (id > 0 && INSTANCE != null && id <= INSTANCE.instances.size()) {
            if (invalidator != null)
                INVALIDATORS.add(invalidator);
            return INSTANCE.instances.get(id - 1);
        }
        if (invalidator != null)
            invalidator.run();
        return null;
    }

    public static BagInstance getBagHandle(Level level, BlockPos pos, Runnable invalidator) {
        BagInstance out = roomAt(level, pos);
        if (out != null) {
            if (invalidator != null)
                INVALIDATORS.add(invalidator);
            return out;
        }
        if (invalidator != null)
            invalidator.run();
        return null;
    }

    /**
     * Alternative to {@link BagsData#getBagHandle} to run something on a bag immediately without keeping a handle
     */
    public static <T> T runOnBag(int id, Function<BagInstance, T> run, T onFail) {
        if (id > 0 && INSTANCE != null && id <= INSTANCE.instances.size())
            return run.apply(INSTANCE.instances.get(id - 1));
        return onFail;
    }

    public static boolean runOnBag(int id, Consumer<BagInstance> run) {
        if (id > 0 && INSTANCE != null && id <= INSTANCE.instances.size()) {
            run.accept(INSTANCE.instances.get(id - 1));
            return true;
        }
        return false;
    }

    public static <T> T runOnBag(Level level, BlockPos pos, Function<BagInstance, T> run, T onFail) {
        BagInstance out = roomAt(level, pos);
        if (out != null)
            return run.apply(out);
        return onFail;
    }

    public static boolean runOnBag(Level level, BlockPos pos, Consumer<BagInstance> run) {
        BagInstance out = roomAt(level, pos);
        if (out != null) {
            run.accept(out);
            return true;
        }
        return false;
    }

    public static int newBagId() {
        if (INSTANCE != null) {
            CompoundTag rawBag = new CompoundTag();
            INSTANCE.raw.add(rawBag);
            int id = INSTANCE.raw.size();
            INSTANCE.instances.add(new BagInstance(id, rawBag));
            return id;
        }
        return 0;
    }

    public static int maxBagId() {
        if (INSTANCE != null)
            return INSTANCE.instances.size();
        return 0;
    }

    private BagsData() {
        this(new CompoundTag());
    }
    private BagsData(CompoundTag data) {
        raw = data.getList("bags", Tag.TAG_COMPOUND);
        for (int i = 0; i < raw.size(); ++i)
            instances.add(new BagInstance(i + 1, raw.getCompound(i)));
    }

    private static void invalidate() {
        if (INSTANCE != null)
            for (BagInstance instance : INSTANCE.instances)
                instance.invalidate();
        INSTANCE = null;
        for (Runnable invalidator : INVALIDATORS)
            invalidator.run();
        INVALIDATORS.clear();
    }

    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level && level.dimension().equals(Level.OVERWORLD)) {
            invalidate();
            INSTANCE = level.getDataStorage().computeIfAbsent(BagsData::new, BagsData::new, "bags");
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level && level.dimension().equals(Level.OVERWORLD))
            invalidate();
    }

    @Override
    @Nonnull
    public CompoundTag save(@Nonnull CompoundTag nbt) {
        for (int i = 0; i < instances.size(); ++i)
            instances.get(i).storeOn(raw.getCompound(i));
        nbt.put("bags", raw);
        return nbt;
    }

    @Override
    public boolean isDirty() { return true; }
}
