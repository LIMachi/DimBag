package com.limachi.dimensional_bags.client.rootWidgets;

import com.limachi.dimensional_bags.client.widgets.Root;

import java.util.UUID;

public interface RootProvider {
    UUID NULL_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    UUID getId();
    Root getRoot();
}
