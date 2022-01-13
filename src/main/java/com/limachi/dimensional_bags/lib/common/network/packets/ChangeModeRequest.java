package com.limachi.dimensional_bags.lib.common.network.packets;

import com.limachi.dimensional_bags.common.bag.modes.ModeManager;
import com.limachi.dimensional_bags.lib.common.network.PacketHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;

public class ChangeModeRequest extends PacketHandler.Message {
    int eye;
    boolean up;

    public ChangeModeRequest(PacketBuffer buffer) {
        eye = buffer.readInt();
        up = buffer.readBoolean();
    }

    public ChangeModeRequest(int slot, boolean up) {
        this.eye = slot;
        this.up = up;
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(eye);
        buffer.writeBoolean(up);
    }

    @Override
    public void serverWork(ServerPlayerEntity player) { ModeManager.changeModeRequest(player, eye, up, false); }
}
