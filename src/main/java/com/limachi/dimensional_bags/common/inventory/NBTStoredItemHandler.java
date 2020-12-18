package com.limachi.dimensional_bags.common.inventory;

import com.limachi.dimensional_bags.common.NBTUtils;
import com.limachi.dimensional_bags.common.data.IMarkDirty;
import javafx.util.Pair;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class NBTStoredItemHandler implements InventoryUtils.IFormatAwareItemHandler, IMarkDirty {

    public static class ShulkerBoxItemHandler extends NBTStoredItemHandler {

        public ShulkerBoxItemHandler(@Nonnull ItemStack shulkerBox, @Nullable IMarkDirty makeDirty) {
            super(()->{
                if (shulkerBox.getTag() == null) {
                    shulkerBox.setTag(new CompoundNBT());
                }
                return shulkerBox.getTag().getCompound("BlockEntityTag");
            }, nbt->{
                shulkerBox.getTag().put("BlockEntityTag", nbt);
                if (makeDirty != null)
                    makeDirty.markDirty();
            });
        }
    }

    public static final int DEFAULT_SIZE = 27;
    protected ItemStack[] stacks;
    protected InventoryUtils.ItemStackIORights[] rights;
    protected final Consumer<CompoundNBT> write;
    protected final Supplier<CompoundNBT> read;
    protected boolean wasFormatted;
    protected InventoryUtils.ItemHandlerFormat format = InventoryUtils.ItemHandlerFormat.CHEST;
    protected int columns = 9;
    protected int rows = 3;
    protected boolean hadRights;

    /**
     * this class should be compatible with the shulker box, but can also be used for any nbt based itemprovider with the ability to change this nbt
     * @param read: a lambda to read the nbt, the supplied value can be null (treated as an uninitialized inventory of `DEFAULT_SIZE` (27) slots, like a single chest/shulkerbox)
     * @param write: the parameter will contain the serialized inventory, merged on the compound returned by read (for easier assignation)
     */
    public NBTStoredItemHandler(@Nonnull Supplier<CompoundNBT> read, @Nonnull Consumer<CompoundNBT> write) {
        this.read = read;
        this.write = write;
        CompoundNBT nbt = read.get();
        if (nbt == null) nbt = new CompoundNBT();
        int size = DEFAULT_SIZE;
        wasFormatted = false;
        if (nbt.keySet().contains("Size")) {
            wasFormatted = true;
            size = nbt.getInt("Size");
        }
        stacks = new ItemStack[size];
        rights = new InventoryUtils.ItemStackIORights[size];
        for (int i = 0; i < size; ++i) {
            stacks[i] = ItemStack.EMPTY;
            rights[i] = new InventoryUtils.ItemStackIORights();
        }
        if (wasFormatted && nbt.keySet().contains("Columns"))
            columns = nbt.getInt("Columns");
        if (wasFormatted && nbt.keySet().contains("Rows"))
            rows = nbt.getInt("Rows");
        if (wasFormatted && nbt.keySet().contains("Format"))
            format = InventoryUtils.ItemHandlerFormat.values()[nbt.getInt("Format")];
        ListNBT list = nbt.getList("Items", 10);
        for (int i = 0; i < list.size(); ++i) {
            CompoundNBT entry = list.getCompound(i);
            stacks[entry.getInt("Slot")] = ItemStack.read(entry);
        }
        hadRights = false;
        if (nbt.keySet().contains("Rights")) {
            hadRights = true;
            list = nbt.getList("Rights", 10);
            for (int i = 0; i < list.size(); ++i) {
                CompoundNBT entry = list.getCompound(i);
                rights[entry.getInt("Slot")].readNBT(entry);
            }
        }
    }

    @Override
    public int getRows() { return rows; }

    @Override
    public int getColumns() { return columns; }

    @Override
    public InventoryUtils.ItemHandlerFormat getFormat() { return format; }

    @Override
    public void setRows(int rows) {
        this.rows = rows;
        markDirty();
    }

    @Override
    public void setColumns(int columns) {
        this.columns = columns;
        markDirty();
    }

    @Override
    public void setFormat(InventoryUtils.ItemHandlerFormat format) {
        this.format = format;
        markDirty();
    }

    /**
     * simple helper constructor that will link a compound to an item handler (the writes will use a nbt merge)
     * @param nbt the compound to be used as inventory (must already have been initialized with at least an int in the key 'Size')
     * @return an ItemHandler linked to the given nbt
     */
    public static NBTStoredItemHandler createInPlace(CompoundNBT nbt) {
        return new NBTStoredItemHandler(()->nbt, w->NBTUtils.deepMergeNBTInternal(nbt, w));
    }

    public static NBTStoredItemHandler createInPlace(int size) {
        return createInPlace(NBTUtils.newCompound("Size", size));
    }

    public NBTStoredItemHandler resize(int newSize) {
        if (newSize == stacks.length) return this;
        ItemStack[] tmp = new ItemStack[newSize];
        InventoryUtils.ItemStackIORights[] tmpRights = new InventoryUtils.ItemStackIORights[newSize];
        if (newSize > stacks.length) {
            int i = 0;
            for (; i < stacks.length; ++i) {
                tmp[i] = stacks[i];
                tmpRights[i] = rights[i];
            }
            for (; i < newSize; ++i) {
                tmp[i] = ItemStack.EMPTY;
                tmpRights[i] = new InventoryUtils.ItemStackIORights();
            }
        } else
            for (int i = 0; i < newSize; ++i) {
                tmp[i] = stacks[i];
                tmpRights[i] = rights[i];
            }
        stacks = tmp;
        rights = tmpRights;
        markDirty();
        return this;
    }

    @Override
    public void markDirty() {
        CompoundNBT out = read.get();
        if (out == null)
            out = new CompoundNBT();
        ListNBT list = new ListNBT();
        for (int i = 0; i < stacks.length; ++i)
            if (!stacks[i].isEmpty()) {
                CompoundNBT entry = stacks[i].write(new CompoundNBT());
                entry.putInt("Slot", i);
                list.add(entry);
            }
        out.put("Items", list);
        if (wasFormatted || stacks.length != DEFAULT_SIZE)
            out.putInt("Size", stacks.length);
        if (wasFormatted || columns != 9)
            out.putInt("Columns", columns);
        if (wasFormatted || rows != 3)
            out.putInt("Rows", rows);
        if (wasFormatted || format != InventoryUtils.ItemHandlerFormat.CHEST)
            out.putInt("Format", format.ordinal());
        if (hadRights) {
            list = new ListNBT();
            for (int i = 0; i < rights.length; ++i)
                if (!rights[i].isVanilla()) {
                    CompoundNBT entry = rights[i].writeNBT(new CompoundNBT());
                    entry.putInt("Slot", i);
                    list.add(entry);
                }
            out.put("Rights", list);
        }
        write.accept(out);
    }

    @Override
    public int getSlots() { return stacks.length; }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) { return slot < 0 || slot >= stacks.length ? ItemStack.EMPTY : stacks[slot]; }

    @Nonnull
    @Override
    public InventoryUtils.ItemStackIORights getRightsInSlot(int slot) { return slot < 0 || slot >= rights.length ? InventoryUtils.ItemStackIORights.VANILLA : rights[slot]; }

    @Override
    public void setRightsInSlot(int slot, InventoryUtils.ItemStackIORights right) {
        if (slot >= 0 && slot < rights.length) {
            rights[slot] = right;
            markDirty();
        }
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (slot < 0 || slot >= stacks.length) return stack;
        Pair<ItemStack, ItemStack> p = rights[slot].mergeIn(stacks[slot], stack);
        if (!simulate) {
            stacks[slot] = p.getKey();
            markDirty();
        }
        return p.getValue();
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot < 0 || slot >= stacks.length || amount == 0) return ItemStack.EMPTY;
        Pair<ItemStack, ItemStack> p = rights[slot].mergeOut(stacks[slot], amount, ItemStack.EMPTY);
        if (!simulate) {
            stacks[slot] = p.getKey();
            markDirty();
        }
        return p.getValue();
    }

    @Override
    public int getSlotLimit(int slot) { return slot < 0 || slot >= stacks.length ? 64 : rights[slot].maxStack; }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) { return slot >= 0 && slot < stacks.length && rights[slot].canInsert(stacks[slot], stack); }
}
