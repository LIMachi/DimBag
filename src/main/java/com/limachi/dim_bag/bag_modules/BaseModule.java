package com.limachi.dim_bag.bag_modules;

import com.ibm.icu.util.BasicTimeZone;
import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.bag_data.BagInstance;
import com.limachi.dim_bag.bag_modes.ModesRegistry;
import com.limachi.dim_bag.bag_modes.Settings;
import com.limachi.dim_bag.capabilities.entities.BagMode;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.lim_lib.capabilities.Cap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class BaseModule extends Block {
    public BaseModule() {
        super(Properties.copy(Blocks.BEDROCK).isValidSpawn((s, b, p, e)->false).isSuffocating((s, b, p)->false).noOcclusion().noLootTable().isViewBlocking((s, l, p)->false));
        init();
    }

    @SubscribeEvent
    public static void addLabelTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof BlockItem item && item.getBlock() instanceof BaseModule) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("label", Tag.TAG_STRING))
                event.getToolTip().add(1, Component.Serializer.fromJson(tag.getString("label")));
        }
    }

    protected void init() {}

    @Override
    final public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (BagsData.runOnBag(player.getItemInHand(hand), b->"Settings".equals(Cap.run(player, BagMode.TOKEN, c->c.getMode(b.bagId()), "")) && wrench(b, player, level, pos, player.getItemInHand(hand)), false))
            return InteractionResult.SUCCESS;
        if (BagsData.runOnBag(level, pos, b->use(b, player, level, pos, hand), false))
            return InteractionResult.SUCCESS;
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
     * called when a module is right-clicked by a bag in settings/wrench mode
     * return true to cancel the right click (consume)
     */
    public boolean wrench(BagInstance bag, Player player, Level level, BlockPos pos, ItemStack stack) { return false; }

    public boolean use(BagInstance bag, Player player, Level level, BlockPos pos, InteractionHand hand) {
        return false;
    }

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
