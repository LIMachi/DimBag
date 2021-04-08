package com.limachi.dimensional_bags.common.inventory;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.wrapper.EmptyHandler;

public class EmptySimpleItemHandlerSerializable extends EmptyHandler implements ISimpleItemHandlerSerializable {
    @Override
    public void readFromBuff(PacketBuffer buff) {}

    @Override
    public void writeToBuff(PacketBuffer buff) {}

    @Override
    public CompoundNBT serializeNBT() {return null;}

    @Override
    public void deserializeNBT(CompoundNBT nbt) {}
}
