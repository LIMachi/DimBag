package com.limachi.dimensional_bags.common.dimensions;

import com.limachi.dimensional_bags.common.blocks.BagEye;
import com.limachi.dimensional_bags.common.init.eventSubscriberForge;
import com.limachi.dimensional_bags.common.tileentity.BagEyeTileEntity;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
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
import net.minecraft.world.gen.*;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

import static com.limachi.dimensional_bags.common.init.eventSubscriberForge.DIM_TYPE_RL;

public class BagDimension extends Dimension {

//    public static DimensionType sType;

    public BagDimension(World worldIn, DimensionType type) {
        super(worldIn, type, 0.0f);
//        sType = type;
    }

    public BagDimension(World worldIn, DimensionType type, float low_light) {
        super(worldIn, type, low_light);
//        sType = type;
    }

    public static ServerWorld get(MinecraftServer server) {
        return server.getWorld(DimensionType.byName(eventSubscriberForge.DIM_TYPE_RL));
    }

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
    public BlockPos findSpawn(ChunkPos chunkPosIn, boolean checkValid) {
        return null;
    }

    @Nullable
    @Override
    public BlockPos findSpawn(int posX, int posZ, boolean checkValid) {
        return null;
    }

    @Override
    public float calculateCelestialAngle(long worldTime, float partialTicks) {
        return 0;
    }

    @Override
    public boolean isSurfaceWorld() {
        return false;
    }

    @Override
    @MethodsReturnNonnullByDefault
    @OnlyIn(Dist.CLIENT)
    public Vec3d getFogColor(float celestialAngle, float partialTicks) {
        return new Vec3d(0.0f, 0.0f, 0.0f);
    }

    @Override
    public boolean canRespawnHere() {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean doesXZShowFog(int x, int z) {
        return false;
    }

    public static void teleportPlayer(ServerPlayerEntity player, DimensionType destType, BlockPos destPos) { //should be a fast travel
        if (player.world.isRemote() || player == null || !player.isAlive()) return ;
//        player.dimension = destType;
        player.teleport(player.server.getWorld(destType), destPos.getX(), destPos.getY(), destPos.getZ(), player.rotationYaw, player.rotationPitch);
        //ServerWorld world = player.getServer().getWorld(destType);
        //world.getChunk(destPos);
        //player.teleport(world, destPos.getX(), destPos.getY(), destPos.getZ(), player.rotationYaw, player.rotationPitch);
    }

    @Nullable
    public static BagEyeTileEntity getRoomEye(MinecraftServer server, int id) {
        BlockPos pos = new BlockPos(id * 1024 + 8, 128, 8);
        World world = server.getWorld(DimensionType.byName(DIM_TYPE_RL));
        BlockState bs = world.getBlockState(pos);
        if (bs.hasTileEntity() && bs.getBlock() instanceof BagEye) {
            TileEntity eye = world.getTileEntity(pos);
            return ((BagEyeTileEntity)eye);
        }
        return (null);
    }

    /*
    //
    //change the size of a room and update the lvl of the eye
    //
    public static void updateRoom(MinecraftServer server, int id, int radius) {
        BlockState wall = Blocks.BEDROCK.getDefaultState();
        BlockState eye = BagEye.instance.getDefaultState();
        BlockState air = Blocks.AIR.getDefaultState();
        int dx = id * 1024 + 8;
        BlockPos pos;
        int radius = DimBagConfig.listOfRadius.get(lvl) + 1; //add 1 to make place for walls
        for (int x = dx - radius; x <= dx + radius; ++x)
            for (int y = 128 - radius; y <= 128 + radius; ++y)
                for (int z = 8 - radius; z <= 8 + radius; ++z) {
                    pos = new BlockPos(x, y, z);
                    if (x == dx - radius || x == dx + radius || y == 128 - radius || y == 128 + radius || z == 8 - radius || z == 8 + radius)
                        Sworld.setBlockState(pos, wall, 2);
                    else if (x == dx && y == 128 && z == 8) {
                        if (!(Sworld.getBlockState(pos).getBlock() instanceof BagEye))
                            Sworld.setBlockState(pos, eye, 2); //if the eye wasn't there (new room), create it from the base block state
                        ((BagEyeTileEntity)Sworld.getTileEntity(pos)).setLvl(lvl); //make sure that the eye has the correct lvl
                    } else if (Sworld.getBlockState(pos) == wall)
                        Sworld.setBlockState(pos, air, 2);

                }
    }
    */

    //need to rewrite
    public static int newRoom(ServerPlayerEntity player) {
//        int id = SavedData.get(player.getServer()).nextId();
//        updateRoom(id, lvl);
        return 0;
    }

    public static void teleportToRoom(ServerPlayerEntity player, int destinationId) {
        ServerWorld world = player.getServer().getWorld(DimensionType.byName(DIM_TYPE_RL));
        BagEyeTileEntity te = getRoomEye(player.server, destinationId);
        if (te == null) return;
//        te.newPTB(player); //store the current position and dimension of the player in the eye of the room
        teleportPlayer(player, DimensionType.byName(DIM_TYPE_RL), new BlockPos(destinationId * 1024 + 8, 130, 8));
    }

    public static void teleportBackFromRoom(ServerPlayerEntity player, int currentRoomID) {
        BagEyeTileEntity te = getRoomEye(player.server, currentRoomID);
        if (te == null) return; //no luck there, use vanilla command and think about the sin of destroying the eye
//        BagEyeTileEntity.PlayerTPBack PTB = te.getPTBForPlayer(player);
//        teleportPlayer(player, PTB.type, PTB.pos);
    }
}