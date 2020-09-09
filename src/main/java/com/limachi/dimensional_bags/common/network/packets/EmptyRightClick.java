package com.limachi.dimensional_bags.common.network.packets;

import com.limachi.dimensional_bags.KeyMapController;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.items.IDimBagCommonItem;
import com.limachi.dimensional_bags.common.network.PacketHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class EmptyRightClick {

    public void toBytes(PacketBuffer buffer) {}

    public static EmptyRightClick fromBytes(PacketBuffer buffer) {
        return new EmptyRightClick();
    }

    public static void enqueue(EmptyRightClick pack, Supplier<NetworkEvent.Context> ctxs) {
        NetworkEvent.Context ctx = ctxs.get();
        PacketHandler.Target t = PacketHandler.target(ctx);
        if (t == PacketHandler.Target.SERVER)
            ctx.enqueueWork(() -> {
                    //MinecraftForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickItem(ctx.getSender(), Hand.MAIN_HAND))
                if (KeyMapController.getKey(ctx.getSender(), KeyMapController.BAG_ACTION_KEY)) {
                    IDimBagCommonItem.ItemSearchResult src = IDimBagCommonItem.searchItem(ctx.getSender(), 0, Bag.class, (x)->true);
                    if (src != null) ((Bag)src.stack.getItem()).onItemRightClick(ctx.getSender().world, ctx.getSender(), src.index);
                }});
        ctx.setPacketHandled(true);
    }
}
