package com.limachi.dimensional_bags.common.blocks;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.Registries;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import java.util.function.Supplier;

@StaticInit
public class Brain extends Block implements ITileEntityProvider {

    public static final String NAME = "brain";

    public static final Supplier<Brain> INSTANCE = Registries.registerBlock(NAME, Brain::new);
    public static final Supplier<BlockItem> INSTANCE_ITEM = Registries.registerBlockItem(NAME, NAME, DimBag.DEFAULT_PROPERTIES);

    public static final IntegerProperty POWER = IntegerProperty.create("power", 0, 15);

    public Brain() {
        super(Block.Properties.of(Material.HEAVY_METAL).sound(SoundType.STONE).strength(1.5F, 180000000).isValidSpawn((s, r, p, e)->false));
        this.registerDefaultState(defaultBlockState().setValue(POWER, 0)/*.with(TICK_RATE, 2)*/);
    }
    
    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWER);
    }

    @Override
    public int getDirectSignal(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) { return blockState.getValue(POWER); }

    @Nullable
    @Override
    public TileEntity newBlockEntity(IBlockReader worldIn) { return Registries.getBlockEntityType(NAME).create(); }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) //only do the default behavior if the new state is of a different block
            super.onRemove(state, worldIn, pos, newState, isMoving);
        worldIn.updateNeighbourForOutputSignal(pos, this);
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (worldIn.isClientSide()) {
            return ActionResultType.SUCCESS;
        } else {
            TileEntity tileentity = worldIn.getBlockEntity(pos);
//            if (tileentity instanceof BrainTileEntity)
//                Network.openBrainInterface((ServerPlayerEntity)player, (BrainTileEntity)tileentity);
            return ActionResultType.SUCCESS;
        }
    }
}
