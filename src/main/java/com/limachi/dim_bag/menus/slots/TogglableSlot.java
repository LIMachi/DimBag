package com.limachi.dim_bag.menus.slots;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class TogglableSlot extends Slot {
    protected Function<TogglableSlot, Boolean> isActive;

    public TogglableSlot(Container container, int index, int x, int y, Function<TogglableSlot, Boolean> isActive) {
        super(container, index, x, y);
        this.isActive = isActive;
    }

    @Override
    public boolean isActive() { return isActive.apply(this); }

    @Override
    public boolean isHighlightable() { return isActive(); }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) { return isActive() && super.mayPlace(stack); }

    @Override
    public boolean mayPickup(@Nonnull Player player) { return isActive() && super.mayPickup(player); }

    @Override
    public int getMaxStackSize() { return isActive() ? super.getMaxStackSize() : 0; }

    @Override
    public int getMaxStackSize(@Nonnull ItemStack stack) { return isActive() ? super.getMaxStackSize(stack) : 0; }

    @Override
    public @Nonnull ItemStack getItem() { return isActive() ? super.getItem() : ItemStack.EMPTY; }

    @Override
    public @Nonnull ItemStack remove(int amount) {
        return isActive() ? super.remove(amount) : ItemStack.EMPTY;
    }
}
