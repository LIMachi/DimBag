package com.limachi.dimensional_bags.common.inventory;

import com.limachi.dimensional_bags.common.data.IMarkDirty;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class NBTStoredItemHandler implements IItemHandler, IMarkDirty {

    public static class ShulkerBoxItemHandler extends NBTStoredItemHandler {

        public ShulkerBoxItemHandler(@Nonnull ItemStack shulkerBox, @Nullable IMarkDirty makeDirty) {
            super(()->{
                if (shulkerBox.getTag() == null) {
                    shulkerBox.setTag(new CompoundNBT());
                    if (makeDirty != null)
                        makeDirty.markDirty();
                }
                return shulkerBox.getTag().getCompound("BlockEntityTag");
            }, nbt->shulkerBox.getTag().put("BlockEntityTag", nbt), makeDirty);
        }
    }

    public static final int DEFAULT_SIZE = 27;
    protected ItemStack[] stacks;
    protected final Consumer<CompoundNBT> write;
    protected final Supplier<CompoundNBT> read;
    protected final IMarkDirty makeDirty;
    protected boolean wasSized;

    /**
     * this class should be compatible with the shulker box, but can also be used for any nbt based itemprovider with the ability to change this nbt
     * @param read: a lambda to read the nbt, the supplied value can be null (treated as an uninitialized inventory of `DEFAULT_SIZE` (27) slots, like a single chest/shulkerbox)
     * @param write: the parameter will contain the serialized inventory, merged on the compound returned by read (for easier assignation)
     */
    public NBTStoredItemHandler(@Nonnull Supplier<CompoundNBT> read, @Nonnull Consumer<CompoundNBT> write, @Nullable IMarkDirty makeDirty) {
        this.read = read;
        this.write = write;
        this.makeDirty = makeDirty;
        CompoundNBT nbt = read.get();
        int size = DEFAULT_SIZE;
        wasSized = false;
        if (nbt != null && nbt.keySet().contains("Size")) {
            wasSized = true;
            size = nbt.getInt("Size");
        }
        stacks = new ItemStack[size];
        for (int i = 0; i < size; ++i)
            stacks[i] = ItemStack.EMPTY;
        if (nbt != null) {
            ListNBT list = nbt.getList("Items", 10);
            for (int i = 0; i < list.size(); ++i) {
                CompoundNBT entry = list.getCompound(i);
                stacks[entry.getInt("Slot")] = ItemStack.read(entry);
            }
        }
    }

    public NBTStoredItemHandler resize(int newSize) {
        if (newSize == stacks.length) return this;
        ItemStack[] tmp = new ItemStack[newSize];
        if (newSize > stacks.length) {
            int i = 0;
            for (; i < stacks.length; ++i)
                tmp[i] = stacks[i];
            for (; i < newSize; ++i)
                tmp[i] = ItemStack.EMPTY;
        } else
            for (int i = 0; i < newSize; ++i)
                tmp[i] = stacks[i];
        stacks = tmp;
        markDirty();
        return this;
    }

    @Override
    public void markDirty() {
        ListNBT list = new ListNBT();
        for (int i = 0; i < stacks.length; ++i)
            if (!stacks[i].isEmpty()) {
                CompoundNBT entry = stacks[i].write(new CompoundNBT());
                entry.putInt("Slot", i);
                list.add(entry);
            }
        CompoundNBT out = read.get();
        if (wasSized || stacks.length != DEFAULT_SIZE)
            out.putInt("Size", stacks.length);
        out.put("Items", list);
        write.accept(out);
        if (makeDirty != null)
            makeDirty.markDirty();
    }

    @Override
    public int getSlots() { return stacks.length; }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) { return slot < 0 || slot >= stacks.length ? ItemStack.EMPTY : stacks[slot]; }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        if (!isItemValid(slot, stack)) return stack;
        ItemStack stackInSlot = getStackInSlot(slot);

        int stackLimit = Math.min(stack.getMaxStackSize(), getSlotLimit(slot));

        if (!stackInSlot.isEmpty())
        {
            if (stackInSlot.getCount() >= stackLimit) return stack; //limit already reached (note: since we merge stacks, there is no need to test the size of the stack already in the slot)
            if (!ItemHandlerHelper.canItemStacksStack(stack, stackInSlot)) return stack; //those stacks can't be merged
            stackLimit -= stackInSlot.getCount(); //how much can be inserted (tacking into acount the number of items already in the slot)
            if (stack.getCount() <= stackLimit) { //there is enough room to insert the entirety of the stack
                if (!simulate) {
                    ItemStack copy = stack.copy();
                    copy.grow(stackInSlot.getCount());
                    stacks[slot] = copy;
                    markDirty();
                }
                return ItemStack.EMPTY;
            } else {//not enough room, we will return a truncated stack
                stack = stack.copy();
                if (!simulate)
                {
                    ItemStack copy = stack.split(stackLimit);
                    copy.grow(stackInSlot.getCount());
                    stacks[slot] = copy;
                    markDirty();
                    return stack;
                } else {
                    stack.shrink(stackLimit);
                    return stack;
                }
            }
        } else { //the slot we want to insert the stack into is empty
            if (stackLimit < stack.getCount()) {
                stack = stack.copy();
                if (!simulate) {
                    stacks[slot] = stack.split(stackLimit);
                    markDirty();
                    return stack;
                } else {
                    stack.shrink(stackLimit);
                    return stack;
                }
            } else {
                if (!simulate) {
                    stacks[slot] = stack;
                    markDirty();
                }
                return ItemStack.EMPTY;
            }
        }
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) return ItemStack.EMPTY; //nothing requested
        ItemStack stackInSlot = getStackInSlot(slot);
        if (stackInSlot.isEmpty()) return ItemStack.EMPTY; //nothing to extract

        amount = Math.min(amount, Math.max(0, stackInSlot.getCount()));
        if (amount == 0) return ItemStack.EMPTY; //minimum limit reached, can't remove more items

        if (simulate) {
            if (stackInSlot.getCount() < amount)
                return stackInSlot.copy();
            else {
                ItemStack copy = stackInSlot.copy();
                copy.setCount(amount);
                return copy;
            }
        } else {
            ItemStack decrStackSize = amount > 0 ? stacks[slot].split(amount) : ItemStack.EMPTY;
            markDirty();
            return decrStackSize;
        }
    }

    @Override
    public int getSlotLimit(int slot) { return 64; }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return slot >= 0 && slot < stacks.length && (stacks[slot].isEmpty() || (stacks[slot].isItemEqual(stack) && ItemStack.areItemStackTagsEqual(stacks[slot], stack)));
    }
}
