package com.limachi.dimensional_bags.lib.common.network.packets;

/*
public class WidgetDataPacket extends PacketHandler.Message {
    private int windowId;
    private int widget;
    private CompoundNBT data;

    public WidgetDataPacket(PacketBuffer buffer) {
        this.windowId = buffer.readByte();
        this.widget = buffer.readShort();
        this.data = buffer.readAnySizeNbt();
    }

    public WidgetDataPacket(int windowIdIn, int widgetIn, CompoundNBT dataIn) {
        this.windowId = windowIdIn;
        this.widget = widgetIn;
        this.data = dataIn;
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeByte(this.windowId);
        buffer.writeShort(this.widget);
        buffer.writeNbt(this.data);
    }

    @Override
    public void clientWork() {
        ClientPlayerEntity player = (ClientPlayerEntity) DimBag.getPlayer();
        if (player != null && player.containerMenu.containerId == windowId && !(windowId > -3 && windowId < 1)) //containers -2, -1 and 0 are reserved for creative and inworld
            ((BaseContainer)player.containerMenu).receiveWidgetChange(player, widget, data);
    }

    @Override
    public void serverWork(ServerPlayerEntity player) {
        if (player != null && player.containerMenu.containerId == windowId && !(windowId > -3 && windowId < 1))
            ((BaseContainer)player.containerMenu).receiveWidgetChange(player, widget, data);
    }
}*/
