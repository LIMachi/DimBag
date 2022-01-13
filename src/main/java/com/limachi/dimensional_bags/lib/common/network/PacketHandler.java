package com.limachi.dimensional_bags.lib.common.network;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.lib.common.network.packets.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

import com.limachi.dimensional_bags.StaticInit;

@StaticInit
public class PacketHandler {
    public static abstract class Message {
        public Message() {}
        public Message(PacketBuffer buffer) {}
        public abstract void toBytes(PacketBuffer buffer);
        public void clientWork() {}
        public void serverWork(ServerPlayerEntity player) {}
    }

    protected static <T extends Message> void registerMsg(Class<T> clazz) {
        try {
            HANDLER.registerMessage(
                    index++,
                    clazz,
                    (msg, buffer) -> {
                        clazz.cast(msg).toBytes(buffer);
                    },
                    buffer->{
                        try {
                            return clazz.getConstructor(PacketBuffer.class).newInstance(buffer);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                    , (msg, scntx) -> {
                        if (!clazz.isInstance(msg)) {
                            //FIXME: add some kind of error there
                            return;
                        }
                        NetworkEvent.Context ctx = scntx.get();
                        PacketHandler.Target t = PacketHandler.target(ctx);
                        if (t == PacketHandler.Target.CLIENT)
                            ctx.enqueueWork(((Message)msg)::clientWork);
                        if (t == PacketHandler.Target.SERVER)
                            ctx.enqueueWork(()->((Message)msg).serverWork(ctx.getSender()));
                        ctx.setPacketHandled(true);
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    private static int index;

    static {
        index = 0;
        registerMsg(WorldSavedDataSyncMsg.class);
//        registerMsg(SlotIORightsChanged.class);
        registerMsg(ChangeModeRequest.class);
        registerMsg(KeyStateMsg.class);
//        registerMsg(TrackedStringSyncMsg.class);
//        registerMsg(FluidSlotSyncMsg.class);
        registerMsg(SetSlotPacket.class);

//        registerMsg(OpenSettingsMsg.class);

        registerMsg(SyncCompoundNBT.SCNBTC.class);
        registerMsg(SyncCompoundNBT.SCNBTD.class);
        registerMsg(SyncCompoundNBT.SCNBTU.class);

//        registerMsg(WidgetDataPacket.class);

        registerMsg(UpstreamTileUpdateMsg.class);

        registerMsg(PlayerPersistentDataAction.class);
    }

    public static <T extends Message> void toServer(T msg) { if (msg != null) HANDLER.sendToServer(msg); }
    public static <T extends Message> void toClients(T msg) {
        if (msg != null)
            for (ServerPlayerEntity player : DimBag.getServer().getPlayerList().getPlayers())
                toClient(player, msg);
    }
    public static <T extends Message> void toClient(ServerPlayerEntity player, T msg) { if (msg != null && !(player instanceof FakePlayer)) HANDLER.sendTo(msg, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT); }
}
