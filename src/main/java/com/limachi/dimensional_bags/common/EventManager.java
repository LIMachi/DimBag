package com.limachi.dimensional_bags.common;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.KeyMapController;
import com.limachi.dimensional_bags.common.data.DimBagData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.HolderData;
import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.items.GhostBag;
import com.limachi.dimensional_bags.common.items.IDimBagCommonItem;
import com.limachi.dimensional_bags.common.items.entity.BagEntityItem;
import javafx.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.world.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.function.Supplier;

@Mod.EventBusSubscriber
public class EventManager {
    public static int tick = 0;
    public static List<Pair<Integer, Supplier<Boolean>>> DelayedTasks = new ArrayList<>();

    private static void runDelayedTasks() {
        DelayedTasks.removeIf(x -> {
            if (x.getKey() == tick) {
                x.getValue().get();
                return true;
            }
            return false;
        });
    }

    public static void addDelayedTask(int tick, Supplier<Boolean> func) { DelayedTasks.add(new Pair<>(tick, func)); }

    @SubscribeEvent
    public static void onSleepFinishedTime(SleepFinishedTimeEvent event) { //sync the sleep in the rift dimension with the sleep in the overworld
        if (event.getWorld() instanceof ServerWorld && ((ServerWorld)event.getWorld()).getDimensionKey().compareTo(WorldUtils.DimBagRiftKey) == 0) { //players just finished sleeping in the rift dimension
            ServerWorld overworld = WorldUtils.getOverWorld();
            overworld.func_241114_a_(net.minecraftforge.event.ForgeEventFactory.onSleepFinished(overworld, event.getNewTime(), overworld.getDayTime())); //sending the wakeup + time set in the overworld
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END || event.side.isClient()) return;
        ++tick;
        runDelayedTasks();
        if ((tick & 7) == 0) { //determine how often i run the logic, for now, once every 8 ticks (every 0.4s)
            MinecraftServer server = DimBag.getServer();
            World dbworld = WorldUtils.getRiftWorld(); //note: massive lag, should always have a chunk loaded to prevent loading/unloading of the world if there is no player in it
            DimBagData dbd = DimBagData.get(server);
            int l = dbd.getLastId() + 1;
            for (int i = 1; i < l; ++i) {
                HolderData holderData = HolderData.getInstance(null, i);
                Entity user = holderData != null ? holderData.getEntity() : null;
//                if (user instanceof ServerPlayerEntity) { //test if the player has the bag on them
//                    ServerPlayerEntity player = (ServerPlayerEntity)user;
//                    boolean present = false;
//                    for (int j = 0; j < player.inventory.getSizeInventory(); ++j) {
//                        ItemStack stack = player.inventory.getStackInSlot(j);
//                        if (stack.getItem() instanceof Bag && stack.hasTag() && stack.getTag().getInt(EyeData.ID_KEY) == i) {
//                            present = true;
//                            break;
//                        }
//                    }
//                    if (!present) {
//                        data.setUser(null);
//                        user = null;
//                    }
//                }
                //here do updates on bags that need the user, like testing if the user is in water
                if (user != null) {
                    if (user.isInWater() && (tick & 63) == 0) {
                        DimBag.LOGGER.info("that bag is swiming! (" + i + ")");
                        BlockState water = Blocks.WATER.getDefaultState().with(FlowingFluidBlock.LEVEL, 1);
//                        if (dbworld.getBlockState(data.getEyePos().down()) == Blocks.AIR.getDefaultState()) {
//                            dbworld.setBlockState(data.getEyePos().down(), water, 11);
//                            dbworld.setBlockState(data.getEyePos().down().east(), water, 11);
//                        }
                    }
                    if (user.isBurning())
                        DimBag.LOGGER.info("that bag is on fire (" + i + ")");
                    if (user.isInLava())
                        DimBag.LOGGER.info("Everybody's lava jumpin'! (" + i + ")");
//                    DimBag.LOGGER.info("Ima load this chunk: (" + WorldUtils.worldRKToString(WorldUtils.worldRKFromWorld(user.getEntityWorld())) + ") " + user.getPosition().getX() + ", " + user.getPosition().getY() + ", " + user.getPosition().getZ() + " (" + i + ")");
                    dbd.loadChunk((ServerWorld) user.getEntityWorld(), user.getPosition().getX(), user.getPosition().getZ(), i);
                    if ((user instanceof BagEntity || user instanceof BagEntityItem /*|| data.shouldCreateCloudInVoid()*/) && user.getPosY() <= 3) { //this bag might fall in the void, time to fix this asap
                        generateCloud(user.getEntityWorld(), new BlockPos(user.getPosition().getX(), 0, user.getPosition().getZ()), 2f, 1.5f);
                        if (user.getPosY() < 1) {
                            WorldUtils.teleportEntity(user, user.getEntityWorld().getDimensionKey(), user.getPosX(), 1, user.getPosZ());
                            user.setMotion(user.getMotion().x, 0, user.getMotion().y);
                        }
//                        DimBag.LOGGER.info("I just saved a bag from the void, few (" + i + ")");
                    }
                } else { //did not find a user, usually meaning the bag item/entity isn't loaded or the entity using it isn't loaded
//                    DimBag.LOGGER.info("that bag is MIA (" + i + ")");
                    dbd.unloadChunk(server, i);
                }
            }
        }
    }

    private static void generateCloud(World world, BlockPos pos, float treshold, float divider) {
        if (treshold < 0.001 || divider < 0.001) return;
        for (Direction dir : Direction.values()) {
            if (dir == Direction.UP) continue;
            BlockPos cloudTry = dir == Direction.DOWN ? pos : pos.offset(dir);
            if (dir == Direction.DOWN && world.getBlockState(cloudTry) == Blocks.AIR.getDefaultState())
                world.setBlockState(cloudTry, Registries.CLOUD_BLOCK.get().getDefaultState());
            else if (Math.random() > 1D - treshold)
                generateCloud(world, cloudTry, treshold / divider, divider);
        }
    }

    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) { //used to track non-player entity holding a bag
        //DimBag.LOGGER.info("InventoryChangeEvent: " + event.getEntity() + " slot: " + event.getSlot() + ", " + event.getFrom() + " -> " + event.getTo());
        if (event.getEntity() instanceof ServerPlayerEntity) return; //tracking of the bag for players is done by the item ticking in their inventory
        if (event.getTo().getItem() instanceof Bag) { //this entity equiped a bag
            HolderData holderData = HolderData.getInstance(null, Bag.getEyeId(event.getTo()));
            if (holderData != null)
                holderData.setHolder(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (!event.getPlayer().getEntityWorld().isRemote()) {
            if (event.getTarget() instanceof BagEntity) {//detect that a bag was punched by a player, will try to give the player back the bag (in curios or hand, otherwise prevent the punch)
                DimBag.LOGGER.info("bag is attacked by " + event.getPlayer().getUniqueID());
                event.setCanceled(true);
                PlayerEntity player = event.getPlayer();
                ItemStack new_bag = ItemStack.read(event.getTarget().getPersistentData().getCompound(BagEntity.ITEM_KEY));
                if ((KeyMapController.getKey(player, KeyMapController.CROUCH_KEY) && !(player.inventory.armorInventory.get(EquipmentSlotType.CHEST.getIndex()).getItem() instanceof Bag) && Bag.equipBagOnChestSlot(new_bag, player)) || player.addItemStackToInventory(new_bag))
                    event.getTarget().remove();
            }
        }
    }

    /*@SubscribeEvent
    public static void onItemRightClick(PlayerInteractEvent.RightClickItem event) {
        PlayerEntity player = event.getPlayer();
        if (player != null) {
            if (KeyMapController.getKey(player, KeyMapController.BAG_ACTION_KEY)) {
                IDimBagCommonItem.ItemSearchResult src = IDimBagCommonItem.searchItem(player, 0, Bag.class, (x)->true);
                if (src != null) {
                    ActionResult<ItemStack> res = ((Bag)src.stack.getItem()).onItemRightClick(player.world, player, src.index);
                    if (res.getType().isSuccessOrConsume())
                        event.setCanceled(true);
                }
            }
        }
    }*/

    /*@SubscribeEvent
    public static void onItemRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getPlayer().getEntityWorld().isRemote()) {
            if (KeyMapController.getKey(event.getPlayer(), KeyMapController.BAG_ACTION_KEY)) {
                IDimBagCommonItem.ItemSearchResult src = IDimBagCommonItem.searchItem(event.getPlayer(), 0, Bag.class, (x)->true);
                if (src != null) {
                    ActionResultType res = ((Bag)src.stack.getItem()).onItemUse(event.getWorld(), event.getPlayer(), src.index, Bag.rayTrace(event.getWorld(), event.getPlayer(), RayTraceContext.FluidMode.ANY));
                    if (res.isSuccessOrConsume()) {
                        event.setUseBlock(Event.Result.DENY);
                        event.setUseItem(Event.Result.DENY);
                        event.setCanceled(true);
                    }
                }
            }
        }
    }*/

    /*
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!event.getPlayer().getEntityWorld().isRemote()) {
            PlayerEntity player = event.getPlayer();
            if (event.getTarget() instanceof PlayerEntity && KeyMapController.getKey(player, KeyMapController.CROUCH_KEY)) { //the player clicked on another player
                PlayerEntity target = (PlayerEntity)event.getTarget();
                Vector3d deltaXZ = target.getPositionVec().subtract(player.getPositionVec()).mul(1, 0, 1).normalize(); //where is the player relative to the target, only in XZ coordinates
                double lookDelta = deltaXZ.dotProduct(target.getLookVec().mul(1, 0, 1).normalize()); //dot product, which can be used as the angle between two vectors
                if (lookDelta > 0.25) {//range [-1,1] -1 -> oposite vectors: entities look each other, 0 -> perpendicular, 1 -> aligned vectors (entities look in the same directon, so we can assume the clicker is behind the clicked)
                    //will now look if the target has a bag equiped (armor slot), and if the bag can be invaded
                    ItemStack stack = target.inventory.armorItemInSlot(EquipmentSlotType.CHEST.getIndex());
                    if (!stack.isEmpty() && stack.getItem() instanceof Bag) {
                        EyeData data = EyeData.get(Bag.getId(stack));
                        if (data != null && data.canInvade(player)) {
                            event.setCanceled(true);
                            data.tpIn(player);
                        }
                    }
                }
            }
        }
    }
     */

    @SubscribeEvent
    public static void onKeyMapChanged(KeyMapController.KeyMapChangedEvent event) {
        if (!event.getPlayer().getEntityWorld().isRemote()) {
            if (event.getChangedKeys()[KeyMapController.BAG_ACTION_KEY]) {
                if (event.getKeys()[KeyMapController.BAG_ACTION_KEY]) {
                    IDimBagCommonItem.ItemSearchResult res = IDimBagCommonItem.searchItem(event.getPlayer(), 0, Bag.class, o -> true, false);
                    if (res != null && res.index != -1 && res.index != IDimBagCommonItem.slotFromHand(event.getPlayer(), Hand.MAIN_HAND) && res.index != IDimBagCommonItem.slotFromHand(event.getPlayer(), Hand.OFF_HAND))
                        event.getPlayer().replaceItemInInventory(IDimBagCommonItem.slotFromHand(event.getPlayer(), Hand.MAIN_HAND), GhostBag.ghostBagFromStack(event.getPlayer().getHeldItem(Hand.MAIN_HAND), event.getPlayer()));
                } else {
                    IDimBagCommonItem.ItemSearchResult res = IDimBagCommonItem.searchItem(event.getPlayer(), 0, GhostBag.class, o -> true, false);
                    if (res != null && res.index != -1)
                        event.getPlayer().replaceItemInInventory(res.index, GhostBag.getOriginalStack(res.stack));
                }
            }
        }
    }

    /*
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        BlockState bs = event.getPlayer().getEntityWorld().getBlockState(event.getPos());
        if (bs == Registries.TUNNEL_BLOCK.get().getDefaultState()) { //trying to use a tunnel (

        }
    }
    */

    /*@SubscribeEvent
    public static void onItemEntity(ItemEvent event) {
        if (event.getEntityItem().getItem().getItem() instanceof Bag)
            DimBag.LOGGER.info(event.toString());
    }*/
}