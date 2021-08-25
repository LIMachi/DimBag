package com.limachi.dimensional_bags.common.inventory;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.IInventory;

public interface IEntityInventoryProxyIsActiveSlot extends IInventory {
    boolean isActiveSlot(int slot);
    String getEntityName();
    Entity getEntity();
}
