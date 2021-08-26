package com.limachi.dimensional_bags.common.blocks;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.KeyMapController;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.client.render.screen.PadScreen;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.tileentities.PadTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
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
public class Pad extends RSReactiveBlock implements ITileEntityProvider {

    public static final String NAME = "pad";

    public static final Supplier<Pad> INSTANCE = Registries.registerBlock(NAME, Pad::new);
    public static final Supplier<BlockItem> INSTANCE_ITEM = Registries.registerBlockItem(NAME, NAME, DimBag.DEFAULT_PROPERTIES);

    public Pad() { super(Properties.of(Material.HEAVY_METAL).sound(SoundType.STONE)); }

    @Nullable
    @Override
    public TileEntity newBlockEntity(IBlockReader worldIn) {
        return Registries.getBlockEntityType(PadTileEntity.NAME).create();
    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, worldIn, pos, newState, isMoving);
        if (state.getBlock() == newState.getBlock()) {
            if (isPowered(state) != isPowered(newState)) {
                TileEntity te = worldIn.getBlockEntity(pos);
                if (te instanceof PadTileEntity)
                    ((PadTileEntity) te).needUpdate();
            }
        }
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        int eyeId = SubRoomsManager.getEyeId(worldIn, pos, false);
        if (eyeId <= 0) return ActionResultType.FAIL;
        if (KeyMapController.KeyBindings.SNEAK_KEY.getState(player))
            SubRoomsManager.execute(eyeId, sm->sm.leaveBag(player, false, null, null));
        else
            PadScreen.open((PadTileEntity) worldIn.getBlockEntity(pos));
        return ActionResultType.CONSUME;
    }
}