package com.limachi.utils;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;

@SuppressWarnings("unused")
public class Network {
    public static abstract class Message {
        public Message() {}
        public Message(FriendlyByteBuf buffer) {}
        public abstract void toBytes(FriendlyByteBuf buffer);
        public void clientWork(Player player) {}
        public void serverWork(Player player) {}
    }

    @Retention(RetentionPolicy.RUNTIME)
    @java.lang.annotation.Target(ElementType.TYPE)
    public @interface RegisterMessage {
        int value();
        String modId() default "";
    }

    private static void discoverMsgRegistry(String modId) {
        for (ModAnnotation a : ModAnnotation.iterModAnnotations(modId, RegisterMessage.class))
            registerMsg(a.getData("modId", modId), (Class<Message>)a.getAnnotatedClass(), a.getData("value", -1));
    }

    public static void register(String modId) {
        discoverMsgRegistry(modId);
    }

    protected static final HashMap<String, SimpleChannel> HANDLERS = new HashMap<>();

    protected static SimpleChannel getChannel(String modId) {
        if (!HANDLERS.containsKey(modId))
            HANDLERS.put(modId, NetworkRegistry.newSimpleChannel(new ResourceLocation(modId, "network"), ()->"1", "1"::equals, "1"::equals));
        return HANDLERS.get(modId);
    }

    protected static <T extends Message> void registerMsg(String modId, Class<T> clazz, int id) {
        try {
            getChannel(modId).registerMessage(
                    id,
                    clazz,
                    (msg, buffer) -> {
                        clazz.cast(msg).toBytes(buffer);
                    },
                    buffer->{
                        try {
                            return clazz.getConstructor(FriendlyByteBuf.class).newInstance(buffer);
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
                        Network.Target t = Network.target(ctx);
                        if (t == Network.Target.CLIENT)
                            ctx.enqueueWork(()->(msg).clientWork(Sides.getPlayer()));
                        if (t == Network.Target.SERVER)
                            ctx.enqueueWork(()->(msg).serverWork(ctx.getSender()));
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

    public static <T extends Message> void toServer(String modId, T msg) { if (msg != null) getChannel(modId).sendToServer(msg); }
    public static <T extends Message> void toClients(String modId, T msg) {
        if (msg != null) {
            SimpleChannel channel = getChannel(modId);
            for (ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers())
                channel.sendTo(msg, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        }
    }
    public static <T extends Message> void toClient(String modId, ServerPlayer player, T msg) { if (msg != null && !(player instanceof FakePlayer)) getChannel(modId).sendTo(msg, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT); }
}
