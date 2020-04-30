package com.limachi.dimensional_bags.common.upgradesManager;

import com.limachi.dimensional_bags.DimensionalBagsMod;
import net.minecraft.item.Item;

public class ItemUpgrade extends Item {

    public ItemUpgrade(int maxStack) {
        super(new Properties().group(DimensionalBagsMod.ItemGroup.instance).maxStackSize(maxStack));
    }
}
