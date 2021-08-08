package com.limachi.dimensional_bags.common.blocks;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.function.Supplier;

@StaticInit
public class BagGateway extends Block /*implements ITileEntityProvider*/ {

    public static final String NAME = "bag_gateway";

    public static final Supplier<BagGateway> INSTANCE = Registries.registerBlock(NAME, BagGateway::new);
    public static final Supplier<BlockItem> INSTANCE_ITEM = Registries.registerBlockItem(NAME, NAME, DimBag.DEFAULT_PROPERTIES);

    public BagGateway() {
        super(Properties.create(Material.PORTAL, MaterialColor.BLACK).doesNotBlockMovement().setLightLevel(state->15).hardnessAndResistance(-1.0F, 3600000.0F).noDrops());
    }

//    public TileEntity createNewTileEntity(IBlockReader worldIn) { return Registries.getTileEntityType(NAME).create(); }

    //on collision, enter the nearest bag, or do nothing if no bag is near
    @Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        SubRoomsManager.getRoomIds(worldIn, pos, false, true).ifPresent(p -> SubRoomsManager.execute(p.getKey(), sm -> sm.enterBag(entityIn)));
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) { return /*BlockRenderType.ENTITYBLOCK_ANIMATED*/BlockRenderType.INVISIBLE; }

    @Override
    public boolean isTransparent(BlockState state) { return true; }
}
