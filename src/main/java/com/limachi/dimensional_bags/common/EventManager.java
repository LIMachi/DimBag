package com.limachi.dimensional_bags.common;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.data.DimBagData;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.items.Bag;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class EventManager {
    public static int tick = 0;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END || event.side.isClient()) return;
        if ((tick = ((tick + 1) & 7)) == 0) { //determine how often i run the logic, for now, once every 8 ticks (every 0.4s)
            MinecraftServer server = DimBag.getServer(null);
            int l = DimBagData.get(server).getLastId() + 1;
            for (int i = 1; i < l; ++i) {
                EyeData data = EyeData.get(server, i);
                Entity user = data.getUser();
                //here do updates on bags that need the user, like testing if the user is in water
                if (user != null) {
                    if (user.isInWater())
                        DimBag.LOGGER.info("that bag is swiming! (" + data.getId() + ")");
                    if (user.isBurning())
                        DimBag.LOGGER.info("that bag is on fire (" + data.getId() + ")");
                    if (user.isInLava())
                        DimBag.LOGGER.info("Everybody's lava jumpin'! (" + data.getId() + ")");
                }
                if (user instanceof ServerPlayerEntity) { //player specific logic goes there

                }
                data.setUser(null);
            }
        }
    }

    @SubscribeEvent
    public static void onItemEntity(ItemEvent event) {
        if (event.getEntityItem().getItem().getItem() instanceof Bag)
            DimBag.LOGGER.info(event.toString());
    }
}