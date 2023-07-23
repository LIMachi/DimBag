package com.limachi.dim_bag.bag_modes;

import com.limachi.dim_bag.items.BagItem;
import com.limachi.dim_bag.menus.BagMenu;
import com.limachi.dim_bag.save_datas.BagsData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class Tank extends BaseMode {
    public Tank() { super("Tank", false); }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (ctx.getPlayer() instanceof ServerPlayer player) {
            if (FluidUtil.interactWithFluidHandler(player, ctx.getHand(), ctx.getLevel(), ctx.getClickedPos(), ctx.getClickedFace())) return InteractionResult.SUCCESS;
            if (FluidUtil.tryPickUpFluid(ctx.getItemInHand(), player, ctx.getLevel(), ctx.getClickedPos(), ctx.getClickedFace()).isSuccess()) return InteractionResult.SUCCESS;
            if (FluidUtil.tryPickUpFluid(ctx.getItemInHand(), player, ctx.getLevel(), ctx.getClickedPos().relative(ctx.getClickedFace()), null).isSuccess()) return InteractionResult.SUCCESS;
            FluidStack test = BagsData.runOnBag(ctx.getItemInHand(), b -> b.tanksHandle().map(d -> d.drain(1000, IFluidHandler.FluidAction.SIMULATE)).orElse(FluidStack.EMPTY), FluidStack.EMPTY);
            if (!test.isEmpty())
                FluidUtil.tryPlaceFluid(player, ctx.getLevel(), ctx.getHand(), ctx.getClickedPos().relative(ctx.getClickedFace()), ctx.getItemInHand(), test);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player p, InteractionHand hand) {
        if (p instanceof ServerPlayer player) {
            BlockHitResult blockhitresult = BagItem.raycast(level, player, ClipContext.Fluid.SOURCE_ONLY);
            boolean source = blockhitresult.getType().equals(HitResult.Type.BLOCK);
            if (!source)
                blockhitresult = BagItem.raycast(level, player, ClipContext.Fluid.ANY);
            ItemStack bag = player.getItemInHand(hand);
            if (!blockhitresult.getType().equals(HitResult.Type.MISS)) {
                if (source && FluidUtil.tryPickUpFluid(bag, player, level, blockhitresult.getBlockPos(), blockhitresult.getDirection()).isSuccess())
                    return InteractionResultHolder.success(p.getItemInHand(hand));
                FluidStack test = BagsData.runOnBag(bag, b -> b.tanksHandle().map(d -> d.drain(1000, IFluidHandler.FluidAction.SIMULATE)).orElse(FluidStack.EMPTY), FluidStack.EMPTY);
                if (!test.isEmpty() && FluidUtil.tryPlaceFluid(player, level, hand, blockhitresult.getBlockPos(), bag, test).isSuccess())
                    return InteractionResultHolder.success(p.getItemInHand(hand));
            }
            BagMenu.open(player, BagItem.getBagId(bag), 1);
        }
        return InteractionResultHolder.success(p.getItemInHand(hand));
    }
}
