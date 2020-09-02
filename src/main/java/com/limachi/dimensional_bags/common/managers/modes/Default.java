package com.limachi.dimensional_bags.common.managers.modes;

import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.managers.Mode;
import com.limachi.dimensional_bags.common.network.Network;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class Default extends Mode {

    public static final String ID = "Default";

    public Default() { super(ID, true, true); } //will always be called last (if no mode consumed the event first)

    @Override
    public ActionResultType onItemUse(EyeData data, ItemUseContext context) { //called when the bag is right clicked on something, before the bag does anything
        if (context.getPlayer() == null || !context.getPlayer().isCrouching()) return ActionResultType.PASS; //only test a player croushing
        int x = Math.abs(context.getPos().getX() - context.getPlayer().getPosition().getX());
        int y = Math.abs(context.getPos().getY() - context.getPlayer().getPosition().getY());
        int z = Math.abs(context.getPos().getZ() - context.getPlayer().getPosition().getZ());
        if (x > 1 || y > 2 || z > 1) return ActionResultType.PASS; //only validate if the click is close enough to the player
        //spawn the entity instead of oppening the upgrade GUI
        if (!context.getItem().hasTag()) return ActionResultType.PASS; //missing tag
        int id = context.getItem().getTag().getInt(Bag.ID_KEY);
        if (id == 0) return ActionResultType.PASS; //invalid id
//        EyeData data = EyeData.get(context.getWorld().getServer(), id); //request the bag data
        BagEntity.spawn(context.getWorld(), context.getPos().up(1), id, context.getItem()); //spawn the bag entity and attach the bag item to it
        context.getPlayer().setHeldItem(context.getHand(), ItemStack.EMPTY); //remove the bag from the player inventory
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(EyeData data, World world, PlayerEntity player, Hand hand) { //called when the bag is right clicked in the air or shift-right-clicked, before the bag does anything (except set the id if needed and accessing data)
        if (!player.isCrouching()) {
            Network.openEyeInventory((ServerPlayerEntity) player, data);
            return new ActionResult<>(ActionResultType.SUCCESS, player.getHeldItem(hand));
        }
        return new ActionResult<>(ActionResultType.PASS, player.getHeldItem(hand));
    }
}
