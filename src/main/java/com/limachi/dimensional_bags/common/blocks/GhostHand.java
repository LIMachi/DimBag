package com.limachi.dimensional_bags.common.blocks;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.tileentities.GhostHandTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.function.Supplier;

@StaticInit
public class GhostHand extends AbstractTileEntityBlock<GhostHandTileEntity> {

    public static final String NAME = "ghost_hand";

    public static final Supplier<GhostHand> INSTANCE = Registries.registerBlock(NAME, GhostHand::new);
    public static final Supplier<BlockItem> INSTANCE_ITEM = Registries.registerBlockItem(NAME, NAME, DimBag.DEFAULT_PROPERTIES);

    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public GhostHand() { super(NAME, Properties.of(Material.HEAVY_METAL).sound(SoundType.STONE).strength(1.5F, 180000000).isValidSpawn((s, r, p, e)->false), GhostHandTileEntity.class, GhostHandTileEntity.NAME); }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    @Override
    public <B extends AbstractTileEntityBlock<GhostHandTileEntity>> B getInstance() { return (B)INSTANCE.get(); }

    @Override
    public BlockItem getItemInstance() { return INSTANCE_ITEM.get(); }

    @Nullable
    @Override
    public TileEntity newBlockEntity(@Nonnull IBlockReader worldIn) { return Registries.getBlockEntityType(GhostHandTileEntity.NAME).create(); }

    @Override
    public void onRemove(BlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) //only do the default behavior if the new state is of a different block
            super.onRemove(state, worldIn, pos, newState, isMoving);
        worldIn.updateNeighbourForOutputSignal(pos, this);
    }

    public static boolean isPowered(BlockState state) { return state.getValue(POWERED); }

    public static void setPowered(World world, BlockPos pos, boolean state) {
        if (world.getBlockState(pos).getBlock() instanceof GhostHand && isPowered(world.getBlockState(pos)) != state)
            world.setBlock(pos, Registries.getBlock(NAME).defaultBlockState().setValue(POWERED, state), Constants.BlockFlags.DEFAULT_AND_RERENDER);
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!worldIn.isClientSide()) {
            TileEntity tileentity = worldIn.getBlockEntity(pos);
            if (tileentity instanceof GhostHandTileEntity) {
                GhostHandTileEntity ghosthand = (GhostHandTileEntity)tileentity;
                boolean flag = worldIn.hasNeighborSignal(pos);
                boolean flag1 = isPowered(state);
                setPowered(worldIn, pos, flag);
                if (!flag1 && flag) {
                    ghosthand.runCommand();
                    worldIn.getBlockTicks().scheduleTick(pos, this, 1);
                }
            }
        }
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (worldIn.isClientSide()) {
            return ActionResultType.SUCCESS;
        } else {
            TileEntity tileentity = worldIn.getBlockEntity(pos);
//            if (tileentity instanceof GhostHandTileEntity)
//                Network.openGhostHandInterface((ServerPlayerEntity)player, (GhostHandTileEntity)tileentity);
            return ActionResultType.SUCCESS;
        }
    }
}
