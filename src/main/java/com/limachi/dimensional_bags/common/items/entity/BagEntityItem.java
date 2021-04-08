package com.limachi.dimensional_bags.common.items.entity;

import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.HolderData;
import com.limachi.dimensional_bags.common.data.IEyeIdHolder;
import com.limachi.dimensional_bags.common.items.Bag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import com.limachi.dimensional_bags.StaticInit;

@StaticInit
public class BagEntityItem extends ItemEntity implements IEyeIdHolder {

    public static final String NAME = "bag_item";

    static {
        Registries.registerEntityType(NAME, ()->EntityType.Builder.<BagEntityItem>create(BagEntityItem::new, EntityClassification.MISC).size(0.25F, 0.25F).build("bag_item"));
    }

    public BagEntityItem(EntityType<? extends ItemEntity> type, World world) { //registry constructor?
        super(type, world);
    }

    public BagEntityItem(World worldIn, double x, double y, double z, ItemStack stack) {
        super(Registries.getEntityType(NAME), worldIn);
        this.setPosition(x, y, z);
        this.setItem(stack);
        setInvulnerable(true);
        lifespan = Integer.MAX_VALUE;
        setPickupDelay(10);
    }

    @Override
    public void onKillCommand() {}

    @Override
    protected void outOfWorld() {}

    @Override
    public boolean isImmuneToExplosions() {
        return true;
    }

    public void onItemPickup(Entity entityIn, int quantity) {

    }

    @Override
    public IPacket<?> createSpawnPacket() { return NetworkHooks.getEntitySpawningPacket(this); }

    @Override
    public void tick() {
        if (!world.isRemote()) {
            if (getPosition().getY() < 1)
                setPosition(getPosX(), 1, getPosZ());
            int id = Bag.getEyeId(getItem());
            if (id <= 0) return;
            HolderData.execute(id, holderData -> holderData.setHolder(this));
        }
        super.tick();
    }

    public boolean attackEntityFrom(DamageSource source, float amount) {
        return false;
    }

    @Override
    public int getEyeId() {
        return Bag.getEyeId(getItem());
    }
}
