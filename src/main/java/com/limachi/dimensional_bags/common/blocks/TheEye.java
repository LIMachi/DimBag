package com.limachi.dimensional_bags.common.blocks;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.KeyMapController;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.network.Network;
import com.limachi.dimensional_bags.common.tileentities.TheEyeTileEntity;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
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
public class TheEye extends ContainerBlock {

    public static final String NAME = "bag_eye";

    static {
        Registries.registerBlock(NAME, TheEye::new);
        Registries.registerBlockItem(NAME, NAME, DimBag.DEFAULT_PROPERTIES);
    }

    public TheEye() {
        super(Properties.create(Material.REDSTONE_LIGHT).hardnessAndResistance(-1f, 3600000f).sound(SoundType.GLASS));
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) { return Registries.getTileEntityType(TheEyeTileEntity.NAME).create(); }

    @Override
    public BlockRenderType getRenderType(BlockState state) { return BlockRenderType.MODEL; }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult ray) {
        if (!DimBag.isServer(world)) return ActionResultType.SUCCESS;
        int eyeId = SubRoomsManager.getEyeId(world, pos, true);
        if (eyeId <= 0)
            return ActionResultType.FAIL;
        if (KeyMapController.KeyBindings.SNEAK_KEY.getState(player))
            SubRoomsManager.execute(eyeId, sm->sm.leaveBag(player, false, null, null));
        else
            Network.openEyeInventory((ServerPlayerEntity) player, eyeId);
        return ActionResultType.SUCCESS;
    }
}
