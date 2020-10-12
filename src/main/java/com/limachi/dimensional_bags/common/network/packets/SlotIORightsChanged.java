package com.limachi.dimensional_bags.common.network.packets;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.container.WrappedPlayerInventoryContainer;
import com.limachi.dimensional_bags.common.inventory.Wrapper;
import com.limachi.dimensional_bags.common.network.PacketHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;

public class SlotIORightsChanged extends PacketHandler.Message {

    int index;
    Wrapper.IORights rights;

    public SlotIORightsChanged(PacketBuffer buffer) {
        index = buffer.readInt();
        rights = new Wrapper.IORights(buffer);
    }

    public SlotIORightsChanged(int index, Wrapper.IORights rights) {
        this.index = index;
        this.rights = rights;
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(index);
        rights.toBytes(buffer);
    }

    @Override
    public void clientWork() {
        PlayerEntity player = DimBag.getPlayer();
        if (player.openContainer instanceof WrappedPlayerInventoryContainer) {
            WrappedPlayerInventoryContainer playerInterface = (WrappedPlayerInventoryContainer)player.openContainer;
            playerInterface.changeRights(index, rights);
        }
    }
}
