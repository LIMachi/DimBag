package com.limachi.dimensional_bags.common.bag.modes;

import com.limachi.dimensional_bags.common.bagDimensionOnly.TunnelBlock;
import com.limachi.dimensional_bags.lib.common.container.SettingsContainer;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.bag.BagItem;
import com.limachi.dimensional_bags.lib.utils.WorldUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;

public class Settings extends AbstractMode {

    public static final String ID = "Settings";

    public Settings() { super(ID, false, true); }

    @Override
    public ActionResultType onItemRightClick(int bagId, World world, PlayerEntity player) {
        SettingsContainer.open(player, bagId);
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType onItemUse(int bagId, World world, PlayerEntity player, BlockRayTraceResult ray) {
        /**behavior: if in main hand and block in off hand (or inside ghost bag), allow replacement of walls with the selected block (except tunnels)*/
        if (!world.isClientSide() && ray.getType() == RayTraceResult.Type.BLOCK && !(world.getBlockState(ray.getBlockPos()).getBlock() instanceof TunnelBlock) && SubRoomsManager.isWall(world, ray.getBlockPos()) && player.getItemInHand(Hand.MAIN_HAND).getItem() instanceof BagItem && player.getItemInHand(Hand.OFF_HAND).getItem() instanceof BlockItem) {
            if (WorldUtils.replaceBlockAndGiveBack(ray.getBlockPos(), player, Hand.OFF_HAND, !player.isCreative(), n->!(n == null || n.hasTileEntity() || VoxelShapes.joinIsNotEmpty(n.getCollisionShape(world, ray.getBlockPos()), VoxelShapes.block(), IBooleanFunction.NOT_SAME))))
                return ActionResultType.SUCCESS;
        }
        return onItemRightClick(bagId, world, player);
    }
}