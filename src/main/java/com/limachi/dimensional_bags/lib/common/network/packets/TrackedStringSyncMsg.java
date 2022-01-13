package com.limachi.dimensional_bags.lib.common.network.packets;

/*
public class TrackedStringSyncMsg extends PacketHandler.Message {

    int windowId;
    int stringId;
    String data;

    public TrackedStringSyncMsg(PacketBuffer buffer) {
        windowId = buffer.readInt();
        stringId = buffer.readInt();
        data = buffer.readString();
    }

    public TrackedStringSyncMsg(int windowId, int stringId, String data) {
        this.windowId = windowId;
        this.stringId = stringId;
        this.data = data;
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(windowId);
        buffer.writeInt(stringId);
        buffer.writeString(data);
    }

    @Override
    public void clientWork() {
        PlayerEntity player = DimBag.getPlayer();
        if (player.openContainer instanceof BaseContainer && player.openContainer.windowId == windowId)
            ((BaseContainer)player.openContainer).loadStringChange(stringId, data);
    }

    @Override
    public void serverWork(ServerPlayerEntity player) {
        if (player == null) return;
        if (player.openContainer instanceof BaseContainer && player.openContainer.windowId == windowId)
            ((BaseContainer)player.openContainer).loadStringChange(stringId, data);
    }
}
*/