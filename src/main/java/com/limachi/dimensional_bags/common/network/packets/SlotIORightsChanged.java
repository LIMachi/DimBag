package com.limachi.dimensional_bags.common.network.packets;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.container.WrappedPlayerInventoryContainer;
import com.limachi.dimensional_bags.common.inventory.Wrapper;
import com.limachi.dimensional_bags.common.network.PacketHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SlotIORightsChanged {

    int index;
    Wrapper.IORights rights;

    public SlotIORightsChanged(int index, Wrapper.IORights rights) {
        this.index = index;
        this.rights = rights;
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(index);
        rights.toBytes(buffer);
    }

    public static SlotIORightsChanged fromBytes(PacketBuffer buffer) {
        int index = buffer.readInt();
        Wrapper.IORights rights = new Wrapper.IORights(buffer);
        return new SlotIORightsChanged(index, rights);
    }

    public static void enqueue(SlotIORightsChanged pack, Supplier<NetworkEvent.Context> ctxs) {
        NetworkEvent.Context ctx = ctxs.get();
        PacketHandler.Target t = PacketHandler.target(ctx);
        if (t == PacketHandler.Target.CLIENT)
            ctx.enqueueWork(() -> {
                PlayerEntity player = DimBag.getPlayer();
                if (player.openContainer instanceof WrappedPlayerInventoryContainer) {
                    WrappedPlayerInventoryContainer playerInterface = (WrappedPlayerInventoryContainer)player.openContainer;
                    playerInterface.changeRights(pack.index, pack.rights);
                }
            });
        ctx.setPacketHandled(true);
    }
}
