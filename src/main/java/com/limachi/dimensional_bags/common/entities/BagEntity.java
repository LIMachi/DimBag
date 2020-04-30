package com.limachi.dimensional_bags.common.entities;

import com.limachi.dimensional_bags.DimensionalBagsMod;
import com.limachi.dimensional_bags.common.data.DimBagData;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.data.IdHandler;
import com.limachi.dimensional_bags.common.dimensions.BagDimension;
import com.limachi.dimensional_bags.common.network.Network;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class BagEntity extends MobEntity {

    Integer id = null;

    public BagEntity(EntityType<? extends MobEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected boolean canTriggerWalking() { return false; } //prevent walking (meh)

    @Override
    public void applyEntityCollision(Entity entityIn) {
        DimensionalBagsMod.LOGGER.info("bag collided with " + entityIn);
    } //prevent pushing (at least, i though it was, rip)

    @Override
    public ItemStack getPickedResult(RayTraceResult target) {
        return new ItemStack(Items.AIR); //no spawn egg for you my good lad/lass
    }

    @Override
    public boolean canBeCollidedWith() { return true; } //if false, prevent punch :(

    @Override
    public boolean canBePushed() { return false; } //don't seem to work, sad :(

    @Override
    public boolean shouldRiderSit() { return false; }

    @Override
    public boolean canRiderInteract() { return true; }

    @Override
    public boolean canBeRiddenInWater(Entity rider) { return true; }

    @Override
    public EntityClassification getClassification(boolean forSpawnCount) { return null; }

    @Override
    public void tick() {
        super.tick();
        if (this.world.isRemote()) return; //do nothing client side
        EyeData data = DimBagData.get(this.world.getServer()).getEyeData(this.getId());
        if (this.getPosition() != data.getPosition() || this.dimension != data.getDimension()) //if the bag changed position or dimension
            data.updateBagPosition(this.getPosition(), this.dimension);
    }

    int getId() {
        if (this.id == null)
            this.id = new IdHandler(this).getId();
        return this.id;
    }

    @Override
    protected boolean processInteract(PlayerEntity player, Hand hand) {
        if (player.world.isRemote()) return false;
        if (player.isCrouching()) {
            BagDimension.teleportToRoom((ServerPlayerEntity) player, this.getId());
            return true;
        }
        Network.openGUIEye((ServerPlayerEntity) player, DimBagData.get(player.getServer()).getEyeData(this.getId()), "");
        return true;
    }
}
