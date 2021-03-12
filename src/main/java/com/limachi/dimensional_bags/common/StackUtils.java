package com.limachi.dimensional_bags.common;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

/**
 * mostly is used so the count in itemstack is communicated as such and not just truncated to a byte (simple hack to help compatibility with drawer style inventories)
 * those functions are compatible with forge but will result in a potential loss of data if read by forge and pollution of item tags for the packet W/R functions
 */

public class StackUtils {

    public static CompoundNBT writeAsCompound(ItemStack stack) {
        CompoundNBT out = stack.write(new CompoundNBT());
        if (stack.getCount() > 64) {
            out.putByte("Count", (byte)64); //make sure at the very least that if the item count is lost, we can save a full stack
            out.putInt("realCount", stack.getCount());
        }
        return out;
    }

    public static ItemStack readFromCompound(CompoundNBT nbt) {
        ItemStack out = ItemStack.read(nbt);
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
            buff.writeVarInt(Item.getIdFromItem(item));
            buff.writeByte(Math.min(stack.getCount(), 64)); //make sure at the very least that if the item count is lost, we can save a full stack
            CompoundNBT compoundnbt = null;
            if (item.isDamageable(stack) || item.shouldSyncTag()) {
                compoundnbt = limitedTag ? stack.getShareTag() : stack.getTag();
            }
            if (stack.getCount() > 64) {
                if (compoundnbt == null)
                    compoundnbt = new CompoundNBT();
                compoundnbt.putInt("realCount", stack.getCount()); //use the tags to communicate the real size, as it's the only part in the buffer that can have it's size changed with little incompatibility with forge
            }

            buff.writeCompoundTag(compoundnbt);
        }
        return buff;
    }

    public static ItemStack readFromPacket(PacketBuffer buff) {
        if (!buff.readBoolean()) {
            return ItemStack.EMPTY;
        } else {
            int i = buff.readVarInt();
            int j = buff.readByte();
            ItemStack itemstack = new ItemStack(Item.getItemById(i), j);
            itemstack.readShareTag(buff.readCompoundTag());
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
}
