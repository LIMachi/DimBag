package com.limachi.dimensional_bags.common.items.upgrades.bag;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.ConfigManager.Config;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.EnergyData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.HolderData;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.items.upgrades.BaseUpgradeBag;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResultType;
import net.minecraft.world.World;

@StaticInit
public class KineticGeneratorUpgrade extends BaseUpgradeBag<KineticGeneratorUpgrade> {

    public static final String NAME = "kinetic_generator_upgrade";

    @Config(cmt = "can this upgrade be installed")
    public static boolean ACTIVE = true;

    @Config(min = "0", max = "3", cmt = "how much slowness should be applied on the entity when it generates energy")
    public static int SLOWNESS_POWER = 1;

    @Config(min = "1", max = "65535", cmt = "multiply the generation algorithm by this value (1 is so low that this upgrade would produce virtually 0 RF/FE)")
    public static int GENERATION_MULTIPLIER = 10;

    static {
        UpgradeManager.registerUpgrade(NAME, KineticGeneratorUpgrade::new);
    }

    public KineticGeneratorUpgrade() { super(DimBag.DEFAULT_PROPERTIES); }

    @Override
    public boolean canBeInstalled() { return ACTIVE; }

    @Override
    public String upgradeName() { return NAME; }

    protected int motionGeneration(int eyeId, Entity entity) { //FIXME: use a better algorithm and make it configurable

        boolean isRiding = entity.getVehicle() != null;
        boolean isElytraFlying = entity instanceof PlayerEntity && ((PlayerEntity) entity).isFallFlying();
        boolean isInWater = entity.isInWater();
        boolean isInLava = entity.isInLava();

        double generation = HolderData.execute(eyeId, (holderData)->entity.position().distanceTo(holderData.getLastKnownPosition()), 0D);
        if (isElytraFlying && !isInWater && !isInLava)
            generation /= 12D;
        if (isElytraFlying && (isInWater || isInLava))
            generation /= 4D;
        if (isInWater)
            generation *= 1.5D;
        if (entity.isSwimming())
            generation *= 2D;
        if (isInLava)
            generation *= 2D;
        if (isRiding)
            generation /= 4D;
        if (entity instanceof LivingEntity && SLOWNESS_POWER > 0)
            ((LivingEntity)entity).addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 20, SLOWNESS_POWER - 1, false, false, false));
        return (int)(generation * GENERATION_MULTIPLIER);
    }

    @Override
    public ActionResultType upgradeEntityTick(int eyeId, World world, Entity entity) {
        if (entity instanceof LivingEntity && Bag.isEquippedOnCuriosSlot((LivingEntity)entity, eyeId) == eyeId)
            EnergyData.execute(eyeId, (energyData)->energyData.receiveEnergy(motionGeneration(eyeId, entity), false), 0);
        return ActionResultType.SUCCESS;
    }
}
