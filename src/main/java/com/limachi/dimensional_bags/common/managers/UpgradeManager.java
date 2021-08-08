package com.limachi.dimensional_bags.common.managers;

import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.WorldSavedDataManager;
import com.limachi.dimensional_bags.common.items.upgrades.BaseUpgrade;
import com.limachi.dimensional_bags.common.items.upgrades.HiddenUpgrade;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class UpgradeManager extends WorldSavedDataManager.EyeWorldSavedData {

    public static final String COUNT_NBT_KEY = "Count";

    protected static final HashSet<String> UPGRADES = new HashSet<>();

    public static void registerUpgrade(String id, Supplier<? extends BaseUpgrade> sup) {
        UPGRADES.add(id);
        Registries.registerItem(id, sup);
    }

    public static BaseUpgrade getUpgrade(String id) { return Registries.getItem(id); }

    /**
     * helper function to quickly test if an upgrade is installed for the given bag id
     */
    public static boolean hasUpgrade(int id, String upgradeName) {
        return execute(id, um->um.getInstalledUpgrades().contains(upgradeName), false);
    }

    private CompoundNBT upgradesNBT;

    public UpgradeManager(int id) { this("upgrade_manager", id, true); }

    public UpgradeManager(String suffix, int id, boolean client) {
        super(suffix, id, client, false);
        upgradesNBT = new CompoundNBT();
    }

    public CompoundNBT getUpgradesNBT() { return upgradesNBT; }

    public static Set<String> getUpgradesNames() { return UPGRADES; }

    public static Optional<CompoundNBT> isUpgradeInstalled(int eyeId, String upgradeName) {
        return execute(eyeId, um -> um.getInstalledUpgrades().contains(upgradeName) ? Optional.of(um.getUpgradesNBT().getCompound(upgradeName)) : Optional.empty(), Optional.empty());
    }

    public CompoundNBT getMemory(String key, boolean createIfMissing) {
        if (createIfMissing && !upgradesNBT.contains(key)) {
            upgradesNBT.put(key, new CompoundNBT());
            markDirty();
        }
        return upgradesNBT.getCompound(key);
    }

    public ArrayList<String> getInstalledUpgrades() {
        ArrayList<String> out = new ArrayList<>();
        for (String key : UPGRADES)
            if (getMemory(getUpgrade(key).getMemoryKey(), false).getInt(COUNT_NBT_KEY) > 0)
                out.add(key);
        return out;
    }

    public int getUpgradeCount(String name) {
        BaseUpgrade upgrade = getUpgrade(name);
        if (upgrade != null)
            return getMemory(upgrade.getMemoryKey(), false).getInt(COUNT_NBT_KEY);
        return 0;
    }

    public void inventoryTick(World worldIn, Entity entityIn) {
        for (String upgrade : getInstalledUpgrades()) {
            BaseUpgrade up = getUpgrade(upgrade);
            if (up.isActive(getEyeId()))
                up.upgradeEntityTick(getEyeId(), worldIn, entityIn);
        }
        getUpgrade(HiddenUpgrade.NAME).upgradeEntityTick(getEyeId(), worldIn, entityIn);
    }

    @Override
    public void read(CompoundNBT nbt) { upgradesNBT = nbt; }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        nbt.merge(upgradesNBT);
        return nbt;
    }

    static public UpgradeManager getInstance(int id) {
        return WorldSavedDataManager.getInstance(UpgradeManager.class, null, id);
    }

    static public <T> T execute(int id, Function<UpgradeManager, T> executable, T onErrorReturn) {
        return WorldSavedDataManager.execute(UpgradeManager.class, null, id, executable, onErrorReturn);
    }

    static public boolean execute(int id, Consumer<UpgradeManager> executable) {
        return WorldSavedDataManager.execute(UpgradeManager.class, null, id, data->{executable.accept(data); return true;}, false);
    }
}