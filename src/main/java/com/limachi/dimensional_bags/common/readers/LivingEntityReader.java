package com.limachi.dimensional_bags.common.readers;

import net.minecraft.entity.LivingEntity;

public class LivingEntityReader<T extends LivingEntity> extends EntityReader<T> {

    public LivingEntityReader(T entity) { super(entity); }

    protected void registerSuppliers() {
        super.registerSuppliers();
        suppliers.register("health", LivingEntity::getHealth);
        suppliers.register("max_health", LivingEntity::getMaxHealth);
        suppliers.register("arrow_count_in_entity", LivingEntity::getArrowCountInEntity);
        suppliers.register("bee_sting_count_in_entity", LivingEntity::getBeeStingCount);
        suppliers.register("bed_position_x", t -> t.getBedPosition().isPresent() ? t.getBedPosition().get().getX() : 0);
        suppliers.register("bed_position_y", t -> t.getBedPosition().isPresent() ? t.getBedPosition().get().getY() : 0);
        suppliers.register("bed_position_z", t -> t.getBedPosition().isPresent() ? t.getBedPosition().get().getZ() : 0);
        suppliers.register("is_swing_in_progress", t -> t.isSwingInProgress);
        suppliers.register("swinging_hand", t -> t.swingingHand != null ? t.swingingHand.name() : "");
        suppliers.register("swing_progress_int", t -> t.swingProgressInt);
        suppliers.register("arrow_hit_timer", t -> t.arrowHitTimer);
        suppliers.register("bee_sting_removal_cooldown", t -> t.beeStingRemovalCooldown);
        suppliers.register("hurt_time", t -> t.hurtTime);
        suppliers.register("max_hurt_time", t -> t.maxHurtTime);
        suppliers.register("attacked_at_yaw", t ->t.attackedAtYaw);
        suppliers.register("death_time", t -> t.deathTime);
        suppliers.register("previous_swing_progress", t ->t.prevSwingProgress);
        suppliers.register("swing_progress", t ->t.swingProgress);
        suppliers.register("previous_limb_swing_amount", t ->t.prevLimbSwingAmount);
        suppliers.register("limb_swing_amount", t ->t.limbSwingAmount);
        suppliers.register("max_hurt_resistant_time", t -> t.maxHurtResistantTime);
        suppliers.register("render_yaw_offset", t ->t.renderYawOffset);
        suppliers.register("prev_render_yaw_offset", t ->t.prevRenderYawOffset);
        suppliers.register("rotation_yaw_head", t ->t.rotationYawHead);
        suppliers.register("prev_rotation_yaw_head", t ->t.prevRotationYawHead);
        suppliers.register("jump_movement_factor", t ->t.jumpMovementFactor);
        suppliers.register("attacking_entity", LivingEntity::getAttackingEntity);
        //TODO: finish to add the other properties
    }
}
