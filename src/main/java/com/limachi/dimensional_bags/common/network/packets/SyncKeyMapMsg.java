package com.limachi.dimensional_bags.common.network.packets;

import com.limachi.dimensional_bags.KeyMapController;
import com.limachi.dimensional_bags.common.network.PacketHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;

public class SyncKeyMapMsg extends PacketHandler.Message {
    boolean[] keys;

    public SyncKeyMapMsg(PacketBuffer buffer) {
        keys = new boolean[buffer.readByte()];
        for (int i = 0; i < keys.length; ++i)
            keys[i] = buffer.readBoolean();
    }

    public SyncKeyMapMsg(boolean[] keys) {
        this.keys = keys;
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeByte(keys.length);
        for (boolean key : keys) buffer.writeBoolean(key);
    }

    @Override
    public void serverWork(ServerPlayerEntity player) { KeyMapController.syncKeyMapMsg(player, keys); }
}
