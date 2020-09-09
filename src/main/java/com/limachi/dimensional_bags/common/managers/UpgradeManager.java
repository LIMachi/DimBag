package com.limachi.dimensional_bags.common.managers;

import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.managers.upgrades.*;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.*;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.HashMap;
import java.util.Map;

public class UpgradeManager {
    public static final int ROWS = 0;
    public static final int COLUMNS = 1;
    public static final int RADIUS = 2;
    public static final int SLOT = -1;
    public static final int SLOT_x4 = -2;
    public static final int SLOT_x9 = -3;

    /*
    private static Upgrade upgrades[] = { //those upgrades require an int stored in data
            new RowUpgrade(), //augment the row
            new ColumnUpgrade(),
            new RadiusUpgrade()
    };

    private static Upgrade upgradeVariants[] = { //those upgrades have an effect that dosen't require to use an int
            new SlotUpgrade(1),
            new SlotUpgrade(4),
            new SlotUpgrade(9)
    };
     */

    public static final Map<String, Upgrade> UPGRADES = new HashMap<>();
    static {
        new RowUpgrade().attach(UPGRADES);
        new ColumnUpgrade().attach(UPGRADES);
        new RadiusUpgrade().attach(UPGRADES);
        new SlotUpgrade(1).attach(UPGRADES);
        new SlotUpgrade(4).attach(UPGRADES);
        new SlotUpgrade(9).attach(UPGRADES);
        new ElytraUpgrade().attach(UPGRADES);
        new TankUpgrade().attach(UPGRADES);
        new TankCapacityUpgrade().attach(UPGRADES);
    }

    /*
    public static String getSId(int id) { return id < 0 || id >= upgrades.length ? -id - 1 >= upgradeVariants.length ? null : upgradeVariants[-id - 1].getId() : upgrades[id].getId(); }
    public static int getLimit(int id) { return id < 0 || id >= upgrades.length ? -id - 1 >= upgradeVariants.length ? 0 : upgradeVariants[-id - 1].getLimit() : upgrades[id].getLimit(); }
    public static int getStart(int id) { return id < 0 || id >= upgrades.length ? -id - 1 >= upgradeVariants.length ? 0 : upgradeVariants[-id - 1].getStart() : upgrades[id].getStart(); }
    public static String getDescription(int id) { return id < 0 || id >= upgrades.length ? -id - 1 >= upgradeVariants.length ? "" : upgradeVariants[-id - 1].getDescription() : upgrades[id].getDescription(); }

    public static ItemStack defaultStack(int id) {
        if (id < 0 && -id - 1 < upgradeVariants.length) return new ItemStack(upgradeVariants[-id - 1].getItem(), upgradeVariants[-id - 1].getStart());
        if (id < 0 || id >= upgrades.length) return ItemStack.EMPTY;
        return new ItemStack(upgrades[id].getItem(), upgrades[id].getStart());
    }

    public static Wrapper.IORights defaultRights(int id) {
        if (id < 0 && -id - 1 < upgradeVariants.length) return new Wrapper.IORights(Wrapper.IORights.CANINPUT, (byte)upgradeVariants[-id - 1].getStart(), (byte)upgradeVariants[-id - 1].getLimit());
        if (id < 0 || id >= upgrades.length) return new Wrapper.IORights();
        return new Wrapper.IORights(Wrapper.IORights.CANINPUT, (byte)upgrades[id].getStart(), (byte)upgrades[id].getLimit());
    }
    */

    public static Upgrade getUpgrade(String id) {
        return UPGRADES.get(id);
    }

    public static void bakeConfig() {
//        for (Upgrade upgrade : upgrades) upgrade.bakeConfig();
//        for (Upgrade upgradeVariant : upgradeVariants) upgradeVariant.bakeConfig();
        for (String key : UPGRADES.keySet())
            UPGRADES.get(key).bakeConfig();
    }

    public static void buildConfig(ForgeConfigSpec.Builder builder) {
//        for (Upgrade upgrade : upgrades) upgrade.buildConfig(builder);
//        for (Upgrade upgradeVariant : upgradeVariants) upgradeVariant.buildConfig(builder);
        for (String key : UPGRADES.keySet())
            UPGRADES.get(key).buildConfig(builder);
    }

    public static void registerItems(DeferredRegister<Item> itemsReg) {
//        for (Upgrade upgrade : upgrades) upgrade.register(itemsReg);
//        for (Upgrade upgradeVariant : upgradeVariants) upgradeVariant.register(itemsReg);
        for (String key : UPGRADES.keySet())
            UPGRADES.get(key).register(itemsReg);
    }

    public static void registerRecipes(IForgeRegistry<IRecipeSerializer<?>> registry) {
//        for (Upgrade upgrade : upgrades) upgrade.getRecipe().register(registry);
//        for (Upgrade upgradeVariant : upgradeVariants) upgradeVariant.getRecipe().register(registry);
        for (String key : UPGRADES.keySet())
            UPGRADES.get(key).getRecipe().register(registry);
    }

    public static void startingUpgrades(EyeData data) {
        for (String key : UPGRADES.keySet()) {
            Upgrade upgrade = UPGRADES.get(key);
            if (upgrade.getStart() > 0)
                upgrade.setCount(data, upgrade.getStart());
        }
    }

    /*
    public static int[] startingUpgrades() {
        int out[] = new int[upgrades.length];
        for (int i = 0; i < upgrades.length; ++i)
            out[i] = upgrades[i].getStart();
        return out;
    }

    public static Item getItemById(int id) {
        if (id < 0 && -id - 1 < upgradeVariants.length) return upgradeVariants[-id - 1].getItem();
        if (id < 0 || id >= upgrades.length) return Items.AIR;
        return upgrades[id].getItem();
    }

    public static Integer getIdByStack(ItemStack stack) { //return null on invalid stack
        Item item = stack.getItem();
        if (!(item instanceof Upgrade.UpgradeItem)) return null; //not an upgrade
        for (int i = 0; i < upgrades.length; ++i) {
            if (item.getRegistryName() == upgrades[i].getItem().getRegistryName())
                return i;
        }
        for (int i = 0; i < upgradeVariants.length; ++i) {
            if (item.getRegistryName() == upgradeVariants[i].getItem().getRegistryName())
                return -i - 1;
        }
        return null;
    }

    public static Integer getIdByName(String name) { //return null on invalid stack
        for (int i = 0; i < upgrades.length; ++i) {
            if (name.equals(upgrades[i].getId()))
                return i;
        }
        for (int i = 0; i < upgradeVariants.length; ++i) {
            if (name.equals(upgradeVariants[i].getId()))
                return -i - 1;
        }
        return null;
    }

    public static int upgradesCount() { return upgrades.length; }
    public static int upgradeVariantsCount() { return upgradeVariants.length; }
     */
}