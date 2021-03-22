package com.limachi.dimensional_bags.common.items.upgrades;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.ConfigManager.Config;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.EnergyData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.HolderData;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResultType;
import net.minecraft.world.World;

@StaticInit
public class KineticGeneratorUpgrade extends BaseUpgrade {

    public static final String NAME = "kinetic_generator_upgrade";

    @Config(cmt = "can this upgrade be installed")
    public static boolean ACTIVE = true;
    @Config(min = "0", max = "3", cmt = "how much slowness should be applied on the entity when it generates energy")
    public static int MULTIPLIER = 1;

    static {
        Registries.registerItem(NAME, KineticGeneratorUpgrade::new);
        UpgradeManager.registerUpgrade(NAME, new KineticGeneratorUpgrade());
    }

    public KineticGeneratorUpgrade() { super(DimBag.DEFAULT_PROPERTIES); }

    @Override
    public boolean canBeInstalled() { return ACTIVE; }

    @Override
    public String upgradeName() { return NAME; }

    @Override
    public int installUpgrade(UpgradeManager manager, int qty) { return qty; }

    protected int motionGeneration(int eyeId, Entity entity) {

        boolean isRiding = entity.getRidingEntity() != null;
        boolean isElytraFlying = entity instanceof PlayerEntity && ((PlayerEntity) entity).isElytraFlying();
        boolean isInWater = entity.isInWater();
        boolean isInLava = entity.isInLava();

        double generation = HolderData.execute(eyeId, (holderData)->entity.getPositionVec().distanceTo(holderData.getLastKnownPosition()), 0D);
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
        if (entity instanceof LivingEntity && MULTIPLIER > 0)
            ((LivingEntity)entity).addPotionEffect(new EffectInstance(Effects.SLOWNESS, 20, MULTIPLIER - 1, false, false, false));
        return (int)(generation * 10);
    }

    @Override
    public ActionResultType upgradeEntityTick(int eyeId, World world, Entity entity) {
        if (entity instanceof LivingEntity && Bag.isEquipedOnCuriosSlot((LivingEntity)entity, eyeId) == eyeId)
            EnergyData.execute(eyeId, (energyData)->energyData.receiveEnergy(motionGeneration(eyeId, entity), false), 0);
        return ActionResultType.SUCCESS;
    }
}
