package com.limachi.dimensional_bags.common.managers.upgrades;

import com.limachi.dimensional_bags.common.data.EyeDataMK2.EnergyData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.HolderData;
import com.limachi.dimensional_bags.common.managers.Upgrade;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.world.World;

public class KineticGeneratorUpgrade extends Upgrade {

    public KineticGeneratorUpgrade() { super("kinetic_generator", true, 0, 1, 0, 1); }

    protected int motionGeneration(int eyeId, Entity entity) {
//        if (entity instanceof ServerPlayerEntity) {
//            ServerPlayerEntity player = (ServerPlayerEntity) entity;
//            player.getStats().getValue(Stats.WA)
//        }
        boolean isRiding = entity.getRidingEntity() != null;
        boolean isElytraFlying = entity instanceof PlayerEntity && ((PlayerEntity) entity).isElytraFlying();
        boolean isInWater = entity.isInWater();
        boolean isInLava = entity.isInLava();

        HolderData holderData = HolderData.getInstance(null, eyeId);
        if (holderData == null) return 0;

        double generation = entity.getPositionVec().distanceTo(holderData.getLastKnownPosition());
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
//        if (generation != 0)
//            DimBag.LOGGER.info("generated: " + (int)(generation * 10));
        return (int)(generation * 10);
    }

    @Override
    public ActionResultType upgradeEntityTick(int eyeId, boolean isSelected, ItemStack stack, World world, Entity entity, int itemSlot) {
        EnergyData energyData = EnergyData.getInstance(null, eyeId);
        if (energyData == null) return ActionResultType.FAIL;
        int tickGeneration = 0;
        {
            tickGeneration = motionGeneration(eyeId, entity);
        }
        energyData.receiveEnergy(tickGeneration, false);
        return ActionResultType.SUCCESS;
    }
}
