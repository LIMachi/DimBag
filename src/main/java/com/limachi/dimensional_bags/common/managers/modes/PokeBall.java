package com.limachi.dimensional_bags.common.managers.modes;

import com.limachi.dimensional_bags.common.WorldUtils;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.managers.Mode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.List;

public class PokeBall extends Mode {
    public PokeBall() {
        super("PokeBall", false, true);
    }

    @Override
    public ActionResultType onAttack(EyeData data, ItemStack stack, PlayerEntity player, Entity entity) {
        data.tpIn(entity);
        return ActionResultType.SUCCESS;
    }

    private Entity getFirstNonPlayerEntityNestToEye(EyeData data) {
        WorldUtils.getRiftWorld().getChunk(data.getEyePos());
        List<Entity> le = WorldUtils.getRiftWorld().getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(data.getEyePos().add(-7, -7, -7), data.getEyePos().add(7, 7, 7)), e->!(e instanceof PlayerEntity));
        return le.size() > 0 ? le.get(0) : null;
    }

    @Override
    public ActionResultType onItemUse(EyeData data, ItemUseContext context) {
        WorldUtils.teleportEntity(getFirstNonPlayerEntityNestToEye(data), WorldUtils.worldRKFromWorld(context.getWorld()), context.getPos().offset(Direction.UP));
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(EyeData data, World world, PlayerEntity player, Hand hand) {
        if (!player.isCrouching()) {
            WorldUtils.teleportEntity(getFirstNonPlayerEntityNestToEye(data), WorldUtils.worldRKFromWorld(player.world), player.getPositionVec().add(0, 1, 0).add(player.getLookVec().scale(5)));
            return new ActionResult<>(ActionResultType.SUCCESS, player.getHeldItem(hand));
        }
        return new ActionResult<>(ActionResultType.PASS, player.getHeldItem(hand));
    }
}
