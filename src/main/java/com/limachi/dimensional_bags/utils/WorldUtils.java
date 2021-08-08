package com.limachi.dimensional_bags.utils;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.EventManager;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.blocks.*;
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
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

@Mod.EventBusSubscriber
public class WorldUtils { //TODO: remove bloat once MCP/Forge mappings are better/more stable

    public static final RegistryKey<World> DimBagRiftKey = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation("dim_bag:bag_rift"));

    public static @Nullable World getRiftWorld() { return getWorld(DimBagRiftKey); }
    public static @Nullable World getOverWorld() { return getWorld(World.OVERWORLD); }

    public static Iterable<ServerWorld> getAllWorlds() { return DimBag.getServer() != null ? DimBag.getServer().getWorlds() : null; }

    public static World getWorld(RegistryKey<World> reg) {
        if (DimBag.isServer(null))
            return DimBag.getServer().getWorld(reg);
        World test = DimBag.getPlayer().world;
        if (test.getDimensionKey().equals(reg))
            return test;
        return null;
    }

    public static ServerWorld getWorld(MinecraftServer server, String regName) {
        if (server == null) return null;
        return server.getWorld(stringToWorldRK(regName));
    }

    public static ServerWorld getWorld(MinecraftServer server, RegistryKey<World> reg) {
        if (server == null) return null;
        return server.getWorld(reg);
    }

    public static ClientWorld getWorld() {
        return DimBag.runLogicalSide(null, ()->()-> Minecraft.getInstance().world, ()->()->null);
    }

    public static RegistryKey<World> stringToWorldRK(String str) {
        return RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(str));
    }

    public static String worldRKToString(RegistryKey<World> reg) {
        return reg.getLocation().toString();
    }

    public static boolean replaceBlockAndGiveBack(BlockPos pos, Block block, PlayerEntity player) {
        ItemStack prevHand = player.getHeldItem(Hand.MAIN_HAND);
        player.setHeldItem(Hand.MAIN_HAND, new ItemStack(block, 64));
        boolean res = replaceBlockAndGiveBack(pos, player, Hand.MAIN_HAND, !player.isCreative(), p->true);
        player.setHeldItem(Hand.MAIN_HAND, prevHand);
        return res;
    }

    public static boolean replaceBlockAndGiveBack(BlockPos pos, PlayerEntity player, Hand hand, boolean giveback, Predicate<BlockState> isNewStateValid) {
        World world = player.world;

        if (world.isRemote()) return false;

        ItemStack block = player.getHeldItem(hand);

        if (!(block.getItem() instanceof BlockItem)) return false;

        Block replace = ((BlockItem)block.getItem()).getBlock();

        Direction dir = Direction.getFacingDirections(player)[0];
        Vector3d hitVec = new Vector3d((double)pos.getX() + 0.5D + (double)dir.getXOffset() * 0.5D, (double)pos.getY() + 0.5D + (double)dir.getYOffset() * 0.5D, (double)pos.getZ() + 0.5D + (double)dir.getZOffset() * 0.5D);
        BlockState prev = world.getBlockState(pos);

        world.setBlockState(pos, Blocks.AIR.getDefaultState());

        BlockItemUseContext use = new BlockItemUseContext(world, player, hand, block, new BlockRayTraceResult(hitVec, dir, pos, true));
        BlockState next = replace.getStateForPlacement(use);

        if (isNewStateValid == null || !isNewStateValid.test(next)) {
            world.setBlockState(pos, prev);
            return false;
        }

        ActionResultType ok = ((BlockItem) block.getItem()).tryPlace(use);

        if (ok.isSuccessOrConsume()) { //if the place succeeded, give the previous block
            if (giveback && !(prev.getBlock() instanceof Wall)) //wall is an exception, we don't want players to farm wall blocks (exploit)
                player.inventory.addItemStackToInventory(new ItemStack(prev.getBlock()));
            return true;
        } else
            world.setBlockState(pos, prev); //else revert the change in air block of the previous wall
        return false;
    }

    private static void resetPotionEffects(LivingEntity e) {
        ArrayList<EffectInstance> l = new ArrayList<>(e.getActivePotionEffects());
        for (EffectInstance effect : l)
            if (effect != null) {
                e.removePotionEffect(effect.getPotion());
                e.addPotionEffect(effect);
            }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void fixPotionEffectsDisappearingOnDimensionChange(EntityTravelToDimensionEvent event) {
        if (event.getEntity() instanceof LivingEntity) {
            LivingEntity e = (LivingEntity) event.getEntity();
            if (!e.getActivePotionEffects().isEmpty())
                EventManager.delayedTask(1, () -> resetPotionEffects(e));
        }
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

    public static void buildRoom(World world, BlockPos center, int radius) {
        BlockState wall = Registries.getBlock(Wall.NAME).getDefaultState();
        BlockState gateway = Registries.getBlock(BagGateway.NAME).getDefaultState();

        int dx = center.getX();
        int dy = center.getY();
        int dz = center.getZ();

        if (dz == 8) { //if this is a main room (Z position 8) add the eye and 9 default pillars
            world.setBlockState(center, Registries.getBlock(TheEye.NAME).getDefaultState());

            BlockPos pillarPos = center.add(0, -radius + 1, 0);
            BlockItem pillar = Registries.getItem(Pillar.NAME);

            //FIXME: this tryplace should use a spiral system and only place the default amount of pillars/crystals/fountains
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

        for (int x = -radius - 1; x <= radius + 1; ++x)
            for (int y = -radius - 1; y <= radius + 1; ++y)
                for (int z = -radius - 1; z <= radius + 1; ++z)
                    if (x == radius + 1 || x == -radius - 1 || y == radius + 1 || y == -radius - 1 || z == radius + 1 || z == -radius - 1)
                        world.setBlockState(new BlockPos(dx + x, dy + y, dz + z), gateway, 2);
                    else if (x == radius || x == -radius || y == radius || y == -radius || z == radius || z == -radius)
                        world.setBlockState(new BlockPos(dx + x, dy + y, dz + z), wall, 2);
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
