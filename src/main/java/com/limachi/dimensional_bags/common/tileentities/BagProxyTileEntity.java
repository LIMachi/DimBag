package com.limachi.dimensional_bags.common.tileentities;

import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.blocks.BagProxy;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.EnergyData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.InventoryData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.TankData;
import com.limachi.dimensional_bags.common.items.Bag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
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

@StaticInit
public class BagProxyTileEntity extends BaseTileEntity implements IEnergyStorage, IItemHandler, IFluidHandler {

    public static final String NAME = "bag_proxy";

    static {
        Registries.registerTileEntity(NAME, BagProxyTileEntity::new, ()->Registries.getBlock(BagProxy.NAME), null);
    }

    public BagProxyTileEntity() { super(Registries.getTileEntityType(NAME)); }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || cap == CapabilityEnergy.ENERGY || cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            return LazyOptional.of(()->this).cast();
        return super.getCapability(cap, side);
    }

    /**
     * used by the block to know if this proxy should run without a bag attached
     */
    public boolean isOP() { return getTileData().getBoolean("IsOP"); }
    public void setOP(boolean state) { getTileData().putBoolean("IsOP", state); markDirty(); }

    public int eyeId() { return getTileData().getInt("EyeId"); }
    public void setID(int eye) {
        getTileData().putInt("EyeId", eye);
        itemHandlerWeakReference = new WeakReference<>(null); //reset the references to make sure the next time we access those handler, they point to the correct bag
        fluidHandlerWeakReference = new WeakReference<>(null);
        energyStorageWeakReference = new WeakReference<>(null);
        markDirty();
    }

    private WeakReference<IItemHandler> itemHandlerWeakReference = new WeakReference<>(null);
    protected <T> T itemHandler(Function<IItemHandler, T> run, T def) {
        if (itemHandlerWeakReference.get() == null)
            itemHandlerWeakReference = new WeakReference<>(InventoryData.getInstance(eyeId()));
        IItemHandler t = itemHandlerWeakReference.get();
        if (t == null) return def;
        return run.apply(t);
    }

    private WeakReference<IFluidHandler> fluidHandlerWeakReference = new WeakReference<>(null);
    protected <T> T fluidHandler(Function<IFluidHandler, T> run, T def) {
        if (fluidHandlerWeakReference.get() == null)
            fluidHandlerWeakReference = new WeakReference<>(TankData.getInstance(eyeId()));
        IFluidHandler t = fluidHandlerWeakReference.get();
        if (t == null) return def;
        return run.apply(t);
    }

    private WeakReference<IEnergyStorage> energyStorageWeakReference = new WeakReference<>(null);
    protected <T> T energyStorage(Function<IEnergyStorage, T> run, T def) {
        if (energyStorageWeakReference.get() == null)
            energyStorageWeakReference = new WeakReference<>(EnergyData.getInstance(eyeId()));
        IEnergyStorage t = energyStorageWeakReference.get();
        if (t == null) return def;
        return run.apply(t);
    }

    @Override
    public void tick(int tick) {
        int eye = eyeId();
        if (world instanceof ServerWorld)
            Bag.tickEye(eye, world, Bag.getFakePlayer(eye, (ServerWorld) world).get(), false);
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
}
