package com.limachi.dimensional_bags.common.items;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.Registries;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TunnelPlacer extends Item {
    public TunnelPlacer() {
        super(new Properties().group(DimBag.ITEM_GROUP));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) { //detect right clic on walls to transform them in tunnels, consuming 1 tunnel placer
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        if (world.getBlockState(pos) != Registries.WALL_BLOCK.get().getDefaultState()) return ActionResultType.PASS;
        world.setBlockState(pos, Registries.TUNNEL_BLOCK.get().getDefaultState());
        ItemStack stack = context.getItem();
        stack.shrink(1);
//        if (stack.getCount() >= 1)
//            context.getPlayer().setHeldItem(context.getHand(), stack);
//        else
//            context.getPlayer().setHeldItem(context.getHand(), ItemStack.EMPTY);
        return ActionResultType.SUCCESS;
    }
}