package com.limachi.dim_bag.items;

import com.limachi.lim_lib.registries.annotations.RegisterItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

public class ModuleFrame extends Item {

    @RegisterItem
    public static RegistryObject<Item> R_ITEM;

    public ModuleFrame() { super(new Item.Properties()); }
}
