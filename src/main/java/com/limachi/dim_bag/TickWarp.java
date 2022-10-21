package com.limachi.dim_bag;

import com.limachi.lim_lib.EntityUtils;
import com.limachi.lim_lib.Log;
import com.limachi.lim_lib.World;
import com.limachi.lim_lib.commands.CommandManager;
import com.limachi.lim_lib.commands.arguments.ChunkPosArg;
import com.limachi.lim_lib.commands.arguments.DoubleArg;
import com.limachi.lim_lib.saveData.AbstractSyncSaveData;
import com.limachi.lim_lib.saveData.RegisterSaveData;
import com.limachi.lim_lib.saveData.SaveDataManager;
import com.mojang.datafixers.util.Pair;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber
@RegisterSaveData
public final class TickWarp extends AbstractSyncSaveData {
    private static final HashSet<String> BLACK_LIST_BLOCKS = new HashSet<>();
    private static final HashSet<String> BLACK_LIST_ENTITIES = new HashSet<>();

    private final HashMap<ChunkPos, Double> ratios = new HashMap<>();
    private static final HashSet<Pair<Level, ChunkPos>> reloads = new HashSet<>();
    private static int entropy = 0;
    private static final int INFINITE_ENTROPY = 2073600000; //About 12 full real days of 100 tickers in a single chunk (20t*60s*60m*24h*12d*100e), equal to 96.56% of the positive int representation, leaving 73883647 ticks of margin to prevent overflow

    public static final int TICK_FLAG_BLOCK = 1;
    public static final int TICK_FLAG_ENTITIES = 2;
    public static final int TICK_FLAG_RANDOM = 4;

    public static int TICK_FLAG = TICK_FLAG_BLOCK | TICK_FLAG_ENTITIES;

    private static boolean tick() {
        if (entropy <= 0) return false;
        if (entropy < INFINITE_ENTROPY)
            --entropy;
        return true;
    }

    private static boolean canTick() { return entropy > 0 && TICK_FLAG != 0; }

    private static void store(int amount) {
        if (entropy <= INFINITE_ENTROPY)
            entropy += amount;
    }

    /**
     * only applies to block entities ticking, not random ticks!
     */
    public static void blackListBlockByRegex(String regex) { BLACK_LIST_BLOCKS.add(regex); }

    public static void blackListEntityByRegex(String regex) { BLACK_LIST_ENTITIES.add(regex); }

    public TickWarp(String name) { super(name); }

    static {
        CommandManager.registerCmd(TickWarp.class, "cWarp", s->s.hasPermission(2), "/tick_warp <chunk_pos> <ratio>", new ChunkPosArg(), new DoubleArg(0., 100.));
    }

    public static int cWarp(CommandSourceStack source, ChunkPos chunk, double ratio) {
        TickWarp instance = SaveDataManager.getInstance("tick_warp", source.getLevel());
        instance.warp(chunk, ratio);
        return 0;
    }

    public void warp(ChunkPos chunk, double ratio) {
        ratio = Mth.clamp(ratio, 0., 1024.);
        if (ratio == 1.) {
            if (ratios.containsKey(chunk)) {
                restoreTicking(chunk);
                ratios.remove(chunk);
                setDirty();
            }
            return;
        }
        else {
            Double r = ratios.get(chunk);
            if (ratio < 1. && (r == null || r > 1.) && (TICK_FLAG & TICK_FLAG_BLOCK) != 0) {
                Level sl = World.getLevel(level);
                sl.blockEntityTickers.removeIf(be -> !inRegexSet(ForgeRegistries.BLOCKS.getKey(sl.getBlockState(be.getPos()).getBlock()).toString(), BLACK_LIST_BLOCKS));
            }
        }
        ratios.put(chunk, ratio);
        setDirty();
    }

    private static boolean inRegexSet(String find, HashSet<String> set) {
        for (String regex : set)
            if (find.matches(regex))
                return true;
        return false;
    }

    private void restoreTicking(ChunkPos pos) {
        if (ratios.get(pos) < 1. && (TICK_FLAG & TICK_FLAG_BLOCK) != 0) {
            Level sl = World.getLevel(level);
            LevelChunk chunk = sl.getChunk(pos.x, pos.z);
            chunk.getBlockEntities().forEach((p, b) -> {
                if (!inRegexSet(ForgeRegistries.BLOCKS.getKey(b.getBlockState().getBlock()).toString(), BLACK_LIST_BLOCKS)) {
                    chunk.updateBlockEntityTicker(b);
                    sl.addBlockEntityTicker(chunk.tickersInLevel.get(p));
                }
            });
        }
    }

    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent event) { //FIXME: should probably also do this for multi place event (if multi place is on a chunk boundary, might miss an update)
        if (event.getPlacedBlock().getBlock() instanceof EntityBlock) {
            TickWarp instance = SaveDataManager.getInstance("tick_warp", (Level)event.getLevel());
            if (instance == null) return;
            ChunkPos pos = new ChunkPos(event.getBlockSnapshot().getPos());
            Double ratio = instance.ratios.get(pos);
            if (ratio != null && ratio < 1. && (TICK_FLAG & TICK_FLAG_BLOCK) != 0)
                reloads.add(new Pair<>((Level)event.getLevel(), pos));
        }
    }

    private static boolean forceTick = false;

    @SubscribeEvent
    public static void onEntityTick(LivingEvent.LivingTickEvent event) {
        if (!event.isCanceled() && !EntityUtils.truePlayer(event.getEntity()) && !forceTick && (TICK_FLAG & TICK_FLAG_ENTITIES) != 0) {
            LivingEntity entity = event.getEntity();
            if (entity.isRemoved() || inRegexSet(entity.getType().getDescription().getString(), BLACK_LIST_ENTITIES)) return;
            TickWarp instance = SaveDataManager.getInstance("tick_warp", entity.level);
            if (instance == null) return;
            Double ratio = instance.ratios.get(entity.chunkPosition());
            if (ratio != null && ratio < 1.)
                event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (!event.level.isClientSide() && event.level.dimension().equals(Level.OVERWORLD))
                Log.warn("Current global entropy: " + entropy);
            TickWarp instance = SaveDataManager.getInstance("tick_warp", event.level);
            instance.ratios.forEach((p, r) -> {
                LevelChunk chunk = event.level.getChunk(p.x, p.z);
                Pair<Level, ChunkPos> tlcp = new Pair<>(event.level, p);
                if (reloads.contains(tlcp)) {
                    reloads.remove(tlcp);
                    if (r < 1. && (TICK_FLAG & TICK_FLAG_BLOCK) != 0)
                        event.level.blockEntityTickers.removeIf(be->!inRegexSet(ForgeRegistries.BLOCKS.getKey(event.level.getBlockState(be.getPos()).getBlock()).toString(), BLACK_LIST_BLOCKS));
                }
                if (r >= 2 && canTick()) {
                    forceTick = true;
                    List<LivingEntity> le = event.level.getEntities(EntityTypeTest.forClass(LivingEntity.class), new AABB(p.x << 4, chunk.getMinBuildHeight(), p.z << 4, (p.x << 4) + 16, chunk.getMaxBuildHeight(), (p.z << 4) + 16), fe -> !inRegexSet(fe.getType().getDescription().getString(), BLACK_LIST_ENTITIES));
                    int rt = event.level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
                    for (int i = 1; i < r; ++i) {
                        if ((TICK_FLAG & TICK_FLAG_BLOCK) != 0 && canTick()) chunk.tickersInLevel.forEach((bp, w) -> {
                            if (tick()) w.tick();
                        });
                        if ((TICK_FLAG & TICK_FLAG_ENTITIES) != 0 && canTick()) le.forEach(e -> {
                            if (tick()) e.tick();
                        });
                        if ((TICK_FLAG & TICK_FLAG_RANDOM) != 0 && canTick() && rt > 0 && event.level instanceof ServerLevel sl)
                            for (LevelChunkSection section : chunk.getSections())
                                if (!section.hasOnlyAir() && section.isRandomlyTicking())
                                    for (int k = 0; k < rt; ++k) {
                                        BlockPos blockpos = sl.getBlockRandomPos(chunk.getPos().getMinBlockX(), section.bottomBlockY(), chunk.getPos().getMinBlockZ(), 15);
                                        BlockState blockstate = section.getBlockState(blockpos.getX() - chunk.getPos().getMinBlockX(), blockpos.getY() - section.bottomBlockY(), blockpos.getZ() - chunk.getPos().getMinBlockZ());
                                        if (blockstate.isRandomlyTicking() && tick())
                                            blockstate.randomTick(sl, blockpos, sl.random);
                                        FluidState fluidState = section.getFluidState(blockpos.getX() - chunk.getPos().getMinBlockX(), blockpos.getY() - section.bottomBlockY(), blockpos.getZ() - chunk.getPos().getMinBlockZ());
                                        if (fluidState.isRandomlyTicking() && tick())
                                            fluidState.randomTick(sl, blockpos, sl.random);
                                    }
                    }
                    forceTick = false;
                }
                else if (r >= 0 && r < 1) {
                    if (r > 0) {
                        int ri = (int) Math.round(1. / r);
                        if (World.tick() % ri == 0) {
                            forceTick = true;
                            if ((TICK_FLAG & TICK_FLAG_BLOCK) != 0) chunk.tickersInLevel.forEach((bp, w) -> w.tick());
                            if ((TICK_FLAG & TICK_FLAG_ENTITIES) != 0) event.level.getEntities(EntityTypeTest.forClass(LivingEntity.class), new AABB(p.x << 4, chunk.getMinBuildHeight(), p.z << 4, (p.x << 4) + 16, chunk.getMaxBuildHeight(), (p.z << 4) + 16), fe -> !inRegexSet(fe.getType().getDescription().getString(), BLACK_LIST_ENTITIES)).forEach(LivingEntity::tick);
                            forceTick = false;
                            return;
                        }
                    }
                    if ((TICK_FLAG & TICK_FLAG_BLOCK) != 0) store(chunk.tickersInLevel.size());
                    if ((TICK_FLAG & TICK_FLAG_ENTITIES) != 0) store(event.level.getEntities(EntityTypeTest.forClass(LivingEntity.class), new AABB(p.x << 4, chunk.getMinBuildHeight(), p.z << 4, (p.x << 4) + 16, chunk.getMaxBuildHeight(), (p.z << 4) + 16), fe -> !inRegexSet(fe.getType().getDescription().getString(), BLACK_LIST_ENTITIES)).size());
                    if ((TICK_FLAG & TICK_FLAG_RANDOM) != 0) store(event.level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING));
                }
            });
        }
    }

    @NotNull
    @Override
    public CompoundTag save(CompoundTag nbt) {
        ListTag list = new ListTag();
        for (Map.Entry<ChunkPos, Double> entry : ratios.entrySet()) {
            CompoundTag o = new CompoundTag();
            o.putDouble("Ratio", entry.getValue());
            o.putLong("Chunk", entry.getKey().toLong());
            list.add(o);
        }
        nbt.put("Warps", list);
        return nbt;
    }

    @Override
    public void load(CompoundTag nbt) {
        ListTag list = nbt.getList("Warps", Tag.TAG_COMPOUND);
        for (ChunkPos pos : ratios.keySet())
            restoreTicking(pos);
        ratios.clear();
        for (Tag entry : list) {
            ChunkPos pos = new ChunkPos(((CompoundTag)entry).getLong("Chunk"));
            double ratio = ((CompoundTag)entry).getDouble("Ratio");
            if (ratio < 1. && (TICK_FLAG & TICK_FLAG_BLOCK) != 0) {
                Level sl = World.getLevel(level);
                sl.blockEntityTickers.removeIf(be -> !inRegexSet(ForgeRegistries.BLOCKS.getKey(sl.getBlockState(be.getPos()).getBlock()).toString(), BLACK_LIST_BLOCKS));
            }
            ratios.put(pos, ratio);
        }
    }
}
