package com.limachi.dimensional_bags.common.items;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.Registries;
import net.minecraft.item.Item;

/**
 * components are blank items that are only used as a step in a craft, they do have models, names, etc... (note: we could use a file to generate those items)
 */

@StaticInit
public class Components extends Item {

    static {
        registerComponents("battery_component", "blank_upgrade", "compression_field");
    }

    public Components() { super(DimBag.DEFAULT_PROPERTIES); }

    public static void registerComponents(String ...names) {
        for (String name : names)
            Registries.registerItem(name, Components::new);
    }
}
