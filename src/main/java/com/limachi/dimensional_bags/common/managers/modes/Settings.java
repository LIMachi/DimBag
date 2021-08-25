package com.limachi.dimensional_bags.common.managers.modes;

import com.limachi.dimensional_bags.common.blocks.Tunnel;
import com.limachi.dimensional_bags.common.container.SettingsContainer;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.managers.Mode;
import com.limachi.dimensional_bags.utils.WorldUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;

public class Settings extends Mode {

    public static final String ID = "Settings";

    public Settings() { super(ID, false, true); }

    @Override
    public ActionResultType onItemRightClick(int eyeId, World world, PlayerEntity player) {
        new SettingsContainer(0, player.inventory, eyeId).open(player);
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType onItemUse(int eyeId, World world, PlayerEntity player, BlockRayTraceResult ray) {
        /**behavior: if in main hand and block in off hand (or inside ghost bag), allow replacement of walls with the selected block (except tunnels)*/
        if (!world.isClientSide() && ray.getType() == RayTraceResult.Type.BLOCK && !(world.getBlockState(ray.getBlockPos()).getBlock() instanceof Tunnel) && SubRoomsManager.isWall(world, ray.getBlockPos()) && player.getItemInHand(Hand.MAIN_HAND).getItem() instanceof Bag && player.getItemInHand(Hand.OFF_HAND).getItem() instanceof BlockItem) {
            if (WorldUtils.replaceBlockAndGiveBack(ray.getBlockPos(), player, Hand.OFF_HAND, !player.isCreative(), n->!(n == null || n.hasTileEntity() || VoxelShapes.joinIsNotEmpty(n.getCollisionShape(world, ray.getBlockPos()), VoxelShapes.block(), IBooleanFunction.NOT_SAME))))
                return ActionResultType.SUCCESS;
        }
        return onItemRightClick(eyeId, world, player);
    }
}