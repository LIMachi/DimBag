package com.limachi.dimensional_bags.lib.common.inventory;

import net.minecraft.network.PacketBuffer;

public interface IPacketSerializable {
    void readFromBuff(PacketBuffer buff);
    void writeToBuff(PacketBuffer buff);
}
