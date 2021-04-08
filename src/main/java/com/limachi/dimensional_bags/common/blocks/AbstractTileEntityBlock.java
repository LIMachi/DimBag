package com.limachi.dimensional_bags.common.blocks;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.Registries;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * helper class to create blocks that have tileentities linked to them and that use those tileentities to manipulate drops (by default copy the tile entity to "BlockEntityTag" on break, including creative break)
 * @param <T>
 */
public abstract class AbstractTileEntityBlock<T extends TileEntity> extends ContainerBlock {

    public final Class<T> tileEntityClass;
    public final String tileEntityRegistryName;
    public final String registryName;

    public AbstractTileEntityBlock(String registryName, Properties properties, Class<T> tileEntityClass, String tileEntityRegistryName) {
        super(properties);
        this.tileEntityClass = tileEntityClass;
        this.tileEntityRegistryName = tileEntityRegistryName;
        this.registryName = registryName;
    }

    public abstract <B extends AbstractTileEntityBlock<T>> B getInstance();
    public abstract BlockItem getItemInstance();

    public abstract void onValidPlace(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack, T tileEntity);
    public abstract void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving, T tileEntity);

    @Override
    public BlockRenderType getRenderType(BlockState state) { return BlockRenderType.MODEL; }

    @Override
    public ItemStack getItem(IBlockReader worldIn, BlockPos pos, BlockState state) {
        TileEntity te = worldIn.getTileEntity(pos);
        return asItem(state, tileEntityClass.isInstance(te) ? (T)te : null);
    }

    /**
     * common function to generate an itemstack from a BlockState and TileEntity
     * @param state (should be of the same block type of the INSTANCE of this block)
     * @param tileEntity (should be of the same tile entity type of this block)
     * @return a valid itemstack (might be empty on invalid state)
     */
    public ItemStack asItem(BlockState state, @Nullable T tileEntity) {
        if (state.getBlock() != getInstance()) return ItemStack.EMPTY;
        ItemStack out = new ItemStack(getItemInstance());
        if (out.getTag() == null)
            out.setTag(new CompoundNBT());
        if (tileEntity != null)
            out.getTag().put("BlockEntityTag", tileEntity.serializeNBT());
        return out;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (tileEntityClass.isInstance(te))
            onValidPlace(worldIn, pos, state, placer, stack, (T)te);
    }

    @Override //creative destroying drops
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        if (DimBag.isServer(worldIn) && player.isCreative()) {
            TileEntity te = worldIn.getTileEntity(pos);
            ItemStack itemstack = asItem(state, tileEntityClass.isInstance(te) ? (T)te : null);
            if (itemstack.isEmpty()) return;
            ItemEntity itementity = new ItemEntity(worldIn, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, itemstack);
            itementity.setDefaultPickupDelay();
            worldIn.addEntity(itementity);
        }
        super.onBlockHarvested(worldIn, pos, state, player);
    }

    @Override //standard drop
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        ArrayList<ItemStack> list = new ArrayList<>();
        TileEntity te = builder.get(LootParameters.BLOCK_ENTITY);
        list.add(asItem(state, tileEntityClass.isInstance(te) ? (T)te : null));
        return list;
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.isIn(newState.getBlock()) && state.isIn((Block)getInstance())) {
            TileEntity te = worldIn.getTileEntity(pos);
            if (tileEntityClass.isInstance(te))
                onRemove(state, worldIn, pos, newState, isMoving, (T)te);
        }
        super.onReplaced(state, worldIn, pos, newState, isMoving);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) { return Registries.getTileEntityType(tileEntityRegistryName).create(); }
}
