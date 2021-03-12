package com.limachi.dimensional_bags.common;

import com.google.common.collect.ArrayListMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.limachi.dimensional_bags.CuriosIntegration;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.KeyMapController;
import com.limachi.dimensional_bags.common.Config.ConfigEvents;
import com.limachi.dimensional_bags.common.blocks.Cloud;
import com.limachi.dimensional_bags.common.data.DimBagData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.ClientDataManager;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.HolderData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.WorldSavedDataManager;
import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.items.GhostBag;
import com.limachi.dimensional_bags.common.items.IDimBagCommonItem;
import com.limachi.dimensional_bags.common.items.entity.BagEntityItem;
import com.limachi.dimensional_bags.common.managers.Upgrade;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.command.CommandSource;
import net.minecraft.command.impl.TimeCommand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SEntityEquipmentPacket;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber
public class EventManager {

    private static int tick = 0;
    private static ArrayListMultimap<Integer, Runnable> pendingTasks = ArrayListMultimap.create();

    public static void delayedTask(int ticksToWait, Runnable run) { if (ticksToWait <= 0) ticksToWait = 1; pendingTasks.put(ticksToWait + tick, run); }

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
    public static class ReloadListenerConfig extends JsonReloadListener {

        private static final Gson GSON = (new GsonBuilder()).create();

        public ReloadListenerConfig() {
            super(GSON, "fake_folder");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> objectIn, IResourceManager resourceManagerIn, IProfiler profilerIn) {
            //virtually does nothing with json for now, only does a new bake
            ConfigEvents.bakeAll();
        }
    }

    @SubscribeEvent
    public static void addReloadListenerEvent(AddReloadListenerEvent e) {
        e.addListener(new ReloadListenerConfig());
    }*/

    @SubscribeEvent
    public static void onSleepFinishedTime(SleepFinishedTimeEvent event) { //sync the sleep in the rift dimension with the sleep in the overworld
        if (event.getWorld() instanceof ServerWorld && ((ServerWorld)event.getWorld()).getDimensionKey().compareTo(WorldUtils.DimBagRiftKey) == 0) { //players just finished sleeping in the rift dimension
            TimeCommand.setTime(DimBag.silentCommandSource(), 0); //use a set time command instead
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END || event.side.isClient()) return;
        if ((tick & 7) == 0) { //determine how often i run the logic, for now, once every 8 ticks (every 0.4s)
            MinecraftServer server = DimBag.getServer();
            World dbworld = WorldUtils.getRiftWorld(); //note: massive lag, should always have a chunk loaded to prevent loading/unloading of the world if there is no player in it
            DimBagData dbd = DimBagData.get(server);
            int l = dbd.getLastId() + 1;
            for (int i = 1; i < l; ++i) {
                Entity user = HolderData.execute(i, HolderData::getEntity, null);
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
                    if ((user instanceof BagEntity || user instanceof BagEntityItem /*|| data.shouldCreateCloudInVoid()*/) && user.getPosY() <= 3) { //this bag might fall in the void, time to fix this asap, FIXME: should be done in the IA of the bag entity/item
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
                world.setBlockState(cloudTry, Registries.getBlock(Cloud.NAME).getDefaultState());
            else if (Math.random() > 1D - treshold)
                generateCloud(world, cloudTry, treshold / divider, divider);
        }
    }

    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) { //used to track non-player entity holding a bag
        //DimBag.LOGGER.info("InventoryChangeEvent: " + event.getEntity() + " slot: " + event.getSlot() + ", " + event.getFrom() + " -> " + event.getTo());
        if (event.getEntity() instanceof ServerPlayerEntity) return; //tracking of the bag for players is done by the item ticking in their inventory
        if (event.getTo().getItem() instanceof Bag) { //this entity equiped a bag
            HolderData.execute(Bag.getEyeId(event.getTo()), holderData -> holderData.setHolder(event.getEntity()));
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
                ClientDataManager.getInstance(Bag.getEyeId(new_bag)).store(new_bag); //resync the bag data
                if ((KeyMapController.KeyBindings.SNEAK_KEY.getState(player) && !(player.inventory.armorInventory.get(EquipmentSlotType.CHEST.getIndex()).getItem() instanceof Bag) && Bag.equipBagOnCuriosSlot(new_bag, player)) || player.addItemStackToInventory(new_bag))
                    event.getTarget().remove();
            }
        }
    }

//    public static class ForceEnterTheBag extends Event {
//
//    }
//
//    @SubscribeEvent
//    public static void forceEnterTheBag(ForceEnterTheBag event) {
//
//    }

    @SubscribeEvent
    public static void turtleUpgrade(LivingDamageEvent event) { //protect and tp the player in the bag if it would take damage setting it's health below a threshold
        LivingEntity entity = event.getEntityLiving();
        if (!entity.isAlive() || event.getAmount() <= 0) return;
        int id = Bag.getBag(entity, 0);
        if (id == 0) return;
        UpgradeManager up = UpgradeManager.getInstance(id);
        if (!up.getInstalledUpgrades().contains("turtle")) return;
        Upgrade turtle = UpgradeManager.getUpgrade("turtle");
        int limit = turtle.getMemory(up).getInt("turtleUpBelow");
        if (entity.getHealth() <= limit && entity.getHealth() - event.getAmount() / 4 > 0) {
            event.setAmount(event.getAmount() / 4);
            entity.addPotionEffect(new EffectInstance(Effects.MINING_FATIGUE, 100, 2, false, false, false));
            entity.addPotionEffect(new EffectInstance(Effects.RESISTANCE, 100, 2, false, false, false));
            delayedTask(0, ()->SubRoomsManager.execute(id,sm->sm.enterBag(entity)));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void deathHopperUpgrade(LivingDropsEvent event) { //collect the player item's on death and send them inside the bag (change the cooldown of the items so they don't despawn)
        if (event.getDrops().isEmpty()) return;
        SubRoomsManager sm = null;
        for (ItemEntity item : event.getDrops())
            if (item.getItem().getItem() instanceof Bag && Bag.getEyeId(item.getItem()) != 0) {
                int id = Bag.getEyeId(item.getItem());
                UpgradeManager up = UpgradeManager.getInstance(id);
                if (!up.getInstalledUpgrades().contains("death_hopper")) continue;
                sm = SubRoomsManager.getInstance(id);
                if (sm != null) {
                    BagEntity.spawn(event.getEntity().world, event.getEntity().getPosition(), item.getItem());
                    item.remove();
                    break;
                }
            }
        if (sm != null) {
            for (ItemEntity item : event.getDrops())
                if (!item.removed) {
                    item.setNoDespawn();
                    sm.enterBag(item, false, true, false);
                }
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void blockPlaceWatcher(BlockEvent.EntityPlaceEvent event) {
        if (((World)event.getWorld()).getDimensionKey().compareTo(WorldUtils.DimBagRiftKey) != 0) { //we are outside the dimension
            ITag<Block> bagDimOnly = BlockTags.getCollection().get(new ResourceLocation("dim_bag", "placement_restriction/only_place_in_bag_dimension"));
            if (bagDimOnly != null && bagDimOnly.contains(event.getPlacedBlock().getBlock())) {
                DimBag.LOGGER.warn("placement of block " + event.getPlacedBlock() + " prevented in dimension " + event.getWorld() + " (" + event.getPos() + ")");
                if (event.getEntity() instanceof ServerPlayerEntity) {
                    SyncUtils.resyncPlayerHands((ServerPlayerEntity) event.getEntity(), true, true);
                }
                event.setCanceled(true);
                return;
            }
        }
        ITag<Block> kt = BlockTags.getCollection().get(new ResourceLocation(DimBag.MOD_ID, "keep_nbt_on_break"));
        if (kt != null && kt.contains(event.getPlacedBlock().getBlock())) { //do nbt manipulation
            DimBag.LOGGER.info("nbt storing block placement detected: " + event.getPlacedBlock());
        }
    }

    @SubscribeEvent
    public static void blockBreakWatcher(BlockEvent.BreakEvent event) {
        ITag<Block> kt = BlockTags.getCollection().get(new ResourceLocation(DimBag.MOD_ID, "keep_nbt_on_break"));
        if (kt != null && kt.contains(event.getState().getBlock())) { //do nbt manipulation
            DimBag.LOGGER.info("nbt storing block break detected: " + event.getState());
//            event.
        }
    }

//    @SubscribeEvent(priority = EventPriority.LOWEST)
//    public static void tombstoneModule(LivingDamageEvent event) { //need to refine how the items will be stored/given back, also we need to look for compatibility with other mods
//        LivingEntity entity = event.getEntityLiving();
//        if (!entity.isAlive() || event.getAmount() <= 0 || entity.getHealth() - event.getAmount() > 0) return; //only work on entities that are about to die due to damage
//        int id = Bag.getBag(entity, 0);
//        if (id == 0) return;
//        UpgradeManager up = UpgradeManager.getInstance(id);
//        if (!up.getInstalledUpgrades().contains("tombstone")) return;
//        Upgrade tombstone = UpgradeManager.getUpgrade("tombstone");
//    }

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

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!event.getPlayer().getEntityWorld().isRemote()) {
            PlayerEntity player = event.getPlayer();
            if (event.getTarget() instanceof PlayerEntity && KeyMapController.KeyBindings.SNEAK_KEY.getState(player)) { //the player clicked on another player
                PlayerEntity target = (PlayerEntity)event.getTarget();
                Vector3d deltaXZ = target.getPositionVec().subtract(player.getPositionVec()).mul(1, 0, 1).normalize(); //where is the player relative to the target, only in XZ coordinates
                double lookDelta = deltaXZ.dotProduct(target.getLookVec().mul(1, 0, 1).normalize()); //dot product, which can be used as the angle between two vectors
                if (lookDelta > 0.25) {//range [-1,1] -1 -> oposite vectors: entities look each other, 0 -> perpendicular, 1 -> aligned vectors (entities look in the same directon, so we can assume the clicker is behind the clicked)
                    //will now look if the target has a bag equiped (armor slot), and if the bag can be invaded
                    ItemStack stack = target.inventory.armorItemInSlot(EquipmentSlotType.CHEST.getIndex());
                    if (!stack.isEmpty() && stack.getItem() instanceof Bag) {
                        event.setCanceled(true);
                        SubRoomsManager.execute(Bag.getEyeId(stack), sm->sm.enterBag(player));
//                        EyeData data = EyeData.get(Bag.getId(stack));
//                        if (data != null && data.canInvade(player)) {
//                            event.setCanceled(true);
//                            data.tpIn(player);
//                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void ghostBagItemGiver(KeyMapController.KeyMapChangedEvent event) {
        if (!event.getPlayer().getEntityWorld().isRemote()) {
            if (event.getChangedKeys()[KeyMapController.KeyBindings.BAG_KEY.ordinal()]) {
                if (event.getKeys()[KeyMapController.KeyBindings.BAG_KEY.ordinal()]) {
//                    IDimBagCommonItem.ItemSearchResult res = IDimBagCommonItem.searchItem(event.getPlayer(), 0, Bag.class, o -> true, false);
//                    List<CuriosIntegration.ProxyItemStackModifier> res = CuriosIntegration.searchItem(event.getPlayer(), Bag.class, o->true, false);
                    if (!(event.getPlayer().getHeldItem(Hand.MAIN_HAND).getItem() instanceof Bag) && !(event.getPlayer().getHeldItem(Hand.MAIN_HAND).getItem() instanceof GhostBag) && !(event.getPlayer().getHeldItem(Hand.OFF_HAND).getItem() instanceof Bag) && !(event.getPlayer().getHeldItem(Hand.OFF_HAND).getItem() instanceof GhostBag))
                        event.getPlayer().replaceItemInInventory(IDimBagCommonItem.slotFromHand(event.getPlayer(), Hand.MAIN_HAND), GhostBag.ghostBagFromStack(event.getPlayer().getHeldItem(Hand.MAIN_HAND), event.getPlayer()));
                } else {
//                    IDimBagCommonItem.ItemSearchResult res = IDimBagCommonItem.searchItem(event.getPlayer(), 0, GhostBag.class, o -> true, false);
                    CuriosIntegration.ProxyItemStackModifier res = CuriosIntegration.searchItem(event.getPlayer(), GhostBag.class, o->true);
                    if (res != null)
//                        event.getPlayer().replaceItemInInventory(res.index, GhostBag.getOriginalStack(res.stack));
                        res.set(GhostBag.getOriginalStack(res.get()));
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

    /*
    private static int tick = 0;
    private static ArrayListMultimap<Integer, Runnable> pendingTasks = ArrayListMultimap.create();

    public static <T> void delayedTask(int ticksToWait, Runnable run) { pendingTasks.put(ticksToWait + tick, run); }

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
    }*/
}