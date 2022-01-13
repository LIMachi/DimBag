package com.limachi.dimensional_bags.common.bagDimensionOnly.bagPad;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.KeyMapController;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.client.render.screen.PadScreen;
import com.limachi.dimensional_bags.common.references.Registries;
import com.limachi.dimensional_bags.lib.common.blocks.AbstractTileEntityBlock;
import com.limachi.dimensional_bags.lib.common.blocks.IBagWrenchable;
import com.limachi.dimensional_bags.lib.common.blocks.IGetUseSneakWithItemEvent;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.SubRoomsManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

@StaticInit
public class PadBlock extends AbstractTileEntityBlock<PadTileEntity> implements IBagWrenchable, IGetUseSneakWithItemEvent {

    public static final String NAME = "pad";

    public static final IntegerProperty POWER_RECEIVED = IntegerProperty.create("power_received", 0, 15);
    public static final BooleanProperty DOWN = BooleanProperty.create("down");

    public static final Supplier<PadBlock> INSTANCE = Registries.registerBlock(NAME, PadBlock::new);
    public static final Supplier<BlockItem> INSTANCE_ITEM = Registries.registerBlockItem(NAME, NAME, DimBag.DEFAULT_PROPERTIES);

    public PadBlock() {
        super(NAME, Properties.of(Material.HEAVY_METAL).strength(1.5f, 3600000f).sound(SoundType.STONE).isSuffocating((s, w, p)->false), PadTileEntity.class, PadTileEntity.NAME);
        this.registerDefaultState(defaultBlockState().setValue(POWER_RECEIVED, 0).setValue(DOWN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWER_RECEIVED).add(DOWN);
    }

    @Override
    public void onRemove(BlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) //only do the default behavior if the new state is of a different block
            super.onRemove(state, worldIn, pos, newState, isMoving);
        worldIn.updateNeighbourForOutputSignal(pos, this);
        if (state.getBlock() == newState.getBlock()) {
            if (isPowered(state) != isPowered(newState)) {
                TileEntity te = worldIn.getBlockEntity(pos);
                if (te instanceof PadTileEntity)
                    ((PadTileEntity) te).needUpdate();
            }
        }
    }

    public static boolean isPowered(BlockState state) { return state.getValue(POWER_RECEIVED) > 0; }

    public static int getPower(BlockState state) { return state.getValue(POWER_RECEIVED); }

    public static void setPowered(World world, BlockPos pos, int state) {
        if (world.getBlockState(pos).getBlock() instanceof PadBlock && getPower(world.getBlockState(pos)) != state)
            world.setBlock(pos, world.getBlockState(pos).getBlock().defaultBlockState().setValue(POWER_RECEIVED, state), Constants.BlockFlags.DEFAULT_AND_RERENDER);
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!worldIn.isClientSide())
            setPowered(worldIn, pos, worldIn.hasNeighborSignal(pos) ? worldIn.getBestNeighborSignal(pos) : 0);
    }

    @Override
    public <B extends AbstractTileEntityBlock<PadTileEntity>> B getInstance() { return (B)INSTANCE.get(); }

    @Override
    public BlockItem getItemInstance() { return INSTANCE_ITEM.get(); }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        int bagId = SubRoomsManager.getbagId(worldIn, pos, false);
        if (bagId <= 0) return ActionResultType.FAIL;
        if (KeyMapController.KeyBindings.SNEAK_KEY.getState(player))
            SubRoomsManager.execute(bagId, sm->sm.leaveBag(player));
        else
            PadScreen.open((PadTileEntity) worldIn.getBlockEntity(pos));
        return ActionResultType.CONSUME;
    }

    public static boolean isDownFacing(BlockState state) {
        if (state.getBlock() instanceof PadBlock)
            return state.getValue(DOWN);
        return false;
    }

    @Override
    public ActionResultType wrenchWithBag(World world, BlockPos pos, BlockState state, Direction face) {
        world.setBlock(pos, state.setValue(DOWN, !state.getValue(DOWN)), Constants.BlockFlags.DEFAULT_AND_RERENDER);
        return ActionResultType.SUCCESS;
    }
}