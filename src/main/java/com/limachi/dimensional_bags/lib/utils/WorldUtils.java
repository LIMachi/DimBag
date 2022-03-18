package com.limachi.dimensional_bags.lib.utils;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.client.render.Vector2d;
import com.limachi.dimensional_bags.common.events.EventManager;
import com.limachi.dimensional_bags.common.references.Registries;
import com.limachi.dimensional_bags.common.bagDimensionOnly.bagSlot.SlotBlock;
import com.limachi.dimensional_bags.common.bagDimensionOnly.WallBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.DirectionalPlaceContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;
import net.minecraft.world.storage.IWorldInfo;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

@Mod.EventBusSubscriber
public class WorldUtils { //TODO: remove bloat once MCP/Forge mappings are better/more stable

    public static final RegistryKey<World> DimBagRiftKey = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("dim_bag:bag_rift"));

    public static @Nullable World getRiftWorld() { return getWorld(DimBagRiftKey); }
    public static @Nullable World getOverWorld() { return getWorld(World.OVERWORLD); }

    public static Iterable<ServerWorld> getAllWorlds() { return DimBag.getServer() != null ? DimBag.getServer().getAllLevels() : null; }

    public static BlockPos getWorldSpawn(RegistryKey<World> reg) {
        World w = getWorld(reg);
        if (w == null) return new BlockPos(0, 255, 0);
        IWorldInfo i = w.getLevelData();
        return new BlockPos(i.getXSpawn(), i.getYSpawn(), i.getZSpawn());
    }

    public static Vector3d getWorldSpawnF(RegistryKey<World> reg) {
        BlockPos pos = getWorldSpawn(reg);
        return new Vector3d(pos.getX(), pos.getY(), pos.getZ());
    }

    public static World getWorld(RegistryKey<World> reg) {
        if (DimBag.isServer(null))
            return DimBag.getServer().getLevel(reg);
        World test = DimBag.getPlayer().level;
        if (test.dimension().equals(reg))
            return test;
        return null;
    }

    public static ServerWorld getWorld(MinecraftServer server, String regName) {
        if (server == null) return null;
        return server.getLevel(stringToWorldRK(regName));
    }

    public static ServerWorld getWorld(MinecraftServer server, RegistryKey<World> reg) {
        if (server == null) return null;
        return server.getLevel(reg);
    }

    public static ClientWorld getWorld() {
        return DimBag.runLogicalSide(null, ()->()-> Minecraft.getInstance().level, ()->()->null);
    }

    public static RegistryKey<World> stringToWorldRK(String str) {
        return RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(str));
    }

    public static String worldRKToString(RegistryKey<World> reg) { return reg.location().toString(); }

    public static boolean replaceBlockAndGiveBack(BlockPos pos, Block block, PlayerEntity player) {
        ItemStack prevHand = player.getItemInHand(Hand.MAIN_HAND);
        player.setItemInHand(Hand.MAIN_HAND, new ItemStack(block, 64));
        boolean res = replaceBlockAndGiveBack(pos, player, Hand.MAIN_HAND, !player.isCreative(), p->true);
        player.setItemInHand(Hand.MAIN_HAND, prevHand);
        return res;
    }

    public static boolean replaceBlockAndGiveBack(BlockPos pos, PlayerEntity player, Hand hand, boolean giveback, Predicate<BlockState> isNewStateValid) {
        World world = player.level;

        if (world.isClientSide()) return false;

        ItemStack block = player.getItemInHand(hand);

        if (!(block.getItem() instanceof BlockItem)) return false;

        Block replace = ((BlockItem)block.getItem()).getBlock();

        Direction dir = Direction.orderedByNearest(player)[0];
        Vector3d hitVec = new Vector3d((double)pos.getX() + 0.5D + (double)dir.getStepX() * 0.5D, (double)pos.getY() + 0.5D + (double)dir.getStepY() * 0.5D, (double)pos.getZ() + 0.5D + (double)dir.getStepZ() * 0.5D);
        BlockState prev = world.getBlockState(pos);

        world.setBlock(pos, Blocks.AIR.defaultBlockState(), Constants.BlockFlags.DEFAULT_AND_RERENDER);

        BlockItemUseContext use = new BlockItemUseContext(world, player, hand, block, new BlockRayTraceResult(hitVec, dir, pos, true));
        BlockState next = replace.getStateForPlacement(use);

        if (isNewStateValid == null || !isNewStateValid.test(next)) {
            world.setBlock(pos, prev, Constants.BlockFlags.DEFAULT_AND_RERENDER);
            return false;
        }

        ActionResultType ok = ((BlockItem) block.getItem()).place(use);

        if (ok.consumesAction()) { //if the place succeeded, give the previous block
            if (giveback && !(prev.getBlock() instanceof WallBlock)) //wall is an exception, we don't want players to farm wall blocks (exploit)
                player.inventory.add(new ItemStack(prev.getBlock()));
            return true;
        } else
            world.setBlock(pos, prev, Constants.BlockFlags.DEFAULT_AND_RERENDER); //else revert the change in air block of the previous wall
        return false;
    }

    private static void resetPotionEffects(LivingEntity e) {
        ArrayList<EffectInstance> l = new ArrayList<>(e.getActiveEffects());
        for (EffectInstance effect : l)
            if (effect != null) {
                e.removeEffect(effect.getEffect());
                e.addEffect(effect);
            }
    }

    public static <T extends TileEntity> List<T> getTileEntitiesWithinAABB(World world, Class<T> teClass, AxisAlignedBB aabb, @Nullable Predicate<T> pred) {
        ArrayList<T> out = new ArrayList<>();
        for (int x = (int)aabb.minX; x <= aabb.maxX; ++x)
            for (int y = (int)aabb.minY; y <= aabb.maxY; ++y)
                for (int z = (int)aabb.minZ; z <= aabb.maxZ; ++z) {
                    TileEntity te = world.getBlockEntity(new BlockPos(x, y, z));
                    if (teClass.isInstance(te) && pred.test((T)te))
                        out.add((T)te);
                }
        return out;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void fixPotionEffectsDisappearingOnDimensionChange(EntityTravelToDimensionEvent event) {
        if (event.getEntity() instanceof LivingEntity) {
            LivingEntity e = (LivingEntity) event.getEntity();
            if (!e.getActiveEffects().isEmpty())
                EventManager.delayedTask(1, () -> resetPotionEffects(e));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void preventKeepingBagWhileTeleportingInRiftDimensionPartOne(EntityTravelToDimensionEvent event) {
        Entity e = event.getEntity();
        if (WorldUtils.DimBagRiftKey.equals(event.getDimension()) && !WorldUtils.DimBagRiftKey.equals(e.level.dimension())) {
            CompoundNBT d = new CompoundNBT();
            d.putDouble("X", e.getX());
            d.putDouble("Y", e.getY());
            d.putDouble("Z", e.getZ());
            d.putString("D", worldRKToString(e.level.dimension()));
            e.getPersistentData().put("PKBDTP", d);
        }
    }
/*
    @SubscribeEvent
    public static void preventKeepingBagWhileTeleportingInRiftDimensionPartTwo(EntityEvent.EnteringChunk event) {
        Entity e = event.getEntity();
        if (WorldUtils.DimBagRiftKey.equals(e.level.dimension())) {
            BlockPos pbp;
            int e1 = 0;
            CompoundNBT PKBDTP = e.getPersistentData().getCompound("PKBDTP");
            RegistryKey<World> rw = DimBagRiftKey;
            if (!PKBDTP.isEmpty()) {
                pbp = new BlockPos(PKBDTP.getDouble("X"), PKBDTP.getDouble("Y"), PKBDTP.getDouble("Z"));
                rw = stringToWorldRK(PKBDTP.getString("D"));
                e.getPersistentData().remove("PKBDTP");
            } else {
                pbp = new BlockPos(e.xo, e.yo, e.zo);
                e1 = SubRoomsManager.getbagId(e.level, pbp, false);
            }
            int e2 = SubRoomsManager.getbagId(e.level, e.blockPosition(), false);
            if (e1 != e2) {
                DimBag.LOGGER.warn("Ya Bobo -> pos1: {}, world1: {}, eye1: {}, pos2: {}, world2: bag, eye2: {}", pbp, rw, e1, e.blockPosition(), e2); //FIXME: finish this protection against outside teleport
            }
        }
    }
*/
    private static Entity teleport(Entity entityIn, ServerWorld worldIn, double x, double y, double z, float yaw, float pitch) { //modified version of TeleportCommand.java: 123: TeleportCommand#teleport(CommandSource source, Entity entityIn, ServerWorld worldIn, double x, double y, double z, Set<SPlayerPositionLookPacket.Flags> relativeList, float yaw, float pitch, @Nullable TeleportCommand.Facing facing) throws CommandSyntaxException
        if (entityIn.removed) return entityIn;
        SyncUtils.XPSnapShot xp = entityIn instanceof ServerPlayerEntity ? new SyncUtils.XPSnapShot((PlayerEntity)entityIn) : SyncUtils.XPSnapShot.ZERO;
        Set<SPlayerPositionLookPacket.Flags> set = EnumSet.noneOf(SPlayerPositionLookPacket.Flags.class);
        set.add(SPlayerPositionLookPacket.Flags.X_ROT);
        set.add(SPlayerPositionLookPacket.Flags.Y_ROT);
        if (entityIn instanceof ServerPlayerEntity) {
            ChunkPos chunkpos = new ChunkPos(new BlockPos(x, y, z));
            worldIn.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, chunkpos, 1, entityIn.getId());
            entityIn.stopRiding();
            if (((ServerPlayerEntity)entityIn).isSleeping())
                ((ServerPlayerEntity)entityIn).stopSleepInBed(true, true);
            if (worldIn == entityIn.level)
                ((ServerPlayerEntity)entityIn).connection.teleport(x, y, z, yaw, pitch, set);
            else
                ((ServerPlayerEntity)entityIn).teleportTo(worldIn, x, y, z, yaw, pitch);
            entityIn.setYHeadRot(yaw);
        } else {
            float f1 = MathHelper.wrapDegrees(yaw);
            float f = MathHelper.wrapDegrees(pitch);
            f = MathHelper.clamp(f, -90.0F, 90.0F);
            if (worldIn == entityIn.level) {
                entityIn.moveTo(x, y, z, f1, f);
                entityIn.setYHeadRot(f1);
            } else {
                entityIn.unRide();
                Entity entity = entityIn;
                entityIn = entityIn.getType().create(worldIn);
                if (entityIn == null)
                    return entityIn;
                entityIn.restoreFrom(entity);
                entityIn.moveTo(x, y, z, f1, f);
                entityIn.setYHeadRot(f1);
                worldIn.addFromAnotherDimension(entityIn);
                entity.removed = true;
            }
        }
        if (!(entityIn instanceof LivingEntity) || !((LivingEntity)entityIn).isFallFlying()) {
            entityIn.setDeltaMovement(entityIn.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D));
            entityIn.setOnGround(true);
        }
        if (entityIn instanceof CreatureEntity) {
            ((CreatureEntity)entityIn).getNavigation().stop();
        }
        if (entityIn instanceof ServerPlayerEntity) {
            SyncUtils.resyncXP((ServerPlayerEntity) entityIn, xp);
        }
        return entityIn;
    }

    public static Entity teleportEntity(Entity entity, RegistryKey<World> destType, float x, float y, float z, float xRot, float yRot) {
        if (entity == null || entity.level.isClientSide()) return null;
        ServerWorld world;
        if (destType != null && entity.getServer() != null)
            world = entity.getServer().getLevel(destType);
        else
            world = (ServerWorld)entity.level;
        return teleport(entity, world, x, y, z, yRot, xRot);
    }

    public static Entity teleportEntity(Entity entity, RegistryKey<World> destType, BlockPos destPos, float xRot, float yRot) {
        return teleportEntity(entity, destType, destPos.getX() + 0.5f, destPos.getY(), destPos.getZ() + 0.5f, xRot, yRot);
    }

    public static Entity teleportEntity(Entity entity, RegistryKey<World> destType, BlockPos destPos) {
        return teleportEntity(entity, destType, destPos.getX() + 0.5f, destPos.getY(), destPos.getZ() + 0.5f, entity.xRot, entity.yRot);
    }

    public static Entity teleportEntity(Entity entity, RegistryKey<World> destType, Vector3d vec, float xRot, float yRot) {
        return teleportEntity(entity, destType, (float)vec.x, (float)vec.y, (float)vec.z, xRot, yRot);
    }

    public static Entity teleportEntity(Entity entity, RegistryKey<World> destType, Vector3d vec) {
        return teleportEntity(entity, destType, (float)vec.x, (float)vec.y, (float)vec.z, entity.xRot, entity.yRot);
    }

    /*
    public static Entity teleportEntity(Entity entity, RegistryKey<World> destType, double x, double y, double z) {
        if (entity == null || entity.level.isClientSide()) return entity;
        ServerWorld world;
        if (destType != null && entity.getServer() != null)
            world = entity.getServer().getLevel(destType);
        else
            world = (ServerWorld)entity.level;
        return teleport(entity, world, x, y, z, entity.yRot, entity.xRot);
    }*/

    public static void buildRoom(World world, BlockPos center, int radius) {
        BlockState wall = Registries.getBlock(WallBlock.NAME).defaultBlockState();
//        BlockState gateway = Registries.getBlock(BagGateway.NAME).defaultBlockState();

        int dx = center.getX();
        int dy = center.getY();
        int dz = center.getZ();

        if (dz == 8) { //if this is a main room (Z position 8) add the eye and 9 default pillars
//            world.setBlock(center, Registries.getBlock(TheEye.NAME).defaultBlockState(), Constants.BlockFlags.DEFAULT_AND_RERENDER);

            BlockPos pillarPos = center.offset(0, -radius + 1, 0);
            BlockItem pillar = Registries.getItem(SlotBlock.NAME);

            //FIXME: this tryplace should use a spiral system and only place the default amount of pillars/crystals/fountains
            pillar.place(new DirectionalPlaceContext(world, pillarPos, Direction.DOWN, new ItemStack(pillar), Direction.UP));
            pillar.place(new DirectionalPlaceContext(world, pillarPos.offset(1, 0, 0), Direction.DOWN, new ItemStack(pillar), Direction.UP));
            pillar.place(new DirectionalPlaceContext(world, pillarPos.offset(-1, 0, 0), Direction.DOWN, new ItemStack(pillar), Direction.UP));
            pillar.place(new DirectionalPlaceContext(world, pillarPos.offset(1, 0, 1), Direction.DOWN, new ItemStack(pillar), Direction.UP));
            pillar.place(new DirectionalPlaceContext(world, pillarPos.offset(0, 0, 1), Direction.DOWN, new ItemStack(pillar), Direction.UP));
            pillar.place(new DirectionalPlaceContext(world, pillarPos.offset(-1, 0, 1), Direction.DOWN, new ItemStack(pillar), Direction.UP));
            pillar.place(new DirectionalPlaceContext(world, pillarPos.offset(1, 0, -1), Direction.DOWN, new ItemStack(pillar), Direction.UP));
            pillar.place(new DirectionalPlaceContext(world, pillarPos.offset(0, 0, -1), Direction.DOWN, new ItemStack(pillar), Direction.UP));
            pillar.place(new DirectionalPlaceContext(world, pillarPos.offset(-1, 0, -1), Direction.DOWN, new ItemStack(pillar), Direction.UP));
        }

        for (int x = -radius/* - 1*/; x <= radius/* + 1*/; ++x)
            for (int y = -radius/* - 1*/; y <= radius/* + 1*/; ++y)
                for (int z = -radius - 1; z <= radius + 1; ++z)
//                    if (x == radius + 1 || x == -radius - 1 || y == radius + 1 || y == -radius - 1 || z == radius + 1 || z == -radius - 1)
//                        world.setBlock(new BlockPos(dx + x, dy + y, dz + z), gateway, 2);
//                    else
                        if (x == radius || x == -radius || y == radius || y == -radius || z == radius || z == -radius)
                        world.setBlock(new BlockPos(dx + x, dy + y, dz + z), wall, 2);
    }

    public static Entity getEntityByUUIDInChunk(Chunk chunk, UUID entityId) {
        if (chunk == null || entityId.equals(UUIDUtils.NULL_UUID)) return null;
        ClassInheritanceMultiMap<Entity>[] LayeredEntityList = chunk.getEntitySections();
        for (ClassInheritanceMultiMap<Entity> map : LayeredEntityList)
            for (Entity tested : map.find(Entity.class))
                if (tested.getUUID().equals(entityId))
                    return tested;
        return null;
    }

//    public static <T extends Entity> List<T> getEntitiesInRadius(World world, Vector3d pos, double radius, Class<? extends T> entities) {
//        return world.getEntities(entities, new AxisAlignedBB(pos.add(-radius, -radius, -radius), pos.add(radius, radius, radius)), e->e.getPosX() * e.getPosX() + e.getPosY() * e.getPosY() + e.getPosZ() * e.getPosZ() <= radius * radius);
//    }
}
