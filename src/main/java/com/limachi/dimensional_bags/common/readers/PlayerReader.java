package com.limachi.dimensional_bags.common.readers;

import com.limachi.dimensional_bags.KeyMapController;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class PlayerReader<T extends PlayerEntity> extends LivingEntityReader<T> {

    public PlayerReader(T entity) { super(entity); }

    @Override
    protected void initSuppliers() {
        super.initSuppliers();
        suppliers.put("absorbtion", new EntityValueReader(ValueType.DOUBLE, t->(double)t.getAbsorptionAmount(), PlayerEntity.class));
        suppliers.put("score", new EntityValueReader(ValueType.INTEGER, PlayerEntity::getScore, PlayerEntity.class));
        suppliers.put("main_hand", new EntityValueReader(ValueType.STRING, t->t.getPrimaryHand().toString(), PlayerEntity.class));
        suppliers.put("food_level", new EntityValueReader(ValueType.INTEGER, t->t.getFoodStats().getFoodLevel(), PlayerEntity.class));
        suppliers.put("saturation_level", new EntityValueReader(ValueType.DOUBLE, t->(double)t.getFoodStats().getSaturationLevel(), PlayerEntity.class));
        suppliers.put("previous_camera_yaw", new EntityValueReader(ValueType.DOUBLE, t->(double)t.prevCameraYaw, PlayerEntity.class));
        suppliers.put("camera_yaw", new EntityValueReader(ValueType.DOUBLE, t->(double)t.cameraYaw, PlayerEntity.class));
        suppliers.put("sleep_timer", new EntityValueReader(ValueType.INTEGER, PlayerEntity::getSleepTimer, PlayerEntity.class));
        suppliers.put("ability_disable_damage", new EntityValueReader(ValueType.BOOLEAN, t->t.abilities.disableDamage, PlayerEntity.class));
        suppliers.put("ability_is_flying", new EntityValueReader(ValueType.BOOLEAN, t->t.abilities.isFlying, PlayerEntity.class));
        suppliers.put("ability_allow_flying", new EntityValueReader(ValueType.BOOLEAN, t->t.abilities.allowFlying, PlayerEntity.class));
        suppliers.put("ability_is_creative_mode", new EntityValueReader(ValueType.BOOLEAN, t->t.abilities.isCreativeMode, PlayerEntity.class));
        suppliers.put("ability_allow_edit", new EntityValueReader(ValueType.BOOLEAN, t->t.abilities.allowEdit, PlayerEntity.class));
        suppliers.put("ability_fly_speed", new EntityValueReader(ValueType.DOUBLE, t->(double)t.abilities.getFlySpeed(), PlayerEntity.class));
        suppliers.put("ability_walk_speed", new EntityValueReader(ValueType.DOUBLE, t->(double)t.abilities.getWalkSpeed(), PlayerEntity.class));
        suppliers.put("experience_level", new EntityValueReader(ValueType.INTEGER, t->t.experienceLevel, PlayerEntity.class));
        suppliers.put("experience_total", new EntityValueReader(ValueType.INTEGER, t->t.experienceTotal, PlayerEntity.class));
        suppliers.put("experience", new EntityValueReader(ValueType.DOUBLE, t->(double)t.experience, PlayerEntity.class));
        suppliers.put("player_name", new EntityValueReader(ValueType.STRING, t->t.getGameProfile().getName(), PlayerEntity.class));
        suppliers.put("player_uuid", new EntityValueReader(ValueType.STRING, t->t.getGameProfile().getId().toString(), PlayerEntity.class));
        suppliers.put("is_sneaking", new EntityValueReader(ValueType.BOOLEAN, Entity::isCrouching, PlayerEntity.class));
        suppliers.put("is_sneaking_key_down", new EntityValueReader(ValueType.BOOLEAN, t->KeyMapController.getKey(t, KeyMapController.CROUCH_KEY), PlayerEntity.class));
        suppliers.put("is_bag_key_down", new EntityValueReader(ValueType.BOOLEAN, t->KeyMapController.getKey(t, KeyMapController.BAG_ACTION_KEY), PlayerEntity.class));
        suppliers.put("max_portal_time", new EntityValueReader(ValueType.INTEGER, PlayerEntity::getMaxInPortalTime, PlayerEntity.class));
    }
}