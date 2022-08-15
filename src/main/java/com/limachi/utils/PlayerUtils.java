package com.limachi.utils;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("unused")
public class PlayerUtils {
    public static final int MAX_SLOT = 41;
    public static void giveOrDrop(Player player, ItemStack stack) {
        if (!player.addItem(stack)) { player.drop(stack, true); }
    }
}
