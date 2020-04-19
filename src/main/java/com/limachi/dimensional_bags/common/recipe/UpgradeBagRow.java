package com.limachi.dimensional_bags.common.recipe;

import com.limachi.dimensional_bags.common.init.Registries;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.items.BaseUpgrade;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class UpgradeBagRow extends ShapelessRecipe {

    private ItemStack upgrade;
    private int max_amount;

    public UpgradeBagRow(ResourceLocation id, ItemStack upgradeType, int max_amount) {
        super(id, "", new ItemStack(Registries.BAG_ITEM.get()), NonNullList.from(Ingredient.EMPTY,
            Ingredient.fromStacks(new ItemStack(Registries.BAG_ITEM.get())),
            Ingredient.fromStacks(upgradeType)));
        this.upgrade = upgradeType;
        this.max_amount = max_amount;
    }

    public ItemStack getUpgrade() { return upgrade; }

    public int getMaxAmount() { return max_amount; }

    @Override
    public boolean matches(CraftingInventory inv, World world) {
        if (super.matches(inv, world)) {
            ItemStack bag = null;
            for (int i = 0; i < inv.getSizeInventory(); ++i) {
                ItemStack it = inv.getStackInSlot(i);
                if (it.getItem() instanceof Bag) {
                    bag = it;
                } else if (it.getItem() instanceof BaseUpgrade) {
                    if (it.getItem().getClass() != upgrade.getItem().getClass())
                        return false;
                }
            }
            if (bag == null)
                return false;
            return Bag.getUpgrade(bag, ((BaseUpgrade)upgrade.getItem()).getId()) < max_amount;
        }
        return false;
    }
}
