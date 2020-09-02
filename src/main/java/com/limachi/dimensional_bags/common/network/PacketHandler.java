package com.limachi.dimensional_bags.common.network;

import com.limachi.dimensional_bags.common.network.packets.ChangeModeRequest;
import com.limachi.dimensional_bags.common.network.packets.OpenGuiRequest;
import com.limachi.dimensional_bags.common.network.packets.SlotIORightsChanged;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

public class PacketHandler {
    public static enum Target {
        CLIENT,
        SERVER,
        I_M_NOT_SURE
    }

    public static Target target(NetworkEvent.Context ctx) {
        if (ctx.getDirection().getOriginationSide() == LogicalSide.SERVER)
            return ctx.getDirection().getReceptionSide() == LogicalSide.CLIENT ? Target.CLIENT : Target.I_M_NOT_SURE;
        if (ctx.getDirection().getOriginationSide() == LogicalSide.CLIENT)
            return ctx.getDirection().getReceptionSide() == LogicalSide.SERVER ? Target.SERVER : Target.I_M_NOT_SURE;
        return Target.I_M_NOT_SURE;
    }

    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel HANDLER = NetworkRegistry.newSimpleChannel(new ResourceLocation(MOD_ID, "network"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    private static int index = 0;

    static {
        HANDLER.registerMessage(index++, SlotIORightsChanged.class, SlotIORightsChanged::toBytes, SlotIORightsChanged::fromBytes, SlotIORightsChanged::enqueue);
        HANDLER.registerMessage(index++, ChangeModeRequest.class, ChangeModeRequest::toBytes, ChangeModeRequest::fromBytes, ChangeModeRequest::enqueue);
        HANDLER.registerMessage(index++, OpenGuiRequest.class, OpenGuiRequest::toBytes, OpenGuiRequest::fromBytes, OpenGuiRequest::enqueue);
    }

    public static <T> void toServer(T msg) { HANDLER.sendToServer(msg); }
    public static <T> void toClients(T msg) { HANDLER.send(PacketDistributor.ALL.noArg(), msg); }
    public static <T> void toClient(T msg, ServerPlayerEntity player) { HANDLER.sendTo(msg, player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT); }
}
