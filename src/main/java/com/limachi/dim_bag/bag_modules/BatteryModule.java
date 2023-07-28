package com.limachi.dim_bag.bag_modules;

import com.limachi.dim_bag.bag_data.BagInstance;
import com.limachi.dim_bag.bag_modules.block_entity.BatteryModuleBlockEntity;
import com.limachi.lim_lib.registries.annotations.RegisterBlock;
import com.limachi.lim_lib.registries.annotations.RegisterBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class BatteryModule extends BaseModule implements EntityBlock {

    @OnlyIn(Dist.CLIENT)
    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class BatteryColorHandler {
        @SubscribeEvent
        public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
            event.register(BatteryColorHandler::getColor, R_BLOCK.get());
        }

        public static int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int index) {
            if (index == 0) return 0xFFFFFFFF;
            if (index == 2) return 0xFF555555;
            if (level != null && pos != null && level.getBlockEntity(pos) instanceof BatteryModuleBlockEntity battery) return battery.getColor();
            return 0xFFAAAAAA;
        }
    }

    public enum PushPull implements StringRepresentable {
        NONE("none"),
        PUSH("push"),
        PULL("pull");

        public final String val;

        PushPull(String val) { this.val = val; }

        @Override
        public String getSerializedName() { return val; }

        public PushPull cycle() {
            return switch (this) {
                case NONE -> PUSH;
                case PUSH -> PULL;
                case PULL -> NONE;
            };
        }
    }

    public static final IntegerProperty FULLNESS = IntegerProperty.create("fullness", 0, 13);
    public static final EnumProperty<PushPull> UP = EnumProperty.create("up", PushPull.class);
    public static final EnumProperty<PushPull> DOWN = EnumProperty.create("down", PushPull.class);
    public static final EnumProperty<PushPull> NORTH = EnumProperty.create("north", PushPull.class);
    public static final EnumProperty<PushPull> EAST = EnumProperty.create("east", PushPull.class);
    public static final EnumProperty<PushPull> SOUTH = EnumProperty.create("south", PushPull.class);
    public static final EnumProperty<PushPull> WEST = EnumProperty.create("west", PushPull.class);

    public static final String NAME = "battery";

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FULLNESS, UP, DOWN, NORTH, EAST, SOUTH, WEST);
    }

    @Override
    protected void init() {
        registerDefaultState(stateDefinition.any()
            .setValue(FULLNESS, 0)
            .setValue(UP, PushPull.NONE).setValue(DOWN, PushPull.NONE)
            .setValue(NORTH, PushPull.NONE).setValue(SOUTH, PushPull.NONE)
            .setValue(EAST, PushPull.NONE).setValue(WEST, PushPull.NONE));
    }

    @Override
    public boolean wrench(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack, BlockHitResult hit) {
        BlockState bs = level.getBlockState(pos);
        level.setBlockAndUpdate(pos, switch (hit.getDirection()) {
            case UP -> bs.setValue(UP, bs.getValue(UP).cycle());
            case DOWN -> bs.setValue(DOWN, bs.getValue(DOWN).cycle());
            case NORTH -> bs.setValue(NORTH, bs.getValue(NORTH).cycle());
            case EAST -> bs.setValue(EAST, bs.getValue(EAST).cycle());
            case SOUTH -> bs.setValue(SOUTH, bs.getValue(SOUTH).cycle());
            case WEST -> bs.setValue(WEST, bs.getValue(WEST).cycle());
        });
        return true;
    }

    @RegisterBlock
    public static RegistryObject<BatteryModule> R_BLOCK;
    @RegisterBlockItem
    public static RegistryObject<BlockItem> R_ITEM;

    @Override
    public void install(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) {
        bag.installModule(NAME, pos, new CompoundTag());
        bag.energyHandle().ifPresent(e->e.addBatteryModule(stack.getOrCreateTag().getLong(NAME)));
    }

    @Override
    public void uninstall(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) {
        bag.uninstallModule(NAME, pos);
        bag.energyHandle().ifPresent(e->stack.getOrCreateTag().putLong(NAME, e.removeBatteryModule()));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return BatteryModuleBlockEntity.R_TYPE.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level unusedLevel, @Nonnull BlockState unusedState, @Nonnull BlockEntityType<T> unusedType) {
        return (level, pos, state, be) -> {
            if (be instanceof BatteryModuleBlockEntity o)
                o.tick();
        };
    }
}
