package com.limachi.dimensional_bags.common.bagDimensionOnly.bagWatcher.watchers;

import net.minecraft.entity.player.PlayerEntity;

import com.limachi.dimensional_bags.StaticInit;

@StaticInit
public class PlayerWatcher {

    static {
        EntityWatcher.register(PlayerEntity.class, "absorbtion", PlayerEntity::getAbsorptionAmount);
        EntityWatcher.register(PlayerEntity.class, "score", PlayerEntity::getScore);
        /*
        EntityReader.register(PlayerEntity.class, "main_hand", t -> t.getPrimaryHand().toString());
        EntityReader.register(PlayerEntity.class, "food_level", t -> t.getFoodStats().getFoodLevel());
        EntityReader.register(PlayerEntity.class, "saturation_level", t -> t.getFoodStats().getSaturationLevel());
        EntityReader.register(PlayerEntity.class, "previous_camera_yaw", t -> t.prevCameraYaw);
        EntityReader.register(PlayerEntity.class, "camera_yaw", t -> t.cameraYaw);
        EntityReader.register(PlayerEntity.class, "sleep_timer", PlayerEntity::getSleepTimer);
        EntityReader.register(PlayerEntity.class, "ability_disable_damage", t -> t.abilities.disableDamage);
        EntityReader.register(PlayerEntity.class, "ability_is_flying", t -> t.abilities.isFlying);
        EntityReader.register(PlayerEntity.class, "ability_allow_flying", t -> t.abilities.allowFlying);
        EntityReader.register(PlayerEntity.class, "ability_is_creative_mode", t -> t.abilities.isCreativeMode);
        EntityReader.register(PlayerEntity.class, "ability_allow_edit", t -> t.abilities.allowEdit);
        EntityReader.register(PlayerEntity.class, "ability_fly_speed", t -> t.abilities.getFlySpeed());
        EntityReader.register(PlayerEntity.class, "ability_walk_speed", t -> t.abilities.getWalkSpeed());
        EntityReader.register(PlayerEntity.class, "experience_level", t -> t.experienceLevel);
        EntityReader.register(PlayerEntity.class, "experience_total", t -> t.experienceTotal);
        EntityReader.register(PlayerEntity.class, "experience", t -> t.experience);
        EntityReader.register(PlayerEntity.class, "player_name", t -> t.getGameProfile().getName());
        EntityReader.register(PlayerEntity.class, "player_uuid", t -> t.getGameProfile().getId().toString());
        EntityReader.register(PlayerEntity.class, "is_sneaking", Entity::isCrouching);
        EntityReader.register(PlayerEntity.class, "is_sneaking_key_down", KeyMapController.KeyBindings.SNEAK_KEY::getState);
        EntityReader.register(PlayerEntity.class, "is_bag_key_down", KeyMapController.KeyBindings.BAG_KEY::getState);
        EntityReader.register(PlayerEntity.class, "max_portal_time", PlayerEntity::getMaxInPortalTime);*/
        //TODO: finish to add the other properties
    }
}