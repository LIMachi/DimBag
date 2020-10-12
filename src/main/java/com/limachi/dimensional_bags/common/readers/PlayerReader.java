package com.limachi.dimensional_bags.common.readers;

import com.limachi.dimensional_bags.KeyMapController;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class PlayerReader<T extends PlayerEntity> extends LivingEntityReader<T> {

    public PlayerReader(T entity) { super(entity); }

    @Override
    protected void registerSuppliers() {
        super.registerSuppliers();
        suppliers.register("absorbtion", PlayerEntity::getAbsorptionAmount);
        suppliers.register("score", PlayerEntity::getScore);
        suppliers.register("main_hand", t -> t.getPrimaryHand().toString());
        suppliers.register("food_level", t -> t.getFoodStats().getFoodLevel());
        suppliers.register("saturation_level", t -> t.getFoodStats().getSaturationLevel());
        suppliers.register("previous_camera_yaw", t -> t.prevCameraYaw);
        suppliers.register("camera_yaw", t -> t.cameraYaw);
        suppliers.register("sleep_timer", PlayerEntity::getSleepTimer);
        suppliers.register("ability_disable_damage", t -> t.abilities.disableDamage);
        suppliers.register("ability_is_flying", t -> t.abilities.isFlying);
        suppliers.register("ability_allow_flying", t -> t.abilities.allowFlying);
        suppliers.register("ability_is_creative_mode", t -> t.abilities.isCreativeMode);
        suppliers.register("ability_allow_edit", t -> t.abilities.allowEdit);
        suppliers.register("ability_fly_speed", t -> t.abilities.getFlySpeed());
        suppliers.register("ability_walk_speed", t -> t.abilities.getWalkSpeed());
        suppliers.register("experience_level", t -> t.experienceLevel);
        suppliers.register("experience_total", t -> t.experienceTotal);
        suppliers.register("experience", t -> t.experience);
        suppliers.register("player_name", t -> t.getGameProfile().getName());
        suppliers.register("player_uuid", t -> t.getGameProfile().getId().toString());
        suppliers.register("is_sneaking", Entity::isCrouching);
        suppliers.register("is_sneaking_key_down", t -> KeyMapController.getKey(t, KeyMapController.CROUCH_KEY));
        suppliers.register("is_bag_key_down", t -> KeyMapController.getKey(t, KeyMapController.BAG_ACTION_KEY));
        suppliers.register("max_portal_time", PlayerEntity::getMaxInPortalTime);
    }
}