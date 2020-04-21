package com.limachi.dimensional_bags.common.data;

import com.limachi.dimensional_bags.common.config.DimBagConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;

public class EyeData { //all information about an eye (accessed through bag item, bag entity, bag eye and other tiles in the bag

    public class ItemStackDirtyable {
        public boolean dirty;
        public ItemStack stack;
        public ItemStackDirtyable(ItemStack stack, boolean dirty) {
            this.stack = stack;
            this.dirty = dirty;
        }
    }

    private int id;
    private int rows;
    private int columns;
    private int radius;

    private NonNullList<ItemStackDirtyable> items;
    private int dirtyIttems;

    public boolean dirty;

    public EyeData(int id) {
        this.id = id;
        this.rows = DimBagConfig.startingRows;
        this.columns = DimBagConfig.startingColumns;
        this.radius = DimBagConfig.startingRadius;
        this.items = NonNullList.withSize(this.rows * this.columns, new ItemStackDirtyable(ItemStack.EMPTY, false));
        this.dirty = id != 0;
    }

    public IdHandler getId() { return new IdHandler(this.id); }
    public int getRows() { return this.rows; }
    public int getColumns() { return this.columns; }
    public int getRadius() { return this.radius; }
    public NonNullList<ItemStackDirtyable> getItems() { return this.items; }

    public CompoundNBT writeItems(CompoundNBT nbt) {
        ListNBT itemList = new ListNBT();
        for (int i = 0; i < this.items.size(); ++i)
            if (!this.items.get(i).stack.isEmpty()) {
                ItemStack item = this.items.get(i).stack;
                CompoundNBT stack = new CompoundNBT();
                stack.putInt("index", i);
                item.write(stack);
                itemList.add(stack);
            }
        nbt.put("items", itemList);
        return nbt;
    }

    public CompoundNBT write(CompoundNBT nbt) {
        nbt.putInt("id", id);
        nbt.putInt("rows", this.rows);
        nbt.putInt("columns", this.columns);
        nbt.putInt("radius", this.radius);
        nbt = this.writeItems(nbt);
        return nbt;
    }

    public void readItems(CompoundNBT nbt) {
        ListNBT itemList = (ListNBT) nbt.get("items");
        for (int i = 0; i < itemList.size(); ++i) {
            CompoundNBT stack = itemList.getCompound(i);
            int id = stack.getInt("index");
            this.items.set(id, new ItemStackDirtyable(ItemStack.read(stack), true));
            ++this.dirtyIttems;
        }
    }

    public void read(CompoundNBT nbt) {
        this.id = nbt.getInt("id");
        this.rows = nbt.getInt("rows");
        this.columns = nbt.getInt("columns");
        this.radius = nbt.getInt("radius");
        this.items = NonNullList.withSize(this.rows * this.columns, new ItemStackDirtyable(ItemStack.EMPTY, false));
        this.readItems(nbt);
        this.dirty = true;
    }

    public void toBytes(PacketBuffer buff) { //exact order matters
        buff.writeInt(this.id);
        buff.writeInt(this.rows);
        buff.writeInt(this.columns);
        buff.writeInt(this.radius);
        buff.writeInt(this.dirtyIttems);
        for (int i = 0; i < this.items.size(); ++i) {
            ItemStackDirtyable stack = this.items.get(i);
            if (stack.dirty) {
                buff.writeInt(i);
                buff.writeItemStack(stack.stack);
                stack.dirty = false;
            }
        }
        this.dirtyIttems = 0;
        this.dirty = false;
    }

    public void readBytes(PacketBuffer buff) { //exact order matters
        this.id = buff.readInt();
        this.rows = buff.readInt();
        this.columns = buff.readInt();
        this.radius = buff.readInt();
        int c = buff.readInt();
        while (this.items.size() < this.rows * this.columns)
            this.items.add(new ItemStackDirtyable(ItemStack.EMPTY, false));
        for (int i = 0; i < c; ++i) {
            int id = buff.readInt();
            this.items.set(id, new ItemStackDirtyable(buff.readItemStack(), false)); //should be set dirty only server side // you know what, scrap that will redo storage tomorow
        }
    }
}
