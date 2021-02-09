package com.limachi.dimensional_bags.common.container.slot;

import com.limachi.dimensional_bags.common.inventory.Wrapper;

public interface IIORightsSlot {
    byte getRights();
    void setRightsFlag(byte rights);
    void setRights(Wrapper.IORights rights);
}
