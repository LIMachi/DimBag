package com.limachi.dimensional_bags.common.dimension;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.upgradeManager.UpgradeManager;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.provider.BiomeProviderType;
import net.minecraft.world.biome.provider.SingleBiomeProvider;
import net.minecraft.world.biome.provider.SingleBiomeProviderSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.ChunkGeneratorType;
import net.minecraft.world.gen.FlatChunkGenerator;
import net.minecraft.world.gen.FlatGenerationSettings;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ModDimension;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

public class BagRiftDimension extends Dimension {
    public static final String STR_ID = "bag_rift";
    public static final String REG_ID = MOD_ID + ":" + STR_ID;
    public static final ResourceLocation DIM_TYPE_RL = new ResourceLocation(MOD_ID, STR_ID);

    public BagRiftDimension(World world, DimensionType type) { this(world, type, 0f); }
    public BagRiftDimension(World world, DimensionType type, float low_light) { super(world, type, low_light); }

    public static DimensionType getDimensionType() { return DimensionType.byName(DIM_TYPE_RL); }

    public static World getWorld(MinecraftServer server) { return server.getWorld(DimensionType.byName(DIM_TYPE_RL)); }

    @Override
    public ChunkGenerator<?> createChunkGenerator() {
        ChunkGeneratorType<FlatGenerationSettings, FlatChunkGenerator> chunkGenerator = ChunkGeneratorType.FLAT;
        BiomeProviderType<SingleBiomeProviderSettings, SingleBiomeProvider> biomeProvider = BiomeProviderType.FIXED;
        SingleBiomeProviderSettings biome = biomeProvider.createSettings(this.world.getWorldInfo()).setBiome(Biomes.THE_VOID);
        FlatGenerationSettings generationSettings = ChunkGeneratorType.FLAT.createSettings();
        generationSettings.setBiome(Biomes.THE_VOID);
        return chunkGenerator.create(this.world, biomeProvider.create(biome), generationSettings);
    }

    @Nullable
    @Override
    public BlockPos findSpawn(ChunkPos chunkPosIn, boolean checkValid) { return null; }

    @Nullable
    @Override
    public BlockPos findSpawn(int posX, int posZ, boolean checkValid) { return null; }

    @Override
    public float calculateCelestialAngle(long worldTime, float partialTicks) {
        double d0 = MathHelper.frac(/*(double)worldTime*//*6000d*/18000D / 24000.0D - 0.25D);
        double d1 = 0.5D - Math.cos(d0 * Math.PI) / 2.0D;
        return (float)(d0 * 2.0D + d1) / 3.0F;
    }

    @Override
    public boolean canDoRainSnowIce(Chunk chunk) { return false; }

    @Override
    public boolean isSurfaceWorld() { return true; }

    @Override
    public boolean isDaytime() { return /*true*/false; }

    @Override
    public boolean hasSkyLight() { return true; }

    @Override
    @OnlyIn(Dist.CLIENT)
    public float getCloudHeight() { return 256f; }

    @Override
    @MethodsReturnNonnullByDefault
    @OnlyIn(Dist.CLIENT)
    public Vec3d getFogColor(float celestialAngle, float partialTicks) { return new Vec3d(0.0f, 0.0f, 0.0f); }

    @Override
    public boolean canRespawnHere() { return false; }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean doesXZShowFog(int x, int z) { return false; }

    public static void teleportEntity(Entity entity, DimensionType destType, BlockPos destPos) {
        if (!DimBag.isServer(entity.world)) return;
        ServerWorld world = entity.getServer().getWorld(destType);
        world.getChunk(destPos); //charge the chunk before teleport
        entity.changeDimension(destType); //change the dimension of the entity FIXME: vanilla teleporter for nether teleport, flawed, need to be rewritten for the mod
        double x = destPos.getX() + 0.5d;
        double y = destPos.getY() + 0.5d;
        double z = destPos.getZ() + 0.5d;
        if (entity instanceof ServerPlayerEntity) { //player specific way of teleporting
            ((ServerPlayerEntity)entity).connection.setPlayerLocation(x, y, z, entity.rotationYaw, entity.rotationPitch);
            ((ServerPlayerEntity)entity).connection.captureCurrentPosition();
        } else //other entities way of teleporting
            entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);
    }

    public static class BagRiftModDimension extends ModDimension {
        @Override
        public BiFunction<World, DimensionType, ? extends Dimension> getFactory() { return BagRiftDimension::new; }
    }

    public static void buildRoom(World world, BlockPos center, int radius, int prevRad) { //build a new main room or expand it by tearing down walls a adding new ones further TODO: add code to keep doors and covers on walls
        BlockState wall = Blocks.BEDROCK.getDefaultState(); //FIXME: use my own block instead of bedrock
        BlockState eye = Registries.BAG_EYE_BLOCK.get().getDefaultState();
        BlockState air = Blocks.AIR.getDefaultState();
        BlockState tunnel = Blocks.OBSIDIAN.getDefaultState(); //FIXME: use my own block for the portals
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

    /*
    public static Vec3i subRoomVirtualPos(int id) { //calculate the virtual coordinates of a sub room based on id (those AREN'T the coordinates used in world)
        if (id == 0) return new Vec3i(0, 0, 0);
        int r = 1;
        while (r < 128) { //theorical max radius could be 256 (which result in intmax as the maximum id for a room)
            int v = r * 2 - 1;
            int s = v * v; //start of this spiral part
            int q = r * 2; //size of a quarter of this part
            if (s <= id && id < s + 4 * q) {
                if (id < s + q) //first quarter, positive Z
                    return new Vec3i(r - (s + q - id), 0, r);
                else if (id < s + 2 * q) //second quarter, positive X
                    return new Vec3i(r, 0, -r + (s + 2 * q - id));
                else if (id < s + 3 * q) //third quarter, negative Z
                    return new Vec3i(-r + (s + 3 * q - id), 0, -r);
                else //fourth quarter, negative X
                    return new Vec3i(-r, 0, r - (s + 4 * q - id));
            }
            ++r;
        }
        return null;
    }

    public static int subRoomId(Vec3i pos) {
        if (pos.getX() == 0 && pos.getZ() == 0) return 0;
        int r = Math.max(Math.abs(pos.getX()), Math.abs(pos.getZ()));
        int v = r * 2 - 1;
        int s = v * v;
        int q = r * 2;
        int x = pos.getX() + r; //moved from the range [-r, r] to [0, q]
        int z = pos.getZ() + r; //moved from the range [-r, r] to [0, q]
        if (z == q) //first quarter, positive Z
            return (s + x);
        else if (x == q) //second quarter, positive X
            return (s + q * 2 - z);
        else if (z == 0) //third quarter, negative Z
            return (s + 3 * q - x);
        return (s + 3 * q + z); //fourth quarter, negative X
    }

    public static BlockPos roomCenter(int eyeId, int roomId) { //get the actual center of a room based on his eye id and sub room id, return null on invalid id
        if (eyeId <= 0 || roomId < 0) return null;
        int x;
        if (((eyeId - 1) & 1) == 0) //even id
            x = ((eyeId - 1) >> 1) << 10;
        else
            x = -(eyeId >> 1) << 10;
        int z;
        if ((roomId & 1) == 0) //even id
            z = (roomId >> 1) << 10;
        else
            z = -((roomId + 1) >> 1) << 10;
        return new BlockPos(x + 8, 128, z + 8);
    }
    */
    ///new idea: store the id of a room in the form EYEID(2 bytes)POSX(1 byte)POSZ(1 byte) in a single int (4bytes)
}
