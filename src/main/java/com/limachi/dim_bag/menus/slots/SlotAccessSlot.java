package com.limachi.dim_bag.menus.slots;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class SlotAccessSlot extends Slot {
    public static final Container EMPTY = new SimpleContainer(0);
    protected SlotAccess access;

    public SlotAccessSlot(SlotAccess access, int x, int y) {
        super(EMPTY, 0, x, y);
        this.access = access;
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) { return !stack.isEmpty(); }

    @Override
    @Nonnull
    public ItemStack getItem() { return access.get(); }

    @Override
    public void set(@Nonnull ItemStack stack) { access.set(stack); }

    @Override
    public void onQuickCraft(@Nonnull ItemStack oldStack, @Nonnull ItemStack newStack) {}

    @Override
    public int getMaxStackSize() { return 64; }

    @Override
    public int getMaxStackSize(@Nonnull ItemStack stack) {
        return Math.min(getMaxStackSize(), stack.getMaxStackSize());
    }

    @Override
    @Nonnull
    public ItemStack remove(int amount) {
        ItemStack current = getItem();
        int max = Math.min(amount, current.getCount());
        if (max > 0) {
            if (max < current.getCount()) {
                ItemStack extracted = current.copyWithCount(max);
                current.setCount(current.getCount() - max);
                return extracted;
            }
            access.set(ItemStack.EMPTY);
            return current;
        }
        return ItemStack.EMPTY;
    }
}
