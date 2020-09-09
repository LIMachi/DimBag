package com.limachi.dimensional_bags.common.readers;

import com.limachi.dimensional_bags.KeyMapController;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class PlayerReader<T extends PlayerEntity> extends LivingEntityReader<T> {

    public PlayerReader(T entity) {
        super(entity);
    }

    @Override
    protected void initSuppliers() {
        super.initSuppliers();
        suppliersDouble.put("absorbtion", t->(double)t.getAbsorptionAmount());
        suppliersInteger.put("score", PlayerEntity::getScore);
        suppliersString.put("main_hand", t->t.getPrimaryHand().toString());
        suppliersInteger.put("food_level", t->t.getFoodStats().getFoodLevel());
        suppliersDouble.put("saturation_level", t->(double)t.getFoodStats().getSaturationLevel());
        suppliersDouble.put("previous_camera_yaw", t->(double)t.prevCameraYaw);
        suppliersDouble.put("camera_yaw", t->(double)t.cameraYaw);
        suppliersInteger.put("sleep_timer", PlayerEntity::getSleepTimer);
        suppliersBoolean.put("ability_disable_damage", t->t.abilities.disableDamage);
        suppliersBoolean.put("ability_is_flying", t->t.abilities.isFlying);
        suppliersBoolean.put("ability_allow_flying", t->t.abilities.allowFlying);
        suppliersBoolean.put("ability_is_creative_mode", t->t.abilities.isCreativeMode);
        suppliersBoolean.put("ability_allow_edit", t->t.abilities.allowEdit);
        suppliersDouble.put("ability_fly_speed", t->(double)t.abilities.getFlySpeed());
        suppliersDouble.put("ability_walk_speed", t->(double)t.abilities.getWalkSpeed());
        suppliersInteger.put("experience_level", t->t.experienceLevel);
        suppliersInteger.put("experience_total", t->t.experienceTotal);
        suppliersDouble.put("experience", t->(double)t.experience);
        suppliersString.put("player_name", t->t.getGameProfile().getName());
        suppliersString.put("player_uuid", t->t.getGameProfile().getId().toString());
        suppliersBoolean.put("is_sneaking", Entity::isCrouching);
        suppliersBoolean.put("is_sneaking_key_down", t->KeyMapController.getKey(t, KeyMapController.CROUCH_KEY));
        suppliersBoolean.put("is_bag_key_down", t->KeyMapController.getKey(t, KeyMapController.BAG_ACTION_KEY));
        suppliersInteger.put("max_portal_time", PlayerEntity::getMaxInPortalTime);
    }
}