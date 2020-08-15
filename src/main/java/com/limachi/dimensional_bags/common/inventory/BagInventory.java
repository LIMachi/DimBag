package com.limachi.dimensional_bags.common.inventory;

import com.limachi.dimensional_bags.common.data.EyeData;

public class BagInventory extends Wrapper {
    public BagInventory(EyeData data) {
        super(data.getRows() * data.getColumns(), data::markDirty);
    }
}