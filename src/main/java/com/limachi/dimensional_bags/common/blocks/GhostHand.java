package com.limachi.dimensional_bags.common.blocks;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.tileentities.GhostHandTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ITileEntityProvider;
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

import javax.annotation.Nullable;

import java.util.function.Supplier;

@StaticInit
public class GhostHand extends Block implements ITileEntityProvider {

    public static final String NAME = "ghost_hand";

    public static final Supplier<GhostHand> INSTANCE = Registries.registerBlock(NAME, GhostHand::new);
    public static final Supplier<BlockItem> INSTANCE_ITEM = Registries.registerBlockItem(NAME, NAME, DimBag.DEFAULT_PROPERTIES);

    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public GhostHand() {
        super(Properties.create(Material.ROCK).sound(SoundType.STONE));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(POWERED);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) { return Registries.getTileEntityType(GhostHandTileEntity.NAME).create(); }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) //only do the default behavior if the new state is of a different block
            super.onReplaced(state, worldIn, pos, newState, isMoving);
        worldIn.notifyNeighborsOfStateChange(pos, this);
    }

    public static boolean isPowered(BlockState state) {
        return state.get(POWERED);
    }

    public static void setPowered(World world, BlockPos pos, boolean state) {
        if (world.getBlockState(pos).getBlock() instanceof GhostHand && isPowered(world.getBlockState(pos)) != state)
            world.setBlockState(pos, Registries.getBlock(NAME).getDefaultState().with(POWERED, state));
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!worldIn.isRemote) {
            TileEntity tileentity = worldIn.getTileEntity(pos);
            if (tileentity instanceof GhostHandTileEntity) {
                GhostHandTileEntity ghosthand = (GhostHandTileEntity)tileentity;
                boolean flag = worldIn.isBlockPowered(pos);
                boolean flag1 = isPowered(state);
                setPowered(worldIn, pos, flag);
                if (!flag1 && flag) {
                    ghosthand.runCommand();
                    worldIn.getPendingBlockTicks().scheduleTick(pos, this, 1);
                }
            }
        }
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (worldIn.isRemote) {
            return ActionResultType.SUCCESS;
        } else {
            TileEntity tileentity = worldIn.getTileEntity(pos);
//            if (tileentity instanceof GhostHandTileEntity)
//                Network.openGhostHandInterface((ServerPlayerEntity)player, (GhostHandTileEntity)tileentity);
            return ActionResultType.SUCCESS;
        }
    }
}
