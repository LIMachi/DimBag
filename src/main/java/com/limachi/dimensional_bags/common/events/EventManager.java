package com.limachi.dimensional_bags.common.events;

import com.google.common.collect.ArrayListMultimap;
import com.limachi.dimensional_bags.lib.ConfigManager;
import com.limachi.dimensional_bags.lib.CuriosIntegration;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.KeyMapController;
import com.limachi.dimensional_bags.common.CloudBlock;
import com.limachi.dimensional_bags.lib.common.blocks.IBagWrenchable;
import com.limachi.dimensional_bags.lib.common.blocks.IGetUseSneakWithItemEvent;
import com.limachi.dimensional_bags.lib.common.network.PacketHandler;
import com.limachi.dimensional_bags.lib.common.network.packets.PlayerPersistentDataAction;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.bag.BagEntity;
import com.limachi.dimensional_bags.common.bag.BagItem;
import com.limachi.dimensional_bags.common.bag.GhostBagItem;
import com.limachi.dimensional_bags.lib.common.items.IDimBagCommonItem;
import com.limachi.dimensional_bags.lib.utils.SyncUtils;
import com.limachi.dimensional_bags.lib.utils.WorldUtils;
import net.minecraft.block.*;
import net.minecraft.command.impl.TimeCommand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber
public class EventManager {

    public static int tick = 0;
    private static ArrayListMultimap<Integer, Runnable> pendingTasks = ArrayListMultimap.create();

    public static Random RANDOM = new Random();

    /**
     * queue a delayed task to be run in X ticks (minimum 1 tick)
     */
    public static void delayedTask(int ticksToWait, Runnable run) { if (ticksToWait <= 0) ticksToWait = 1; pendingTasks.put(ticksToWait + tick, run); }

    /**
     * handles delayed tasks on the TickEvent server side, if you need something delayed client side, please use the same function but in the client package
     */
    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            List<Runnable> tasks = pendingTasks.get(tick);
            if (tasks != null)
                for (Runnable task : tasks)
                    task.run();
        } else if (event.phase == TickEvent.Phase.END) {
            pendingTasks.removeAll(tick);
            ++tick;
        }
    }

    /*
    @ConfigManager.Config
    public static int PERCENT_CHANCE_TO_ADD_ENDSTONE_TO_ENDERMAN = 10;

    @ConfigManager.Config
    public static int PERCENT_CHANCE_TO_ADD_CHORUS_FLOWER_TO_ENDERMAN = 2;
     */

//    public static final BlockState ENDSTONE = Blocks.END_STONE.defaultBlockState();

//    public static final BlockState CHORUS_FLOWER = Blocks.CHORUS_FLOWER.defaultBlockState();

    @SubscribeEvent
    public static void fixPlayerPersistentDataDesync(PlayerEvent.PlayerLoggedInEvent event) {
        if (DimBag.isServer(event.getPlayer().level))
            PacketHandler.toClient((ServerPlayerEntity)event.getPlayer(), new PlayerPersistentDataAction(PlayerPersistentDataAction.Actions.OVERRIDE, event.getPlayer().getPersistentData()));
    }

    /**
     * might add chorus flowers or endstone to the hands of newly spawned enderman, chance is config dependent
     */
    /*
    @SubscribeEvent
    public static void addRandomEndstoneAndChorusFlowerToNewEnderman(EntityEvent.EntityConstructing event) {
        Entity ent = event.getEntity();
        if (ent instanceof EndermanEntity) {
            BlockState bs = ((EndermanEntity)ent).getCarriedBlock();
            if ((bs == null || bs.getBlock() instanceof AirBlock)) {
                if (PERCENT_CHANCE_TO_ADD_ENDSTONE_TO_ENDERMAN > 0 && RANDOM.nextInt(100) < PERCENT_CHANCE_TO_ADD_ENDSTONE_TO_ENDERMAN)
                    ((EndermanEntity) ent).setCarriedBlock(ENDSTONE);
                else if (PERCENT_CHANCE_TO_ADD_CHORUS_FLOWER_TO_ENDERMAN > 0 && RANDOM.nextInt(100) < PERCENT_CHANCE_TO_ADD_CHORUS_FLOWER_TO_ENDERMAN)
                    ((EndermanEntity) ent).setCarriedBlock(CHORUS_FLOWER);
            }
        }
    }*/

    /**
     * prevent mining animation of wall blocks (no need to check for creative players, they plain remove the block without animation)
     */
    @SubscribeEvent
    public static void wallBlocksCannotBeMined(PlayerEvent.BreakSpeed event) {
        if (SubRoomsManager.isWall(event.getPlayer().level, event.getPos()))
            event.setCanceled(true);
    }

    /**
     * prevent destruction of wall blocks (except if the player breaking is in creative)
     */
    @SubscribeEvent
    public static void wallBlocksCannotBeBroken(BlockEvent.BreakEvent event) {
        if (!event.getPlayer().isCreative() && SubRoomsManager.isWall((World)event.getWorld(), event.getPos()))
            event.setCanceled(true);
    }

    /**
     * allow blocks flagged by the IGetUseSneakWithItemEvent interface to accept sneaking use while holding an item (if the item is not a block, and if the item is not a bag wrench if the block can be wrenched)
     */
    @SubscribeEvent
    public static void acceptSneakUseOfBlockWithItem(PlayerInteractEvent.RightClickBlock event) {
        Block block = event.getWorld().getBlockState(event.getHitVec().getBlockPos()).getBlock();
        Item item = event.getPlayer().getItemInHand(event.getHand()).getItem();
        if (block instanceof IGetUseSneakWithItemEvent && !(item instanceof BlockItem || (block instanceof IBagWrenchable && item instanceof BagItem))) {
            event.setUseBlock(Event.Result.ALLOW);
            event.setUseItem(Event.Result.DENY);
        }
    }

    /**
     * try to skip the night when players sleep inside the bag
     */
    @SubscribeEvent //FIXME: not working with a few mods
    public static void onSleepFinishedInBag(SleepFinishedTimeEvent event) { //sync the sleep in the rift dimension with the sleep in the overworld
        if (event.getWorld() instanceof ServerWorld && ((ServerWorld)event.getWorld()).dimension().compareTo(WorldUtils.DimBagRiftKey) == 0) { //players just finished sleeping in the rift dimension
            TimeCommand.setTime(DimBag.silentCommandSource(), 0); //use a set time command instead
        }
    }

    /**
     * all-purpose tick function, but mainly used to tick bags that are not on players or in entity form
     */
    /*
    @SubscribeEvent //FIXME: rework some behaviors (should only generate tick for items that are not on players and use the tick method instead of doing this here)
    public static void everyBodyDoTheTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END || event.side.isClient()) return;
        if ((tick & 7) == 0) { //determine how often i run the logic, for now, once every 8 ticks (every 0.4s)
            MinecraftServer server = DimBag.getServer();
            World dbworld = WorldUtils.getRiftWorld(); //note: massive lag, should always have a chunk loaded to prevent loading/unloading of the world if there is no player in it
            DimBagData dbd = DimBagData.get();
            if (dbd == null) return;
            int l = dbd.getLastId() + 1;
            for (int i = 1; i < l; ++i) {
                Entity user = HolderData.execute(i, HolderData::getEntity, null);
//                if (user instanceof ServerPlayerEntity) { //test if the player has the bag on them
//                    ServerPlayerEntity player = (ServerPlayerEntity)user;
//                    boolean present = false;
//                    for (int j = 0; j < player.inventory.getContainerSize(); ++j) {
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
                        BlockState water = Blocks.WATER.defaultBlockState().setValue(FlowingFluidBlock.LEVEL, 1);
//                        if (dbworld.getBlockState(data.getEyePos().down()) == Blocks.AIR.defaultBlockState()) {
//                            dbworld.setBlock(data.getEyePos().down(), water, 11);
//                            dbworld.setBlock(data.getEyePos().down().east(), water, 11);
//                        }
                    }
                    if (user.isOnFire())
                        DimBag.LOGGER.info("that bag is on fire (" + i + ")");
                    if (user.isInLava())
                        DimBag.LOGGER.info("Everybody's lava jumpin'! (" + i + ")");
//                    DimBag.LOGGER.info("Ima load this chunk: (" + WorldUtils.worldRKToString(WorldUtils.worldRKFromWorld(user.level)) + ") " + user.getPosition().getX() + ", " + user.getPosition().getY() + ", " + user.getPosition().getZ() + " (" + i + ")");
                    dbd.chunkloadder.loadChunk((ServerWorld) user.level, user.blockPosition(), i);
                    if ((user instanceof BagEntity || user instanceof BagEntityItem) && user.position().y <= 3) { //this bag might fall in the void, time to fix this asap, FIXME: should be done in the IA of the bag entity/item
                        generateCloud(user.level, new BlockPos(user.blockPosition().getX(), 0, user.blockPosition().getZ()), 2f, 1.5f);
                        if (user.position().y < 1) {
                            WorldUtils.teleportEntity(user, user.level.dimension(), user.position().x, 1, user.position().z);
                            user.setDeltaMovement(user.getDeltaMovement().x, 0, user.getDeltaMovement().y);
                        }
//                        DimBag.LOGGER.info("I just saved a bag from the void, few (" + i + ")");
                    }
                } else { //did not find a user, usually meaning the bag item/entity isn't loaded or the entity using it isn't loaded
//                    DimBag.LOGGER.info("that bag is MIA (" + i + ")");
                    dbd.chunkloadder.unloadChunk(i);
                }
            }
            if (l >= 1)
                dbd.setDirty();
        }
    }*/

    /**
     * when a bag holder is about to fall in the void, generate a platform of cloud blocks
     */
    //FIXME: should be an upgrade
    private static void generateCloud(World world, BlockPos pos, float treshold, float divider) {
        if (treshold < 0.001 || divider < 0.001) return;
        for (Direction dir : Direction.values()) {
            if (dir == Direction.UP) continue;
            BlockPos cloudTry = dir == Direction.DOWN ? pos : pos.offset(dir.getNormal());
            if (dir == Direction.DOWN && world.getBlockState(cloudTry) == Blocks.AIR.defaultBlockState())
                world.setBlock(cloudTry, CloudBlock.INSTANCE.get().defaultBlockState(), Constants.BlockFlags.DEFAULT_AND_RERENDER);
            else if (Math.random() > 1D - treshold)
                generateCloud(world, cloudTry, treshold / divider, divider);
        }
    }

    /**
     * track when non player entity pickup bags (so they can be set as the current user of the bag)
     */
//    @SubscribeEvent
//    public static void entityEquipsABag(LivingEquipmentChangeEvent event) {
//        //DimBag.LOGGER.info("InventoryChangeEvent: " + event.getEntity() + " slot: " + event.getSlot() + ", " + event.getFrom() + " -> " + event.getTo());
//        if (event.getEntity() instanceof ServerPlayerEntity) return; //tracking of the bag for players is done by the item ticking in their inventory
//        if (event.getTo().getItem() instanceof Bag) { //this entity equiped a bag
//            HolderData.execute(Bag.getbagId(event.getTo()), holderData -> holderData.setHolder(event.getEntity()));
//            if (event.getEntity() instanceof MobEntity)
//                ((MobEntity)event.getEntity()).requiresCustomPersistence();
//        }
//    }

    /**
     * detect a bag being attacked by a player, so they can pick it up (or quick equip if sneaking)
     */
    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) { //FIXME: move this to the bag entity (on tacking damage)
        if (!event.getPlayer().level.isClientSide()) {
            if (event.getTarget() instanceof BagEntity) {//detect that a bag was punched by a player, will try to give the player back the bag (in curios or hand, otherwise prevent the punch)
                DimBag.LOGGER.info("bag is attacked by " + event.getPlayer().getUUID());
                event.setCanceled(true);
                PlayerEntity player = event.getPlayer();
                ItemStack new_bag = ItemStack.of(event.getTarget().getPersistentData().getCompound(BagEntity.ITEM_KEY));
                if ((KeyMapController.KeyBindings.SNEAK_KEY.getState(player) && !(player.inventory.armor.get(EquipmentSlotType.CHEST.getIndex()).getItem() instanceof BagItem) && BagItem.equipBagOnCuriosSlot(new_bag, player)) || player.addItem(new_bag))
                    event.getTarget().remove();
            }
        }
    }

    @SubscribeEvent
    public static void blockPlaceWatcher(BlockEvent.EntityPlaceEvent event) { //FIXME: not used/not working for now
        if (((World)event.getWorld()).dimension().compareTo(WorldUtils.DimBagRiftKey) != 0) { //we are outside the dimension
            ITag<Block> bagDimOnly = BlockTags.getAllTags().getTag(new ResourceLocation("dim_bag", "placement_restriction/only_place_in_bag_dimension"));
            if (bagDimOnly != null && bagDimOnly.contains(event.getPlacedBlock().getBlock())) {
                DimBag.LOGGER.warn("placement of block " + event.getPlacedBlock() + " prevented in dimension " + event.getWorld() + " (" + event.getPos() + ")");
                if (event.getEntity() instanceof ServerPlayerEntity) {
                    SyncUtils.resyncPlayerHands((ServerPlayerEntity) event.getEntity(), true, true);
                }
                event.setCanceled(true);
                return;
            }
        }
        ITag<Block> kt = BlockTags.getAllTags().getTag(new ResourceLocation(DimBag.MOD_ID, "keep_nbt_on_break"));
        if (kt != null && kt.contains(event.getPlacedBlock().getBlock())) { //do nbt manipulation
            DimBag.LOGGER.info("nbt storing block placement detected: " + event.getPlacedBlock());
        }
    }

//    @SubscribeEvent
//    public static void blockBreakWatcher(BlockEvent.BreakEvent event) {
//        ITag<Block> kt = BlockTags.getCollection().get(new ResourceLocation(DimBag.MOD_ID, "keep_nbt_on_break"));
//        if (kt != null && kt.contains(event.getState().getBlock())) { //do nbt manipulation
//            DimBag.LOGGER.info("nbt storing block break detected: " + event.getState());
//            event.
//        }
//    }

    /**
     * handles when a player right click a bag on another player to interact with it (should be changed to allow non sneak interact to access bag inventory, this might require to rework the rule for the container to be "still visible by a player")
     */
    @SubscribeEvent
    public static void interactWithBagOnOtherPlayer(PlayerInteractEvent.EntityInteract event) {
        if (!event.getPlayer().level.isClientSide()) {
            PlayerEntity player = event.getPlayer();
            if (event.getTarget() instanceof PlayerEntity && KeyMapController.KeyBindings.SNEAK_KEY.getState(player)) { //the player clicked on another player
                PlayerEntity target = (PlayerEntity)event.getTarget();
                Vector3d deltaXZ = target.position().subtract(player.position()).multiply(1, 0, 1).normalize(); //where is the player relative to the target, only in XZ coordinates
                double lookDelta = deltaXZ.dot(target.getLookAngle().multiply(1, 0, 1).normalize()); //dot product, which can be used as the angle between two vectors
                if (lookDelta > 0.25) {//range [-1,1] -1 -> oposite vectors: entities look each other, 0 -> perpendicular, 1 -> aligned vectors (entities look in the same directon, so we can assume the clicker is behind the clicked)
                    //will now look if the target has a bag equipped, and if the bag can be invaded
                    int bag = BagItem.getBag(target, 0, true, true);
                    if (bag > 0) {
                        event.setCanceled(true);
                        SubRoomsManager.execute(bag, sm->sm.enterBag(player));
                    }
                }
            }
        }
    }

    /**
     * handles when a players hold the bag action key, giving them a ghost bag for as long as the key stays held
     */
    @SubscribeEvent
    public static void ghostBagItemGiver1(KeyMapController.KeyMapChangedEvent event) {
        if (!event.getPlayer().level.isClientSide()) {
            if (event.getChangedKeys()[KeyMapController.KeyBindings.BAG_KEY.ordinal()]) {
                if (event.getKeys()[KeyMapController.KeyBindings.BAG_KEY.ordinal()]) {
                    if (event.getPlayer().containerMenu == event.getPlayer().inventoryMenu && !(event.getPlayer().getItemInHand(Hand.MAIN_HAND).getItem() instanceof BagItem) && !(event.getPlayer().getItemInHand(Hand.OFF_HAND).getItem() instanceof BagItem))
                        event.getPlayer().setSlot(IDimBagCommonItem.slotFromHand(event.getPlayer(), Hand.MAIN_HAND), GhostBagItem.ghostBagFromStack(event.getPlayer().getItemInHand(Hand.MAIN_HAND), event.getPlayer()));
                } else {
                    CuriosIntegration.ProxySlotModifier res = CuriosIntegration.searchItem(event.getPlayer(), GhostBagItem.class, o->true);
                    if (res != null)
                        res.set(GhostBagItem.getOriginalStack(res.get()));
                }
            }
        }
    }
}