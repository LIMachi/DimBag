package com.limachi.dimensional_bags.common.data.EyeDataMK2;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.function.Consumer;
import java.util.function.Function;

public class EnergyData extends WorldSavedDataManager.EyeWorldSavedData implements IEnergyStorage {

    private long energy;
    private long capacity;
    private int tickCursor;
    public static final int ENERGY = 0;
    public static final int RECEIVED = 1;
    public static final int EXTRACTED = 2;
    private final long[] extractedLastMinute = new long[1200];
    private final long[] receivedLastMinute = new long[1200];
    private final long[] energyStateLastMinute = new long[1200];

    public EnergyData(String suffix, int id, boolean client) {
        super(suffix, id, client);
        energy = 0;
        capacity = 0;
        tickCursor = 0;
    }

    public void changeBatterySize(long newSize) {
        capacity = newSize;
        if (energy > newSize)
            energy = newSize;
        markDirty();
    }

    public void tick() {
        ++tickCursor;
        if (tickCursor >= 1200)
            tickCursor = 0;
        extractedLastMinute[tickCursor] = 0;
        receivedLastMinute[tickCursor] = 0;
        energyStateLastMinute[tickCursor] = energy;
    }

    public long[][] getLastMinuteGraph() {
        long[][] out = new long[1200][3];
        for (int i = 0; i < 1200; ++i) {
            int j = (i + tickCursor) % 1200;
            out[i][ENERGY] = energyStateLastMinute[j];
            out[i][RECEIVED] = receivedLastMinute[j];
            out[i][EXTRACTED] = extractedLastMinute[j];
        }
        return out;
    }

    @Override
    public void read(CompoundNBT nbt) {
        capacity = nbt.getInt("Capacity");
        energy = nbt.getInt("Energy");
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        nbt.putLong("Capacity", capacity);
        nbt.putLong("Energy", energy);
        return nbt;
    }

    static public EnergyData getInstance(int id) {
        return WorldSavedDataManager.getInstance(EnergyData.class, null, id);
    }

    static public <T> T execute(int id, Function<EnergyData, T> executable, T onErrorReturn) {
        return WorldSavedDataManager.execute(EnergyData.class, null, id, executable, onErrorReturn);
    }

    static public boolean execute(int id, Consumer<EnergyData> executable) {
        return WorldSavedDataManager.execute(EnergyData.class, null, id, data->{executable.accept(data); return true;}, false);
    }

    @Override
    public int receiveEnergy(int receive, boolean simulate) {
        long energyReceived = Math.min(capacity - energy, receive);
        if (!simulate && energyReceived != 0) {
            energy += energyReceived;
            receivedLastMinute[tickCursor] += energyReceived;
            energyStateLastMinute[tickCursor] = energy;
            markDirty();
        }
        return (int)energyReceived;
    }

    @Override
    public int extractEnergy(int extract, boolean simulate) {
        long energyExtracted = Math.min(energy, extract);
        if (!simulate && energyExtracted != 0) {
            energy -= energyExtracted;
            extractedLastMinute[tickCursor] += energyExtracted;
            energyStateLastMinute[tickCursor] = energy;
            markDirty();
        }
        return (int)energyExtracted;
    }

    @Override
    public int getEnergyStored() { return (int)energy; }

    @Override
    public int getMaxEnergyStored() { return (int)capacity; }

    @Override
    public boolean canExtract() { return true; }

    @Override
    public boolean canReceive() { return true; }
}
