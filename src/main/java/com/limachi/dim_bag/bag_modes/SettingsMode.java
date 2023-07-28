package com.limachi.dim_bag.bag_modes;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.bag_data.BagInstance;
import com.limachi.dim_bag.bag_modules.BaseModule;
import com.limachi.dim_bag.blocks.WallBlock;
import com.limachi.dim_bag.items.BagItem;
import com.limachi.dim_bag.menus.BagMenu;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.lim_lib.KeyMapController;
import com.limachi.lim_lib.PlayerUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;

public class SettingsMode extends BaseMode {

    public static final String NAME = "Settings";

    public SettingsMode() { super(NAME, null); }

    @Override
    public boolean canDisable() { return false; }

    @Override //open settings screen (bag screen index 1)
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        BagMenu.open(player, BagItem.getBagId(player.getItemInHand(hand)), 2);
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Override //if offhand is valid block, swap wall state
    public InteractionResult useOn(UseOnContext ctx) {
        BlockPos target = ctx.getClickedPos();
        if (ctx.getLevel().getBlockState(ctx.getClickedPos()).getBlock() instanceof BaseModule module)
            if (BagsData.runOnBag(ctx.getLevel(), ctx.getClickedPos(), b->module.wrench(b, ctx.getPlayer(), ctx.getLevel(), ctx.getClickedPos(), ctx.getItemInHand(), new BlockHitResult(ctx.getClickLocation(), ctx.getClickedFace(), ctx.getClickedPos(), ctx.isInside())), false))
                return InteractionResult.SUCCESS;
        if (ctx.getPlayer() != null && BagsData.runOnBag(ctx.getLevel(), target, b->b.isWall(target), false)) {
            ItemStack offStack = ctx.getPlayer().getOffhandItem();
            if (offStack.getItem() instanceof BlockItem bi) {
                BlockState original = ctx.getLevel().getBlockState(target);
                ctx.getLevel().setBlock(target, Blocks.AIR.defaultBlockState(), 0, 0); //use 0, 0 as update flags as this is probably a temporary swap, allowing to construct a new BlockPlaceContext with air (since BlockPlaceContext checks if the block targeted is replaceable)
                BlockState bs = bi.getBlock().getStateForPlacement(new BlockPlaceContext(ctx));
                boolean successful = false;
                if (bs != null) {
                    String bn = ForgeRegistries.BLOCKS.getKey(bs.getBlock()).toString();
                    if (!original.getBlock().equals(bs.getBlock()) && !bs.hasBlockEntity() && !(bs.getBlock() instanceof FallingBlock) && Arrays.stream(WallBlock.BLACKLISTED_WALL_BLOCKS).noneMatch(bn::matches)) { //prevent any block that is a tile entity or in the blacklist
                        AABB box = bs.getCollisionShape(ctx.getLevel(), target).bounds();
                        if (box.maxX >= 1 && box.maxY >= 1 && box.maxZ >= 1 && box.minX <= 0 && box.minY <= 0 && box.minZ <= 0) { //test that the block has a full collision box
                            ItemStack stack = ItemStack.EMPTY;
                            if (!original.is(WallBlock.R_BLOCK.get())) {
                                ctx.getLevel().setBlock(target, original, 0, 0); //revert back using 0, 0 as update flags, so we can use the BlockState#getCloneItemStack method to get an itemstack (only if the original was not a wall)
                                stack = original.getCloneItemStack(new BlockHitResult(ctx.getClickLocation(), ctx.getClickedFace(), target, ctx.isInside()), ctx.getLevel(), target, ctx.getPlayer());
                            }
                            ctx.getLevel().setBlockAndUpdate(target, bs); //this time, do a real update (flags 3, 512)
                            bs.getBlock().setPlacedBy(ctx.getLevel(), target, bs, ctx.getPlayer(), offStack);
                            successful = true;
                            if (!ctx.getPlayer().isCreative()) { //if we are not in creative, consume the offhand item
                                offStack.shrink(1);
                                ctx.getPlayer().setItemInHand(InteractionHand.OFF_HAND, offStack);
                            }
                            if (!stack.isEmpty()) //give back the original item if possible
                                PlayerUtils.giveOrDrop(ctx.getPlayer(), stack);
                        }
                    }
                }
                if (!successful)
                    ctx.getLevel().setBlock(target, original, 0, 0); //revert back using 0, 0 as update flags, nothing to see there
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override //pick module up/revert wall (only while sneaking)
    public boolean onLeftClickBlock(ItemStack stack, Player player, BlockPos pos) {
        if (player != null && player.level().dimension() == DimBag.BAG_DIM) {
            BlockState bs = player.level().getBlockState(pos);
            if (bs.is(WallBlock.R_BLOCK.get()))
                return true;
            if (KeyMapController.SNEAK.getState(player))
                BagsData.runOnBag(player.level(), pos, bag->{
                    if (bs.getBlock() instanceof BaseModule module) {
                        ItemStack out = module.getCloneItemStack(player.level(), pos, bs);
                        module.uninstall(bag, player, player.level(), pos, out);
                        player.level().setBlockAndUpdate(pos, bag.isWall(pos) ? WallBlock.R_BLOCK.get().defaultBlockState() : Blocks.AIR.defaultBlockState());
                        PlayerUtils.giveOrDrop(player, out);
                    } else if (bag.isWall(pos)) {
                        ItemStack out = bs.getCloneItemStack(new BlockHitResult(new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), Direction.UP, pos, true), player.level(), pos, player);
                        if (!out.isEmpty())
                            PlayerUtils.giveOrDrop(player, out);
                        player.level().setBlockAndUpdate(pos, WallBlock.R_BLOCK.get().defaultBlockState());
                    }
                });
            return true;
        }
        return false;
    }
}
