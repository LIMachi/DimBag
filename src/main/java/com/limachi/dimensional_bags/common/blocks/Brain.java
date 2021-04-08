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

import net.minecraftforge.common.ToolType;

import java.util.function.Supplier;

@StaticInit
public class Brain extends Block implements ITileEntityProvider {

    public static final String NAME = "brain";

    public static final Supplier<Brain> INSTANCE = Registries.registerBlock(NAME, Brain::new);
    public static final Supplier<BlockItem> INSTANCE_ITEM = Registries.registerBlockItem(NAME, NAME, DimBag.DEFAULT_PROPERTIES);

    public static final IntegerProperty POWER = IntegerProperty.create("power", 0, 15);
//    public static final IntegerProperty TICK_RATE = IntegerProperty.create("tick_rate", 1, /*1200*/20);

    public Brain() {
        super(Block.Properties.create(Material.ROCK).sound(SoundType.STONE).setRequiresTool().hardnessAndResistance(1.5F, 180000000).harvestTool(ToolType.PICKAXE).harvestLevel(2).doesNotBlockMovement().setAllowsSpawn((s, r, p, e)->false));
        this.setDefaultState(getDefaultState().with(POWER, 0)/*.with(TICK_RATE, 2)*/);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(POWER);
//        builder.add(TICK_RATE);
    }

    @Override
    public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        return blockState.get(POWER);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) { return Registries.getTileEntityType(NAME).create(); }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.isIn(newState.getBlock())) //only do the default behavior if the new state is of a different block
            super.onReplaced(state, worldIn, pos, newState, isMoving);
        worldIn.notifyNeighborsOfStateChange(pos, this);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (worldIn.isRemote) {
            return ActionResultType.SUCCESS;
        } else {
            TileEntity tileentity = worldIn.getTileEntity(pos);
//            if (tileentity instanceof BrainTileEntity)
//                Network.openBrainInterface((ServerPlayerEntity)player, (BrainTileEntity)tileentity);
            return ActionResultType.SUCCESS;
        }
    }
}
