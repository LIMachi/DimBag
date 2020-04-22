package com.limachi.dimensional_bags.common.data;

import com.limachi.dimensional_bags.DimensionalBagsMod;
import com.limachi.dimensional_bags.common.config.DimBagConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;

import java.util.Set;
import java.util.UUID;

public class EyeData implements IInventory { //all information about an eye (accessed through bag item, bag entity, bag eye and other tiles in the bag

    private int id;
    private int rows;
    private int columns;
    private int radius;
    private UUID owner;

    private ItemStack[] items;

    public boolean dirty;

    private DimBagData dataManager;

    public EyeData(DimBagData dataManager) { this.id = 0; this.dataManager = dataManager; }

    public EyeData(DimBagData dataManager, PlayerEntity owner, int id) {
        this.dataManager = dataManager;
        this.id = id;
        this.owner = owner.getUniqueID();
        this.rows = DimBagConfig.startingRows;
        this.columns = DimBagConfig.startingColumns;
        this.radius = DimBagConfig.startingRadius;
        this.newItems();
        this.markDirty();
    }

    public void attachDataManager(DimBagData dataManager) { this.dataManager = dataManager; }

    public EyeData(PacketBuffer buff) {
        this((DimBagData)null); //dangerous, dataManager should be attached asap
        this.readBytes(buff);
    }

    private void newItems() {
        this.items = new ItemStack[this.rows * this.columns];
        for (int i = 0; i < this.rows * this.columns; ++i)
            this.items[i] = ItemStack.EMPTY;
    }

    public IdHandler getId() { return new IdHandler(this.id); }
    public int getRows() { return this.rows; }
    public int getColumns() { return this.columns; }
    public int getRadius() { return this.radius; }

    public CompoundNBT write(CompoundNBT nbt) {
        DimensionalBagsMod.LOGGER.info("Storing data for eye " + this.id);
        nbt.putInt("Id", this.id);
        nbt.putUniqueId("Owner", this.owner);
        nbt.putInt("Rows", this.rows);
        nbt.putInt("Columns", this.columns);
        nbt.putInt("Radius", this.radius);
        ListNBT list = new ListNBT();
        for (int i = 0; i < this.items.length; ++i) {
            if (this.items[i].isEmpty()) continue;
            CompoundNBT stack = new CompoundNBT();
            stack.putInt("ItemStackIndex", i);
            this.items[i].write(stack);
            list.add(stack);
        }
        nbt.put("Items", list);
        return nbt;
    }

    public void read(CompoundNBT nbt) {
        this.id = nbt.getInt("Id");
        this.owner = nbt.getUniqueId("Owner");
        this.rows = nbt.getInt("Rows");
        this.columns = nbt.getInt("Columns");
        this.radius = nbt.getInt("Radius");
        this.newItems();
        ListNBT list = nbt.getList("Items", 10); //type 10 == CompoundNBT
        for (int i = 0; i < list.size(); ++i) {
            CompoundNBT stack = list.getCompound(i);
            int index = stack.getInt("ItemStackIndex");
            if (index < items.length)
                this.items[index] = ItemStack.read(stack);
        }
    }

    public void toBytes(PacketBuffer buff) { //exact order matters
        buff.writeInt(this.id);
        buff.writeUniqueId(this.owner);
        buff.writeInt(this.rows);
        buff.writeInt(this.columns);
        buff.writeInt(this.radius);
        for (int i = 0; i < this.rows * this.columns; ++i)
            buff.writeItemStack(this.items[i]);
    }

    public void readBytes(PacketBuffer buff) { //exact order matters
        this.id = buff.readInt();
        this.owner = buff.readUniqueId();
        this.rows = buff.readInt();
        this.columns = buff.readInt();
        this.radius = buff.readInt();
        this.newItems();
        for (int i = 0; i < this.rows * this.columns; ++i)
            this.items[i] = buff.readItemStack();
        this.markDirty();
    }

    @Override
    public int getSizeInventory() {
        return this.columns * this.rows;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack: this.items)
            if (!stack.isEmpty())
                return false;
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        synchronized (this) {
            return this.items[index];
        }
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        synchronized (this) {
            ItemStack item = this.items[index];
            if (!item.isEmpty()) {
                if (item.getCount() <= count) {
                    this.items[index] = ItemStack.EMPTY;
                    this.markDirty();
                    return item;
                }
                ItemStack split = item.split(count);
                if (item.getCount() == 0)
                    this.items[index] = ItemStack.EMPTY;
                else
                    this.items[index] = item;
                this.markDirty();
                return split;
            }
            return ItemStack.EMPTY;
        }
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        synchronized (this) {
            ItemStack stack = this.items[index];
            this.items[index] = ItemStack.EMPTY;
            this.markDirty();
            return stack;
        }
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        synchronized (this) {
            this.items[index] = ItemStack.EMPTY;
            this.markDirty();
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
        DimensionalBagsMod.LOGGER.info("eye " + this.id + " is now dirty"); //send the game in an infinite loop... oops; should rework the data sync
        if (this.dataManager != null && this.dataManager.side == DimBagData.Side.SERVER) {
            this.dirty = true;
            this.dataManager.update(true);
        }
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
        return true; //missing logic
    }

    @Override
    public void openInventory(PlayerEntity player) {

    }

    @Override
    public void closeInventory(PlayerEntity player) {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return false;
    }

    @Override
    public int count(Item itemIn) {
        return 0;
    }

    @Override
    public boolean hasAny(Set<Item> set) {
        return false;
    }

    @Override
    public void clear() {

    }
}
