package com.limachi.dimensional_bags.common.network.packets;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.utils.StackUtils;
import com.limachi.dimensional_bags.common.network.PacketHandler;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public class SetSlotPacket extends PacketHandler.Message {
    private int containerId;
    private int slot;
    private ItemStack item = ItemStack.EMPTY;

    public SetSlotPacket(PacketBuffer buffer) {
        this.containerId = buffer.readByte();
        this.slot = buffer.readShort();
//        this.item = buffer.readItem();
        this.item = StackUtils.readFromPacket(buffer);
    }

    public SetSlotPacket(int containerIdIn, int slotIn, ItemStack itemIn) {
        this.containerId = containerIdIn;
        this.slot = slotIn;
        this.item = itemIn.copy();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeByte(this.containerId);
        buffer.writeShort(this.slot);
//        buffer.writeItemStack(this.item);
        StackUtils.writeAsPacket(buffer, this.item);
    }

    @Override
    public void clientWork() {
        ClientPlayerEntity player = (ClientPlayerEntity) DimBag.getPlayer();
        if (player != null && player.containerMenu.containerId == containerId && !(containerId > -3 && containerId < 1)) //containers -2, -1 and 0 are reserved for creative and inworld
            player.containerMenu.setItem(slot, item);
    }
}
