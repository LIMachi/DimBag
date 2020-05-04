package com.limachi.dimensional_bags.common.dimension;

import com.limachi.dimensional_bags.DimBag;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.provider.BiomeProviderType;
import net.minecraft.world.biome.provider.SingleBiomeProvider;
import net.minecraft.world.biome.provider.SingleBiomeProviderSettings;
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
import net.minecraftforge.fml.common.Mod;

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
    public float calculateCelestialAngle(long worldTime, float partialTicks) { return 0; }

    @Override
    public boolean isSurfaceWorld() { return false; }

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
        entity.dimension = destType;
        double x = destPos.getX() + 0.5d;
        double y = destPos.getY() + 0.5d;
        double z = destPos.getZ() + 0.5d;
        entity.setPositionAndUpdate(x, y, z);
    }

    public static class BagRiftModDimension extends ModDimension {
        @Override
        public BiFunction<World, DimensionType, ? extends Dimension> getFactory() { return BagRiftDimension::new; }
    }
}
