package com.limachi.dim_bag.items;

import com.limachi.lim_lib.registries.annotations.RegisterItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

public class ModuleFrameItem extends Item {

    @RegisterItem
    public static RegistryObject<BlockItem> R_ITEM;

    public ModuleFrameItem() { super(new Properties()); }
}
