package com.limachi.dimensional_bags.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

public class FluidUtils {
    /**
     * test if the given fluid has an equivalent in potion form
     * @param stack
     * @return
     */
    public static boolean isPotion(FluidStack stack) {
        String fluidSimpleName = stack.getFluid().getRegistryName().getPath();
        for (ResourceLocation e : ForgeRegistries.POTION_TYPES.getKeys())
            if (e.getPath().equals(fluidSimpleName)) return true;
        return false;
    }

    /**
     * convert the given fluid to a potion item, handling of shrinking of the fluidstack and multiplication of the potion is to be handled by the caller
     * the potion does not include variations like throwing and lingering, only drinkable
     * @param stack
     * @return ItemStack.EMPTY if the given fluidstack isn't a potion
     */
    public static ItemStack asPotion(FluidStack stack) {
        String fluidSimpleName = stack.getFluid().getRegistryName().getPath();
        for (Map.Entry<RegistryKey<Potion>, Potion> e : ForgeRegistries.POTION_TYPES.getEntries())
            if (e.getKey().getRegistryName().getPath().equals(fluidSimpleName)) {
                ItemStack out = new ItemStack(Items.POTION);
                PotionUtils.setPotion(out, e.getValue());
                return out;
            }
        return ItemStack.EMPTY;
    }

//    /**
//     * convert the given item to a fluid (if the given item is a potion and we can found the equivalent form in fluids)
//     * @param potion
//     * @return FluidStack.EMPTY if the given ItemStack isn't a potion or does not have a fluid form, or as too many effects
//     */
//    public static FluidStack fromPotion(ItemStack potion) {
//        if (!(potion.getItem() instanceof PotionItem)) return FluidStack.EMPTY;
//        List<EffectInstance> effects = PotionUtils.getEffectsFromStack(potion);
//        if (effects.size() != 1) return FluidStack.EMPTY;
//        EffectInstance effect = effects.get(0);
//        String effectSimpleName = effect.getPotion().getRegistryName().getPath();
//        if (effect.getAmplifier() > 0)
//            effectSimpleName = "strong_" + effectSimpleName;
//        else if (effect.getDuration())
//    }
}
