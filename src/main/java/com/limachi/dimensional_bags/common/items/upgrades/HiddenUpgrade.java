package com.limachi.dimensional_bags.common.items.upgrades;

import com.limachi.dimensional_bags.ConfigManager;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.EventManager;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SettingsData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import com.limachi.dimensional_bags.utils.WorldUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.tileentity.ConduitTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@StaticInit
public class HiddenUpgrade extends BaseUpgrade<HiddenUpgrade> {

    public static final String NAME = "hidden_upgrade";

    static {
        UpgradeManager.registerUpgrade(NAME, HiddenUpgrade::new);
    }

    public HiddenUpgrade() { super(DimBag.DEFAULT_PROPERTIES); }

    @Override
    public void initSettings(SettingsData.SettingsReader settingsReader) {}

    @Override
    public boolean canBeInstalled() { return false; }

    @Override
    public String upgradeName() { return NAME; }

    public static Stream<TileEntity> iterBeaconAndConduit(int eye, World world) {
        return world.blockEntityList.stream().filter(te->(te instanceof BeaconTileEntity || te instanceof ConduitTileEntity) && ((te.getBlockPos().getX() - SubRoomsManager.ROOM_OFFSET_X + SubRoomsManager.HALF_ROOM) / SubRoomsManager.ROOM_SPACING + 1 == eye));
    }

    public static void applyBeaconEffect(CompoundNBT beaconNBT, LivingEntity entity) {
        int levels = beaconNBT.getInt("Levels");
        Effect primaryEffect = Effect.byId(beaconNBT.getInt("Primary"));
        Effect secondaryEffect = Effect.byId(beaconNBT.getInt("Secondary"));
        if (primaryEffect != null) {
            double d0 = levels * 10 + 10;
            int i = 0;
            if (levels >= 4 && primaryEffect == secondaryEffect)
                i = 1;
            int j = (9 + levels * 2) * 20;
            entity.addEffect(new EffectInstance(primaryEffect, j, i, true, false, true));
            if (levels >= 4 && primaryEffect != secondaryEffect && secondaryEffect != null)
                entity.addEffect(new EffectInstance(secondaryEffect, j, 0, true, false, true));
        }
    }

    private boolean beaconAndConduitTickBehavior(int eyeId, Entity entity) {
        if (EventManager.tick % 100 != 0 || !(entity instanceof LivingEntity)) return false;
        World w = WorldUtils.getRiftWorld();
        if (w == null) return false;
        iterBeaconAndConduit(eyeId, w).forEach(te->{
            if (te instanceof BeaconTileEntity)
                applyBeaconEffect(te.save(new CompoundNBT()), (LivingEntity) entity);
            if (te instanceof ConduitTileEntity && ((ConduitTileEntity)te).isActive() && entity.isInWaterOrRain())
                ((LivingEntity)entity).addEffect(new EffectInstance(Effects.CONDUIT_POWER, 260, 0, true, false, true));
        });
        return true;
    }

    /**
     * retrieve a list of TE in the bag following a few conditions (time of resolution is dependent on the amount of loaded chunk and therefore loaded TE, as we iterate on ALL available TE in the world)
     * @param world should be the bag dimension
     * @param eye the bag holding the tile entities
     * @param room the room (if negative, get TE in ALL rooms)
     * @param tickOnly get all TE (including static data holders) or only the ticking one
     * @param extraPredicate (nullable) add an extra condition to the TE filtering (the condition is tested first, since most of the time is just a type check)
     * @return a list of TE, note: the list is a shallow and partial clone of the list of TE in world or an empty default list, this is so you can iterate, so you should avoid adding/removing to/from the list (will do nothing to the world), manipulating the TE however WILL have consequences for the world
     */
    public static List<TileEntity> getTileEntitiesInBag(@Nonnull World world, int eye, int room, boolean tickOnly, @Nullable Predicate<TileEntity> extraPredicate) {
        List<TileEntity> empty = new ArrayList<>();
        if (!world.dimension().equals(WorldUtils.DimBagRiftKey)) return empty;
        SubRoomsManager manager = SubRoomsManager.getInstance(eye);
        if (manager == null) return empty;
        Predicate<TileEntity> filter = extraPredicate != null ? te->extraPredicate.test(te) && manager.isInRoom(te.getBlockPos(), room) : te->manager.isInRoom(te.getBlockPos(), room);
        return (tickOnly ? world.tickableBlockEntities : world.blockEntityList).stream().filter(filter).collect(Collectors.toList());
    }

    private static final Random RANDOM = new Random();

    @ConfigManager.Config
    public static int ONE_IN = 20;

    private boolean randomMobErrorBehavior(int eyeId, Entity entity) { //adds a random (configurable) chance that an entity close to the bag (unequiped) will be sucked inside the bag
        if (EventManager.tick % 2000 != 0 || !(entity instanceof BagEntity) || ONE_IN <= 0) return false;
        DimBag.debug("randomMobErrorBehavior");
        Optional<MobEntity> f = entity.level.getEntitiesOfClass(MobEntity.class, new AxisAlignedBB(entity.blockPosition().offset(-2, -2, -2), entity.blockPosition().offset(2, 2, 2)), e->!(e instanceof WitherEntity || e instanceof EnderDragonEntity || e instanceof BagEntity)).stream().findFirst();
        if (f.isPresent() && (((double)RANDOM.nextInt(ONE_IN)) / (double)ONE_IN) * f.get().position().distanceTo(entity.position()) < 1.d / (double)ONE_IN)
            SubRoomsManager.execute(eyeId, srm->srm.enterBag(f.get()));
        return true;
    }

    @Override
    public ActionResultType upgradeEntityTick(int eyeId, World world, Entity entity) {
        beaconAndConduitTickBehavior(eyeId, entity);
        randomMobErrorBehavior(eyeId, entity);
        return ActionResultType.SUCCESS;
    }
}
