package com.limachi.dimensional_bags.common.items.entity;

import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.items.Bag;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class BagEntityItem extends ItemEntity {
    public BagEntityItem(EntityType<? extends ItemEntity> type, World world) { //registry constructor?
        super(type, world);
    }

    public BagEntityItem(World worldIn, double x, double y, double z, ItemStack stack) {
        super(Registries.BAG_ITEM_ENTITY.get(), worldIn);
        this.setPosition(x, y, z);
        this.setItem(stack);
        setInvulnerable(true);
        lifespan = Integer.MAX_VALUE;
        setPickupDelay(10);
    }

    @Override
    public IPacket<?> createSpawnPacket() { return NetworkHooks.getEntitySpawningPacket(this); }

    @Override
    public void tick() {
        if (!world.isRemote()) {
            int id = Bag.getId(getItem());
            if (id != 0) {
                EyeData data = EyeData.get(getServer(), id);
                data.setUser(this);
            }
        }
        super.tick();
    }
}
