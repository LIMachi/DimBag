package com.limachi.dimensional_bags.common.network.packets;

import com.limachi.dimensional_bags.common.data.EyeDataMK2.WorldSavedDataManager;
import com.limachi.dimensional_bags.common.network.PacketHandler;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class WorldSavedDataSyncMsg extends PacketHandler.Message {

    String suffix;
    int id;
    CompoundNBT nbt;

    public WorldSavedDataSyncMsg(PacketBuffer buffer) {
        suffix = buffer.readString();
        id = buffer.readInt();
        nbt = buffer.readCompoundTag();
    }

    public WorldSavedDataSyncMsg(String suffix, int id, CompoundNBT nbt) {
        this.suffix = suffix;
        this.id = id;
        this.nbt = nbt;
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeString(suffix);
        buffer.writeInt(id);
        buffer.writeCompoundTag(nbt);
    }

    @Override
    public void clientWork() {
        WorldSavedDataManager.clientUpdate(suffix, id, nbt);
    }
}
