package com.limachi.dimensional_bags.common.inventory;

import com.limachi.dimensional_bags.common.data.IMarkDirty;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nonnull;

public class BaseInventory implements IBaseInventory {

    protected BaseItemStackAccessor[] items;
    protected int rows;
    protected int columns;
    protected IMarkDirty dirt;

    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("inventory.base.name");
    }

    public int getRows() { return rows; }
    public int getColumns() { return columns; }
    public int getSize() { return items.length; }

    public BaseInventory(int size, int rows, int columns, IMarkDirty dirt) {
        this.items = new BaseItemStackAccessor[size];
        this.rows = rows;
        this.columns = columns;
        this.dirt = dirt;
        for (int i = 0; i < size; ++i)
            this.items[i] = new BaseItemStackAccessor();
    }

    public int getSizeSignature() { return (rows & 0xFF) | ((columns & 0xFF) << 8) | ((items.length & 0xFFFF) << 16); }

    public BaseInventory(PacketBuffer buff) {
        int sizeSignature = buff.readInt();
        int size = (sizeSignature & 0xFFFF0000) >> 16;
        this.items = new BaseItemStackAccessor[size];
        this.rows = sizeSignature & 0xFF;
        this.columns = (sizeSignature & 0xFF00) >> 8;
        for (int i = 0; i < size; ++i)
            this.items[i] = new BaseItemStackAccessor();
        int used = buff.readShort();
        for (int i = 0; i < used; ++i) {
            int index = buff.readShort();
            items[index] = new BaseItemStackAccessor(buff);
        }
        this.dirt = () -> {};
    }

    public void resizeInventory(int size, int rows, int columns) {
        BaseItemStackAccessor[] tmp = new BaseItemStackAccessor[size];
        for (int y = 0; y < rows; ++y)
            for (int x = 0; x < columns; ++x)
                if (x + y * columns < size)
                    tmp[x + y * columns] = (x < this.columns && y < this.rows && x + y * this.columns < this.items.length) ? this.items[x + y * this.columns] : new BaseItemStackAccessor();
        for (int i = rows * columns; i < size; ++i)
            tmp[i] = new BaseItemStackAccessor();
        this.items = tmp;
        this.rows = rows;
        this.columns = columns;
        this.dirt.markDirty();
    }

    public PacketBuffer toBytes(PacketBuffer buff) {
        short used = 0;
        for (BaseItemStackAccessor item : items)
            if (item.shouldSync())
                ++used;
        buff.writeInt(getSizeSignature());
        buff.writeShort(used);
        for (short i = 0; i < items.length; ++i)
            if (items[i].shouldSync()) {
                buff.writeShort(i);
                items[i].toBytes(buff);
            }
        return buff;
    }

    public CompoundNBT write(CompoundNBT nbt) {
        nbt.putInt("Size", items.length);
        nbt.putInt("Rows", rows);
        nbt.putInt("Columns", columns);
        ListNBT list = new ListNBT();
        for (int i = 0; i < items.length; ++i)
            if (items[i].shouldSync()) {
                CompoundNBT compound = new CompoundNBT();
                compound.putInt("Slot", i);
                list.add(items[i].write(compound));
            }
        nbt.put("List", list);
        return nbt;
    }

    public void read(CompoundNBT nbt) {
        int size = nbt.getInt("Size");
        rows = nbt.getInt("Rows");
        columns = nbt.getInt("Columns");
        this.items = new BaseItemStackAccessor[size];
        for (int i = 0; i < size; ++i)
            this.items[i] = new BaseItemStackAccessor();
        ListNBT list = nbt.getList("List", 10);
        for (int i = 0; i < list.size(); ++i) {
            int index = list.getCompound(i).getInt("Slot");
            items[index].read(list.getCompound(i));
        }
    }

    public BaseItemStackAccessor getSlotAccessor(int slot) {
        if (slot < 0 || slot >= items.length) return null;
        return items[slot];
    }

    @Override
    public int getSlots() { return items.length; }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot < 0 || slot >= items.length) return ItemStack.EMPTY;
        return items[slot].stack;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        if (!isItemValid(slot, stack) || !items[slot].canInput) return stack;
        int maxGrowth = Math.min(items[slot].stack.getMaxStackSize(), getSlotLimit(slot));
        ItemStack inSlot = getStackInSlot(slot);
        if (!inSlot.isEmpty())
            maxGrowth -= inSlot.getCount();
        if (maxGrowth <= 0)
            return stack;
        ItemStack out = ItemStack.EMPTY;
        boolean overflow = stack.getCount() > maxGrowth;
        if (overflow) {
            out = stack.copy();
            out.shrink(maxGrowth);
        }
        if (!simulate) {
            if (!inSlot.isEmpty())
                inSlot.grow(overflow ? maxGrowth : stack.getCount());
            else if (!overflow)
                this.items[slot].stack = stack;
            else {
                ItemStack maxStack = stack.copy();
                maxStack.setCount(maxGrowth);
                this.items[slot].stack = maxStack;
            }
            this.dirt.markDirty();
        }
        return out;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0 || slot < 0 || slot >= items.length || !items[slot].canOutput || items[slot].stack.isEmpty()) return ItemStack.EMPTY;
        BaseItemStackAccessor access = items[slot];
        amount = Math.min(Math.min(amount, access.stack.getMaxStackSize()), Math.max(0, access.stack.getCount() - access.minStackSize)); //actual amount that could be removed, tacking into acount the inital request, max stack size of the item type, current stock of items and minimum amount to keep in the slot after operation
        if (amount == 0) return ItemStack.EMPTY; //yes, after the math the amount could go down to 0
        ItemStack out = access.stack.copy();
        if (amount != access.stack.getCount())
            out.setCount(amount);
        if (!simulate) {
            if (amount == access.stack.getCount())
                access.stack = ItemStack.EMPTY;
            else
                access.stack.shrink(amount);
            this.dirt.markDirty();
        }
        return out;
    }

    @Override
    public int getSlotLimit(int slot) {
        if (slot < 0 || slot >= items.length) return 0;
        return items[slot].maxStackSize;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        if (slot < 0 || slot >= items.length) return false;
        return items[slot].canAccept(stack);
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        if (slot < 0 || slot >= items.length) return;
        items[slot].stack = stack;
        this.dirt.markDirty();
    }
}
