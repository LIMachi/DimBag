package com.limachi.dimensional_bags.common.inventory;

import com.limachi.dimensional_bags.common.container.slot.InvWrapperSlot;
import com.limachi.dimensional_bags.common.data.IMarkDirty;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.IIntArray;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;

//import static com.limachi.dimensional_bags.common.inventory.Wrapper.IORights.*;

public class Wrapper {/*implements IItemHandlerModifiable, InventoryUtils.IIORIghtItemHandler {
    @Nonnull
    @Override
    public InventoryUtils.ItemStackIORights getRightsInSlot(int slot) {
        return null;
    } //rewrite of InvWrapper to include IO limitations per slots

    public static class IORights {
        public static final byte CANINPUT = 1;
        public static final byte CANOUTPUT = 2;
        public static final byte ISWHITELIST = 4;
        public static final byte WITHOUTNBT = 8;
        public static final byte WITHOUTDAMAGE = 16;
        public static final byte WITHOUTORE = 32;
        public static final byte EXACTQUANTITY = 64;
        public static final byte OVERIDESTACKLIMIT = -128;
        public byte flags;
        public byte minStack;
        public byte maxStack;
        public NonNullList<ItemStack> blacklist;

        public IORights() { //vanilla slot limitations
            flags = CANINPUT | CANOUTPUT;
            minStack = 0;
            maxStack = 64;
            blacklist = NonNullList.create();
        }

        public boolean isVanilla() { return flags == (CANINPUT | CANOUTPUT) && minStack == 0 && maxStack == 64 && blacklist.size() == 0; }

        public IORights(byte flags, byte minStack, byte maxStack, ItemStack ... items) {
            this.flags = flags;
            this.minStack = minStack;
            this.maxStack = maxStack;
            this.blacklist = NonNullList.from(ItemStack.EMPTY, items);
        }

        public IORights(PacketBuffer buff) {
            flags = buff.readByte();
            minStack = buff.readByte();
            maxStack = buff.readByte();
            byte l = buff.readByte();
            blacklist = NonNullList.create();
            for (byte i = 0; i < l; ++i)
                blacklist.add(buff.readItemStack());
        }

        public IORights(int integer) {
            flags = (byte)(integer & 255);
            minStack = (byte)((integer >> 8) & 255);
            maxStack = (byte)((integer >> 16) & 255);
            blacklist = NonNullList.create();
        }

        public int toInt(int field) {
            switch (field) {
                case 0: return flags;
                case 1: return minStack;
                case 2: return maxStack;
                default: return 0;
            }
        }

        public void setField(int field, int data) {
            switch (field) {
                case 0: flags = (byte)data; return;
                case 1: minStack = (byte)data; return;
                case 2: maxStack = (byte)data;
            }
        }

        public static boolean nbtDamageTag(byte flags, ItemStack tested, ItemStack against) {
            if ((flags & WITHOUTDAMAGE) == 0) {
                if (tested.getDamage() != against.getDamage())
                    return false;
            }
            if ((flags & WITHOUTNBT) == 0) {
                CompoundNBT nbt1 = tested.hasTag() ? tested.getTag() : new CompoundNBT();
                CompoundNBT nbt2 = against.hasTag() ? against.getTag() : new CompoundNBT();
                if (!nbt1.equals(nbt2))
                    return false;
            }
            if ((flags & WITHOUTORE) == 0) {
                boolean contained = false;
                for (ResourceLocation tag : tested.getItem().getTags())
                    if (ItemTags.getCollection().get(tag).contains(against.getItem())) {
                        contained = true;
                        break;
                    }
                if (!contained)
                    return false;
            }
            return true;
        }

        public boolean isItemValid(ItemStack stack) { //might be used by merge, so do not test IO, only the blacklist (or whitelist)
            if (stack.isEmpty()) return true;
            for (ItemStack test : blacklist)
                if (nbtDamageTag(flags, stack, test))
                    return ((flags & ISWHITELIST) == 0);
            return (flags & ISWHITELIST) == 0 || blacklist.size() == 0;
        }

        public CompoundNBT write(CompoundNBT nbt) {
            nbt.putByte("Flags", flags);
            nbt.putByte("MinStack", minStack);
            nbt.putByte("MaxStack", maxStack);
            if (blacklist.size() != 0) {
                ListNBT list = new ListNBT();
                for (ItemStack item : blacklist)
                    list.add(item.write(new CompoundNBT()));
                nbt.put("ItemList", list);
            }
            return nbt;
        }

        public void read(CompoundNBT nbt) {
            flags = nbt.getByte("Flags");
            minStack = nbt.getByte("MinStack");
            maxStack = nbt.getByte("MaxStack");
            ListNBT list = nbt.getList("ItemList", 10);
            if (list.size() > 0) {
                if (blacklist.size() > 0)
                    blacklist = NonNullList.create();
                for (int i = 0; i < list.size(); ++i)
                    blacklist.add(ItemStack.read(list.getCompound(i)));
            }
        }

        public PacketBuffer toBytes(PacketBuffer buff) {
            buff.writeByte(flags);
            buff.writeByte(minStack);
            buff.writeByte(maxStack);
            buff.writeByte(blacklist.size());
            for (ItemStack item : blacklist)
                buff.writeItemStack(item);
            return buff;
        }
    }

    protected InventoryUtils.ItemStackIORights[] IO;
    protected IInventory inv;
    protected IMarkDirty dirt;

    public Wrapper(IInventory inv, final InventoryUtils.ItemStackIORights IO[], IMarkDirty dirt) {
        this.inv = inv;
        this.dirt = dirt;
        if (inv.getSizeInventory() == IO.length)
            this.IO = IO;
        else {
            this.IO = new InventoryUtils.ItemStackIORights[inv.getSizeInventory()];
            for (int i = 0; i < this.IO.length; ++i)
                this.IO[i] = i < IO.length ? IO[i] : new InventoryUtils.ItemStackIORights();
        }
    }

    private Wrapper(int size, IInventory inv, PacketBuffer buffer) {
        if (inv == null)
            this.inv = new Inventory(size);
        else
            this.inv = inv;
        assert (this.inv.getSizeInventory() == size);
        this.IO = new InventoryUtils.ItemStackIORights[size];
        for (int i = 0; i < this.inv.getSizeInventory(); ++i)
            this.IO[i] = new InventoryUtils.ItemStackIORights(buffer);
        this.dirt = null;
    }

    public Wrapper(IInventory inv) { this(inv, baseRights(inv.getSizeInventory()), null); }
    public Wrapper(IInventory inv, IMarkDirty dirt) { this(inv, baseRights(inv.getSizeInventory()), dirt); }
    public Wrapper(int size, IMarkDirty dirt) { this(new Inventory(size), baseRights(size), dirt); }
    public Wrapper(int size) { this(new Inventory(size)); }
    public Wrapper(PacketBuffer buffer) { this(buffer.readInt(), null, buffer); }
    public Wrapper(IInventory inv, PacketBuffer buffer) { this(buffer.readInt(), inv, buffer); }

    public CompoundNBT write(CompoundNBT nbt) {
        nbt.putInt("Size", IO.length);
        ListNBT items = new ListNBT();
        for (int i = 0; i < IO.length; ++i)
            if (!inv.getStackInSlot(i).isEmpty()) {
                CompoundNBT compound = new CompoundNBT();
                compound.putInt("Slot", i);
                items.add(inv.getStackInSlot(i).write(compound));
            }
        nbt.put("Items", items);
        ListNBT rights = new ListNBT();
        for (int i = 0; i < IO.length; ++i)
            if (!IO[i].isVanilla()) {
                CompoundNBT compound = new CompoundNBT();
                compound.putInt("Slot", i);
                rights.add(IO[i].write(compound));
            }
        nbt.put("Rights", rights);
        return nbt;
    }

    public void read(CompoundNBT nbt) {
        int size = nbt.getInt("Size");
        if (size != inv.getSizeInventory()) {
            inv = new Inventory(size);
            IO = new IORights[size];
        }
        for (int i = 0; i < size; ++i) {
            inv.setInventorySlotContents(i, ItemStack.EMPTY);
            IO[i] = new IORights();
        }
        ListNBT items = nbt.getList("Items", 10);
        for (int i = 0; i < items.size(); ++i) {
            int index = items.getCompound(i).getInt("Slot");
            inv.setInventorySlotContents(index, ItemStack.read(items.getCompound(i)));
        }
        ListNBT rights = nbt.getList("Rights", 10);
        for (int i = 0; i < rights.size(); ++i) {
            int index = rights.getCompound(i).getInt("Slot");
            IO[index].read(rights.getCompound(i));
        }
    }

    private static final IORights[] baseRights(int size) {
        IORights out[] = new IORights[size];
        for (int i = 0; i < out.length; ++i)
            out[i] = new IORights();
        return out;
    }

    public void resizeInventory(int newSize, int newRows, int newColumns, int prevRows, int prevColumns) { //note: this will NOT really resize the wrapped/targeted inventory, but create a new one instead
        IInventory newInv = new Inventory(newSize);
        IORights[] newRights = new IORights[newSize];
        for (int y = 0; y < newRows; ++y)
            for (int x = 0; x < newColumns; ++x)
                if (x + y * newColumns < newSize) {
                    if (x < prevColumns && y < prevRows && x + y * prevColumns < inv.getSizeInventory()) {
                        newInv.setInventorySlotContents(x + y * newColumns, this.inv.getStackInSlot(x + y * prevColumns));
                        newRights[x + y * newColumns] = this.IO[x + y * prevColumns];
                    } else {
                        newInv.setInventorySlotContents(x + y * newColumns, ItemStack.EMPTY);
                        newRights[x + y * newColumns] = new IORights();
                    }
                }
        for (int i = newRows * newColumns; i < newSize; ++i) {
            newInv.setInventorySlotContents(i, ItemStack.EMPTY);
            newRights[i] = new IORights();
        }
        this.IO = newRights;
        this.inv = newInv;
        this.dirt.markDirty();
    }

    public PacketBuffer sizeAndRightsToBuffer(PacketBuffer buff) {
        buff.writeInt(IO.length);
        for (IORights r : IO)
            r.toBytes(buff);
        return buff;
    }

    public void setRights(int slot, IORights rights) {
        if (slot < 0 || slot >= IO.length) return;
        IO[slot] = rights;
        if (dirt != null)
            dirt.markDirty();
    }

    public void setRights(int slot, int field, int data) {
        if (slot < 0 || slot >= IO.length) return;
        IO[slot].setField(field, data);
    }

    public IORights getRights(int slot) {
        if (slot < 0 || slot >= IO.length) return new IORights();
        return IO[slot];
    }

    public IIntArray rightsAsIntArray() { //return a minecraft int array, used by containers, to sync rights between clients and server while the container is open
        return new IIntArray() {
            @Override
            public int get(int i) { return getRights(i / 3).toInt(i % 3); }
            @Override
            public void set(int i, int val) { setRights(i / 3, i % 3, val); }
            @Override
            public int size() { return IO.length * 3; }
        };
    }

    public boolean matchInventory(IInventory inv) { return this.inv == inv; }

    public IInventory getInventory() { return inv; }

    public void markDirty() {
        inv.markDirty();
        if (dirt != null)
            dirt.markDirty();
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {

        if (stack.isEmpty()) return ItemStack.EMPTY;
        if ((IO[slot].flags & CANINPUT) == 0 || !isItemValid(slot, stack)) return stack; //can't touch this slot with this filthy stack
        ItemStack stackInSlot = inv.getStackInSlot(slot);

        int stackLimit = (IO[slot].flags & OVERIDESTACKLIMIT) != 0 ? IO[slot].maxStack : Math.min(stack.getMaxStackSize(), getSlotLimit(slot));

        if (!stackInSlot.isEmpty())
        {
            if (inv instanceof PlayerInventory) return stack; //FIXME: for security issues, the merging is disabled with player inventory, might implement a workaround someday (one solution could be to dellay all acces of the inventory and at the end of the game tick resolve all access while checking mutually exclusive ones)
            if (stackInSlot.getCount() >= stackLimit) return stack; //limit already reached (note: since we merge stacks, there is no need to test the size of the stack already in the slot)
            if (!ItemHandlerHelper.canItemStacksStack(stack, stackInSlot)) return stack; //those stacks can't be merged
            stackLimit -= stackInSlot.getCount(); //how much can be inserted (tacking into acount the number of items already in the slot)
            if (stack.getCount() <= stackLimit) { //there is enough room to insert the entirety of the stack
                if (!simulate) {
                    ItemStack copy = stack.copy();
                    copy.grow(stackInSlot.getCount());
                    inv.setInventorySlotContents(slot, copy);
                    inv.markDirty();
                }
                return ItemStack.EMPTY;
            } else {//not enough room, we will return a truncated stack
                stack = stack.copy();
                if (!simulate)
                {
                    ItemStack copy = stack.split(stackLimit);
                    copy.grow(stackInSlot.getCount());
                    inv.setInventorySlotContents(slot, copy);
                    inv.markDirty();
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
                    inv.setInventorySlotContents(slot, stack.split(stackLimit));
                    inv.markDirty();
                    return stack;
                } else {
                    stack.shrink(stackLimit);
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

        if (amount == 0) return ItemStack.EMPTY; //nothing requested
        if ((IO[slot].flags & CANOUTPUT) == 0) return ItemStack.EMPTY; //can't touch this slot
        ItemStack stackInSlot = inv.getStackInSlot(slot);
        if (stackInSlot.isEmpty()) return ItemStack.EMPTY; //nothing to extract

        amount = Math.min(amount, Math.max(0, stackInSlot.getCount() - IO[slot].minStack));
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
            ItemStack decrStackSize = inv.decrStackSize(slot, amount);
            inv.markDirty();
            return decrStackSize;
        }
    }

    public static boolean mergeItemStack(List<Slot> inventorySlots, ItemStack stack, int startIndex, int endIndex, boolean reverseDirection, ArrayList<Integer> blackListSlot) {
        boolean flag = false; //tell if something was put, return false if the stack in input wasn't shrinked/consumed
        int i = reverseDirection ? endIndex - 1 : startIndex;
        if (stack.isStackable()) //first try to merge with already present stack
            while (!stack.isEmpty()) {
                if (reverseDirection ? (i < startIndex) : (i >= endIndex)) break;
                if (blackListSlot.contains(i)) {
                    i += reverseDirection ? -1 : 1;
                    continue;
                }
                Slot slot = inventorySlots.get(i);
                if (slot instanceof InvWrapperSlot) {
                    Wrapper wrap = (Wrapper)((InvWrapperSlot)slot).getItemHandler();
                    if ((wrap.getRights(slot.getSlotIndex()).flags & CANINPUT) == 0 || !slot.isItemValid(stack) || !wrap.isItemValid(slot.getSlotIndex(), stack)) { //skip this slot as it is not valid for input
                        i += reverseDirection ? -1 : 1;
                        continue;
                    }
                }
                ItemStack itemStack = slot.getStack();
                if (!itemStack.isEmpty() && itemStack.getItem() == stack.getItem() && IORights.nbtDamageTag(WITHOUTORE, itemStack, stack)) { //similar itemstacks (matching item, nbt and damage), will try to merge
                    int j = itemStack.getCount() + stack.getCount(); //new stack size after full merge (might need to be truncated if it exceed stack size)
                    int maxSize = Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize());
                    if (maxSize != itemStack.getCount()) {//slot is not already full
                        if (j <= maxSize) { //the slot can fit all the new merged stack
                            stack.setCount(0);
                            itemStack.setCount(j);
                            slot.onSlotChanged();
                            flag = true;
                        } else {
                            stack.shrink(maxSize - itemStack.getCount());
                            itemStack.setCount(maxSize);
                            slot.onSlotChanged();
                            flag = true;
                        }
                    }
                }
                i += reverseDirection ? -1 : 1;
            }
        if (!stack.isEmpty()) { //there is still something left to merge
            i = reverseDirection ? endIndex - 1 : startIndex;
            while (reverseDirection ? (i >= startIndex) : (i < endIndex)) {
                if (blackListSlot.contains(i)) {
                    i += reverseDirection ? -1 : 1;
                    continue;
                }
                Slot slot = inventorySlots.get(i);
                if (slot instanceof InvWrapperSlot) {
                    Wrapper wrap = (Wrapper)((InvWrapperSlot)slot).getItemHandler();
                    if ((wrap.getRights(slot.getSlotIndex()).flags & CANINPUT) == 0 || !slot.isItemValid(stack) || !wrap.isItemValid(slot.getSlotIndex(), stack)) { //skip this slot as it is not valid for input
                        i += reverseDirection ? -1 : 1;
                        continue;
                    }
                }
                ItemStack itemStack = slot.getStack();
                if (itemStack.isEmpty()) { //found empty valid slot, try to put the stack
                    if (stack.getCount() > slot.getSlotStackLimit())
                        slot.putStack(stack.split(slot.getSlotStackLimit()));
                    else
                        slot.putStack(stack.split(stack.getCount()));
                    slot.onSlotChanged();
                    flag = true;
                    break;
                }
                i += reverseDirection ? -1 : 1;
            }
        }
        return flag;
    }

    @Override
    public int getSlots() { return inv.getSizeInventory(); }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) { return inv.getStackInSlot(slot); }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        inv.setInventorySlotContents(slot, stack);
    }

    @Override
    public int getSlotLimit(int slot) {
        if (slot < 0 || slot >= IO.length) return 0;
        if ((IO[slot].flags & OVERIDESTACKLIMIT) != 0)
            return IO[slot].maxStack;
        return inv.getInventoryStackLimit();
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        if (slot < 0 || slot >= IO.length) return false;
        return inv.isItemValidForSlot(slot, stack) && IO[slot].isItemValid(stack);
    }
    */
}
