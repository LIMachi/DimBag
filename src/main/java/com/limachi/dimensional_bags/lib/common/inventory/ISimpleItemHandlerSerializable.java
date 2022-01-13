package com.limachi.dimensional_bags.lib.common.inventory;

import net.minecraft.inventory.container.Slot;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;

public interface ISimpleItemHandlerSerializable extends IItemHandlerModifiable, INBTSerializable<CompoundNBT>, IPacketSerializable, ISlotProvider {
    default Slot createSlot(int index, int x, int y) {
        return new SlotItemHandler(this, index, x, y);
    }
}
