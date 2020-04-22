package com.limachi.dimensional_bags.common.blocks;

//import com.limachi.dimensional_bags.common.data.inventory.container.BagEyeContainer;
import com.limachi.dimensional_bags.common.data.DimBagData;
import com.limachi.dimensional_bags.common.data.inventory.container.DimBagContainer;
import com.limachi.dimensional_bags.common.dimensions.BagDimension;
import com.limachi.dimensional_bags.common.init.Registries;
import com.limachi.dimensional_bags.common.tileentity.BagEyeTileEntity;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.Random;

public class BagEye extends ContainerBlock {

    public BagEye() {
        super(Properties.create(Material.REDSTONE_LIGHT).hardnessAndResistance(-1.0F,3600000.0F).sound(SoundType.GLASS));
    }

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
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof BagEyeTileEntity) {
//                int id = (pos.getX() - 8) / 1024;
                int id = ((BagEyeTileEntity)tile).getId().getId();
                if (player.isCrouching()) {
//                    BagDimension.teleportBackFromRoom((ServerPlayerEntity) player, id); //no longer valid, need to be reworked
                    return ActionResultType.SUCCESS;
                }
//                INamedContainerProvider cp = this.getContainer(state, world, pos);
//                if (cp != null)
//                    player.openContainer(cp);
                /*{
                    NetworkHooks.openGui((ServerPlayerEntity) player, new INamedContainerProvider() {
                        @Override
                        public ITextComponent getDisplayName() { return new TranslationTextComponent(getTranslationKey()); }

                        @Nullable
                        @Override
                        public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity _player) {
//                            return new BagEyeContainer(windowId, inventory, (BagEyeTileEntity) tile);
                            return new DimBagContainer(windowId, inventory, id);
                        }
                    }, packetBuffer -> packetBuffer.writeInt(id));
                }*/
            }
        }
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
