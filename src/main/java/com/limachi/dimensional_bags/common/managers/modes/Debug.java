package com.limachi.dimensional_bags.common.managers.modes;

import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.managers.Mode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

import static com.limachi.dimensional_bags.DimBag.LOGGER;

public class Debug extends Mode {
    public Debug() { super("Debug", false, false); }

    public ActionResultType onPlayerTick(EyeData data, ItemStack stack, World world, Entity player, int itemSlot, boolean isSelected) { LOGGER.info("onPlayerTick"); return ActionResultType.SUCCESS; }
    public ActionResultType onEntityTick(EyeData data, ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) { LOGGER.info("onEntityTick"); return ActionResultType.SUCCESS; }
    public ActionResultType onItemUse(EyeData data, World world, PlayerEntity player, int slot, BlockRayTraceResult ray) { LOGGER.info("onItemUse"); return ActionResultType.SUCCESS; }
    public ActionResult<ItemStack> onItemRightClick(EyeData data, World world, PlayerEntity player, int slot) { LOGGER.info("onItemRightClick"); return new ActionResult<>(ActionResultType.SUCCESS, player.inventory.getStackInSlot(slot)); }
    public ActionResultType onAttack(EyeData data, ItemStack stack, PlayerEntity player, Entity entity) { LOGGER.info("onAttack"); return ActionResultType.SUCCESS; }
}
