package com.limachi.dimensional_bags.common.upgradeManager;

import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.inventory.BaseItemStackAccessor;
import com.limachi.dimensional_bags.common.upgradeManager.upgrades.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.DeferredRegister;

public class UpgradeManager {
    public static final int ROWS = 0;
    public static final int COLUMNS = 1;
    public static final int RADIUS = 2;

    private static Upgrade upgrades[] = {
            new RowUpgrade(),
            new ColumnUpgrade(),
            new RadiusUpgrade(),
    };

    public static int getLimit(int id) { return id < 0 || id >= upgrades.length ? 0 : upgrades[id].getLimit(); }
    public static int getStart(int id) { return id < 0 || id >= upgrades.length ? 0 : upgrades[id].getStart(); }
    public static String getDescription(int id) { return id < 0 || id >= upgrades.length ? "" : upgrades[id].getDescription(); }

    public static BaseItemStackAccessor defaultStackAccessor(int id) {
        if (id < 0 || id >= upgrades.length) return new BaseItemStackAccessor();
        return new BaseItemStackAccessor(new ItemStack(upgrades[id].getItem(), upgrades[id].getStart()), true, false, upgrades[id].getStart(), upgrades[id].getLimit());
    }

    public static void bakeConfig() {
        for (int i = 0; i < upgrades.length; ++i)
            upgrades[i].bakeConfig();
    }

    public static void buildConfig(ForgeConfigSpec.Builder builder) {
        for (int i = 0; i < upgrades.length; ++i)
            upgrades[i].buildConfig(builder);
    }

    public static void register(DeferredRegister<Item> itemsReg) {
        for (int i = 0; i < upgrades.length; ++i)
            upgrades[i].register(itemsReg);
    }

    public static int[] startingUpgrades() {
        int out[] = new int[upgrades.length];
        for (int i = 0; i < upgrades.length; ++i)
            out[i] = upgrades[i].getStart();
        return out;
    }

    public static Item getItemById(int id) {
        if (id < 0 || id >= upgrades.length) return Items.AIR;
        return upgrades[id].getItem();
    }

    public static int getIdByStack(ItemStack stack) { //return -1 on invalid stack
        Item item = stack.getItem();
        if (!(item instanceof Upgrade.UpgradeItem)) return -1; //not an upgrade
        for (int i = 0; i < upgrades.length; ++i) {
            if (item.getRegistryName() == upgrades[i].getItem().getRegistryName())
                return i;
        }
        return -1;
    }

    public static void applyUpgrade(int id, int countBefore, int countAfter, EyeData data) {
        if (id < 0 || id >= upgrades.length) return;
        upgrades[id].applyUpgrade(countBefore, countAfter, data);
    }

    public static int upgradesCount() { return upgrades.length; }
}