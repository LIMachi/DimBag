package com.limachi.dimensional_bags.common.items.upgrades.bag;

import com.limachi.dimensional_bags.ConfigManager.Config;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.EventManager;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.EnergyData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SettingsData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.limachi.dimensional_bags.common.items.upgrades.BaseUpgradeBag;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import com.limachi.dimensional_bags.common.tileentities.BagProxyTileEntity;
import com.limachi.dimensional_bags.common.tileentities.IisBagTE;
import com.limachi.dimensional_bags.utils.WorldUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.tileentity.ConduitTileEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@StaticInit
public class TimeDistortionUpgrade extends BaseUpgradeBag<TimeDistortionUpgrade> {

    public static final String NAME = "time_distortion_upgrade";

    @Config(cmt = "can this upgrade be installed")
    public static boolean ACTIVE = true;

    @Config(cmt = "the time at will be distorted in the range 2^[-MAX_FACTOR, MAX_FACTOR] (0 = vanilla time, 6 = 64x the vanilla time, -6 = 1/64x vanilla time)")
    public static int MAX_FACTOR = 6;

    @Config(cmt = "how much energy should be used per extra tick and per TE and Entity", min = "0")
    public static int ENERGY_CONSUMPTION = 16;

    @Config(cmt = "how much energy should be created per skipped tick for each TE and Entity", min = "0")
    public static int ENERGY_PRODUCTION = 16;

    @Config(cmt = "blacklist those Tile Entities from being manipulated by this upgrade (ResourceLocation regex style notation, 'dim_bag:bag_proxy' would ignore any \"Dimensional Bag Proxy\", 'mekanism:.*' would ignore any mekanism machine), please include the default blacklist (to prevent crashes with other mods)")
    public static String[] BLACK_LIST_TE = {"mininggadgets:renderblock", "mininggadgets:modificationtable"};

    @Config(cmt = "blacklist those Entities from being manipulated by this upgrade (ResourceLocation regex style notation, 'dim_bag:bag_entity' would ignore any bag in entity form, 'minecraft:.*' would ignore any vanilla mobs), please include the default blacklist (to prevent crashes with other mods)")
    public static String[] BLACK_LIST_ENTITY = {};

    static {
        UpgradeManager.registerUpgrade(NAME, TimeDistortionUpgrade::new);
    }

    @Override
    public void initSettings(SettingsData.SettingsReader settingsReader) {
        settingsReader.integer("factor", 0, -MAX_FACTOR, MAX_FACTOR, true);
    }

    public TimeDistortionUpgrade() { super(DimBag.DEFAULT_PROPERTIES); }

    @Override
    public boolean canBeInstalled() { return ACTIVE; }

    @Override
    public String upgradeName() { return NAME; }

    public static boolean isValidByList(@Nullable ResourceLocation toTest, String[] against, boolean isWhiteList, boolean testPath) {
        if (toTest == null) return false;
        String test = toTest.toString();
        for (String a : against)
            if (test.matches(a))
                return isWhiteList;
        if (testPath) {
            test = toTest.getPath();
            for (String a : against)
                if (test.matches(a))
                    return isWhiteList;
        }
        return !isWhiteList;
    }

    public static boolean isValidByList(@Nullable ResourceLocation toTest, Iterable<String> against, boolean isWhiteList, boolean testPath) {
        if (toTest == null) return false;
        String test = toTest.toString();
        for (String a : against)
            if (test.matches(a))
                return isWhiteList;
        if (testPath) {
            test = toTest.getPath();
            for (String a : against)
                if (test.matches(a))
                    return isWhiteList;
        }
        return !isWhiteList;
    }

    public static boolean filterTE(int eye, TileEntity te) {
        return !(te instanceof IisBagTE || te instanceof BeaconTileEntity || te instanceof ConduitTileEntity || te instanceof BagProxyTileEntity) && (te.getBlockPos().getX() - SubRoomsManager.ROOM_OFFSET_X + SubRoomsManager.HALF_ROOM) / SubRoomsManager.ROOM_SPACING + 1 == eye && isValidByList(te.getType().getRegistryName(), BLACK_LIST_TE, false, false);
    }

    public static boolean filterEntity(int eye, Entity e) {
        return !(e instanceof ItemEntity || e instanceof PlayerEntity || e instanceof BagEntity || e instanceof FallingBlockEntity) && isValidByList(e.getType().getRegistryName(), BLACK_LIST_ENTITY, false, false);
    }

    /**
     * this a broad removal of TE (does not check if the TE is inside a room, only it's X coordinate)
     */
    public static void removeTickingTEInBag(int eye, World world) {
        world.tickableBlockEntities.removeIf(te->filterTE(eye, te));
    }

    public static void addTickingTEInBag(int eye, World world) {
        world.tickableBlockEntities.addAll(iterTickingTEInBag(eye, world).filter(t->!world.tickableBlockEntities.contains(t)).collect(Collectors.toList()));
    }

    /**
     * expect TE to already have been removed from the ticking list (search by iterating on world entities, broad search, only X coordinate)
     */
    public static Stream<TileEntity> iterTickingTEInBag(int eye, World world) {
        return world.blockEntityList.stream().filter(te->te instanceof ITickableTileEntity && filterTE(eye, te));
    }

    public static void forEachEntityInBag(int eye, World world, Consumer<Entity> run) {
        SubRoomsManager.getRoomsAABB(eye).forEach(aabb->world.getEntities(null, aabb).forEach(e->{if (filterEntity(eye, e)) run.accept(e);}));
    }

    /**
     * derived from ServerWorld#tickChunk(Chunk, int):498
     */

    public static void randomTickBagBlocks(int eye, ServerWorld world, int iterations) {
        for (ChunkPos chunkPos : SubRoomsManager.execute(eye, SubRoomsManager::getComposingChunks, new HashSet<ChunkPos>())) {
            int chunkX = chunkPos.getMinBlockX();
            int chunkZ = chunkPos.getMinBlockZ();
            for (ChunkSection chunkSection : world.getChunk(chunkPos.x, chunkPos.z).getSections())
                if (chunkSection != Chunk.EMPTY_SECTION && chunkSection.isRandomlyTicking()) {
                    int y = chunkSection.bottomBlockY();
                    for (int i = 0; i < iterations; ++i) {
                        BlockPos blockPos = world.getBlockRandomPos(chunkX, y, chunkZ, 15);
                        BlockState blockstate = chunkSection.getBlockState(blockPos.getX() - chunkX, blockPos.getY() - y, blockPos.getZ() - chunkZ);
                        if (blockstate.isRandomlyTicking()) {
                            blockstate.randomTick(world, blockPos, world.random);
                        }
                        FluidState fluidstate = blockstate.getFluidState();
                        if (fluidstate.isRandomlyTicking()) {
                            fluidstate.randomTick(world, blockPos, world.random);
                        }
                    }
                }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        if (stack.getItem() instanceof BaseUpgradeBag && ((BaseUpgradeBag)stack.getItem()).canBeInstalled())
            tooltip.add(new TranslationTextComponent("tooltip.upgrades.time_distortion_upgrade.warning").withStyle(TextFormatting.RED));
    }

    /**
     * note: random tick speed is per world, but we can mess with it a bit (at least we can increase it by calling some functions, reducing it will be harder)
     * if upgrades keep getting weirder, it might be a good idea to have a world per bag, or just go back and remove the concept of subrooms completely
     * @return
     */

    @Override
    public ActionResultType upgradeEntityTick(int eyeId, World worldIn, Entity entity) {
        final World world = worldIn.dimension() == WorldUtils.DimBagRiftKey ? worldIn : WorldUtils.getRiftWorld();
        if (world == null) return ActionResultType.PASS;
        TimeDistortionUpgrade upgrade = getInstance(NAME);
        if (!upgrade.isActive(eyeId)) return ActionResultType.PASS;
        int factor = upgrade.getSetting(eyeId, "factor");
        if (factor < 0) {
            removeTickingTEInBag(eyeId, world);
            forEachEntityInBag(eyeId, world, e->e.canUpdate(false));
        }
        else {
            addTickingTEInBag(eyeId, world);
            forEachEntityInBag(eyeId, world, e->e.canUpdate(true));
        }
        if (factor >= 0 || EventManager.tick % Math.pow(2, -factor) == 0) {
            final int mi = factor > 0 ? (int)Math.pow(2, factor) : 1;
            iterTickingTEInBag(eyeId, world).forEach(te->{
                for (int i = 0; i < mi; ++i) {
                    if (factor >= 0 && (i == 0 || EnergyData.extractEnergy(eyeId, ENERGY_CONSUMPTION) != ENERGY_CONSUMPTION)) continue; //only consume energy if the factor is positive, and skip the first tick (has it will/was handled by the vanilla tick)
                    ((ITickableTileEntity) te).tick();
                }
            });
            forEachEntityInBag(eyeId, world, e->{
                for (int i = 0; i < mi; ++i) {
                    if (factor >= 0 && (i == 0 || EnergyData.extractEnergy(eyeId, ENERGY_CONSUMPTION) != ENERGY_CONSUMPTION)) continue; //only consume energy if the factor is positive, and skip the first tick (has it will/was handled by the vanilla tick)
                    e.tick();
                }
            });
            int randomTick = world.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
            if (randomTick > 0 && world instanceof ServerWorld)
                randomTickBagBlocks(eyeId, (ServerWorld)world, randomTick * (mi - 1) * 4);
        } else {
            int[] ec = {0};
            forEachEntityInBag(eyeId, world, e->++ec[0]);
            EnergyData.receiveEnergy(eyeId, (int) ((Math.pow(2, -factor) - 1) * ENERGY_PRODUCTION * (iterTickingTEInBag(eyeId, world).count() + ec[0])));
        }
        return ActionResultType.SUCCESS;
    }
}
