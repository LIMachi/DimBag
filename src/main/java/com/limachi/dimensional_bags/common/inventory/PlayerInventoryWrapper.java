package com.limachi.dimensional_bags.common.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

public class PlayerInventoryWrapper implements IBaseInventory { //mix of BaseInventory and InvWrapper

    protected int rows;
    protected int columns;
    protected final IInventory inv;

    public PlayerInventoryWrapper(IInventory inv, int rows, int columns) {
        this.inv = inv;
        this.rows = rows;
        this.columns = columns;
    }

    public PlayerInventoryWrapper(PacketBuffer buffer) { //client side, fake inventory
        rows = buffer.readInt();
        columns = buffer.readInt();
        inv = new IInventory() {
            private ItemStack items[] = new ItemStack[rows * columns];
            @Override
            public int getSizeInventory() { return rows * columns; }
            @Override
            public boolean isEmpty() { return false; } //for now, always return that the inventory contains something, even if it's not the case
            @Override
            public ItemStack getStackInSlot(int index) { return items[index]; }
            @Override
            public ItemStack decrStackSize(int index, int count) { return items[index].split(count); }
            @Override
            public ItemStack removeStackFromSlot(int index) {
                ItemStack out = items[index];
                items[index] = ItemStack.EMPTY;
                return out;
            }
            @Override
            public void setInventorySlotContents(int index, ItemStack stack) { items[index] = stack; }
            @Override
            public void markDirty() {}
            @Override
            public boolean isUsableByPlayer(PlayerEntity player) { return true; }
            @Override
            public void clear() {}
        };
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        PlayerInventoryWrapper that = (PlayerInventoryWrapper) o;

        return inv.equals(that.getInv());

    }

    public IInventory getInv() { return inv; }

    @Override
    public int hashCode() { return inv.hashCode(); }

    @Override
    public int getSlots() { return inv.getSizeInventory(); }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int slot) { return inv.getStackInSlot(slot); }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty())
            return ItemStack.EMPTY;

        ItemStack stackInSlot = inv.getStackInSlot(slot);

        int m;
        if (!stackInSlot.isEmpty()) {
            if (stackInSlot.getCount() >= Math.min(stackInSlot.getMaxStackSize(), getSlotLimit(slot)))
                return stack;

            if (!ItemHandlerHelper.canItemStacksStack(stack, stackInSlot))
                return stack;

            if (!inv.isItemValidForSlot(slot, stack))
                return stack;

            m = Math.min(stack.getMaxStackSize(), getSlotLimit(slot)) - stackInSlot.getCount();

            if (stack.getCount() <= m) {
                if (!simulate) {
                    ItemStack copy = stack.copy();
                    copy.grow(stackInSlot.getCount());
                    inv.setInventorySlotContents(slot, copy);
                    inv.markDirty();
                }

                return ItemStack.EMPTY;
            } else {
                stack = stack.copy();
                if (!simulate) {
                    ItemStack copy = stack.split(m);
                    copy.grow(stackInSlot.getCount());
                    inv.setInventorySlotContents(slot, copy);
                    inv.markDirty();
                    return stack;
                } else {
                    stack.shrink(m);
                    return stack;
                }
            }
        } else {
            if (!inv.isItemValidForSlot(slot, stack))
                return stack;

            m = Math.min(stack.getMaxStackSize(), getSlotLimit(slot));
            if (m < stack.getCount()) {
                stack = stack.copy();
                if (!simulate) {
                    inv.setInventorySlotContents(slot, stack.split(m));
                    inv.markDirty();
                    return stack;
                } else {
                    stack.shrink(m);
                    return stack;
                }
            } else {
                if (!simulate) {
                    inv.setInventorySlotContents(slot, stack);
                    inv.markDirty();
                }
                return ItemStack.EMPTY;
            }
        }
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0)
            return ItemStack.EMPTY;

        ItemStack stackInSlot = inv.getStackInSlot(slot);

        if (stackInSlot.isEmpty())
            return ItemStack.EMPTY;

        if (simulate) {
            if (stackInSlot.getCount() < amount) {
                return stackInSlot.copy();
            }
            else {
                ItemStack copy = stackInSlot.copy();
                copy.setCount(amount);
                return copy;
            }
        } else {
            int m = Math.min(stackInSlot.getCount(), amount);

            ItemStack decrStackSize = inv.decrStackSize(slot, m);
            inv.markDirty();
            return decrStackSize;
        }
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) { inv.setInventorySlotContents(slot, stack); }

    @Override
    public int getSlotLimit(int slot) { return inv.getInventoryStackLimit(); }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) { return inv.isItemValidForSlot(slot, stack); }

    @Override
    public ITextComponent getDisplayName() { return new TranslationTextComponent("inventory.wrapper.name"); }

    @Override
    public int getRows() { return rows; }

    @Override
    public int getColumns() { return columns; }

    @Override
    public int getSize() { return inv.getSizeInventory(); }

    @Override
    public int getSizeSignature() { return (rows & 0xFF) | ((columns & 0xFF) << 8) | ((inv.getSizeInventory() & 0xFFFF) << 16); }

    @Override
    public void resizeInventory(int size, int rows, int columns) {} //noop, this inventory isn't really mine

    @Override
    public PacketBuffer toBytes(PacketBuffer buff) { //normally, used to send the inital state of the inventory to the client side, will only send the rows/columns for now
        buff.writeInt(rows);
        buff.writeInt(columns);
        return buff;
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) { return nbt; } //noop, the inventory is strong and independent

    @Override
    public void read(CompoundNBT nbt) {} //noop, the inventory is strong and independent

    @Override
    public BaseItemStackAccessor getSlotAccessor(int slot) {
        return new BaseItemStackAccessor(inv.getStackInSlot(slot), true, true, 0, inv.getInventoryStackLimit());
    }
}
