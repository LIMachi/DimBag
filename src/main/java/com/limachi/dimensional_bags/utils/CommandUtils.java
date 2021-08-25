package com.limachi.dimensional_bags.utils;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;

import java.util.Collection;

public class CommandUtils {
/*
    public static void give(Collection<PlayerEntity> targets, ItemStack stack) { //based on GiveCommand::giveItem
        for(PlayerEntity playerentity : targets) {
            if (!(playerentity instanceof ServerPlayerEntity)) continue; //tricky way of allowing this method to run client side
            int i = stack.getCount();
            while(i > 0) {
                int j = Math.min(stack.getItem().getMaxStackSize(), i);
                i -= j;
                ItemStack itemstack = stack.copy();
                stack.setCount(j);
                boolean flag = playerentity.inventory.addItem(itemstack);
                if (flag && itemstack.isEmpty()) {
                    itemstack.setCount(1);
                    ItemEntity itementity1 = playerentity.dropItem(itemstack, false);
                    if (itementity1 != null) {
                        itementity1.makeFakeItem();
                    }

                    playerentity.world.playSound((PlayerEntity)null, playerentity.getPosX(), playerentity.getPosY(), playerentity.getPosZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((playerentity.getRNG().nextFloat() - playerentity.getRNG().nextFloat()) * 0.7F + 1.0F) * 2.0F);
                    playerentity.container.broadcastChanges();
                } else {
                    ItemEntity itementity = playerentity.dropItem(itemstack, false);
                    if (itementity != null) {
                        itementity.setNoPickupDelay();
                        itementity.setOwnerId(playerentity.getUUID());
                    }
                }
            }
        }
    }*/
}
