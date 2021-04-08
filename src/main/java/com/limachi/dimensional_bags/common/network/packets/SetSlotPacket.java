package com.limachi.dimensional_bags.common.network.packets;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.utils.StackUtils;
import com.limachi.dimensional_bags.common.network.PacketHandler;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public class SetSlotPacket extends PacketHandler.Message {
    private int windowId;
    private int slot;
    private ItemStack item = ItemStack.EMPTY;

    public SetSlotPacket(PacketBuffer buffer) {
        this.windowId = buffer.readByte();
        this.slot = buffer.readShort();
//        this.item = buffer.readItemStack();
        this.item = StackUtils.readFromPacket(buffer);
    }

    public SetSlotPacket(int windowIdIn, int slotIn, ItemStack itemIn) {
        this.windowId = windowIdIn;
        this.slot = slotIn;
        this.item = itemIn.copy();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeByte(this.windowId);
        buffer.writeShort(this.slot);
//        buffer.writeItemStack(this.item);
        StackUtils.writeAsPacket(buffer, this.item);
    }

    @Override
    public void clientWork() {
        ClientPlayerEntity player = (ClientPlayerEntity) DimBag.getPlayer();
        if (player != null && player.openContainer.windowId == windowId && !(windowId > -3 && windowId < 1)) //containers -2, -1 and 0 are reserved for creative and inworld
            player.openContainer.putStackInSlot(slot, item);
    }
}
