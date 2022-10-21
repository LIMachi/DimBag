package com.limachi.dim_bag;

import com.limachi.dim_bag.items.BagItem;
import com.limachi.lim_lib.ModBase;
import com.limachi.lim_lib.integration.CuriosIntegration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.fml.common.Mod;

@SuppressWarnings("unused")
@Mod(Constants.MOD_ID)
public class DimBag extends ModBase
{
    public static DimBag INSTANCE;

    static {
        ResourceLocation icon = new ResourceLocation(Constants.MOD_ID, "item/ghost_bag");
        CuriosIntegration.registerIcon(icon);
        CuriosIntegration.registerSlot(Constants.BAG_CURIO_SLOT, icon, 1);
    }

    public static final CreativeModeTab TAB = createTab(Constants.MOD_ID, ()->BagItem.R_ITEM);

    public DimBag() {
        super(Constants.MOD_ID, "Dimensional Bags", TAB);
        INSTANCE = this;
    }
}
