package com.limachi.dimensional_bags.common.blocks;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.network.Network;
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

public class TheEye extends ContainerBlock {

    public TheEye() {
        super(Properties.create(Material.REDSTONE_LIGHT).hardnessAndResistance(-1f, 3600000f).sound(SoundType.GLASS));
    }

    private EyeData getEyeData(World world, BlockPos pos) {
        return EyeData.get(world.getServer(), (pos.getX() - 8) / 1024 + 1);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) { return Registries.BAG_EYE_TE.get().create(); }

    @Override
    public BlockRenderType getRenderType(BlockState state) { return BlockRenderType.MODEL; }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult ray) {
        if (!DimBag.isServer(world)) return super.onBlockActivated(state, world, pos, player, hand, ray);
        EyeData data = getEyeData(world, pos);
        if (player.isCrouching())
            data.tpBack(player);
        else
            Network.openEyeInventory((ServerPlayerEntity) player, data.getInventory());
        return ActionResultType.SUCCESS;
    }
}
