package com.limachi.dimensional_bags.common.bagDimensionOnly.bagBattery;

import com.limachi.dimensional_bags.lib.ConfigManager.Config;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.references.Registries;
import com.limachi.dimensional_bags.lib.common.blocks.AbstractTileEntityBlock;
import com.limachi.dimensional_bags.lib.common.blocks.IBagWrenchable;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.EnergyData;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.SubRoomsManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

@StaticInit
public class BatteryBlock extends AbstractTileEntityBlock<BatteryTileEntity> implements IBagWrenchable {

    @Config(min = "128", max = "16777216", cmt = "how much a single crystal adds to the global energy capacity of a bag")
    public static int ENERGY_PER_BATTERY = 524288;
    @Config(min = "1048576", max = "2147483647", cmt = "maximum amount of energy that can be stored in a network of crystals (per bag)")
    public static int TOTAL_MAX_ENERGY = 67108864;

    @Config(min = "8", max = "65536", cmt = "how much a single crystal adds to the global output capacity of crystals (each additional crystal will increase the amount of energy each crystal can output by this amount)")
    public static int OUTPUT_PER_BATTERY = 256;
    @Config(min = "8", max = "2147483647", cmt = "maximum amount of energy that can be extracted by crystals, machines and upgrades, per tick and per face")
    public static int TOTAL_MAX_OUTPUT = 32768;

    @Config(min = "8", max = "65536", cmt = "how much a single crystal adds to the global input capacity of crystals (each additional crystal will increase the amount of energy each crystal can input by this amount)")
    public static int INPUT_PER_BATTERY = 256;
    @Config(min = "8", max = "2147483647", cmt = "maximum amount of energy that can be inserted by crystals, machines and upgrades, per tick and per face")
    public static int TOTAL_MAX_INPUT = 32768;

    public static final String NAME = "battery";

    public static final Supplier<BatteryBlock> INSTANCE = Registries.registerBlock(NAME, BatteryBlock::new);
    public static final Supplier<BlockItem> INSTANCE_ITEM = Registries.registerBlockItem(NAME, NAME, DimBag.DEFAULT_PROPERTIES);

    public static final BooleanProperty PULL = BooleanProperty.create("pull");

    public BatteryBlock() {
        super(NAME, Properties.of(Material.HEAVY_METAL).strength(1.5f, 3600000f).sound(SoundType.GLASS), BatteryTileEntity.class, BatteryTileEntity.NAME);
    }

    @Override
    protected boolean canPlace(BlockItemUseContext context) {
        return EnergyData.execute(SubRoomsManager.getbagId(context.getLevel(), context.getClickedPos(), false), EnergyData::canAddCrystal, false);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) { builder.add(PULL); }

    @Nullable
    @Override
    public TileEntity newBlockEntity(@Nonnull IBlockReader worldIn) { return Registries.getBlockEntityType(BatteryTileEntity.NAME).create(); }

    @Override
    public <B extends AbstractTileEntityBlock<BatteryTileEntity>> B getInstance() { return (B)INSTANCE.get(); }

    @Override
    public BlockItem getItemInstance() { return INSTANCE_ITEM.get(); }

    @Override
    public ItemStack asItem(BlockState state, @Nullable BatteryTileEntity tileEntity) {
        ItemStack out = super.asItem(state, tileEntity);
        if (tileEntity != null && out.getTag() != null)
            out.getTag().putInt("LocalEnergy", tileEntity.getLocalEnergy());
        return out;
    }

    @Override
    public void onValidPlace(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack, BatteryTileEntity crystal) {
        EnergyData.execute(SubRoomsManager.getbagId(worldIn, pos, false), ed->ed.addCrystal(stack.getTag() != null ? stack.getTag().getInt("LocalEnergy") : 0));
    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving, BatteryTileEntity crystal) {
        EnergyData.execute(SubRoomsManager.getbagId(worldIn, pos, false), EnergyData::removeCrystal);
    }

    @Override //cycle the face state (push, pull, both, none)
    public ActionResultType wrenchWithBag(World world, BlockPos pos, BlockState state, Direction face) {
        world.setBlock(pos, state.setValue(PULL, !state.getValue(PULL)), Constants.BlockFlags.DEFAULT_AND_RERENDER);
        return ActionResultType.SUCCESS;
    }
}