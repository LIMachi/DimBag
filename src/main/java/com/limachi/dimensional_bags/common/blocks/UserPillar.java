package com.limachi.dimensional_bags.common.blocks;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.container.UserPillarContainer;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.inventory.EntityInventoryProxy;
import com.limachi.dimensional_bags.common.tileentities.UserPillarTileEntity;
import net.minecraft.block.*;
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
public class UserPillar extends ContainerBlock {

    public static final String NAME = "user_pillar";

    public static final Supplier<UserPillar> INSTANCE = Registries.registerBlock(NAME, UserPillar::new);
    public static final Supplier<BlockItem> INSTANCE_ITEM = Registries.registerBlockItem(NAME, NAME, DimBag.DEFAULT_PROPERTIES);

    public UserPillar() {
        super(Properties.of(Material.HEAVY_METAL).strength(1.5f, 3600000f).sound(SoundType.STONE));
    }

    @Nullable
    @Override
    public TileEntity newBlockEntity(IBlockReader worldIn) { return Registries.getBlockEntityType(UserPillarTileEntity.NAME).create(); }

    @Override
    public BlockRenderType getRenderShape(BlockState state) { return BlockRenderType.MODEL; }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult ray) {
        if (!DimBag.isServer(world)) return super.use(state, world, pos, player, hand, ray);
        TileEntity te = world.getBlockEntity(pos);
        if (!(te instanceof UserPillarTileEntity)) return super.use(state, world, pos, player, hand, ray);
        EntityInventoryProxy inv = ((UserPillarTileEntity)te).getInvProxy();
        if (inv != null)
            UserPillarContainer.open(player, SubRoomsManager.getEyeId(world, pos, false));
//        LOGGER.info(inv);
//        if (inv != null)
//            Network.openWrappedPlayerInventory((ServerPlayerEntity) player, inv, te);
        return ActionResultType.SUCCESS;
    }
}
