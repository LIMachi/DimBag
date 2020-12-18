package com.limachi.dimensional_bags.common.container.slot;

import com.limachi.dimensional_bags.common.inventory.InventoryUtils;

public interface IIORightsSlot {
    byte getRights();
    void setRightsFlag(byte rights);
    void setRights(InventoryUtils.ItemStackIORights rights);
}
