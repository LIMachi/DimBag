package com.limachi.dimensional_bags.lib.common.container;

import com.limachi.dimensional_bags.common.references.Registries;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.bag.BagEntity;
import com.limachi.dimensional_bags.common.bag.BagItem;
import com.limachi.dimensional_bags.common.bagProxy.ProxyTileEntity;
import com.limachi.dimensional_bags.lib.utils.WorldUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nonnull;

public abstract class BaseEyeContainer<C extends BaseContainer<C>> extends BaseContainer<C> {

    public int bagId;

    protected BaseEyeContainer(ContainerType<?> containerType, int windowId, PlayerInventory playerInv, int bagId) {
        super(containerType, windowId, playerInv);
        this.bagId = bagId;
    }

    protected BaseEyeContainer(ContainerType<?> containerType, int windowId, PlayerInventory playerInv, PacketBuffer buff) {
        super(containerType, windowId, playerInv, buff);
    }

    @Override
    public void readFromBuff(PacketBuffer buff) { this.bagId = buff.readInt(); }

    @Override
    public void writeToBuff(PacketBuffer buff) { buff.writeInt(bagId); }

    @Override
    public boolean stillValid(@Nonnull PlayerEntity playerIn) { //can interact if: player has bag on itself, player is inside the bag, or player is close to a bag entity (4 blocks)
        if (BagItem.hasBag(bagId, playerIn) || bagId == SubRoomsManager.getbagId(playerIn.level, playerIn.blockPosition(), false)) return true;
        AxisAlignedBB area = new AxisAlignedBB(playerIn.blockPosition().offset(-4, -4, -4), playerIn.blockPosition().offset(4, 4, 4));
        if (!WorldUtils.getTileEntitiesWithinAABB(playerIn.level, ProxyTileEntity.class, area, te->te.bagId() == bagId).isEmpty()) return true;
        return !playerIn.level.getEntities(Registries.getEntityType(BagEntity.NAME), area, entity -> ((BagEntity)entity).getbagId() == bagId).isEmpty();
    }
}
