package com.limachi.dimensional_bags.common.data.EyeDataMK2;

import com.limachi.dimensional_bags.ConfigManager.Config;
import com.limachi.dimensional_bags.common.blocks.Crystal;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Function;

public class EnergyData extends WorldSavedDataManager.EyeWorldSavedData implements IEnergyStorage {

    @Config
    public static int MAX_STORAGE = 2147483647;

    private int energy;
    private int capacity;
    private int tickCursor;
    public static final int ENERGY = 0;
    public static final int RECEIVED = 1;
    public static final int EXTRACTED = 2;
    private final int[] extractedLastMinute = new int[1200];
    private final int[] receivedLastMinute = new int[1200];
    private final int[] energyStateLastMinute = new int[1200];

    public EnergyData(String suffix, int id, boolean client) {
        super(suffix, id, client, false);
        energy = 0;
        capacity = 0;
        tickCursor = 0;
    }

    public EnergyData changeBatterySize(int newSize) {
        capacity = Integer.max(0, Integer.min(newSize, MAX_STORAGE));
        markDirty();
        return this;
    }

    public int getSingleCrystalEnergy() { return (int)Math.round(getEnergyStored() * (Crystal.ENERGY_PER_CRYSTAL / (double)getMaxEnergyStored())); }

    public void tick() {
        ++tickCursor;
        if (tickCursor >= 1200)
            tickCursor = 0;
        extractedLastMinute[tickCursor] = 0;
        receivedLastMinute[tickCursor] = 0;
        energyStateLastMinute[tickCursor] = energy;
    }

    public int[][] getLastMinuteGraph() {
        int[][] out = new int[1200][3];
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

    public static int transferFrom(int eye, int receive, @Nonnull IEnergyStorage from, boolean exactOnly) {
        return execute(eye, ed->{
            if (!ed.canReceive() || !from.canExtract()) return 0;
            if (exactOnly && (from.extractEnergy(receive, true) != receive || ed.receiveEnergy(receive, true) != receive)) return 0;
            return from.extractEnergy(ed.receiveEnergy(from.extractEnergy(receive, true), false), false);
            }, 0);
    }

    public static int receiveEnergy(int eye, int receive) {
        return execute(eye, ed->ed.receiveEnergy(receive, false), 0);
    }

    @Override
    public int receiveEnergy(int receive, boolean simulate) {
        int energyReceived = Integer.max(0, Integer.min(capacity - energy, receive));
        if (!simulate && energyReceived != 0) {
            energy += energyReceived;
            receivedLastMinute[tickCursor] += energyReceived;
            energyStateLastMinute[tickCursor] = energy;
            markDirty();
        }
        return (int)energyReceived;
    }

    public static int transferTo(int eye, int extract, @Nonnull IEnergyStorage to, boolean exactOnly) {
        return execute(eye, ed->{
            if (!ed.canReceive() || !to.canExtract()) return 0;
            if (exactOnly && (ed.extractEnergy(extract, true) != extract || to.receiveEnergy(extract, true) != extract)) return 0;
            return ed.extractEnergy(to.receiveEnergy(ed.extractEnergy(extract, true), false), false);
        }, 0);
    }

    public static int extractEnergy(int eye, int extract) {
        return execute(eye, ed->ed.extractEnergy(extract, false), 0);
    }

    @Override
    public int extractEnergy(int extract, boolean simulate) {
        int energyExtracted = Math.min(energy, extract);
        if (!simulate && energyExtracted != 0) {
            energy -= energyExtracted;
            extractedLastMinute[tickCursor] += energyExtracted;
            energyStateLastMinute[tickCursor] = energy;
            markDirty();
        }
        return energyExtracted;
    }

    public static int getEnergyStored(int eye) {
        return execute(eye, EnergyData::getEnergyStored, 0);
    }

    @Override
    public int getEnergyStored() { return (int)energy; }

    public static int getMaxEnergyStored(int eye) {
        return execute(eye, EnergyData::getMaxEnergyStored, 0);
    }

    @Override
    public int getMaxEnergyStored() { return (int)capacity; }

    @Override
    public boolean canExtract() { return true; }

    @Override
    public boolean canReceive() { return true; }
}
