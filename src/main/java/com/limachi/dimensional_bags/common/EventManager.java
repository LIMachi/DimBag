package com.limachi.dimensional_bags.common;

import com.limachi.dimensional_bags.DimensionalBagsMod;
import com.limachi.dimensional_bags.common.data.DimBagData;
import com.limachi.dimensional_bags.common.dimensions.BagDimension;
import com.limachi.dimensional_bags.common.network.DimBagDataSyncPacket;
import com.limachi.dimensional_bags.common.network.PacketHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EventManager {

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        /*
        if (event.getWorld().isRemote())
            loadManager(true);
            */
        if (!event.getWorld().isRemote() && event.getWorld().getDimension() instanceof BagDimension) {
            DimensionalBagsMod.LOGGER.info("loadding new world, force load the data");
            DimBagData.get(((ServerWorld) event.getWorld()).getServer());
        }
    }

    /*
    @SubscribeEvent
    public static void onWorldSave(WorldEvent.Save event) {
        if (!event.getWorld().isRemote())
            getInstance(false).save(false);
    }
    */

    /*
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        DimensionalBagsMod.LOGGER.info("******* player logged in, sendin sync data");
        PacketHandler.toClient(new DimBagDataSyncPacket(DimBagData.get(event.getPlayer().getServer())), (ServerPlayerEntity)event.getPlayer());
    }
    */

    /*
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
    }
    */

    /*
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        getInstance(false).syncClient(event.getPlayer());
    }
    */
}
