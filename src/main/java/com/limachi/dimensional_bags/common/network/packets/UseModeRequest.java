package com.limachi.dimensional_bags.common.network.packets;

import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.network.PacketHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class UseModeRequest {

    UUID id;
    int bagPos;

    private UseModeRequest(UUID player, int bagPos) {
        this.id = player;
        this.bagPos = bagPos;
    }

    public UseModeRequest(PlayerEntity player, int bagPos) {
        this.id = player.getUniqueID();
        this.bagPos = bagPos;
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeUniqueId(id);
        buffer.writeInt(bagPos);
    }

    public static UseModeRequest fromBytes(PacketBuffer buffer) {
        UUID id = buffer.readUniqueId();
        int bagPos = buffer.readInt();
        return new UseModeRequest(id, bagPos);
    }

    public static void enqueue(UseModeRequest pack, Supplier<NetworkEvent.Context> ctxs) {
        NetworkEvent.Context ctx = ctxs.get();
        PacketHandler.Target t = PacketHandler.target(ctx);
        if (t == PacketHandler.Target.SERVER)
            ctx.enqueueWork(() -> {
                ItemStack stack = ctx.getSender().inventory.getStackInSlot(pack.bagPos);
                EyeData data = EyeData.get(null, Bag.getId(stack));
                if (data == null) return;
                data.modeManager().onActivateItem(stack, ctx.getSender());
            });
        ctx.setPacketHandled(true);
    }
}
