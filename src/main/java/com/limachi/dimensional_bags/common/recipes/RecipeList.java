package com.limachi.dimensional_bags.common.recipes;

import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistry;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

public class RecipeList {
    public static final IRecipe[] RECIPES = {
            new StoneCutting(
                new ResourceLocation(MOD_ID, "slot_from_enderchest"),
                Ingredient.fromItems(Items.ENDER_CHEST),
                (IInventory inv) -> /*IDimBagCommonItem.addToStringList(IDimBagCommonItem.addToStringList(*/new ItemStack(UpgradeManager.getUpgrade("upgrade_slot").getItem(), 9)/*, IDimBagCommonItem.onTickCommands, "cmd.multiply.random.9:27"), IDimBagCommonItem.onTickCommands, "msg.translate.tooltip.upgrade.slot.craft_result")*/),
            new Smithing(new ResourceLocation(MOD_ID, "add_armor_to_bag"),
                ()->{
                    ITag test = ItemTags.getCollection().get(new ResourceLocation("dim_bag", "armor/chestplate"));
                return NonNullList.from(Ingredient.EMPTY, Ingredient.fromItems(Registries.BAG_ITEM.get()), test != null ? Ingredient.fromTag(test) : Ingredient.fromItems(Items.IRON_CHESTPLATE, Items.DIAMOND_CHESTPLATE));
                },
                NonNullList.create(),
                (IInventory inv, World world)->{
                    if (inv.getStackInSlot(0).getItem() instanceof Bag && inv.getStackInSlot(1).getItem() instanceof ArmorItem && ((ArmorItem)inv.getStackInSlot(1).getItem()).getEquipmentSlot() == EquipmentSlotType.CHEST) {
//                        EyeData data = EyeData.get(null, Bag.getId(inv.getStackInSlot(0)));
//                        if (data == null)
//                            return false;
                        return true;
//                        return this.getCount(data) < this.limit;
                    }
                    return false;
                },
                (IInventory inv)->{
                    ItemStack out = inv.getStackInSlot(0).copy();
                    if (out.hasTag())
                        out.getTag().put("ChestPlate", inv.getStackInSlot(1).serializeNBT());
                    return out; })
    };

    public static void registerRecipes(IForgeRegistry<IRecipeSerializer<?>> registry) {
        for (IRecipe recipe : RECIPES) recipe.register(registry);
    }
}
