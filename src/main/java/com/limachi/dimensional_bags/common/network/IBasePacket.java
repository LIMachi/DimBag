package com.limachi.dimensional_bags.common.network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public interface IBasePacket {

    void toBytes(PacketBuffer buff);
}
