package com.limachi.dimensional_bags.common.entities;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.WorldUtils;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.network.Network;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class BagEntity extends MobEntity {

    public static String ITEM_KEY = "BagItemStack";
    public static String ID_KEY = "ID";

    private int id = 0;
    private int tick = 0;

    public BagEntity(EntityType<? extends MobEntity> type, World world) {
        super(type, world);
    }

    public static AttributeModifierMap.MutableAttribute getAttributeMap() {
        return LivingEntity.registerAttributes().createMutableAttribute(Attributes.FOLLOW_RANGE, 16.0D);
    }

    public static BagEntity spawn(World world, BlockPos position, int id, ItemStack bag) {
        BagEntity out = new BagEntity(Registries.BAG_ENTITY.get(), world);
        out.setPosition(position.getX() + 0.5d, position.getY() + 0.5d, position.getZ() + 0.5d);
        out.setInvulnerable(true);
        out.enablePersistence();
        out.id = id;
        out.getPersistentData().put(ITEM_KEY, bag.write(new CompoundNBT()));
        out.getPersistentData().putInt(ID_KEY, id);
        out.setCustomName(bag.getDisplayName());
        out.setCustomNameVisible(true);
        world.addEntity(out);
        return out;
    }

    public boolean attackEntityFrom(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isImmuneToExplosions() {
        return true;
    }

    @Override
    protected boolean canTriggerWalking() { return false; } //prevent walking (meh)

    @Override
    public void applyEntityCollision(Entity entityIn) {
        DimBag.LOGGER.info("bag collided with " + entityIn);
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
        if ((tick & 7) == 0) {
            EyeData data = EyeData.get(this.world.getServer(), getId());
//            if (data != null)
//                LOGGER.info("Hi my name is. What? My name is. Who? My name is " + data.getId());
            if (data != null && data != EyeData.getEyeData(this.world, this.getPosition(), false)) //only update the position if the bag isn't in itself
                data.updateBagPosition(this.getPosition(), (ServerWorld)this.world);
            if (data != null)
                data.setUser(this);
        }
        ++tick;
    }

    public int getId() {
        if (this.id == 0)
            this.id = this.getPersistentData().getInt(ID_KEY);
        return this.id;
    }

    @Override
    protected ActionResultType/*boolean processInteract*/func_230254_b_(PlayerEntity player, Hand hand) { //TODO: update mapping
        if (player.world.isRemote()) return ActionResultType.PASS;
        if (getId() == 0) return ActionResultType.PASS;
        if (player.isCrouching())
            WorldUtils.teleportEntity(player, WorldUtils.DimBagRiftKey, new BlockPos(1024 * (id - 1) + 8, 129, 8));
        else
            Network.openEyeInventory((ServerPlayerEntity)player, EyeData.get(world.getServer(), id));
        return ActionResultType.SUCCESS;
    }

    @Override
    protected void collideWithEntity(Entity entityIn) {

    }
}
