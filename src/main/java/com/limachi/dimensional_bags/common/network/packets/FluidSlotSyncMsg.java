package com.limachi.dimensional_bags.common.network.packets;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.container.BaseContainer;
import com.limachi.dimensional_bags.common.network.PacketHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;

/*
public class FluidSlotSyncMsg extends PacketHandler.Message {

    int windowId;
    int slot;
    FluidStack data;

    public FluidSlotSyncMsg(PacketBuffer buffer) {
        windowId = buffer.readInt();
        slot = buffer.readInt();
        data = buffer.readFluidStack();
    }

    public FluidSlotSyncMsg(int windowId, int slot, FluidStack data) {
        this.windowId = windowId;
        this.slot = slot;
        this.data = data;
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(windowId);
        buffer.writeInt(slot);
        buffer.writeFluidStack(data);
    }

    @Override
    public void clientWork() {
        PlayerEntity player = DimBag.getPlayer();
        if (player.openContainer instanceof BaseContainer && player.openContainer.windowId == windowId)
            ((BaseContainer)player.openContainer).loadFluidSlotChange(slot, data);
    }
}
*/