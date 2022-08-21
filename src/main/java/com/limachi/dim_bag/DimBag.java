package com.limachi.dim_bag;

import com.limachi.dim_bag.items.BagItem;
import com.limachi.utils.CuriosIntegration;
import com.limachi.utils.KeyMapController;
import com.limachi.utils.ModBase;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;

@SuppressWarnings("unused")
@Mod(DimBag.MOD_ID)
public class DimBag extends ModBase
{
    public static final String MOD_ID = "dim_bag";
    public static DimBag INSTANCE;
    public static final KeyMapController.GlobalKeyBinding ACTION_KEY = KeyMapController.registerMouseKeyBind("key.action", 3, "category.dim_bag");
    static {
        ResourceLocation icon = new ResourceLocation(MOD_ID, "item/ghost_bag");
        CuriosIntegration.registerIcon(icon);
        CuriosIntegration.registerSlot(Constants.BAG_CURIO_SLOT, icon, 1);
    }
    public DimBag() {
        super(MOD_ID, "Dimensional Bags");
        INSTANCE = this;
        createTab(MOD_ID, BagItem.R_ITEM);
    }
}
