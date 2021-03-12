package com.limachi.dimensional_bags.common.blocks;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.inventory.PlayerInvWrapper;
import com.limachi.dimensional_bags.common.network.Network;
import com.limachi.dimensional_bags.common.tileentities.PlayerPillarTileEntity;
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

import com.limachi.dimensional_bags.StaticInit;

@StaticInit
public class PlayerPillar extends ContainerBlock {

    public static final String NAME = "player_pillar";

    static {
        Registries.registerBlock(NAME, PlayerPillar::new);
        Registries.registerBlockItem(NAME, NAME, DimBag.DEFAULT_PROPERTIES);
    }

    public PlayerPillar() {
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(2f, 3600000f).sound(SoundType.STONE));
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) { return Registries.getTileEntityType(PlayerPillarTileEntity.NAME).create(); }

    @Override
    public BlockRenderType getRenderType(BlockState state) { return BlockRenderType.MODEL; }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult ray) {
        if (!DimBag.isServer(world)) return super.onBlockActivated(state, world, pos, player, hand, ray);
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof PlayerPillarTileEntity)) return super.onBlockActivated(state, world, pos, player, hand, ray);
        PlayerInvWrapper inv = ((PlayerPillarTileEntity)te).getWrapper();
        LOGGER.info(inv);
        if (inv != null)
            Network.openWrappedPlayerInventory((ServerPlayerEntity) player, inv, te);
        return ActionResultType.SUCCESS;
    }
}
