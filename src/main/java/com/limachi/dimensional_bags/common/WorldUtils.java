package com.limachi.dimensional_bags.common;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.data.EyeData;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.ITeleporter;

import java.util.function.Function;

public class WorldUtils { //TODO: remove bloat once MCP/Forge mappings are better/more stable

    public static final RegistryKey<World> DimBagRiftKey = RegistryKey.func_240903_a_(Registry.WORLD_KEY, new ResourceLocation("dim_bag:bag_rift"));
    public static final RegistryKey<World> DimOverworldKey = World.field_234918_g_; //TODO: replace field mapping

    public static ServerWorld getRiftWorld() { return DimBag.getServer(null).getWorld(DimBagRiftKey); }
    public static ServerWorld getOverWorld() { return DimBag.getServer(null).getWorld(DimOverworldKey); }

    public static ServerWorld getWorld(MinecraftServer server, String regName) {
        return server.getWorld(stringToWorldRK(regName));
    }

    public static RegistryKey<World> worldRKFromWorld(World world) { //TODO: replace this once the mappings are good
        return world.func_234923_W_();
    }

    public static RegistryKey<World> stringToWorldRK(String str) {
        return RegistryKey.func_240903_a_(Registry.WORLD_KEY, new ResourceLocation(str));
    }

    public static String worldRKToString(RegistryKey<World> reg) {
        return reg.func_240901_a_().toString();
    }

    public static void teleportEntity(Entity entity, RegistryKey<World> destType, BlockPos destPos) {
        if (!DimBag.isServer(entity.world)) return;
        ServerWorld world = entity.getServer().getWorld(destType);
        int destId = EyeData.getEyeId(world, destPos);
//        if (destId != 0 && EyeData.getEyeId(entity.world, entity.getPosition()) == destId) return; //invalidate the teleport if the entity is already in the destination room
        world.getChunk(destPos); //charge the chunk before teleport
        entity.changeDimension(/*destType*/world, new ITeleporter() { //vanilla entity teleporter between dimensions, repositionement of entity must be done after this call since it will divide by 8 the position of the entity (vanilla nether portal)
            @Override
            public Entity placeEntity(Entity entity, ServerWorld currentWorld, ServerWorld destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
                return repositionEntity.apply(false); //just run the default teleporter (the nether one) and prevent the spawn of a nether portal
            }
        });
        double x = destPos.getX() + 0.5d;
        double y = destPos.getY() + 0.5d;
        double z = destPos.getZ() + 0.5d;
        if (entity instanceof ServerPlayerEntity) { //player specific way of teleporting
            ((ServerPlayerEntity)entity).connection.setPlayerLocation(x, y, z, entity.rotationYaw, entity.rotationPitch);
            ((ServerPlayerEntity)entity).connection.captureCurrentPosition();
        } else //other entities way of teleporting
            entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);
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
}
