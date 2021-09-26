package com.limachi.dimensional_bags.common.fluids;

import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.EventManager;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.registries.ObjectHolder;

import java.util.ArrayList;

@StaticInit
public class ModCompat {
    @ObjectHolder("pneumaticcraft:memory_essence")
    public static final Fluid pneumaticcraft_xp_fluid = null;
    @ObjectHolder("cofh_core:experience")
    public static final Fluid cofh_xp_fluid = null;
    @ObjectHolder("industrial_foregoing:memory_essence")
    public static final Fluid industrial_foregoing_xp_fluid = null;

    private static ArrayList<BinaryStateSingleFluidHandler.BinaryStateSingleFluidHandlerItem> XP_BOTTLES = new ArrayList<>();

    /**
     * xp conversion by default: 1xp -> 20mb, 1 bottle -> 250mb ~> 12.5xp, 1 bottle broken -> 3~7~11 -> 60~140~220mb (in accordance with other mods and wiki)
     * for less of a diminishing return on bottles, we could push the conversion to 1xp -> 25mb -> 75~175~275mb per bottle, probabilities are still low enough to prevent an exploit, but doing so would open an exploit with other mods
     */

    public static ArrayList<BinaryStateSingleFluidHandler.BinaryStateSingleFluidHandlerItem> xpBottles() {
        if (XP_BOTTLES.isEmpty()) {
            if (pneumaticcraft_xp_fluid != null)
                XP_BOTTLES.add(BinaryStateSingleFluidHandler.BinaryStateSingleFluidHandlerItem.registerBottle(pneumaticcraft_xp_fluid, 250, new ItemStack(Items.EXPERIENCE_BOTTLE)));
            if (cofh_xp_fluid != null)
                XP_BOTTLES.add(BinaryStateSingleFluidHandler.BinaryStateSingleFluidHandlerItem.registerBottle(cofh_xp_fluid, 250, new ItemStack(Items.EXPERIENCE_BOTTLE)));
            if (industrial_foregoing_xp_fluid != null)
                XP_BOTTLES.add(BinaryStateSingleFluidHandler.BinaryStateSingleFluidHandlerItem.registerBottle(industrial_foregoing_xp_fluid, 250, new ItemStack(Items.EXPERIENCE_BOTTLE)));
            //FIXME: DEBUG ONLY: use lava instead of XP to test interactions in dev (if we don't have a compatible mod on hand)
//            XP_BOTTLES.add(BinaryStateSingleFluidHandler.BinaryStateSingleFluidHandlerItem.registerBottle(Fluids.LAVA, 250, new ItemStack(Items.EXPERIENCE_BOTTLE)));
        }
        return XP_BOTTLES;
    }

    @ObjectHolder("create:honey")
    public static final Fluid create_honey_fluid = null;
    @ObjectHolder("cofh_core:honey")
    public static final Fluid cofh_honey_fluid = null;
    @ObjectHolder("resourcefulbees:honey")
    public static final Fluid resourcefulbees_honey_fluid = null;
    private static ArrayList<BinaryStateSingleFluidHandler.BinaryStateSingleFluidHandlerItem> HONEY_BOTTLES = new ArrayList<>();

    public static ArrayList<BinaryStateSingleFluidHandler.BinaryStateSingleFluidHandlerItem> honeyBottles() {
        if (HONEY_BOTTLES.isEmpty()) {
            if (create_honey_fluid != null)
                HONEY_BOTTLES.add(BinaryStateSingleFluidHandler.BinaryStateSingleFluidHandlerItem.registerBottle(create_honey_fluid, 250, new ItemStack(Items.HONEY_BOTTLE)));
            if (cofh_honey_fluid != null)
                HONEY_BOTTLES.add(BinaryStateSingleFluidHandler.BinaryStateSingleFluidHandlerItem.registerBottle(cofh_honey_fluid, 250, new ItemStack(Items.HONEY_BOTTLE)));
            if (resourcefulbees_honey_fluid != null)
                HONEY_BOTTLES.add(BinaryStateSingleFluidHandler.BinaryStateSingleFluidHandlerItem.registerBottle(resourcefulbees_honey_fluid, 250, new ItemStack(Items.HONEY_BOTTLE)));
        }
        return HONEY_BOTTLES;
    }

    /**
     * wait and try to populate the lists later than the calling on ObjectHolder
     * Good, this did the trick!
     */
    static {
        EventManager.delayedTask(1, ()->{
            xpBottles();
            honeyBottles();
        });
    }

    @ObjectHolder("mekanism:oxygen")
    public static final Fluid mekanism_air_fluid = null;
}
