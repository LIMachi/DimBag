package com.limachi.dimensional_bags.common.entities;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.dimension.BagRiftDimension;
import com.limachi.dimensional_bags.common.network.Network;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class BagEntity extends MobEntity {

    public static String ITEM_KEY = "BagItemStack";
    public static String ID_KEY = "ID";

    private int id = 0;
    private int tick = 0;

    public BagEntity(EntityType<? extends MobEntity> type, World world) { super(type, world); }

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
            if (data != null)
                LOGGER.info("Hi my name is. What? My name is. Who? My name is " + data.getId());
            if (data != null && data.getId() != EyeData.getEyeId(this.world, this.getPosition())) //only update the position if the bag isn't in itself
                data.updateBagPosition(this.getPosition(), this.dimension);
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
    protected boolean processInteract(PlayerEntity player, Hand hand) {
        if (player.world.isRemote()) return false;
        if (getId() == 0) return false;
        if (player.isCrouching())
            BagRiftDimension.teleportEntity(player, BagRiftDimension.getDimensionType(), new BlockPos(1024 * (id - 1) + 8, 129, 8));
        else
            Network.openEyeInventory((ServerPlayerEntity)player, EyeData.get(world.getServer(), id).getInventory());
        return true;
    }
}
