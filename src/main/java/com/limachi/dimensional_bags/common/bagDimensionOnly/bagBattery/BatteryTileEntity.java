package com.limachi.dimensional_bags.common.bagDimensionOnly.bagBattery;

import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.references.Registries;
import com.limachi.dimensional_bags.lib.common.tileentities.BaseTileEntity;
import com.limachi.dimensional_bags.lib.common.tileentities.IInstallUpgradeTE;
import com.limachi.dimensional_bags.lib.common.tileentities.IisBagTE;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.EnergyData;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.SubRoomsManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@StaticInit
public class BatteryTileEntity extends BaseTileEntity implements IEnergyStorage, IisBagTE, IInstallUpgradeTE {

    public static final String NAME = "battery";

    WeakReference<EnergyData> data = new WeakReference<>(null);

    static {
        Registries.registerTileEntity(NAME, BatteryTileEntity::new, ()->Registries.getBlock(BatteryBlock.NAME), null);
    }

    public BatteryTileEntity() { super(Registries.getBlockEntityType(NAME)); hasTileData = false; }

    public int getLocalEnergy() {
        EnergyData ed = data.get();
        if (ed != null)
            return ed.getSingleCrystalEnergy();
        return 0;
    }

    public <T> List<T> getSurroundingCapabilities(Capability<T> capability, boolean push){
        if(this.level == null)
            return Collections.emptyList();
        ArrayList<T> list = new ArrayList<>();
        for(Direction facing : Direction.values()){
            TileEntity tile = this.level.getBlockEntity(this.worldPosition.offset(facing.getNormal()));
            if(tile != null && !(tile instanceof BatteryTileEntity) && getBlockState().getValue(BatteryBlock.PULL) != push)
                tile.getCapability(capability, facing.getOpposite()).ifPresent(list::add);
        }
        return list;
    }

    @Override
    public void tick(int tick) {
        EnergyData ed = getEnergyData();
        if (ed != null && ed.canReceive() && ed.getEnergyStored() < ed.getMaxEnergyStored()) //pull
            for (IEnergyStorage storage : getSurroundingCapabilities(CapabilityEnergy.ENERGY, false)) {
                if (!storage.canExtract())
                    continue;
                int amount = storage.extractEnergy(ed.getMaxEnergyInput(), true);
                if (amount > 0)
                    storage.extractEnergy(ed.receiveEnergy(amount, false), false);
            }
        if (ed != null && ed.canExtract() && ed.getEnergyStored() > 0) //push
            for (IEnergyStorage storage : getSurroundingCapabilities(CapabilityEnergy.ENERGY, true)) {
                if (!storage.canReceive())
                    continue;
                int amount = ed.extractEnergy(ed.getMaxEnergyOutput(), true);
                if (amount > 0)
                    ed.extractEnergy(storage.receiveEnergy(amount, false), false);
            }
    }

    protected EnergyData getEnergyData() {
        if (data.get() == null)
            data = new WeakReference<>(EnergyData.getInstance(SubRoomsManager.getbagId(level, worldPosition, false)));
        return data.get();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
        if (capability == CapabilityEnergy.ENERGY)
            return LazyOptional.of(()->this).cast();
        return super.getCapability(capability, facing);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        EnergyData ed = data.get();
        if (ed == null) return 0;
        return ed.receiveEnergy(maxReceive, simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        EnergyData ed = data.get();
        if (ed == null) return 0;
        return ed.extractEnergy(maxExtract, simulate);
    }

    @Override
    public int getEnergyStored() {
        EnergyData ed = data.get();
        if (ed == null) return 0;
        return ed.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored() {
        EnergyData ed = data.get();
        if (ed == null) return 0;
        return ed.getMaxEnergyStored();
    }

    @Override
    public boolean canExtract() {
        EnergyData ed = data.get();
        if (ed == null) return false;
        return ed.canExtract();
    }

    @Override
    public boolean canReceive() {
        EnergyData ed = data.get();
        if (ed == null) return false;
        return ed.canReceive();
    }

    @Override
    public ItemStack installUpgrades(PlayerEntity player, ItemStack stack) {
        return stack;
    }
}