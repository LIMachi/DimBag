package com.limachi.dimensional_bags.common.upgrades;

import com.limachi.dimensional_bags.common.references.Registries;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.WorldSavedDataManager;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class BagUpgradeManager extends WorldSavedDataManager.EyeWorldSavedData {

    public static final String COUNT_NBT_KEY = "Count";

    protected static final HashSet<String> UPGRADES = new HashSet<>();

    public static void registerUpgrade(String id, Supplier<? extends BaseUpgradeBag> sup) {
        UPGRADES.add(id);
        Registries.registerItem(id, sup);
    }

    public static BaseUpgradeBag getUpgrade(String id) { return Registries.getItem(id); }

    private CompoundNBT upgradesNBT;

    public BagUpgradeManager(int id) { this("upgrade_manager", id, true); }

    public BagUpgradeManager(String suffix, int id, boolean client) {
        super(suffix, id, client, false);
        upgradesNBT = new CompoundNBT();
    }

    public CompoundNBT getUpgradesNBT() { return upgradesNBT; }

    public static Set<String> getUpgradesNames() { return UPGRADES; }

    public static Optional<CompoundNBT> isUpgradeInstalled(int bagId, String upgradeName) {
        return execute(bagId, um -> um.getInstalledUpgrades().contains(upgradeName) ? Optional.of(um.getUpgradesNBT().getCompound(upgradeName)) : Optional.empty(), Optional.empty());
    }

    public CompoundNBT getMemory(String key, boolean createIfMissing) {
        if (createIfMissing && !upgradesNBT.contains(key)) {
            upgradesNBT.put(key, new CompoundNBT());
            setDirty();
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
        BaseUpgradeBag upgrade = getUpgrade(name);
        if (upgrade != null)
            return getMemory(upgrade.getMemoryKey(), false).getInt(COUNT_NBT_KEY);
        return 0;
    }

    public void inventoryTick(World worldIn, Entity entityIn) {
        for (String upgrade : getInstalledUpgrades()) {
            BaseUpgradeBag up = getUpgrade(upgrade);
            if (up.isActive(getbagId()))
                up.upgradeEntityTick(getbagId(), worldIn, entityIn);
        }
    }

    @Override
    public void load(CompoundNBT nbt) { upgradesNBT = nbt; }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        nbt.merge(upgradesNBT);
        return nbt;
    }

    static public BagUpgradeManager getInstance(int id) { return WorldSavedDataManager.getInstance(BagUpgradeManager.class, id); }

    static public <T> T execute(int id, Function<BagUpgradeManager, T> executable, T onErrorReturn) { return WorldSavedDataManager.execute(BagUpgradeManager.class, id, executable, onErrorReturn); }

    static public boolean execute(int id, Consumer<BagUpgradeManager> executable) { return WorldSavedDataManager.execute(BagUpgradeManager.class, id, data->{executable.accept(data); return true;}, false); }
}