package com.limachi.dim_bag.bag_modules;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.bag_data.BagInstance;
import com.limachi.dim_bag.save_datas.BagsData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class BaseModule extends Block {
    public BaseModule() {
        super(Properties.copy(Blocks.BEDROCK).isValidSpawn((s, b, p, e)->false).isSuffocating((s, b, p)->false).noOcclusion().noLootTable());
        init();
    }

    protected void init() {}

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return super.use(state, level, pos, player, hand, hit);
    }

    /**
     * generate a blockpos inside the bag that would replace air blocks, return null if no valid placement could be found
     */
    @Nullable
    public static BlockPos getAnyInstallPosition(int bag) {
        //FIXME: should search air block in bag
//        return IBagsData.roomCenter(bag);
        return new BlockPos(0, 0, 0);
    }

    /**
     * called when a module is breaking (removed from bag), use this to add removal logic
     */
    public void uninstall(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) {}

    /**
     * called when a module was installed (place inside the bag), use this to add placement logic
     */
    public void install(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) {}

    /**
     * catch manual placement of module inside bag
     */
    @Override
    public final void setPlacedBy(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable LivingEntity entity, @Nonnull ItemStack itemStack) {
        if (!(entity instanceof Player player)) return;
        BagsData.runOnBag(level, pos, b->install(b, player, level, pos, itemStack));
    }

    /**
     * limit placement to inside a bag only
     */
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        if (!BagsData.runOnBag(ctx.getLevel(), ctx.getClickedPos(), b->true, false)) return null;
        return super.getStateForPlacement(ctx);
    }

    /**
     * module might have been removed by creative break instead of attack, fix this
     */
    @Override
    public final boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (player.isCreative() && state.getBlock() instanceof BaseModule module) {
            if (level.dimension().equals(DimBag.BAG_DIM)) {
                ItemStack item = getCloneItemStack(level, pos, state);
                BagsData.runOnBag(level, pos, b->module.uninstall(b, player, level, pos, item));
                player.drop(item, false);
            }
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }
}
