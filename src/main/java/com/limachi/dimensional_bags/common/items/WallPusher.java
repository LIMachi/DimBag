package com.limachi.dimensional_bags.common.items;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

@StaticInit
public class WallPusher extends Item implements IDimBagCommonItem {

    public static String NAME = "wall_pusher";

    static {
        Registries.registerItem(NAME, WallPusher::new);
    }

    public WallPusher() { super(DimBag.DEFAULT_PROPERTIES); }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        World world = context.getLevel();
        if (!(world instanceof ServerWorld)) return ActionResultType.PASS;
        BlockPos pos = context.getClickedPos();
        if (!SubRoomsManager.isWall(world, pos)) return ActionResultType.PASS;

        if (SubRoomsManager.pushWall((ServerWorld) world, pos)) {
            ItemStack stack = context.getItemInHand();
            stack.shrink(1);
        }

        return ActionResultType.SUCCESS;
    }
}
