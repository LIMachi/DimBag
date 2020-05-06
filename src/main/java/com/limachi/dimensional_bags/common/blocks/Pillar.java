package com.limachi.dimensional_bags.common.blocks;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.inventory.PlayerInventoryWrapper;
import com.limachi.dimensional_bags.common.network.Network;
import net.minecraft.block.*;
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

public class Pillar extends ContainerBlock {

    public Pillar() {
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(2f, 3600000f).sound(SoundType.STONE));
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) { return Registries.PILLAR_TE.get().create(); }

    @Override
    public BlockRenderType getRenderType(BlockState state) { return BlockRenderType.MODEL; }

    /*
    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult ray) {
        if (!DimBag.isServer(world)) return super.onBlockActivated(state, world, pos, player, hand, ray);
        EyeData data = EyeData.getEyeData(world, pos, false);
        if (data == null)
            return ActionResultType.FAIL;
        if (data.getUserPlayer() != null)
            Network.openEyeInventory((ServerPlayerEntity) player, new PlayerInventoryWrapper(data.getUserPlayer().inventory, 1, 1)); //FIXME: do not function, at all, should look at InvWrapper
        return ActionResultType.SUCCESS;
    }
    */
}
