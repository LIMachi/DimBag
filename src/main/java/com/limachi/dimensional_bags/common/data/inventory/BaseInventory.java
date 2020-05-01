package com.limachi.dimensional_bags.common.data.inventory;

import com.limachi.dimensional_bags.DimensionalBagsMod;
import com.limachi.dimensional_bags.common.data.container.BaseContainer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BaseInventory implements IInventory {

//    @Nullable
//    private BaseContainer parent;
    private boolean dirty;
    protected int rows;
    protected int columns;
    protected int inUse;
    protected ItemStack[] items = null; //maximum stack size: byte (so 127 signed)

    public BaseInventory(int size, int rows, int columns/*, BaseContainer parent*/) {
        this.resetItems(size);
        this.inUse = 0;
        this.rows = rows;
        this.columns = columns;
//        this.parent = parent;
    }

//    public BaseContainer getParent() { return this.parent; }
//    public void setParent(BaseContainer container) { this.parent = container; }

    public int getRows() { return this.rows; }
    public int getColumns() { return this.columns; }

    @Override
    public int getSizeInventory() { return this.items.length; }

    public void resizeInventory(int size, int rows, int columns) { //now keep the row and column order of items, can remove items
        ItemStack[] tmp = new ItemStack[size];
        for (int y = 0; y < rows; ++y)
            for (int x = 0; x < columns; ++x)
                if (x + y * columns < size)
                    tmp[x + y * columns] = (x < this.columns && y < this.rows && x + y * this.columns < this.items.length) ? this.items[x + y * this.columns] : ItemStack.EMPTY;
        for (int i = rows * columns; i < size; ++i)
            tmp[i] = ItemStack.EMPTY;
        this.inUse = 0;
        this.items = tmp;
        this.rows = rows;
        this.columns = columns;
        for (ItemStack stack : this.items)
            if (!stack.isEmpty())
                ++this.inUse;
        this.markDirty();
    }

    @Override
    public void markDirty() { this.dirty = true; }
    public void unsetDirtyFlag() { this.dirty = false; }
    public boolean isDirty() { return this.dirty; }

    protected void resetItems(int size) {
        if (size == 0)
            DimensionalBagsMod.LOGGER.warn("Inventory with size 0!");
        this.items = new ItemStack[size];
        for (int i = 0; i < size; ++i)
            this.items[i] = ItemStack.EMPTY;
    }

    public PacketBuffer toBytes(PacketBuffer buff) {
        DimensionalBagsMod.LOGGER.info("preparing inventory packet");
        if (this.items.length == 0)
            DimensionalBagsMod.LOGGER.warn("Inventory with size 0!");
        buff.writeInt(this.items.length);
        buff.writeInt(this.rows);
        buff.writeInt(this.columns);
        buff.writeInt(this.inUse);
        for (int i = 0; i < this.items.length; ++i)
            if (!this.items[i].isEmpty()) {
                buff.writeInt(i);
                buff.writeItemStack(this.items[i]);
            }
        return buff;
    }

    public void readBytes(PacketBuffer buff) {
        DimensionalBagsMod.LOGGER.info("got inventory packet");
        this.resetItems(buff.readInt());
        this.rows = buff.readInt();
        this.columns = buff.readInt();
        this.inUse = buff.readInt();
        for (int i = 0; i < this.inUse; ++i) {
            int index = buff.readInt();
            this.items[index] = buff.readItemStack();
        }
    }

    public CompoundNBT write(CompoundNBT nbt) {
        nbt.putInt("ItemStackListSize", this.items.length);
        nbt.putInt("ItemStackListRows", this.rows);
        nbt.putInt("ItemStackListColumns", this.columns);
        nbt.putInt("ItemStackListInUse", this.inUse);
        ListNBT list = new ListNBT();
        for (int i = 0; i < this.items.length; ++i)
            if (!this.items[i].isEmpty()) {
                CompoundNBT stack = new CompoundNBT();
                stack.putInt("ItemStackIndex", i);
                this.items[i].write(stack);
                list.add(stack);
            }
        nbt.put("ItemStackListData", list);
        return nbt;
    }

    public void read(CompoundNBT nbt) {
        this.inUse = nbt.getInt("ItemStackListInUse");
        this.rows = nbt.getInt("ItemStackListRows");
        this.columns = nbt.getInt("ItemStackListColumns");
        ListNBT list = nbt.getList("ItemStackListData", 10); //10 = compounbt
        int nSize = nbt.getInt("ItemStackListSize");
        this.items = new ItemStack[nSize];
        for (int i = 0; i < nSize; ++i)
            this.items[i] = ItemStack.EMPTY;
        for (int i = 0; i < this.inUse; ++i) {
            CompoundNBT stack = list.getCompound(i);
            int index = stack.getInt("ItemStackIndex");
            this.items[index] = ItemStack.read(stack);
        }
    }

    @Override
    public boolean isEmpty() { return this.inUse != 0; }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot < 0 || slot >= this.items.length)
            return ItemStack.EMPTY;
        return this.items[slot];
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        synchronized (this) {
            if (index < 0 || index >= this.items.length) return ItemStack.EMPTY;
            ItemStack item = this.items[index];
            if (!item.isEmpty()) {
                if (item.getCount() <= count) {
                    this.items[index] = ItemStack.EMPTY;
                    --this.inUse; DimensionalBagsMod.LOGGER.info("decrStackSize 114 in use -- -> " + this.inUse);
                    this.markDirty();
                    return item;
                }
                ItemStack split = item.split(count);
                if (item.getCount() == 0) {
                    this.items[index] = ItemStack.EMPTY;
                    --this.inUse; DimensionalBagsMod.LOGGER.info("decrStackSize 121 in use -- -> " + this.inUse);
                }
                else {
                    DimensionalBagsMod.LOGGER.info("decrStackSize 124, still in use" + this.inUse);
                    this.items[index] = item;
                }
                this.markDirty();
                return split;
            }
            return ItemStack.EMPTY;
        }
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        synchronized (this) {
            if (index < 0 || index >= this.items.length) return ItemStack.EMPTY;
            ItemStack stack = this.items[index];
            this.items[index] = ItemStack.EMPTY;
            --this.inUse; DimensionalBagsMod.LOGGER.info("removeStackFromSlot in use -- -> " + this.inUse);
            this.markDirty();
            return stack;
        }
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        synchronized (this) {
            if (index < 0 || index >= this.items.length) return;
            if (this.items[index] == ItemStack.EMPTY && !stack.isEmpty()) {
                ++this.inUse;
                DimensionalBagsMod.LOGGER.info("setInventorySlotContents in use ++ -> " + this.inUse);
            }
            else if (this.items[index] != ItemStack.EMPTY && stack.isEmpty()) {
                --this.inUse;
                DimensionalBagsMod.LOGGER.info("setInventorySlotContents wrongfully set stack to empty? in use -- -> " + this.inUse);
            } else
                DimensionalBagsMod.LOGGER.info("setInventorySlotContents still in use " + this.inUse);
            this.items[index] = stack;
            this.markDirty();
        }
    }
}
