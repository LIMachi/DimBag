package com.limachi.dimensional_bags.common.readers;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class LivingEntityReader<T extends LivingEntity> extends EntityReader<T> {

    public LivingEntityReader(T entity) { super(entity); }

    protected abstract class CommonEntityReader extends LivingEntity {
        protected CommonEntityReader(EntityType<? extends LivingEntity> type, World worldIn) { super(type, worldIn); }
        public int getTickSinceLastSwing() { return this.ticksSinceLastSwing; }
    }

    @Override
    protected void initSuppliers() {
        super.initSuppliers();
        suppliers.put("health", new EntityValueReader(ValueType.DOUBLE, t->(double)t.getHealth(), LivingEntity.class));
        suppliers.put("max_health", new EntityValueReader(ValueType.DOUBLE, t->(double)t.getMaxHealth(), LivingEntity.class));
        suppliers.put("arrow_count_in_entity", new EntityValueReader(ValueType.INTEGER, LivingEntity::getArrowCountInEntity, LivingEntity.class));
        suppliers.put("bee_sting_count_in_entity", new EntityValueReader(ValueType.INTEGER, LivingEntity::getBeeStingCount, LivingEntity.class));
        suppliers.put("bed_position_x", new EntityValueReader(ValueType.INTEGER, t->t.getBedPosition().isPresent() ? t.getBedPosition().get().getX() : 0, LivingEntity.class));
        suppliers.put("bed_position_y", new EntityValueReader(ValueType.INTEGER, t->t.getBedPosition().isPresent() ? t.getBedPosition().get().getY() : 0, LivingEntity.class));
        suppliers.put("bed_position_z", new EntityValueReader(ValueType.INTEGER, t->t.getBedPosition().isPresent() ? t.getBedPosition().get().getZ() : 0, LivingEntity.class));
        suppliers.put("is_swing_in_progress", new EntityValueReader(ValueType.BOOLEAN, t->t.isSwingInProgress, LivingEntity.class));
        suppliers.put("swinging_hand", new EntityValueReader(ValueType.STRING, t->t.swingingHand.name(), LivingEntity.class));
        suppliers.put("swing_progress_int", new EntityValueReader(ValueType.INTEGER, t->t.swingProgressInt, LivingEntity.class));
        suppliers.put("arrow_hit_timer", new EntityValueReader(ValueType.INTEGER, t->t.arrowHitTimer, LivingEntity.class));
        suppliers.put("bee_sting_removal_cooldown", new EntityValueReader(ValueType.INTEGER, t->t.beeStingRemovalCooldown, LivingEntity.class));
        suppliers.put("hurt_time", new EntityValueReader(ValueType.INTEGER, t->t.hurtTime, LivingEntity.class));
        suppliers.put("max_hurt_time", new EntityValueReader(ValueType.INTEGER, t->t.maxHurtTime, LivingEntity.class));
        suppliers.put("attacked_at_yaw", new EntityValueReader(ValueType.DOUBLE, t->(double)t.attackedAtYaw, LivingEntity.class));
        suppliers.put("death_time", new EntityValueReader(ValueType.INTEGER, t->t.deathTime, LivingEntity.class));
        suppliers.put("previous_swing_progress", new EntityValueReader(ValueType.DOUBLE, t->(double)t.prevSwingProgress, LivingEntity.class));
        suppliers.put("swing_progress", new EntityValueReader(ValueType.DOUBLE, t->(double)t.swingProgress, LivingEntity.class));
        suppliers.put("ticks_since_last_swing", new EntityValueReader(ValueType.INTEGER, t->((CommonEntityReader)t).getTickSinceLastSwing(), LivingEntity.class));
        suppliers.put("previous_limb_swing_amount", new EntityValueReader(ValueType.DOUBLE, t->(double)t.prevLimbSwingAmount, LivingEntity.class));
        suppliers.put("limb_swing_amount", new EntityValueReader(ValueType.DOUBLE, t->(double)t.limbSwingAmount, LivingEntity.class));
        suppliers.put("limb_swing", new EntityValueReader(ValueType.DOUBLE, t->(double)t.limbSwing, LivingEntity.class));
        //TODO: finish to add the other properties
    }
}
