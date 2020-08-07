package com.limachi.dimensional_bags.common;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.data.DimBagData;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.limachi.dimensional_bags.common.items.Bag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Hand;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
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
                if (user instanceof ServerPlayerEntity) { //test if the player has the bag on them
                    ServerPlayerEntity player = (ServerPlayerEntity)user;
                    boolean present = false;
                    for (int j = 0; j < player.inventory.getSizeInventory(); ++j) {
                        ItemStack stack = player.inventory.getStackInSlot(j);
                        if (stack.getItem() instanceof Bag && stack.hasTag() && stack.getTag().getInt(Bag.ID_KEY) == i) {
                            present = true;
                            break;
                        }
                    }
                    if (!present) {
                        data.setUser(null);
                        user = null;
                    }
                }
                //here do updates on bags that need the user, like testing if the user is in water
                if (user != null) {
                    if (user.isInWater())
                        DimBag.LOGGER.info("that bag is swiming! (" + data.getId() + ")");
                    if (user.isBurning())
                        DimBag.LOGGER.info("that bag is on fire (" + data.getId() + ")");
                    if (user.isInLava())
                        DimBag.LOGGER.info("Everybody's lava jumpin'! (" + data.getId() + ")");
                }
            }
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (!event.getPlayer().getEntityWorld().isRemote() && event.getTarget() instanceof BagEntity) { //detect that a bag was punched by a player, will try to give the player back the bag (in curios or hand, otherwise prevent the punch)
            DimBag.LOGGER.info("bag is attacked by " + event.getPlayer().getUniqueID());
            event.setCanceled(true);
            PlayerEntity player = event.getPlayer();
            Hand hand;
            if (player.getHeldItemMainhand().getItem() == Items.AIR) {
                hand = Hand.MAIN_HAND;
            } else if (player.getHeldItemOffhand().getItem() == Items.AIR) {
                hand = Hand.OFF_HAND;
            } else return;
            ItemStack new_bag = ItemStack.read(event.getTarget().getPersistentData().getCompound(BagEntity.ITEM_KEY));
            if (new_bag == ItemStack.EMPTY) {
                new_bag = Bag.stackWithId(((BagEntity)event.getEntity()).getId());
            }
            player.setHeldItem(hand, new_bag);
            event.getTarget().remove();
        }
    }

    @SubscribeEvent
    public static void onItemEntity(ItemEvent event) {
        if (event.getEntityItem().getItem().getItem() instanceof Bag)
            DimBag.LOGGER.info(event.toString());
    }
}