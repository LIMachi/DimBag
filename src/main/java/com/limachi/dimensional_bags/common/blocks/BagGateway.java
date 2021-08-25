package com.limachi.dimensional_bags.common.blocks;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItem;
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
        super(Properties.of(Material.PORTAL, MaterialColor.COLOR_BLACK).noCollission().lightLevel(state->15).strength(-1.0F, 3600000.0F).noDrops());
    }

//    public TileEntity newBlockEntity(IBlockReader worldIn) { return Registries.getBlockEntityType(NAME).create(); }

    //on collision, enter the nearest bag, or do nothing if no bag is near
    @Override
    public void entityInside(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        SubRoomsManager.getRoomIds(worldIn, pos, false, true).ifPresent(p -> SubRoomsManager.execute(p.getKey(), sm -> sm.enterBag(entityIn)));
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) { return /*BlockRenderType.ENTITYBLOCK_ANIMATED*/BlockRenderType.INVISIBLE; }

    @Override
    public boolean propagatesSkylightDown(BlockState p_200123_1_, IBlockReader p_200123_2_, BlockPos p_200123_3_) { return true; }
}
