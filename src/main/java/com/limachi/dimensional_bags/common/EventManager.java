package com.limachi.dimensional_bags.common;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.data.DimBagData;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.items.entity.BagEntityItem;
import javafx.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
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

    private static void addDelayedTask(int tick, Supplier<Boolean> func) {
        DelayedTasks.add(new Pair<>(tick, func));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END || event.side.isClient()) return;
        ++tick;
        runDelayedTasks();
        if ((tick & 7) == 0) { //determine how often i run the logic, for now, once every 8 ticks (every 0.4s)
            MinecraftServer server = DimBag.getServer(null);
            World dbworld = WorldUtils.getRiftWorld(); //note: massive lag, should always have a chunk loaded to prevent loading/unloading of the world if there is no player in it
            DimBagData dbd = DimBagData.get(server);
            int l = dbd.getLastId() + 1;
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
                    if (user.isInWater() && (tick & 63) == 0) {
                        DimBag.LOGGER.info("that bag is swiming! (" + data.getId() + ")");
                        BlockState water = Blocks.WATER.getDefaultState().with(FlowingFluidBlock.LEVEL, 1);
                        if (dbworld.getBlockState(data.getEyePos().down()) == Blocks.AIR.getDefaultState()) {
                            dbworld.setBlockState(data.getEyePos().down(), water, 11);
                            dbworld.setBlockState(data.getEyePos().down().east(), water, 11);
                        }
                    }
                    if (user.isBurning())
                        DimBag.LOGGER.info("that bag is on fire (" + data.getId() + ")");
                    if (user.isInLava())
                        DimBag.LOGGER.info("Everybody's lava jumpin'! (" + data.getId() + ")");
//                    DimBag.LOGGER.info("Ima load this chunk: (" + WorldUtils.worldRKToString(WorldUtils.worldRKFromWorld(user.getEntityWorld())) + ") " + user.getPosition().getX() + ", " + user.getPosition().getY() + ", " + user.getPosition().getZ() + " (" + data.getId() + ")");
                    dbd.loadChunk(server, WorldUtils.worldRKFromWorld(user.getEntityWorld()), user.getPosition().getX(), user.getPosition().getZ(), data.getId());
                    if ((user instanceof BagEntity || user instanceof BagEntityItem || data.shouldCreateCloudInVoid()) && user.getPosY() <= 3) { //this bag might fall in the void, time to fix this asap
                        generateCloud(user.getEntityWorld(), new BlockPos(user.getPosition().getX(), 0, user.getPosition().getZ()), 2f, 1.5f);
                        if (user.getPosY() < 1) {
                            WorldUtils.teleportEntity(user, WorldUtils.worldRKFromWorld(user.getEntityWorld()), user.getPosX(), 1, user.getPosZ());
                            user.setMotion(user.getMotion().x, 0, user.getMotion().y);
                        }
//                        DimBag.LOGGER.info("I just saved a bag from the void, few (" + data.getId() + ")");
                    }
                } else { //did not find a user, usually meaning the bag item/entity isn't loaded or the entity using it isn't loaded
//                    DimBag.LOGGER.info("that bag is MIA (" + data.getId() + ")");
                    dbd.unloadChunk(server, data.getId());
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
            int id = Bag.getId(event.getTo());
            if (id == 0) return;
            EyeData data = EyeData.get(null, id);
            data.setUser(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (!event.getPlayer().getEntityWorld().isRemote() && event.getTarget() instanceof BagEntity) { //detect that a bag was punched by a player, will try to give the player back the bag (in curios or hand, otherwise prevent the punch)
            DimBag.LOGGER.info("bag is attacked by " + event.getPlayer().getUniqueID());
            event.setCanceled(true);
            PlayerEntity player = event.getPlayer();
            ItemStack new_bag = ItemStack.read(event.getTarget().getPersistentData().getCompound(BagEntity.ITEM_KEY));
            if (new_bag == ItemStack.EMPTY) {
                new_bag = Bag.stackWithId(((BagEntity)event.getEntity()).getId());
            }
            if (!player.addItemStackToInventory(new_bag))
                return;
            event.getTarget().remove();
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

    @SubscribeEvent
    public static void onItemEntity(ItemEvent event) {
        if (event.getEntityItem().getItem().getItem() instanceof Bag)
            DimBag.LOGGER.info(event.toString());
    }
}