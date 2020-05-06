package com.limachi.dimensional_bags.common.inventory;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.items.IItemHandlerModifiable;

public interface IBaseInventory extends IItemHandlerModifiable {
    ITextComponent getDisplayName();
    int getRows();
    int getColumns();
    int getSize();
    int getSizeSignature();
    void resizeInventory(int size, int rows, int columns);
    PacketBuffer toBytes(PacketBuffer buff);
    CompoundNBT write(CompoundNBT nbt);
    void read(CompoundNBT nbt);
    BaseItemStackAccessor getSlotAccessor(int slot);
}
