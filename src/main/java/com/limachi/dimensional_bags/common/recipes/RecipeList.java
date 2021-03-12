package com.limachi.dimensional_bags.common.recipes;

import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraftforge.registries.IForgeRegistry;

public class RecipeList {
    public static final IRecipe[] RECIPES = {
    };

    public static void registerRecipes(IForgeRegistry<IRecipeSerializer<?>> registry) {
        for (IRecipe recipe : RECIPES) recipe.register(registry);
    }
}
