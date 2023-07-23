package com.limachi.dim_bag.bag_modules;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.bag_data.BagInstance;
import com.limachi.dim_bag.menus.TeleporterMenu;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.lim_lib.Configs;
import com.limachi.lim_lib.World;
import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import com.limachi.lim_lib.registries.annotations.RegisterBlockItem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

@SuppressWarnings({"unused", "deprecation"})
public class TeleportModule extends BaseModule {

    @Configs.Config(cmt = "If set to true, a teleporter will be considered disabled if an entity is already present. Set to false to stack infinite amount of entities on the same teleporter.")
    public static boolean NO_MORE_THAN_ONE_ENTITY_PER_TELEPORTER = true;

    public Component DEFAULT_LABEL = Component.translatable("teleporter.data.label.default");

    @RegisterBlock
    public static RegistryObject<TeleportModule> R_BLOCK;
    @RegisterBlockItem
    public static RegistryObject<BlockItem> R_ITEM;

    @Override
    public void neighborChanged(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Block block, @Nonnull BlockPos pos2, boolean bool) {
        BagsData.runOnBag(level, pos, b->b.getInstalledCompound("teleport", pos).putBoolean("active", level.getBestNeighborSignal(pos) == 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING);
    }

    @Override
    protected void init() {
        registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.FACING, Direction.UP));
    }

    @Override
    public void install(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) {
        CompoundTag data = stack.getOrCreateTag().copy();
        if (data.contains("display", Tag.TAG_COMPOUND)) {
            data.putString("label", data.getCompound("display").getString("Name"));
            data.remove("display");
        }
        if (!data.contains("active", Tag.TAG_BYTE))
            data.putBoolean("active", true);
        if (!data.contains("label", Tag.TAG_STRING))
            data.putString("label", Component.Serializer.toJson(DEFAULT_LABEL));
        bag.compoundInstall("teleport", pos, data);
        if (bag.getModeData("Capture").isEmpty())
            bag.installMode("Capture");
        bag.getModeData("Capture").ifPresent(c->{
            if (c.getBoolean("disabled")) {
                c.putBoolean("disabled", false);
                c.putLong("selected", pos.asLong());
            } else if (!c.contains("selected"))
                c.putLong("selected", pos.asLong());
        });
    }

    @Override
    public void uninstall(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) {
        bag.getModeData("Capture").ifPresent(c->{
            ListTag teleporters = bag.unsafeRawAccess().getCompound("modules").getList("teleport", Tag.TAG_COMPOUND);
            if (teleporters.size() <= 1)
                c.putBoolean("disabled", true);
            else if (c.getLong("selected") == pos.asLong()) {
                long p = teleporters.getCompound(0).getLong("position");
                if (p != 0)
                    c.putLong("selected", p);
                else
                    c.remove("selected");
            }
        });
        stack.getOrCreateTag().merge(bag.compoundUninstall("teleport", pos));
    }

    @Override
    public boolean use(BagInstance bag, Player player, Level level, BlockPos pos, InteractionHand hand) {
        TeleporterMenu.open(player, pos);
        return true;
    }

    @Override
    public boolean wrench(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) {
        BlockState bs = level.getBlockState(pos);
        level.setBlockAndUpdate(pos, bs.setValue(BlockStateProperties.FACING, Direction.values()[(bs.getValue(BlockStateProperties.FACING).ordinal() + 1) % Direction.values().length]));
        return true;
    }

    public static Optional<BlockPos> getDestination(BagInstance bag, Entity entity) {
        return bag.getModeData("Capture").flatMap(c->{
            BlockPos selected = BlockPos.of(c.getLong("selected"));
            CompoundTag target = bag.getInstalledCompound("teleport", selected);
            Level level = World.getLevel(DimBag.BAG_DIM);
            if (!(level.getBlockState(selected).getBlock() instanceof TeleportModule))
                return Optional.empty();
            Direction offset = level.getBlockState(selected).getValue(BlockStateProperties.FACING); //FIXME: might have invalid state on desync
            if (!target.getBoolean("active"))
                return Optional.empty();
            if (NO_MORE_THAN_ONE_ENTITY_PER_TELEPORTER && getTarget(level, selected, offset).isPresent())
                return Optional.empty();
            if (entity instanceof Player && !target.getBoolean("affect_players"))
                return Optional.empty();
            String name = entity.getDisplayName().getString();
            boolean whitelist = target.getBoolean("white_list");
            for (Tag t : target.getList("filters", Tag.TAG_STRING))
                if (t instanceof StringTag s && (s.getAsString().equals(name) || name.matches(s.getAsString())))
                    return whitelist ? Optional.of(selected.relative(offset)) : Optional.empty();
            return whitelist ? Optional.empty() : Optional.of(selected.relative(offset));
        });
    }

    public static Optional<? extends Entity> getTarget(Level level, BlockPos pos, Direction offset) {
        List<Entity> found = level.getEntities((Entity)null, new AABB(pos.relative(offset)), e->true);
        return found.isEmpty() ? Optional.empty() : Optional.of(found.get(0));
    }

    public static Optional<Pair<Component, Optional<? extends Entity>>> getSelectedTeleporterAndTarget(int bag) {
        return BagsData.runOnBag(bag, b->b.getModeData("Capture").flatMap(c->{
            if (c.contains("selected") && !c.getBoolean("disabled")) {
                BlockPos at = BlockPos.of(c.getLong("selected"));
                CompoundTag teleporter = b.getInstalledCompound("teleport", at);
                Component label = Component.Serializer.fromJson(teleporter.getString("label"));
                Level level = World.getLevel(DimBag.BAG_DIM);
                if (level.getBlockState(at).getBlock() instanceof TeleportModule) {
                    Direction offset = level.getBlockState(at).getValue(BlockStateProperties.FACING); //FIXME: might have invalid state on desync
                    return Optional.of(new Pair<>(label, teleporter.getBoolean("active") ? getTarget(level, at, offset) : Optional.empty()));
                }
            }
            return Optional.empty();
        }), Optional.empty());
    }

    public static boolean teleportTargetTo(int bag, ResourceKey<Level> level, BlockPos pos) {
        return BagsData.runOnBag(bag, b->b.getModeData("Capture").map(c->{
            if (c.contains("selected") && !c.getBoolean("disabled")) {
                BlockPos at = BlockPos.of(c.getLong("selected"));
                CompoundTag teleporter = b.getInstalledCompound("teleport", at);
                Level lvl = World.getLevel(DimBag.BAG_DIM);
                if (lvl.getBlockState(at).getBlock() instanceof TeleportModule) {
                    Direction offset = lvl.getBlockState(at).getValue(BlockStateProperties.FACING); //FIXME: might have invalid state on desync
                    return teleporter.getBoolean("active") && getTarget(lvl, at, offset).map(e -> World.teleportEntity(e, level, pos) != null).orElse(false);
                }
            }
            return false;
        }).orElse(false), false);
    }
}
