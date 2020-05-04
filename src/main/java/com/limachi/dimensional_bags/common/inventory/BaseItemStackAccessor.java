package com.limachi.dimensional_bags.common.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class BaseItemStackAccessor {
    public ItemStack stack;
    public boolean canInput = true;
    public boolean canOutput = true;
    public byte maxStackSize = 64;
    public byte minStackSize = 0;

    public BaseItemStackAccessor() {
        this.stack = ItemStack.EMPTY.copy();
    }

    public BaseItemStackAccessor(ItemStack stack, boolean canInput, boolean canOutput, int minStackSize, int maxStackSize) {
        this.stack = stack;
        this.canInput = canInput;
        this.canOutput = canOutput;
        this.minStackSize = (byte)minStackSize;
        this.maxStackSize = (byte)maxStackSize;
    }

    public boolean shouldSync() {
        return !stack.isEmpty() || !canInput || !canOutput || minStackSize != 0 || maxStackSize != 64;
    }

    public boolean canAccept(ItemStack stackIn) {
        if (stack.isEmpty())
            return true;
        if (stackIn.isItemEqual(stack)) {
            CompoundNBT nbt1 = stackIn.hasTag() ? stackIn.getTag() : new CompoundNBT();
            CompoundNBT nbt2 = stack.hasTag() ? stack.getTag() : new CompoundNBT();
            if (nbt1.equals(nbt2) && stackIn.areCapsCompatible(stack))
                return true;
        }
        return false;
    }

    public CompoundNBT write(CompoundNBT nbt) {
        stack.write(nbt);
        nbt.putBoolean("CanInput", canInput);
        nbt.putBoolean("CanOutput", canOutput);
        nbt.putByte("MaxStackSize", maxStackSize);
        nbt.putByte("MinStackSize", minStackSize);
        return nbt;
    }

    public void read(CompoundNBT nbt) {
        stack = ItemStack.read(nbt);
        canInput = nbt.getBoolean("CanInput");
        canOutput = nbt.getBoolean("CanOutput");
        maxStackSize = nbt.getByte("MaxStackSize");
        minStackSize = nbt.getByte("MinStackSize");
    }

    public PacketBuffer toBytes(PacketBuffer buff) {
        buff.writeItemStack(stack);
        buff.writeBoolean(canInput);
        buff.writeBoolean(canOutput);
        buff.writeByte(maxStackSize);
        buff.writeByte(minStackSize);
        return buff;
    }

    public BaseItemStackAccessor(PacketBuffer buff) {
        stack = buff.readItemStack();
        canInput = buff.readBoolean();
        canOutput = buff.readBoolean();
        maxStackSize = buff.readByte();
        minStackSize = buff.readByte();
    }
}
