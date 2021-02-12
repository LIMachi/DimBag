package com.limachi.dimensional_bags.common.blocks;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.KeyMapController;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.tileentities.PadTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import com.limachi.dimensional_bags.StaticInit;

@StaticInit
public class Pad extends RSReactiveBlock implements ITileEntityProvider {

    public static final String NAME = "pad";

    static {
        Registries.registerBlock(NAME, Pad::new);
        Registries.registerBlockItem(NAME, NAME, DimBag.DEFAULT_PROPERTIES);
    }

    public Pad() { super(Properties.create(Material.ROCK).sound(SoundType.STONE)); }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        return Registries.getTileEntityType(PadTileEntity.NAME).create();
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onReplaced(state, worldIn, pos, newState, isMoving);
        if (state.isIn(newState.getBlock())) {
            if (isPowered(state) != isPowered(newState)) {
                TileEntity te = worldIn.getTileEntity(pos);
                if (te instanceof PadTileEntity)
                    ((PadTileEntity) te).needUpdate();
            }
        }
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (worldIn.isRemote) {
            return ActionResultType.SUCCESS;
        } else {
            int eyeId = SubRoomsManager.getEyeId(worldIn, pos, false);
            if (eyeId <= 0) return ActionResultType.FAIL;
            if (KeyMapController.KeyBindings.SNEAK_KEY.getState(player))
                SubRoomsManager.execute(eyeId, sm->sm.leaveBag(player, false, null, null));
            else {
//            TileEntity tileentity = worldIn.getTileEntity(pos);
//            if (tileentity instanceof GhostHandTileEntity)
//                Network.openGhostHandInterface((ServerPlayerEntity)player, (GhostHandTileEntity)tileentity);
            }
            return ActionResultType.CONSUME;
        }
    }
}
