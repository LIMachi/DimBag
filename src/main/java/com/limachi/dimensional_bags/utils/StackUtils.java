package com.limachi.dimensional_bags.utils;

import com.sun.javafx.util.Utils;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

/**
 * mostly is used so the count in itemstack is communicated as such and not just truncated to a byte (simple hack to help compatibility with drawer style inventories)
 * those functions are compatible with forge but will result in a potential loss of data if read by forge and pollution of item tags for the packet W/R functions
 */

public class StackUtils {

    public static CompoundNBT writeAsCompound(ItemStack stack) {
        CompoundNBT out = stack.save(new CompoundNBT());
        if (stack.getCount() > stack.getMaxStackSize()) {
            out.putByte("Count", (byte)stack.getMaxStackSize()); //make sure at the very least that if the item count is lost, we can save a full stack
            out.putInt("realCount", stack.getCount());
        }
        return out;
    }

    public static ItemStack readFromCompound(CompoundNBT nbt) {
        ItemStack out = ItemStack.of(nbt);
        if (nbt.contains("realCount"))
            out.setCount(nbt.getInt("realCount"));
        return out;
    }

    public static PacketBuffer writeAsPacket(PacketBuffer buff, ItemStack stack) { return writeAsPacket(buff, stack, true); }

    public static PacketBuffer writeAsPacket(PacketBuffer buff, ItemStack stack, boolean limitedTag) {
        if (stack.isEmpty()) {
            buff.writeBoolean(false);
        } else {
            buff.writeBoolean(true);
            Item item = stack.getItem();
            buff.writeVarInt(Item.getId(item));
            buff.writeByte(Math.min(stack.getCount(), stack.getMaxStackSize())); //make sure at the very least that if the item count is lost, we can save a full stack
            CompoundNBT compoundnbt = null;
            if (item.isDamageable(stack) || item.shouldOverrideMultiplayerNbt()) {
                compoundnbt = limitedTag ? stack.getShareTag() : stack.getTag();
            }
            if (stack.getCount() > stack.getMaxStackSize()) {
                if (compoundnbt == null)
                    compoundnbt = new CompoundNBT();
                compoundnbt.putInt("realCount", stack.getCount()); //use the tags to communicate the real size, as it's the only part in the buffer that can have it's size changed with little incompatibility with forge
            }

            buff.writeNbt(compoundnbt);
        }
        return buff;
    }

    public static ItemStack readFromPacket(PacketBuffer buff) {
        if (!buff.readBoolean()) {
            return ItemStack.EMPTY;
        } else {
            int i = buff.readVarInt();
            int j = buff.readByte();
            ItemStack itemstack = new ItemStack(Item.byId(i), j);
            itemstack.readShareTag(buff.readAnySizeNbt());
            if (itemstack.getTag() != null) {
                CompoundNBT t = itemstack.getTag();
                if (t.contains("realCount")) {
                    itemstack.setCount(t.getInt("realCount"));
                    t.remove("realCount"); //make sure to clean the nbts
                    if (t.isEmpty())
                        itemstack.setTag(null); //and if needed, remove the nbt entirely
                }
            }
            return itemstack;
        }
    }

    public static ItemStack setInfinite(ItemStack stack) {
        stack.setCount(Integer.MAX_VALUE);
        return stack;
    }

    public static ItemStack setCount(ItemStack stack, int val, boolean vanilla) {
        if (stack.isEmpty())
            return stack;
        if (val <= 0)
            return ItemStack.EMPTY;
        stack.setCount(Integer.min(val, vanilla ? stack.getMaxStackSize() : Integer.MAX_VALUE - 1));
        return stack;
    }

    public static ItemStack grow(ItemStack stack, int val, boolean vanilla) {
        int count = stack.getCount();
        if (count != Integer.MAX_VALUE) {
            int m = vanilla ? stack.getMaxStackSize() : Integer.MAX_VALUE - 1;
            stack.setCount(val > 0 && count > 0 && val + count <= count ? m : Utils.clamp(0, val + count, m));
        }
        return stack;
    }

    public static ItemStack shrink(ItemStack stack, int val, boolean vanilla) { return grow(stack, -val, vanilla); }

    public static ItemStack merge(ItemStack s1, ItemStack s2, boolean vanilla) {
        if (s1.isEmpty() || s2.getCount() == Integer.MAX_VALUE)
            return s2.copy();
        if (s2.isEmpty() || s1.getCount() == Integer.MAX_VALUE)
            return s1.copy();
        ItemStack out = grow(s1.copy(), s2.getCount(), vanilla);
        CompoundNBT tag = s2.getTag();
        if (tag != null)
            out.getOrCreateTag().merge(tag);
        return out;
    }

    public static boolean areStackable(ItemStack s1, ItemStack s2) {
        return ItemStack.isSame(s1, s2) && ItemStack.tagMatches(s1, s2);
    }

    /**
     * standard way of creating a setStackInSlot compatible with the get, extract and insert methods
     * note2: growing and shrinking stacks inside inventories using pointer (getStackInSlot) should be banned (invalid behavior, particularly vanilla in shift-click)
     */
    public static void setStackInSlot(IItemHandler handler, int slot, ItemStack stack) {
        ItemStack p = handler.getStackInSlot(slot).copy();
        if (!p.isEmpty()) {
            ItemStack t = handler.extractItem(slot, p.getCount(), true);
            if (!t.equals(p, false))
                return;
            handler.extractItem(slot, p.getCount(), false);
        }
        if (!handler.insertItem(slot, stack, true).isEmpty()) {
            handler.insertItem(slot, p, false);
            return;
        }
        handler.insertItem(slot, stack, false);
    }

    public static void setSlot(Slot slot, ItemStack stack) {
        if (slot instanceof SlotItemHandler)
            setStackInSlot(((SlotItemHandler)slot).getItemHandler(), slot.getSlotIndex(), stack);
        else
            slot.set(stack);
    }

//    public static class test extends ItemStack {
//        public test(IItemProvider item) { super(item); }
//        public test(IItemProvider item, int quantity) { super(item, quantity); }
//        public test(IItemProvider item, int quantity, @Nullable CompoundNBT nbt) { super(item, quantity, nbt); }
//
//        @Override
//        public void grow(int p_190917_1_) {
//            super.grow(p_190917_1_);
//        }
//    }

    public static class EItemStack {

        private ItemStack stack;
        private boolean vanilla;

        public EItemStack() { this(ItemStack.EMPTY.copy(), true); }

        public EItemStack(ItemStack stack, boolean vanilla) {
            this.stack = stack;
            this.vanilla = vanilla;
        }

        public ItemStack stack() { return stack; }
        public boolean vanilla() { return vanilla; }

        CompoundNBT asCompound() { return writeAsCompound(stack).merge(NBTUtils.toCompoundNBT("Vanilla", vanilla)); }
        public static EItemStack fromCompound(CompoundNBT nbt) { return new EItemStack(readFromCompound(nbt), nbt.getBoolean("Vanilla")); }
        public EItemStack readCompound(CompoundNBT nbt) {
            this.stack = readFromCompound(nbt);
            this.vanilla = nbt.getBoolean("Vanilla");
            return this;
        }

        PacketBuffer asPacket(PacketBuffer buff) {
            writeAsPacket(buff, this.stack);
            buff.writeBoolean(vanilla);
            return buff;
        }
        public static EItemStack fromPacket(PacketBuffer buff) { return new EItemStack(readFromPacket(buff), buff.readBoolean()); }
        public EItemStack readPacket(PacketBuffer buff) {
            this.stack = readFromPacket(buff);
            this.vanilla = buff.readBoolean();
            return this;
        }

        public boolean infinite() { return stack.getCount() == Integer.MAX_VALUE; }
        public EItemStack setInfinite(boolean state) {
            if (state)
                stack.setCount(Integer.MAX_VALUE);
            else if (stack.getCount() == Integer.MAX_VALUE)
                stack.setCount(vanilla ? stack.getMaxStackSize() : Integer.MAX_VALUE - 1);
            return this;
        }
    }
}
