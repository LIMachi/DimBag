package com.limachi.dimensional_bags.common.recipes;

import com.limachi.dimensional_bags.common.items.DimBagCommonItem;
import com.limachi.dimensional_bags.common.upgradeManager.UpgradeManager;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

public class RecipeList {
    public static final IRecipe[] RECIPES = {
            new StoneCutting(
                new ResourceLocation(MOD_ID, "slot_from_enderchest"),
                Ingredient.fromItems(Items.ENDER_CHEST),
                (IInventory inv) -> DimBagCommonItem.addToStringList(DimBagCommonItem.addToStringList(new ItemStack(UpgradeManager.getItemById(UpgradeManager.SLOT), 1), DimBagCommonItem.onTickCommands, "cmd.multiply.random.9:27"), DimBagCommonItem.onTickCommands, "msg.translate.tooltip.upgrade.slot.craft_result"))
    };

    public static void registerRecipes(IForgeRegistry<IRecipeSerializer<?>> registry) {
        for (IRecipe recipe : RECIPES) recipe.register(registry);
    }
}
