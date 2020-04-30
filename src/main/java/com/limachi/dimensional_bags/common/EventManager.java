package com.limachi.dimensional_bags.common;

import com.limachi.dimensional_bags.DimensionalBagsMod;
import com.limachi.dimensional_bags.common.data.DimBagData;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.compat.Curios;
import com.limachi.dimensional_bags.common.data.IdHandler;
import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.limachi.dimensional_bags.common.init.Registries;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.client.event.PlayerSPPushOutOfBlocksEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class EventManager {

    /*
    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        if (!event.getWorld().isRemote() && event.getWorld().getDimension() instanceof BagDimension) {
            DimensionalBagsMod.LOGGER.info("loadding new world, force load the data");
            DimBagData.get(((ServerWorld) event.getWorld()).getServer());
        }
    }
    */

    @SubscribeEvent
    public static void debugPlayer(PlayerEvent event) {
        /*
        if (event.getPlayer().world.isRemote()) { //only test server side events, client side are canceled
            if (event.isCancelable())
                event.setCanceled(true);
            return;
        }
        if (event instanceof PlayerSPPushOutOfBlocksEvent) return;
        if (event instanceof InputUpdateEvent) return;
        if (event instanceof ItemTooltipEvent) return;
        if (event instanceof PlayerInteractEvent.LeftClickBlock) {
            event.setCanceled(true);
            PlayerInteractEvent.LeftClickBlock e = (PlayerInteractEvent.LeftClickBlock)event;
            DimensionalBagsMod.LOGGER.info(e);
        }
        DimensionalBagsMod.LOGGER.info("got event: " + event);
    */}

    @SubscribeEvent
    public static void onBagUsed(Bag.BagEvent event) {
        DimensionalBagsMod.LOGGER.info("caught bag event, forwarding to action map");
        EyeData data = DimBagData.get(event.player.getServer()).getEyeData(event.bagId);
        int id = data.getTrigger(event);
        if (id >= 0) {
            data.runAction(id, event.player, event.offHand ? Hand.OFF_HAND : Hand.MAIN_HAND);
            if (event.isCancelable())
                event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (!event.getPlayer().getEntityWorld().isRemote() && event.getTarget() instanceof BagEntity) { //detect that a bag was punched by a player, will try to give the player back the bag (in curios or hand, otherwise prevent the punch)
            DimensionalBagsMod.LOGGER.info("bag is attacked by " + event.getPlayer().getUniqueID());
            event.setCanceled(true);
            PlayerEntity player = event.getPlayer();
            int slot;
            if (Curios.INSTANCE.getStack(player, Curios.BACKPACK_SLOT_ID, 0).getItem() == Items.AIR) {
                slot = 0;
            } else if (player.getHeldItemMainhand().getItem() == Items.AIR) {
                slot = 1;
            } else if (player.getHeldItemOffhand().getItem() == Items.AIR) {
                slot = 2;
            } else return;
            ItemStack new_bag = ItemStack.read(event.getTarget().getPersistentData().getCompound("ItemBag"));
            if (new_bag == ItemStack.EMPTY) {
                new_bag = new ItemStack(Registries.BAG_ITEM.get());
                IdHandler id = new IdHandler((BagEntity) event.getEntity());
                id.write(new_bag);
            }
            if (slot != 0)
                player.setHeldItem(slot == 1 ? Hand.MAIN_HAND : Hand.OFF_HAND, new_bag);
            else
                Curios.INSTANCE.setStack(player, Curios.BACKPACK_SLOT_ID, 0, new_bag);
            event.getTarget().remove();
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
