package com.limachi.dimensional_bags.common;

import com.limachi.dimensional_bags.DimBag;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class WorldUtils { //TODO: remove bloat once MCP/Forge mappings are better/more stable

    public static final UUID NULLID = new UUID(0, 0);

    public static final RegistryKey<World> DimBagRiftKey = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation("dim_bag:bag_rift"));

    public static ServerWorld getRiftWorld() { return DimBag.getServer() != null ? DimBag.getServer().getWorld(DimBagRiftKey) : null; }
    public static ServerWorld getOverWorld() { return DimBag.getServer() != null ? DimBag.getServer().getWorld(World.OVERWORLD) : null; }

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

    private static void teleport(Entity entityIn, ServerWorld worldIn, double x, double y, double z, float yaw, float pitch) { //modified version of TeleportCommand.java: 123: TeleportCommand#teleport(CommandSource source, Entity entityIn, ServerWorld worldIn, double x, double y, double z, Set<SPlayerPositionLookPacket.Flags> relativeList, float yaw, float pitch, @Nullable TeleportCommand.Facing facing) throws CommandSyntaxException
        if (entityIn.removed) return;
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
                    return;
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
    }

    public static void teleportEntity(Entity entity, RegistryKey<World> destType, BlockPos destPos) {
        if (entity == null || entity.world.isRemote()) return;
        ServerWorld world;
        if (destType != null)
            world = entity.getServer().getWorld(destType);
        else
            world = (ServerWorld)entity.getEntityWorld();
        teleport(entity, world, destPos.getX() + 0.5, destPos.getY(), destPos.getZ() + 0.5, entity.rotationYaw, entity.rotationPitch);
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

    public static void buildRoom(World world, BlockPos center, int radius, int prevRad) { //build a new main room or expand it by tearing down walls a adding new ones further TODO: add code to keep doors and covers on walls
        BlockState wall = Registries.WALL_BLOCK.get().getDefaultState();
        BlockState eye = Registries.BAG_EYE_BLOCK.get().getDefaultState();
        BlockState air = Blocks.AIR.getDefaultState();
        BlockState tunnel = Registries.TUNNEL_BLOCK.get().getDefaultState();
        int dx = center.getX();
        int dy = center.getY();
        int dz = center.getZ();
        if (prevRad == 0 && dz == 8) //if this is a main room (Z position 8) and this is the first construction, add the eye
            world.setBlockState(center, eye);
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
        if (chunk == null || entityId.equals(NULLID)) return null;
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
