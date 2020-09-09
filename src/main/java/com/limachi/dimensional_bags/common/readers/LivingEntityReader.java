package com.limachi.dimensional_bags.common.readers;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class LivingEntityReader<T extends LivingEntity> extends EntityReader<T> {

    public LivingEntityReader(T entity) {
        super(entity);
    }

    protected abstract class CommonEntityReader extends LivingEntity {
        protected CommonEntityReader(EntityType<? extends LivingEntity> type, World worldIn) { super(type, worldIn); }
        public int getTickSinceLastSwing() { return this.ticksSinceLastSwing; }
    }

    @Override
    protected void initSuppliers() {
        super.initSuppliers();
        suppliersDouble.put("health", t->(double)t.getHealth());
        suppliersDouble.put("max_health", t->(double)t.getMaxHealth());
        suppliersInteger.put("arrow_count_in_entity", LivingEntity::getArrowCountInEntity);
        suppliersInteger.put("bee_sting_count_in_entity", LivingEntity::getBeeStingCount);
        suppliersInteger.put("bed_position_x", t->t.getBedPosition().isPresent() ? t.getBedPosition().get().getX() : 0);
        suppliersInteger.put("bed_position_y", t->t.getBedPosition().isPresent() ? t.getBedPosition().get().getY() : 0);
        suppliersInteger.put("bed_position_z", t->t.getBedPosition().isPresent() ? t.getBedPosition().get().getZ() : 0);
        suppliersBoolean.put("is_swing_in_progress", t->t.isSwingInProgress);
        suppliersString.put("swinging_hand", t->t.swingingHand.name());
        suppliersInteger.put("swing_progress_int", t->t.swingProgressInt);
        suppliersInteger.put("arrow_hit_timer", t->t.arrowHitTimer);
        suppliersInteger.put("bee_sting_removal_cooldown", t->t.beeStingRemovalCooldown);
        suppliersInteger.put("hurt_time", t->t.hurtTime);
        suppliersInteger.put("max_hurt_time", t->t.maxHurtTime);
        suppliersDouble.put("attacked_at_yaw", t->(double)t.attackedAtYaw);
        suppliersInteger.put("death_time", t->t.deathTime);
        suppliersDouble.put("previous_swing_progress", t->(double)t.prevSwingProgress);
        suppliersDouble.put("swing_progress", t->(double)t.swingProgress);
        suppliersInteger.put("ticks_since_last_swing", t->((CommonEntityReader)t).getTickSinceLastSwing());
        suppliersDouble.put("previous_limb_swing_amount", t->(double)t.prevLimbSwingAmount);
        suppliersDouble.put("limb_swing_amount", t->(double)t.limbSwingAmount);
        suppliersDouble.put("limb_swing", t->(double)t.limbSwing);
        //TODO: finish to add the other properties
    }
}
