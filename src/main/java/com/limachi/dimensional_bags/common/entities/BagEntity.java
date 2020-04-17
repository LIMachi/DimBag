package com.limachi.dimensional_bags.common.entities;

import com.limachi.dimensional_bags.DimensionalBagsMod;
import net.minecraft.entity.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class BagEntity extends MobEntity {

    public BagEntity(EntityType<? extends MobEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected boolean canTriggerWalking() { return false; } //prevent walking (meh)

    @Override
    public void applyEntityCollision(Entity entityIn) { } //prevent pushing (at least, i though it was, rip)

    @Override
    public ItemStack getPickedResult(RayTraceResult target) {
        DimensionalBagsMod.LOGGER.info("i was called, oopsy daisy");
        return new ItemStack(Items.AIR); //no spawn egg for you my good lad/lass
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    public boolean canRiderInteract() {
        return true;
    }

    @Override
    public boolean canBeRiddenInWater(Entity rider) {
        return true;
    }

    @Override
    public EntityClassification getClassification(boolean forSpawnCount) {
        return null;
    }
}
