package com.limachi.dimensional_bags.common.managers;

import com.limachi.dimensional_bags.common.data.EyeData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.Map;

/*
 * used by the bag to change binding/actions on the fly (meant to be changed by the instalation of upgrades)
 */

public class Mode {

    public final boolean CAN_BACKGROUND;
    public final String NAME;
    public final boolean IS_INSTALED_BY_DEFAULT;

    public Mode(String name, boolean background, boolean installed) {
        this.CAN_BACKGROUND = background;
        this.NAME = name;
        this.IS_INSTALED_BY_DEFAULT = installed;
    }

    public final void attach(Map<String, Mode> col) { col.put(this.NAME, this); }

    public ActionResultType onPlayerTick(EyeData data, ItemStack stack, World world, Entity player, int itemSlot, boolean isSelected) { return ActionResultType.PASS; } //called while the bag is ticking inside a player inventory
    public ActionResultType onEntityTick(EyeData data, ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) { return ActionResultType.PASS; } //called every X ticks by the bag manager
    public ActionResultType onItemUse(EyeData data, ItemUseContext context) { return ActionResultType.PASS; } //called when the bag is right clicked on something, before the bag does anything
    public ActionResult<ItemStack> onItemRightClick(EyeData data, World world, PlayerEntity player, Hand hand) { return new ActionResult<>(ActionResultType.PASS, player.getHeldItem(hand)); } //called when the bag is right clicked in the air or shift-right-clicked, before the bag does anything (except set the id if needed and accessing data)
    public ActionResultType onAttack(EyeData data, ItemStack stack, PlayerEntity player, Entity entity) { return ActionResultType.PASS; } //called when the bag is left-clicked on an entity
}
