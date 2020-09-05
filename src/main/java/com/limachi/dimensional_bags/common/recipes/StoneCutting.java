package com.limachi.dimensional_bags.common.recipes;

import com.google.gson.JsonObject;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

public class StoneCutting extends StonecuttingRecipe implements IRecipe {
    private final ResourceLocation ID;
    private final ResourceLocation NAME;
    private final Function<IInventory, ItemStack> RESULT;
    private final IRecipeSerializer<?> SERIALIZER;

    public StoneCutting(ResourceLocation name, Ingredient ingredient, Function<IInventory, ItemStack> result) {
        this(null, ingredient, result, name, null);
    }

    private StoneCutting(ResourceLocation idIn, Ingredient ingredient, Function<IInventory, ItemStack> result, ResourceLocation name, IRecipeSerializer<?> serializer) {
        super(idIn, result.apply(new Inventory(ingredient.getMatchingStacks()[0])).getItem().toString(), ingredient, ItemStack.EMPTY);
        this.ID = idIn;
        this.NAME = name;
        this.RESULT = result;
        if (serializer == null) {
            class AdvancedRecipeSerializer extends net.minecraftforge.registries.ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<StoneCutting> {
                private final StoneCutting craft;

                public AdvancedRecipeSerializer(StoneCutting craft) {
                    this.craft = craft;
                    this.setRegistryName(NAME);
                }

                @Override
                @Nonnull
                public StoneCutting read(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
                    return craft.cloneWithId(recipeId);
                }

                @Override
                @Nullable
                public StoneCutting read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer) {
                    return craft.cloneWithId(recipeId);
                }

                @Override
                public void write(@Nonnull PacketBuffer buffer, @Nonnull StoneCutting recipe) {
                }
            }
            this.SERIALIZER = new AdvancedRecipeSerializer(this);
        }
        else
            this.SERIALIZER = serializer;
    }

    public ItemStack getCraftingResult(IInventory inv) { return RESULT.apply(inv); }

    public ItemStack getRecipeOutput() { return getCraftingResult(new Inventory(ingredient.getMatchingStacks()[0])); }

    public ItemStack getIcon() { return getRecipeOutput(); }

    public ResourceLocation getId() { return ID; }

    public IRecipeType<?> getType() { return IRecipeType.STONECUTTING; }

    public ResourceLocation getName() { return NAME; }

    public StoneCutting cloneWithId(ResourceLocation id) { return new StoneCutting(id, this.ingredient, RESULT, NAME, SERIALIZER); }

    @Override
    @Nonnull
    public IRecipeSerializer<?> getSerializer() { return this.SERIALIZER; }
}
