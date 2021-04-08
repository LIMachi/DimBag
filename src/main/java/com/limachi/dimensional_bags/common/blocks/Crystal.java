package com.limachi.dimensional_bags.common.blocks;

import com.limachi.dimensional_bags.ConfigManager.Config;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.EnergyData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.tileentities.CrystalTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.function.Supplier;

@StaticInit
public class Crystal extends AbstractTileEntityBlock<CrystalTileEntity> implements IBagWrenchable {

    @Config(min = "128", max = "65536", cmt = "how much a single crystal adds to the global energy capacity of a bag")
    public static int ENERGY_PER_CRYSTAL = 1024;

    @Config(min = "8", max = "65536", cmt = "how much a crystal can output to a side (each side can output this amount on the same tick)")
    public static int OUTPUT = 128;

    @Config(min = "8", max = "65536", cmt = "how much a crystal can input from a side (each side can input this amount on the same tick)")
    public static int INPUT = 128;

    public static final String NAME = "crystal";

    public static final Supplier<Crystal> INSTANCE = Registries.registerBlock(NAME, Crystal::new);
    public static final Supplier<BlockItem> INSTANCE_ITEM = Registries.registerBlockItem(NAME, NAME, DimBag.DEFAULT_PROPERTIES);

    public enum IOEnum implements IStringSerializable {
        DISABLED(false, false),
        PUSH(true, false),
        PULL(false, true),
        BOTH(true, true);

        private final boolean push;
        private final boolean pull;

        IOEnum(boolean push, boolean pull) {
            this.push = push;
            this.pull = pull;
        }

        public boolean isPush() { return push; }
        public boolean isPull() { return pull; }
        public boolean isPushOrPull(boolean testPush) { return testPush ? push : pull; }
        public IOEnum cycle() { return IOEnum.values()[(ordinal() + 1) % IOEnum.values().length]; }

        @Override
        public String getString() { return this.name().toLowerCase(); }
    }

    public static final HashMap<Direction, EnumProperty<IOEnum>> IOSTATES = new HashMap<>();
//    public static final BooleanProperty ACTIVE_PUSH_PULL = BooleanProperty.create("active_push_pull");

    static {
        for (Direction dir : Direction.values())
            IOSTATES.put(dir, EnumProperty.create(dir.getName2(), IOEnum.class));
    }

    public Crystal() {
        super(NAME, Properties.create(Material.REDSTONE_LIGHT).hardnessAndResistance(1.5f, 3600000f).sound(SoundType.GLASS), CrystalTileEntity.class, CrystalTileEntity.NAME);
    }



    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        for (Direction dir : Direction.values())
            builder.add(IOSTATES.get(dir));
//        builder.add(ACTIVE_PUSH_PULL);
    }

    @Nullable
    @Override //this block can only be placed in a subroom
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        int eyeId = SubRoomsManager.getEyeId(context.getWorld(), context.getPos(), false);
        if (eyeId <= 0) return null;
        return super.getStateForPlacement(context);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) { return Registries.getTileEntityType(CrystalTileEntity.NAME).create(); }

    @Override
    public <B extends AbstractTileEntityBlock<CrystalTileEntity>> B getInstance() { return (B)INSTANCE.get(); }

    @Override
    public BlockItem getItemInstance() { return INSTANCE_ITEM.get(); }

    @Override
    public ItemStack asItem(BlockState state, @Nullable CrystalTileEntity tileEntity) {
        ItemStack out = super.asItem(state, tileEntity);
        out.getTag().putInt("LocalEnergy", tileEntity.getLocalEnergy());
        return out;
    }

    @Override
    public void onValidPlace(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack, CrystalTileEntity crystal) {
        EnergyData.execute(SubRoomsManager.getEyeId(worldIn, pos, false), ed->ed.changeBatterySize(ed.getMaxEnergyStored() + ENERGY_PER_CRYSTAL).receiveEnergy(stack.getTag() != null ? stack.getTag().getInt("LocalEnergy") : 0, false));
    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving, CrystalTileEntity crystal) {
        EnergyData.execute(SubRoomsManager.getEyeId(worldIn, pos, false), ed->{int extract = ed.getSingleCrystalEnergy(); ed.changeBatterySize(ed.getMaxEnergyStored() - ENERGY_PER_CRYSTAL).extractEnergy(extract, false);});
    }

    @Override //cycle the face state (push, pull, both, none)
    public ActionResultType wrenchWithBag(World world, BlockPos pos, BlockState state, Direction face) {
        world.setBlockState(pos, state.with(IOSTATES.get(face), state.get(IOSTATES.get(face)).cycle()));
        return ActionResultType.SUCCESS;
    }
}
