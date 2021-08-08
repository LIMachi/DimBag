package com.limachi.dimensional_bags.common.container;

import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.limachi.dimensional_bags.common.items.Bag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nonnull;

public abstract class BaseEyeContainer<C extends BaseContainer<C>> extends BaseContainer<C> {

    public int eyeId;

    protected BaseEyeContainer(ContainerType<?> containerType, int windowId, PlayerInventory playerInv, int eyeId) {
        super(containerType, windowId, playerInv);
        this.eyeId = eyeId;
    }

    protected BaseEyeContainer(ContainerType<?> containerType, int windowId, PlayerInventory playerInv, PacketBuffer buff) {
        super(containerType, windowId, playerInv, buff);
    }

    @Override
    public void readFromBuff(PacketBuffer buff) { this.eyeId = buff.readInt(); }

    @Override
    public void writeToBuff(PacketBuffer buff) { buff.writeInt(eyeId); }

    @Override
    public boolean canInteractWith(@Nonnull PlayerEntity playerIn) { //can interact if: player has bag on itself, player is inside the bag, or player is close to a bag entity (4 blocks)
        if (Bag.hasBag(eyeId, playerIn) || eyeId == SubRoomsManager.getEyeId(playerIn.world, playerIn.getPosition(), false)) return true;
        return !playerIn.world.getEntitiesWithinAABB(Registries.getEntityType(BagEntity.NAME), new AxisAlignedBB(playerIn.getPosition().add(-4, -4, -4), playerIn.getPosition().add(4, 4, 4)), entity -> ((BagEntity)entity).getEyeId() == eyeId && entity.getPositionVec().distanceTo(playerIn.getPositionVec()) < 4).isEmpty();
    }
}
