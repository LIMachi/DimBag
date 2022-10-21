package com.limachi.dim_bag.moduleSystem;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

public interface IModuleBehavior {
    boolean install(boolean simulate, Player player, int bagId, ItemStack stack);
    default boolean listensTo(Class<? extends Event> event) { return false; }
    default void event(Event event) {}
}
