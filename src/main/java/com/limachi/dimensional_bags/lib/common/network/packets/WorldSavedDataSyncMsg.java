package com.limachi.dimensional_bags.lib.common.network.packets;

import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.WorldSavedDataManager;
import com.limachi.dimensional_bags.lib.common.network.PacketHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class WorldSavedDataSyncMsg extends PacketHandler.Message {

    String suffix;
    int id;
    public CompoundNBT nbt;
    public boolean isDiff;

    public WorldSavedDataSyncMsg(PacketBuffer buffer) {
        suffix = buffer.readUtf();
        id = buffer.readInt();
        nbt = buffer.readAnySizeNbt();
        isDiff = buffer.readBoolean();
    }

    public WorldSavedDataSyncMsg(String suffix, int id, CompoundNBT nbt, boolean isDiff) {
        this.suffix = suffix;
        this.id = id;
        this.nbt = nbt;
        this.isDiff = isDiff;
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeUtf(suffix);
        buffer.writeInt(id);
        buffer.writeNbt(nbt);
        buffer.writeBoolean(isDiff);
    }

    @Override
    public void clientWork() { WorldSavedDataManager.clientUpdate(suffix, id, nbt, isDiff); }

    @Override
    public void serverWork(ServerPlayerEntity player) { WorldSavedDataManager.serverUpdate(player, suffix, id, nbt, isDiff); }
}
