package com.limachi.dimensional_bags.lib.common.network.packets;

import com.limachi.dimensional_bags.KeyMapController;
import com.limachi.dimensional_bags.lib.common.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;

public class KeyStateMsg extends PacketHandler.Message {
    int key;
    boolean state;

    public KeyStateMsg(PacketBuffer buffer) {
        key = buffer.readInt();
        state = buffer.readBoolean();
    }

    public KeyStateMsg(int key, boolean state) {
        this.key = key;
        this.state = state;
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(key);
        buffer.writeBoolean(state);
    }

    @Override
    public void serverWork(ServerPlayerEntity player) { KeyMapController.syncKeyMapMsg(player, key, state); }

    @Override
    public void clientWork() { KeyMapController.KeyBindings.values()[key].forceKeyState(Minecraft.getInstance().player, state); }
}
