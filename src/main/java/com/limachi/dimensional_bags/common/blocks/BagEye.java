package com.limachi.dimensional_bags.common.blocks;

//import com.limachi.dimensional_bags.common.data.inventory.container.BagEyeContainer;
import com.limachi.dimensional_bags.common.data.DimBagData;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.dimensions.BagDimension;
import com.limachi.dimensional_bags.common.init.Registries;
import com.limachi.dimensional_bags.common.network.Network;
import com.limachi.dimensional_bags.common.tileentity.BagEyeTileEntity;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Random;

public class BagEye extends ContainerBlock {

    public BagEye() {
        super(Properties.create(Material.REDSTONE_LIGHT).hardnessAndResistance(-1.0F,3600000.0F).sound(SoundType.GLASS));
    }

    @Nullable
    private EyeData getEyeData(World world, BlockPos pos) {
        if (world.isRemote()) return null; //invalid side
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof BagEyeTileEntity)) return null; //invalid tile type
        int id = ((BagEyeTileEntity)te).getId().getId();
        if (id == 0) return null; //invalid tile id
        return DimBagData.get(world.getServer()).getEyeData(id);
    }

    /*
    @Nullable
    public INamedContainerProvider getContainer(BlockState state, World worldIn, BlockPos pos) {
        EyeData data = getEyeData(worldIn, pos);
        if (data != null)
            return Network.eyeContainerProvider(data, this.getTranslationKey());
        return null;
    }
    */

    @Override
    @SuppressWarnings("deprecation")
    public boolean hasTileEntity() { return true; } //I think I forgot that

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return Registries.BAG_EYE_TE.get().create();
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult ray) {
        if (world.isRemote) return super.onBlockActivated(state, world, pos, player, hand, ray);
        EyeData data = getEyeData(world, pos);
        if (player.isCrouching()) {
            BagDimension.teleportBackFromRoom((ServerPlayerEntity) player, data.getId().getId()); //no longer valid, need to be reworked
        } else
            Network.openGUIEye((ServerPlayerEntity) player, data, getTranslationKey());
        return ActionResultType.SUCCESS;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        return Registries.BAG_EYE_TE.get().create();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
        //animate the eye there
    }
}
