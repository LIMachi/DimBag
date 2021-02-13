package com.limachi.dimensional_bags.common.managers.upgrades;

import com.google.common.collect.ImmutableMultimap;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.EnergyData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.HolderData;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.managers.Upgrade;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.ActionResultType;
import net.minecraft.world.World;

import java.util.UUID;

public class KineticGeneratorUpgrade extends Upgrade {

    public static final AttributeModifier KINETIC_GENERATOR_SLOW_MODIFIER = new AttributeModifier(UUID.fromString("f9e786f8-0c69-11eb-adc1-0242ac120002"), "Kinetic Generator Slowness", -0.15, AttributeModifier.Operation.MULTIPLY_TOTAL);

    public KineticGeneratorUpgrade() { super("kinetic_generator", true, 0, 1, 0, 1); }

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
        return (int)(generation * 10);
    }

    @Override
    public void getAttributeModifiers(int eyeId, EquipmentSlotType slot, ImmutableMultimap.Builder<Attribute, AttributeModifier> builder) {
        if (slot == EquipmentSlotType.CHEST)
            builder.put(Attributes.MOVEMENT_SPEED, KINETIC_GENERATOR_SLOW_MODIFIER);
    }

    @Override
    public ActionResultType upgradeEntityTick(int eyeId, World world, Entity entity) {
        if (entity instanceof LivingEntity && /*Bag.getBagSlot((PlayerEntity)entity, eyeId) == 38*/ Bag.isEquipedOnCuriosSlot((LivingEntity)entity, eyeId) == eyeId)
            EnergyData.execute(eyeId, (energyData)->energyData.receiveEnergy(motionGeneration(eyeId, entity), false), 0);
        return ActionResultType.SUCCESS;
    }
}
