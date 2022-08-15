package com.limachi.dim_bag.items;

import com.limachi.dim_bag.DimBag;
import com.limachi.utils.Log;
import com.limachi.utils.Registries;
import com.limachi.utils.scrollSystem.IScrollItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

public class BagItem extends Item implements IScrollItem {
    @Registries.RegisterItem
    public static RegistryObject<BagItem> R_ITEM;
    public BagItem() { super(DimBag.INSTANCE.defaultProps().stacksTo(1)); }

    @Override
    public void scroll(Player player, int slot, int delta) {
        Log.warn("validated scroll: " + delta + " for slot " + slot);
    }

    @Override
    public void scrollFeedBack(Player player, int slot, int delta) {

    }

    @Override
    public boolean canScroll(Player player, int slot) {
        return true;
    }
}
