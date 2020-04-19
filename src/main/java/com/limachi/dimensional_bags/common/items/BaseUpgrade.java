package com.limachi.dimensional_bags.common.items;

import com.limachi.dimensional_bags.DimensionalBagsMod;
import net.minecraft.item.Item;

public class BaseUpgrade extends Item {

    protected String upgrade_id;
    protected int max_upgrade;

    public BaseUpgrade(String upgrade_id, int max_upgrade) {
        super(new Properties().group(DimensionalBagsMod.ItemGroup.instance).maxStackSize(1));
        this.upgrade_id = upgrade_id;
        this.max_upgrade = max_upgrade;
    }

    public String getId() { return this.upgrade_id; }

    public int getMaxUpgrade() { return this.max_upgrade; }
}
