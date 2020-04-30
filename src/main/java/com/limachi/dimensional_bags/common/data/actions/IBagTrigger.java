package com.limachi.dimensional_bags.common.data.actions;

import com.limachi.dimensional_bags.common.items.Bag;

public interface IBagTrigger {
    String printable();
    boolean match(Bag.BagEvent event);
    int mappedAction();
    void mapAction(int id);
}

