package com.limachi.dimensional_bags.common.blocks;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.items.Components;
import com.limachi.dimensional_bags.common.tileentities.BaseTileEntity;
import net.minecraft.block.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * helper class to create blocks that have tileentities linked to them and that use those tileentities to manipulate drops (by default copy the tile entity to "BlockEntityTag" on break, including creative break)
 * @param <T>
 */
public abstract class AbstractTileEntityBlock<T extends BaseTileEntity> extends ContainerBlock {

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

    public void onValidPlace(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack, T tileEntity) {}
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving, T tileEntity) {}
    @Override
    public BlockRenderType getRenderShape(BlockState state) { return BlockRenderType.MODEL; }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        TileEntity te = world.getBlockEntity(pos);
        return asItem(state, tileEntityClass.isInstance(te) ? (T)te : null);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslationTextComponent("tooltip.blocks." + registryName).withStyle(TextFormatting.YELLOW));
        }  else
            tooltip.add(new TranslationTextComponent("tooltip.shift_for_info"));
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
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
            out.getTag().put("BlockEntityTag", tileEntity.populateBlockEntityTag());
        return out;
    }

    @Override
    public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        TileEntity te = worldIn.getBlockEntity(pos);
        if (tileEntityClass.isInstance(te)) {
            if (worldIn.isClientSide()) {
                CompoundNBT tags = stack.getTag();
                if (tags != null && tags.contains("BlockEntityTag"))
                    te.load(state, tags.getCompound("BlockEntityTag"));
            }
            onValidPlace(worldIn, pos, state, placer, stack, (T) te);
            if (!worldIn.isClientSide())
                te.setChanged();
        }
    }

    @Override //creative destroying drops
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
        if (DimBag.isServer(world) && player.isCreative()) {
            TileEntity te = world.getBlockEntity(pos);
            ItemStack itemstack = asItem(state, tileEntityClass.isInstance(te) ? (T)te : null);
            if (itemstack.isEmpty()) return false;
            ItemEntity itementity = new ItemEntity(world, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, itemstack);
            itementity.setDefaultPickUpDelay();
            world.addFreshEntity(itementity);
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
    }

    @Override //standard drop
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        ArrayList<ItemStack> list = new ArrayList<>();
        TileEntity te = builder.getParameter(LootParameters.BLOCK_ENTITY);
        list.add(asItem(state, tileEntityClass.isInstance(te) ? (T)te : null));
        return list;
    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock() && state.getBlock() == getInstance()) {
            TileEntity te = worldIn.getBlockEntity(pos);
            if (tileEntityClass.isInstance(te))
                onRemove(state, worldIn, pos, newState, isMoving, (T)te);
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Nullable
    @Override
    public TileEntity newBlockEntity(IBlockReader p_196283_1_) { return Registries.getBlockEntityType(tileEntityRegistryName).create(); }
}
