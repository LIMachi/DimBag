package com.limachi.dimensional_bags.common.recipes;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;

public interface IRecipe extends net.minecraft.item.crafting.IRecipe<IInventory> {
    public ResourceLocation getName();
    default public void register(net.minecraftforge.registries.IForgeRegistry<IRecipeSerializer<?>> registry) { registry.register(getSerializer()); }
}
