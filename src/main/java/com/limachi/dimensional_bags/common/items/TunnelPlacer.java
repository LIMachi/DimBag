package com.limachi.dimensional_bags.common.items;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.ConfigManager.Config;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.blocks.Tunnel;
import com.limachi.dimensional_bags.common.blocks.Wall;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import com.limachi.dimensional_bags.StaticInit;

@StaticInit
public class TunnelPlacer extends Item implements IDimBagCommonItem {

    @Config(cmt = "does breaking a tunnel give back a tunnel placer that only work on the same wall (false: give back a normal tunnel placer that can be used on any wall)")
    public static boolean NERF_TUNNEL_PLACER = true;

    public static String NAME = "tunnel_placer";

    static {
        Registries.registerItem(NAME, TunnelPlacer::new);
    }

    public TunnelPlacer() { super(new Properties().group(DimBag.ITEM_GROUP)); }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) { //detect right clic on walls to transform them in tunnels, consuming 1 tunnel placer
        World world = context.getWorld();
        if (!(world instanceof ServerWorld)) return ActionResultType.PASS;
        BlockPos pos = context.getPos();
        if (world.getBlockState(pos) != Registries.getBlock(Wall.NAME).getDefaultState()) return ActionResultType.PASS;

        if (SubRoomsManager.tunnel((ServerWorld)world, pos, null, true, false)) {
            world.setBlockState(pos, Registries.getBlock(Tunnel.NAME).getDefaultState());
            ItemStack stack = context.getItem();
            stack.shrink(1);
        }

        return ActionResultType.SUCCESS;
    }
}
