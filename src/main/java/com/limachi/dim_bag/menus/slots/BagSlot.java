package com.limachi.dim_bag.menus.slots;

import com.limachi.dim_bag.items.BagItem;
import com.limachi.dim_bag.save_datas.BagsData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class BagSlot extends SlotItemHandler {
    public int bag;
    public BlockPos slot;
    protected Function<BagSlot, Boolean> isActive;
    LazyOptional<IItemHandler> slotAccess = null;

    public BagSlot(int bag, BlockPos slot, int xPosition, int yPosition, Function<BagSlot, Boolean> isActive) {
        super(EmptyHandler.INSTANCE, 0, xPosition, yPosition);
        this.bag = bag;
        this.slot = slot;
        this.isActive = isActive;
        BagsData.runOnBag(bag, b->slotAccess = b.slotHandle(slot));
    }

    public BagSlot(int xPosition, int yPosition, Function<BagSlot, Boolean> isActive) {
        super(new InvWrapper(new SimpleContainer(1)), 0, xPosition, yPosition);
        this.bag = 0;
        this.slot = null;
        this.isActive = isActive;
    }

    @Override
    public IItemHandler getItemHandler() {
        if (bag > 0 && (slotAccess == null || !slotAccess.isPresent()))
            BagsData.runOnBag(bag, b->slotAccess = b.slotHandle(slot));
        if (slotAccess != null)
            return slotAccess.orElse(EmptyHandler.INSTANCE);
        return bag == 0 ? super.getItemHandler() : EmptyHandler.INSTANCE;
    }

    @Override
    public boolean isActive() { return isActive.apply(this); }

    public void changeSlotServerSide(BlockPos slot) {
        this.slot = slot;
        slotAccess = null;
    }

    @Override
    public boolean isHighlightable() { return isActive(); }

    protected boolean validStack(ItemStack stack) {
        return !(stack.getItem() instanceof BagItem) || BagItem.getBagId(stack) != bag;
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        if (!isActive() || stack.isEmpty() || !validStack(stack))
            return false;
        return getItemHandler().isItemValid(getSlotIndex(), stack);
    }

    @Override
    public boolean mayPickup(@Nonnull Player playerIn) {
        return isActive() && validStack(super.getItem()) && super.mayPickup(playerIn);
    }

    @Override
    public int getMaxStackSize() { return isActive() ? getItemHandler().getSlotLimit(getSlotIndex()) : 0; }

    @Override
    public int getMaxStackSize(@Nonnull ItemStack stack) { return isActive() ? super.getMaxStackSize(stack) : 0; }

    @Override
    public @Nonnull ItemStack getItem() { return isActive() ? super.getItem() : ItemStack.EMPTY; }

    @Override
    public @Nonnull ItemStack remove(int amount) {
        return isActive() && validStack(super.getItem()) ? super.remove(amount) : ItemStack.EMPTY;
    }
}
