package com.limachi.dimensional_bags.common.inventory;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.IItemHandlerModifiable;

public interface IItemHandlerPacket extends IItemHandlerModifiable {
    void readFromBuff(PacketBuffer buff);
    void writeToBuff(PacketBuffer buff);
}
