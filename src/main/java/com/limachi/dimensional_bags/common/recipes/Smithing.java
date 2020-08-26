package com.limachi.dimensional_bags.common.recipes;

import com.google.gson.JsonObject;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.SmithingRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Smithing extends SmithingRecipe implements IRecipe { //smithing use a crafting system similar to that of an anvil, which is not a simple IRecipe<IInventory> implementation
    private final ResourceLocation ID;
    private final NonNullList<Ingredient> INGREDIENTS;
    private final NonNullList<ItemStack> REMAINDER;
    private final Function<IInventory, ItemStack> RESULT;
    private final BiFunction<IInventory, World, Boolean> MATCHER;
    private final ResourceLocation NAME;

    public Smithing(ResourceLocation name, NonNullList<Ingredient> ingredients, NonNullList<ItemStack> remainder, BiFunction<IInventory, World, Boolean> matcher, Function<IInventory, ItemStack> result) {
        this(null, ingredients, remainder, matcher, result, name);
    }

    private Smithing(ResourceLocation idIn, NonNullList<Ingredient> ingredients, NonNullList<ItemStack> remainder, BiFunction<IInventory, World, Boolean> matcher, Function<IInventory, ItemStack> result, ResourceLocation name) {
        super(idIn, ingredients.get(0), ingredients.get(1), ItemStack.EMPTY);
        this.ID = idIn;
        this.INGREDIENTS = ingredients;
        this.REMAINDER = remainder;
        this.MATCHER = matcher;
        this.RESULT = result;
        this.NAME = name;
    }

    public boolean isValidAdditionItem(ItemStack addition) { return INGREDIENTS.get(1).test(addition); }

    public boolean matches(IInventory inv, World worldIn) { return MATCHER.apply(inv, worldIn); }

    public ItemStack getCraftingResult(IInventory inv) { return RESULT.apply(inv); }

    public ItemStack getRecipeOutput() { return ItemStack.EMPTY; }

    public ItemStack getIcon() { return new ItemStack(Blocks.SMITHING_TABLE); }

    public ResourceLocation getId() { return ID; }

    public IRecipeType<?> getType() { return IRecipeType.SMITHING; }

    public ResourceLocation getName() { return NAME; }

    public Smithing cloneWithId(ResourceLocation id) { return new Smithing(id, INGREDIENTS, REMAINDER, MATCHER, RESULT, NAME); }

    @Override
    @Nonnull
    public IRecipeSerializer<?> getSerializer() {
        class AdvancedRecipeSerializer extends net.minecraftforge.registries.ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<Smithing> {
            private final Smithing craft;

            public AdvancedRecipeSerializer(Smithing craft) { this.craft = craft; }

            @Override
            @Nonnull
            public Smithing read(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) { return craft.cloneWithId(recipeId); }

            @Override
            @Nullable
            public Smithing read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer) { return craft.cloneWithId(recipeId); }

            @Override
            public void write(@Nonnull PacketBuffer buffer, @Nonnull Smithing recipe) {}
        }

        return new AdvancedRecipeSerializer(this);
    }
}
