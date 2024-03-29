package com.limachi.dimensional_bags.common.network.packets;

import com.limachi.dimensional_bags.common.managers.ModeManager;
import com.limachi.dimensional_bags.common.network.PacketHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;

public class ChangeModeRequest extends PacketHandler.Message {
    int slot;
    boolean up;

    public ChangeModeRequest(PacketBuffer buffer) {
        slot = buffer.readInt();
        up = buffer.readBoolean();
    }

    public ChangeModeRequest(int slot, boolean up) {
        this.slot = slot;
        this.up = up;
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(slot);
        buffer.writeBoolean(up);
    }

    @Override
    public void serverWork(ServerPlayerEntity player) { ModeManager.changeModeRequest(player, slot, up); }
}
