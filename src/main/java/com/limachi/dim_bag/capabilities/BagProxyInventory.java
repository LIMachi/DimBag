package com.limachi.dim_bag.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.function.Function;
/*
public class BagProxyInventory implements IEnergyStorage, IItemHandler, IFluidHandler, ICapabilityProvider {
    private int bagId;
    private WeakReference<IItemHandler> itemHandlerWeakReference = new WeakReference<>(null);
    private WeakReference<IFluidHandler> fluidHandlerWeakReference = new WeakReference<>(null);
    private WeakReference<IEnergyStorage> energyStorageWeakReference = new WeakReference<>(null);

    final public int getbagId() { return bagId; }

    final public BagProxyInventory setbagId(int eye) {
        bagId = eye;
        itemHandlerWeakReference = new WeakReference<>(null); //reset the references to make sure the next time we access those handler, they point to the correct bag
        fluidHandlerWeakReference = new WeakReference<>(null);
        energyStorageWeakReference = new WeakReference<>(null);
        return this;
    }

    final protected <T> T itemHandler(Function<IItemHandler, T> run, T def) {
        if (itemHandlerWeakReference.get() == null)
            itemHandlerWeakReference = new WeakReference<>(InventoryData.getInstance(bagId));
        IItemHandler t = itemHandlerWeakReference.get();
        if (t == null) return def;
        return run.apply(t);
    }

    final protected <T> T fluidHandler(Function<IFluidHandler, T> run, T def) {
        if (fluidHandlerWeakReference.get() == null)
            fluidHandlerWeakReference = new WeakReference<>(TankData.getInstance(bagId));
        IFluidHandler t = fluidHandlerWeakReference.get();
        if (t == null) return def;
        return run.apply(t);
    }

    final protected <T> T energyStorage(Function<IEnergyStorage, T> run, T def) {
        if (energyStorageWeakReference.get() == null)
            energyStorageWeakReference = new WeakReference<>(EnergyData.getInstance(bagId));
        IEnergyStorage t = energyStorageWeakReference.get();
        if (t == null) return def;
        return run.apply(t);
    }

    @Override
    public int getSlots() { return itemHandler(IItemHandler::getSlots, 0); }

    @Override
    public ItemStack getStackInSlot(int index) { return itemHandler(h->h.getStackInSlot(index), ItemStack.EMPTY); }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) { return itemHandler(h->h.insertItem(slot, stack, simulate), ItemStack.EMPTY); }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) { return itemHandler(h->h.extractItem(slot, amount, simulate), ItemStack.EMPTY); }

    @Override
    public int getSlotLimit(int index) { return itemHandler(h->h.getSlotLimit(index), 0); }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) { return itemHandler(h->h.isItemValid(slot, stack), false); }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) { return energyStorage(h->h.receiveEnergy(maxReceive, simulate), 0); }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) { return energyStorage(h->h.extractEnergy(maxExtract, simulate), 0); }

    @Override
    public int getEnergyStored() { return energyStorage(IEnergyStorage::getEnergyStored, 0); }

    @Override
    public int getMaxEnergyStored() { return energyStorage(IEnergyStorage::getMaxEnergyStored, 0); }

    @Override
    public boolean canExtract() { return energyStorage(IEnergyStorage::canExtract, false); }

    @Override
    public boolean canReceive() { return energyStorage(IEnergyStorage::canReceive, false); }

    @Override
    public int getTanks() { return fluidHandler(IFluidHandler::getTanks, 0); }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) { return fluidHandler(h->h.getFluidInTank(tank), FluidStack.EMPTY); }

    @Override
    public int getTankCapacity(int tank) { return fluidHandler(h->h.getTankCapacity(tank), 0); }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) { return fluidHandler(h->h.isFluidValid(tank, stack), false); }

    @Override
    public int fill(FluidStack resource, FluidAction action) { return fluidHandler(h->h.fill(resource, action), 0); }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) { return fluidHandler(h->h.drain(resource, action), FluidStack.EMPTY); }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) { return fluidHandler(h->h.drain(maxDrain, action), FluidStack.EMPTY); }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || cap == CapabilityEnergy.ENERGY || cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            return LazyOptional.of(()->this).cast();
        return LazyOptional.empty();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) { return getCapability(cap, null); }
}
*/