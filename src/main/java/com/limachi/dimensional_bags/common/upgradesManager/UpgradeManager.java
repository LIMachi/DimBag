package com.limachi.dimensional_bags.common.upgradesManager;

import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.upgradesManager.upgrades.ColumnUpgrade;
import com.limachi.dimensional_bags.common.upgradesManager.upgrades.RadiusUpgrade;
import com.limachi.dimensional_bags.common.upgradesManager.upgrades.RowUpgrade;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.DeferredRegister;

public class UpgradeManager {
    private static Upgrade upgrades[] = {
            new RowUpgrade(),
            new ColumnUpgrade(),
            new RadiusUpgrade(),
    };

    public static int getLimit(int id) { return id < 0 || id >= upgrades.length ? 0 : upgrades[id].getLimit(); }

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
        if (!(item instanceof ItemUpgrade)) return -1; //not an upgrade
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