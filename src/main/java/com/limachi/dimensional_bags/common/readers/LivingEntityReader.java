package com.limachi.dimensional_bags.common.readers;

import net.minecraft.entity.LivingEntity;

import com.limachi.dimensional_bags.StaticInit;

@StaticInit
public abstract class LivingEntityReader {

    static {
        EntityReader.register(LivingEntity.class, "health", LivingEntity::getHealth);
        EntityReader.register(LivingEntity.class, "max_health", LivingEntity::getMaxHealth);
        /*
        EntityReader.register(LivingEntity.class, "arrow_count_in_entity", LivingEntity::getArrowCountInEntity);
        EntityReader.register(LivingEntity.class, "bee_sting_count_in_entity", LivingEntity::getBeeStingCount);
        EntityReader.register(LivingEntity.class, "bed_position_x", t -> t.getBedPosition().isPresent() ? t.getBedPosition().get().getX() : 0);
        EntityReader.register(LivingEntity.class, "bed_position_y", t -> t.getBedPosition().isPresent() ? t.getBedPosition().get().getY() : 0);
        EntityReader.register(LivingEntity.class, "bed_position_z", t -> t.getBedPosition().isPresent() ? t.getBedPosition().get().getZ() : 0);
        EntityReader.register(LivingEntity.class, "is_swing_in_progress", t -> t.isSwingInProgress);
        EntityReader.register(LivingEntity.class, "swinging_hand", t -> t.swingingHand != null ? t.swingingHand.name() : "");
        EntityReader.register(LivingEntity.class, "swing_progress_int", t -> t.swingProgressInt);
        EntityReader.register(LivingEntity.class, "arrow_hit_timer", t -> t.arrowHitTimer);
        EntityReader.register(LivingEntity.class, "bee_sting_removal_cooldown", t -> t.beeStingRemovalCooldown);
        EntityReader.register(LivingEntity.class, "hurt_time", t -> t.hurtTime);
        EntityReader.register(LivingEntity.class, "max_hurt_time", t -> t.maxHurtTime);
        EntityReader.register(LivingEntity.class, "attacked_at_yaw", t ->t.attackedAtYaw);
        EntityReader.register(LivingEntity.class, "death_time", t -> t.deathTime);
        EntityReader.register(LivingEntity.class, "previous_swing_progress", t ->t.prevSwingProgress);
        EntityReader.register(LivingEntity.class, "swing_progress", t ->t.swingProgress);
        EntityReader.register(LivingEntity.class, "previous_limb_swing_amount", t ->t.prevLimbSwingAmount);
        EntityReader.register(LivingEntity.class, "limb_swing_amount", t ->t.limbSwingAmount);
        EntityReader.register(LivingEntity.class, "max_hurt_resistant_time", t -> t.maxHurtResistantTime);
        EntityReader.register(LivingEntity.class, "render_yaw_offset", t ->t.renderYawOffset);
        EntityReader.register(LivingEntity.class, "prev_render_yaw_offset", t ->t.prevRenderYawOffset);
        EntityReader.register(LivingEntity.class, "rotation_yaw_head", t ->t.rotationYawHead);
        EntityReader.register(LivingEntity.class, "prev_rotation_yaw_head", t ->t.prevRotationYawHead);
        EntityReader.register(LivingEntity.class, "jump_movement_factor", t ->t.jumpMovementFactor);
        EntityReader.register(LivingEntity.class, "attacking_entity", LivingEntity::getAttackingEntity);*/
        //TODO: finish to add the other properties
    }
}
