package com.limachi.dimensional_bags.common.managers.modes;

import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.managers.Mode;
import com.limachi.dimensional_bags.common.network.Network;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

public class Default extends Mode {

    public static final String ID = "Default";

    public Default() { super(ID, true, true); } //will always be called last (if no mode consumed the event first)

    @Override
    public ActionResultType onItemUse(EyeData data, World world, PlayerEntity player, int slot, BlockRayTraceResult ray) { //called when the bag is right clicked on something, before the bag does anything
        if (player == null || !player.isCrouching()) return ActionResultType.PASS; //only test a player croushing
        int x = Math.abs(ray.getPos().getX() - player.getPosition().getX());
        int y = Math.abs(ray.getPos().getY() - player.getPosition().getY());
        int z = Math.abs(ray.getPos().getZ() - player.getPosition().getZ());
        if (x > 1 || y > 2 || z > 1) return ActionResultType.PASS; //only validate if the click is close enough to the player
        //spawn the entity instead of oppening the upgrade GUI
        ItemStack stack = player.inventory.getStackInSlot(slot);
        if (!stack.hasTag()) return ActionResultType.PASS; //missing tag
        int id = stack.getTag().getInt(Bag.ID_KEY);
        if (id == 0) return ActionResultType.PASS; //invalid id
//        EyeData data = EyeData.get(context.getWorld().getServer(), id); //request the bag data
        BagEntity.spawn(world, ray.getPos().up(1), id, stack); //spawn the bag entity and attach the bag item to it
        player.inventory.setInventorySlotContents(slot, ItemStack.EMPTY);
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType onActivateItem(EyeData data, ItemStack stack, PlayerEntity player) {
        if (!player.isCrouching()) {
            Network.openEyeInventory((ServerPlayerEntity) player, data);
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }
}
