package com.limachi.dimensional_bags.common.recipe;

import com.limachi.dimensional_bags.common.init.Registries;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class UpgradeRecipeBase extends ShapelessRecipe {

//    private BaseUpgrade upgrade;

    public UpgradeRecipeBase(final ResourceLocation id, ItemStack upgrade) {
        super(id, "", new ItemStack(Registries.BAG_ITEM.get()), NonNullList.from(Ingredient.EMPTY,
                Ingredient.fromStacks(new ItemStack(Registries.BAG_ITEM.get())),
                Ingredient.fromStacks(upgrade)));
//        this.upgrade = ((BaseUpgrade)upgrade.getItem());
    }

    @Override
    public boolean matches(CraftingInventory inv, World world) {
        if (super.matches(inv, world)) {
//            for (int i = 0; i < inv.getSizeInventory(); ++i)
        }
        return false;
    }
}
