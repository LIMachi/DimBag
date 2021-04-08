package com.limachi.dimensional_bags.utils;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.blocks.Pillar;
import com.limachi.dimensional_bags.common.blocks.TheEye;
import com.limachi.dimensional_bags.common.blocks.Tunnel;
import com.limachi.dimensional_bags.common.blocks.Wall;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DirectionalPlaceContext;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;

import java.util.*;

public class WorldUtils { //TODO: remove bloat once MCP/Forge mappings are better/more stable

    public static final RegistryKey<World> DimBagRiftKey = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation("dim_bag:bag_rift"));

    public static ServerWorld getRiftWorld() { return DimBag.getServer() != null ? DimBag.getServer().getWorld(DimBagRiftKey) : null; }
    public static ServerWorld getOverWorld() { return DimBag.getServer() != null ? DimBag.getServer().getWorld(World.OVERWORLD) : null; }

    public static Iterable<ServerWorld> getAllWorlds() { return DimBag.getServer() != null ? DimBag.getServer().getWorlds() : null; }

    public static ServerWorld getWorld(MinecraftServer server, String regName) {
        if (server == null) return null;
        return server.getWorld(stringToWorldRK(regName));
    }

    public static ServerWorld getWorld(MinecraftServer server, RegistryKey<World> reg) {
        if (server == null) return null;
        return server.getWorld(reg);
    }

    public static RegistryKey<World> stringToWorldRK(String str) {
        return RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(str));
    }

    public static String worldRKToString(RegistryKey<World> reg) {
        return reg.getLocation().toString();
    }

    private static Entity teleport(Entity entityIn, ServerWorld worldIn, double x, double y, double z, float yaw, float pitch) { //modified version of TeleportCommand.java: 123: TeleportCommand#teleport(CommandSource source, Entity entityIn, ServerWorld worldIn, double x, double y, double z, Set<SPlayerPositionLookPacket.Flags> relativeList, float yaw, float pitch, @Nullable TeleportCommand.Facing facing) throws CommandSyntaxException
        if (entityIn.removed) return entityIn;
        SyncUtils.XPSnapShot xp = entityIn instanceof ServerPlayerEntity ? new SyncUtils.XPSnapShot(((PlayerEntity)entityIn).experience, ((PlayerEntity)entityIn).experienceLevel, ((PlayerEntity)entityIn).experienceTotal) : SyncUtils.XPSnapShot.ZERO;
        Set<SPlayerPositionLookPacket.Flags> set = EnumSet.noneOf(SPlayerPositionLookPacket.Flags.class);
        set.add(SPlayerPositionLookPacket.Flags.X_ROT);
        set.add(SPlayerPositionLookPacket.Flags.Y_ROT);
        if (entityIn instanceof ServerPlayerEntity) {
            ChunkPos chunkpos = new ChunkPos(new BlockPos(x, y, z));
            worldIn.getChunkProvider().registerTicket(TicketType.POST_TELEPORT, chunkpos, 1, entityIn.getEntityId());
            entityIn.stopRiding();
            if (((ServerPlayerEntity)entityIn).isSleeping())
                ((ServerPlayerEntity)entityIn).stopSleepInBed(true, true);
            if (worldIn == entityIn.world)
                ((ServerPlayerEntity)entityIn).connection.setPlayerLocation(x, y, z, yaw, pitch, set);
            else
                ((ServerPlayerEntity)entityIn).teleport(worldIn, x, y, z, yaw, pitch);
            entityIn.setRotationYawHead(yaw);
        } else {
            float f1 = MathHelper.wrapDegrees(yaw);
            float f = MathHelper.wrapDegrees(pitch);
            f = MathHelper.clamp(f, -90.0F, 90.0F);
            if (worldIn == entityIn.world) {
                entityIn.setLocationAndAngles(x, y, z, f1, f);
                entityIn.setRotationYawHead(f1);
            } else {
                entityIn.detach();
                Entity entity = entityIn;
                entityIn = entityIn.getType().create(worldIn);
                if (entityIn == null)
                    return entityIn;
                entityIn.copyDataFromOld(entity);
                entityIn.setLocationAndAngles(x, y, z, f1, f);
                entityIn.setRotationYawHead(f1);
                worldIn.addFromAnotherDimension(entityIn);
                entity.removed = true;
            }
        }
        if (!(entityIn instanceof LivingEntity) || !((LivingEntity)entityIn).isElytraFlying()) {
            entityIn.setMotion(entityIn.getMotion().mul(1.0D, 0.0D, 1.0D));
            entityIn.setOnGround(true);
        }
        if (entityIn instanceof CreatureEntity) {
            ((CreatureEntity)entityIn).getNavigator().clearPath();
        }
        if (entityIn instanceof ServerPlayerEntity) {
            SyncUtils.resyncXP((ServerPlayerEntity) entityIn, xp);
        }
        return entityIn;
    }

    public static Entity teleportEntity(Entity entity, RegistryKey<World> destType, BlockPos destPos) {
        if (entity == null || entity.world.isRemote()) return null;
        ServerWorld world;
        if (destType != null)
            world = entity.getServer().getWorld(destType);
        else
            world = (ServerWorld)entity.getEntityWorld();
        return teleport(entity, world, destPos.getX() + 0.5, destPos.getY(), destPos.getZ() + 0.5, entity.rotationYaw, entity.rotationPitch);
    }

    public static void teleportEntity(Entity entity, RegistryKey<World> destType, Vector3d vec) {
        teleportEntity(entity, destType, vec.x, vec.y, vec.z);
    }

    public static void teleportEntity(Entity entity, RegistryKey<World> destType, double x, double y, double z) {
        if (entity == null || entity.world.isRemote()) return;
        ServerWorld world;
        if (destType != null)
            world = entity.getServer().getWorld(destType);
        else
            world = (ServerWorld)entity.getEntityWorld();
        teleport(entity, world, x, y, z, entity.rotationYaw, entity.rotationPitch);
    }

    private static void pushWallRec(World world, Direction dir, BlockPos pos, BlockState bs_wall, BlockState bs_tunnel, BlockState bs_air, List<Vector3i> rec, List<BlockPos> rebuildWalls) {
        BlockState bs = world.getBlockState(pos);
        if (bs == bs_tunnel || bs == bs_wall) {
            world.setBlockState(pos.add(dir.getDirectionVec()), bs == bs_tunnel ? bs_tunnel : bs_wall, 10);
            BlockState bb = world.getBlockState(pos.subtract(dir.getDirectionVec()));
            if (bb == bs_tunnel || bb == bs_wall)
                rebuildWalls.add(pos);
            world.setBlockState(pos, bs_air, 10);
            for (Vector3i delta : rec)
                pushWallRec(world, dir, pos.add(delta), bs_wall, bs_tunnel, bs_air, rec, rebuildWalls);
        }
    }

    /**
     * use a flood fill system to push a wall and rebuild his edges
     */
    public static void pushWall(World world, BlockPos start, Direction dir) {
        BlockState wall = Registries.getBlock(Wall.NAME).getDefaultState();
        BlockState air = Blocks.AIR.getDefaultState();
        BlockState tunnel = Registries.getBlock(Tunnel.NAME).getDefaultState();

        ArrayList<Vector3i> rec = new ArrayList<>();
        Direction.Axis doNotInclude = dir.getAxis();
        if (doNotInclude != Direction.Axis.X) {
            rec.add(new Vector3i(1, 0, 0));
            rec.add(new Vector3i(-1, 0, 0));
        }
        if (doNotInclude != Direction.Axis.Y) {
            rec.add(new Vector3i(0, 1, 0));
            rec.add(new Vector3i(0, -1, 0));
        }
        if (doNotInclude != Direction.Axis.Z) {
            rec.add(new Vector3i(0, 0, 1));
            rec.add(new Vector3i(0, 0, -1));
        }
        ArrayList<BlockPos> rebuildWalls = new ArrayList<>();
        pushWallRec(world, dir, start, wall, tunnel, air, rec, rebuildWalls);
        for (BlockPos newWall : rebuildWalls)
            world.setBlockState(newWall, wall, 10);
    }

    public static void buildRoom(World world, BlockPos center, int radius, int prevRad) { //build a new main room or expand it by tearing down walls a adding new ones further TODO: add code to keep doors and covers on walls
        BlockState wall = Registries.getBlock(Wall.NAME).getDefaultState();
        BlockState eye = Registries.getBlock(TheEye.NAME).getDefaultState();
        BlockState air = Blocks.AIR.getDefaultState();
        BlockState tunnel = Registries.getBlock(Tunnel.NAME).getDefaultState();
        BlockItem pillar = Registries.getItem(Pillar.NAME);
        int dx = center.getX();
        int dy = center.getY();
        int dz = center.getZ();
        if (prevRad == 0 && dz == 8) {//if this is a main room (Z position 8) and this is the first construction, add the eye and 9 default pillars
            world.setBlockState(center, eye);
            BlockPos pillarPos = center.add(0, -radius + 1, 0);
            pillar.tryPlace(new DirectionalPlaceContext(world, pillarPos, Direction.DOWN, new ItemStack(pillar), Direction.UP));
            pillar.tryPlace(new DirectionalPlaceContext(world, pillarPos.add(1, 0, 0), Direction.DOWN, new ItemStack(pillar), Direction.UP));
            pillar.tryPlace(new DirectionalPlaceContext(world, pillarPos.add(-1, 0, 0), Direction.DOWN, new ItemStack(pillar), Direction.UP));
            pillar.tryPlace(new DirectionalPlaceContext(world, pillarPos.add(1, 0, 1), Direction.DOWN, new ItemStack(pillar), Direction.UP));
            pillar.tryPlace(new DirectionalPlaceContext(world, pillarPos.add(0, 0, 1), Direction.DOWN, new ItemStack(pillar), Direction.UP));
            pillar.tryPlace(new DirectionalPlaceContext(world, pillarPos.add(-1, 0, 1), Direction.DOWN, new ItemStack(pillar), Direction.UP));
            pillar.tryPlace(new DirectionalPlaceContext(world, pillarPos.add(1, 0, -1), Direction.DOWN, new ItemStack(pillar), Direction.UP));
            pillar.tryPlace(new DirectionalPlaceContext(world, pillarPos.add(0, 0, -1), Direction.DOWN, new ItemStack(pillar), Direction.UP));
            pillar.tryPlace(new DirectionalPlaceContext(world, pillarPos.add(-1, 0, -1), Direction.DOWN, new ItemStack(pillar), Direction.UP));
        }
        for (int i = -radius; i <= radius; ++i)
            for (int j = -radius; j <= radius; ++j) {
                if (prevRad != 0 && i >= -prevRad && i <= prevRad && j >= -prevRad && j <= prevRad) {
                    if (world.getBlockState(new BlockPos(dx + i, dy + j, dz + prevRad)) == tunnel)
                        world.setBlockState(new BlockPos(dx + i, dy + j, dz + radius), tunnel, 2);
                    else
                        world.setBlockState(new BlockPos(dx + i, dy + j, dz + radius), wall, 2);
                    if (world.getBlockState(new BlockPos(dx + i, dy + j, dz - prevRad)) == tunnel)
                        world.setBlockState(new BlockPos(dx + i, dy + j, dz - radius), tunnel, 2);
                    else
                        world.setBlockState(new BlockPos(dx + i, dy + j, dz - radius), wall, 2);
                    if (world.getBlockState(new BlockPos(dx + prevRad, dy + i, dz + j)) == tunnel)
                        world.setBlockState(new BlockPos(dx + radius, dy + i, dz + j), tunnel, 2);
                    else
                        world.setBlockState(new BlockPos(dx + radius, dy + i, dz + j), wall, 2);
                    if (world.getBlockState(new BlockPos(dx - prevRad, dy + i, dz + j)) == tunnel)
                        world.setBlockState(new BlockPos(dx - radius, dy + i, dz + j), tunnel, 2);
                    else
                        world.setBlockState(new BlockPos(dx - radius, dy + i, dz + j), wall, 2);
                    if (world.getBlockState(new BlockPos(dx + i, dy + prevRad, dz + j)) == tunnel)
                        world.setBlockState(new BlockPos(dx + i, dy + radius, dz + j), tunnel, 2);
                    else
                        world.setBlockState(new BlockPos(dx + i, dy + radius, dz + j), wall, 2);
                    if (world.getBlockState(new BlockPos(dx + i, dy - prevRad, dz + j)) == tunnel)
                        world.setBlockState(new BlockPos(dx + i, dy - radius, dz + j), tunnel, 2);
                    else
                        world.setBlockState(new BlockPos(dx + i, dy - radius, dz + j), wall, 2);
                }
                else {
                    world.setBlockState(new BlockPos(dx + i, dy + j, dz + radius), wall, 2);
                    world.setBlockState(new BlockPos(dx + i, dy + j, dz - radius), wall, 2);
                    world.setBlockState(new BlockPos(dx + radius, dy + i, dz + j), wall, 2);
                    world.setBlockState(new BlockPos(dx - radius, dy + i, dz + j), wall, 2);
                    world.setBlockState(new BlockPos(dx + i, dy + radius, dz + j), wall, 2);
                    world.setBlockState(new BlockPos(dx + i, dy - radius, dz + j), wall, 2);
                }
            }
        if (prevRad != 0) //if this is an upgrade from a previous room, tear down the hold walls
            for (int i = -prevRad; i <= prevRad; ++i)
                for (int j = -prevRad; j <= prevRad; ++j) {
                    world.setBlockState(new BlockPos(dx + i, dy + j, dz + prevRad), air, 2);
                    world.setBlockState(new BlockPos(dx + i, dy + j, dz - prevRad), air, 2);
                    world.setBlockState(new BlockPos(dx + prevRad, dy + i, dz + j), air, 2);
                    world.setBlockState(new BlockPos(dx - prevRad, dy + i, dz + j), air, 2);
                    world.setBlockState(new BlockPos(dx + i, dy - prevRad, dz + j), air, 2);
                    world.setBlockState(new BlockPos(dx + i, dy + prevRad, dz + j), air, 2);
                }
    }

    public static Entity getEntityByUUIDInChunk(Chunk chunk, UUID entityId) {
        if (chunk == null || entityId.equals(UUIDUtils.NULL_UUID)) return null;
        ClassInheritanceMultiMap<Entity>[] LayeredEntityList = chunk.getEntityLists();
        for (ClassInheritanceMultiMap<Entity> map : LayeredEntityList)
            for (Entity tested : map.getByClass(Entity.class))
                if (tested.getUniqueID().equals(entityId))
                    return tested;
        return null;
    }

    public static <T extends Entity> List<T> getEntitiesInRadius(World world, Vector3d pos, double radius, Class<? extends T> entities) {
        return world.getEntitiesWithinAABB(entities, new AxisAlignedBB(pos.add(-radius, -radius, -radius), pos.add(radius, radius, radius)), e->e.getPosX() * e.getPosX() + e.getPosY() * e.getPosY() + e.getPosZ() * e.getPosZ() <= radius * radius);
    }
}
