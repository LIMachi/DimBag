package com.limachi.dimensional_bags.common.bagDimensionOnly.worldAccessBlock;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.references.Registries;
import com.limachi.dimensional_bags.lib.common.blocks.AbstractTileEntityBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * bag entity mode:
 * push item: try to use the item
 * push fluid: try to place fluid
 * pull item: try to hopper in items
 * pull fluid: try to pump fluid
 * redstone: while active, repeat the try to use item and disable pump/vacuum
 *
 * equiped bag mode:
 * push item: special action on player (simulate a finished rightclick on the player) (note: this is NOT an autoclicker, use the ghost hand instead)
 * push fluid: special action on player (water: extinguish, lava: set on fire/lava tick, milk: clear all effects, air: waterbreathing, chocolate: remove only harmfull effects, potions: apply the potion effect)
 * pull item: if an item pushed was not consumed by the simulated right click, pull it first, pull items from the world around the player (larger radius than bag entity mode, does not interact with block inventories)
 * pull fluid: pump fluids around the player (larger radius than bag entity mode, does not interact with block inventories)
 * redstone: while active, repeat the try to use item/fluid on entity and disable pump/vacuum
 */

@StaticInit
public class WorldAccessBlock extends AbstractTileEntityBlock<WorldAccessBlockTileEntity> {

    public static final String NAME = "world_access";

    public static final Supplier<WorldAccessBlock> INSTANCE = Registries.registerBlock(NAME, WorldAccessBlock::new);
    public static final Supplier<BlockItem> INSTANCE_ITEM = Registries.registerBlockItem(NAME, NAME, DimBag.DEFAULT_PROPERTIES);

    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public WorldAccessBlock() {
        super(NAME, Properties.of(Material.HEAVY_METAL).strength(1.5f, 3600000f).sound(SoundType.GLASS), WorldAccessBlockTileEntity.class, WorldAccessBlockTileEntity.NAME);
        this.registerDefaultState(defaultBlockState().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    @Override
    public void onRemove(BlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) //only do the default behavior if the new state is of a different block
            super.onRemove(state, worldIn, pos, newState, isMoving);
        worldIn.updateNeighbourForOutputSignal(pos, this);
    }

    public static boolean isPowered(BlockState state) { return state.getValue(POWERED); }

    public static void setPowered(World world, BlockPos pos, boolean state) {
        if (world.getBlockState(pos).getBlock() instanceof WorldAccessBlock && isPowered(world.getBlockState(pos)) != state)
            world.setBlock(pos, world.getBlockState(pos).getBlock().defaultBlockState().setValue(POWERED, state), Constants.BlockFlags.DEFAULT_AND_RERENDER);
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!worldIn.isClientSide()) setPowered(worldIn, pos, worldIn.getBestNeighborSignal(pos) > 0);
    }

    @Override
    public <B extends AbstractTileEntityBlock<WorldAccessBlockTileEntity>> B getInstance() { return (B)INSTANCE.get(); }

    @Override
    public BlockItem getItemInstance() { return INSTANCE_ITEM.get(); }

    @Override
    public void onValidPlace(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack, WorldAccessBlockTileEntity tileEntity) {

    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving, WorldAccessBlockTileEntity tileEntity) {

    }
}
