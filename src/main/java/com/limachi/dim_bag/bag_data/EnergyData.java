package com.limachi.dim_bag.bag_data;

import com.limachi.dim_bag.bag_modules.BatteryModule;
import com.limachi.lim_lib.Configs;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

public class EnergyData implements IEnergyStorage {

    @Configs.Config
    public static long ENERGY_STORAGE_PER_BATTERY = 1_000_000;
    public static final String ENERGY_STORAGE = "energy";

    private LazyOptional<EnergyData> handle = LazyOptional.of(()->this);
    private final BagInstance bag;
    private long totalEnergyStored;

    protected EnergyData(BagInstance bag) {
        this.bag = bag;
        totalEnergyStored = bag.unsafeRawAccess().getLong(ENERGY_STORAGE);
    }

    protected void store() {
        bag.unsafeRawAccess().putLong(ENERGY_STORAGE, totalEnergyStored);
    }

    public long removeBatteryModule() {
        long max = trueMaxEnergyStored();
        if (totalEnergyStored > max) {
            long out = totalEnergyStored - max;
            totalEnergyStored = max;
            return out;
        }
        return 0L;
    }

    public void addBatteryModule(long energy) {
        totalEnergyStored += energy;
    }

    public void invalidate() {
        if (handle != null)
            handle.invalidate();
        handle = null;
    }

    public LazyOptional<EnergyData> getHandle() {
        if (handle == null)
            handle = LazyOptional.of(()->this);
        return handle;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        long available = Math.max(0L, trueMaxEnergyStored() - totalEnergyStored);
        if (available > 0) {
            long insert = Math.min(maxReceive, available);
            if (!simulate)
                totalEnergyStored += insert;
            return (int)insert;
        }
        return 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extract = Math.max(0, Math.min(maxExtract, (int)totalEnergyStored));
        if (!simulate)
            totalEnergyStored -= extract;
        return extract;
    }

    public long trueEnergyStored() { return totalEnergyStored; }

    @Override
    public int getEnergyStored() { return (int)totalEnergyStored; }

    public long trueMaxEnergyStored() {
        return ENERGY_STORAGE_PER_BATTERY * (long)bag.getAllModules(BatteryModule.NAME).getAllKeys().size();
    }

    @Override
    public int getMaxEnergyStored() { return (int)trueMaxEnergyStored(); }

    @Override
    public boolean canExtract() { return bag.isModulePresent(BatteryModule.NAME); }

    @Override
    public boolean canReceive() { return bag.isModulePresent(BatteryModule.NAME); }
}
