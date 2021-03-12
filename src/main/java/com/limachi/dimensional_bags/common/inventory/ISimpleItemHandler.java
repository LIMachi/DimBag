package com.limachi.dimensional_bags.common.inventory;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandlerModifiable;

public interface ISimpleItemHandler extends IItemHandlerModifiable, INBTSerializable<CompoundNBT>, IItemHandlerPacket, IItemHandlerSlotProvider {


}
