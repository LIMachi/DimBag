package com.limachi.dim_bag.mixin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.limachi.dim_bag.bag_modes.ParasiteMode;
import com.limachi.dim_bag.entities.BagEntity;
import com.limachi.dim_bag.save_datas.BagsData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(net.minecraft.world.entity.Entity.class)
public class Entity {
    @Shadow private ImmutableList<net.minecraft.world.entity.Entity> passengers;

    @Shadow private Level level;

    /**
     * When a player is added to a list of passengers, they will be put in front of any other entities (except if a player is already the first passenger).
     * With this (hopefully not too) intrusive mixin, I allow a player to stay behind a bag by swapping back the seats if the bag is in second position and the player in first
     */
    @Inject(method = "addPassenger(Lnet/minecraft/world/entity/Entity;)V", at = @At("RETURN"))
    protected void allowBagsToBeControllingPassenger(net.minecraft.world.entity.Entity rider, CallbackInfo ci) {
        if (!level.isClientSide && rider instanceof Player && passengers.size() > 1 && passengers.get(0) == rider && passengers.get(1) instanceof BagEntity bag && BagsData.runOnBag(bag, b->b.isModeEnabled(ParasiteMode.NAME))) {
            List<net.minecraft.world.entity.Entity> list = Lists.newArrayList(this.passengers);
            list.set(0, bag);
            list.set(1, rider);
            passengers = ImmutableList.copyOf(list);
        }
    }
}
