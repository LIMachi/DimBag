package com.limachi.dimensional_bags.common.network.packets;

import com.limachi.dimensional_bags.client.render.screen.SettingsScreen;
import com.limachi.dimensional_bags.common.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;

/*
public class OpenSettingsMsg extends PacketHandler.Message {

    int id;

    public OpenSettingsMsg(PacketBuffer buffer) { id = buffer.readInt(); }

    public OpenSettingsMsg(int id) { this.id = id; }

    public void toBytes(PacketBuffer buffer) { buffer.writeInt(id); }

    @Override
    public void clientWork() { SettingsScreen.open(Minecraft.getInstance().player, id); }
}
*/