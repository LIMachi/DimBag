package com.limachi.dim_bag.mixin;

import com.limachi.dim_bag.entities.BagEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(net.minecraft.world.entity.animal.camel.Camel.class)
public abstract class Camel {

    @Shadow @Nullable public abstract LivingEntity getControllingPassenger();

    @Inject(method = "clampRotation(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void unClampedRotationWhenCamelIsRiddenByBag(net.minecraft.world.entity.Entity passenger, CallbackInfo ci) {
        if (getControllingPassenger() instanceof BagEntity && passenger instanceof Player)
            ci.cancel();
    }
}
