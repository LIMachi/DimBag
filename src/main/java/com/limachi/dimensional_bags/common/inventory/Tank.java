package com.limachi.dimensional_bags.common.inventory;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;

public class Tank implements IFluidTank {

    private FluidStack fluid;
    private FluidStack whitelist;
    private int capacity;

    public Tank(int capacity) {
        this(capacity, FluidStack.EMPTY);
    }

    public Tank(int capacity, FluidStack whitelist) {
        fluid = FluidStack.EMPTY;
        this.whitelist = FluidStack.EMPTY;
        this.capacity = capacity;
    }

    public Tank(CompoundNBT nbt) {
        this(0);
        read(nbt);
    }

    public CompoundNBT write(CompoundNBT nbt) {
        nbt.put("Fluid", fluid.writeToNBT(new CompoundNBT()));
        nbt.put("Whitelist", whitelist.writeToNBT(new CompoundNBT()));
        nbt.putInt("Capacity", capacity);
        return nbt;
    }

    public void read(CompoundNBT nbt) {
        fluid = FluidStack.loadFluidStackFromNBT(nbt.getCompound("Fluid"));
        whitelist = FluidStack.loadFluidStackFromNBT(nbt.getCompound("Whitelist"));
        capacity = nbt.getInt("Capacity");
    }

    @Nonnull
    @Override
    public FluidStack getFluid() {
        return fluid;
    }

    @Override
    public int getFluidAmount() {
        return fluid.getAmount();
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public boolean isFluidValid(FluidStack stack) {
        return getFluidAmount() > 0 ? stack.isFluidEqual(fluid) : whitelist.isEmpty() || stack.isFluidEqual(whitelist);
    }

    @Override
    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        if (resource.isEmpty() || !isFluidValid(resource))
            return 0;
        if (action.simulate())
        {
            if (fluid.isEmpty())
                return Math.min(capacity, resource.getAmount());
            return Math.min(capacity - fluid.getAmount(), resource.getAmount());
        }
        if (fluid.isEmpty())
        {
            fluid = new FluidStack(resource, Math.min(capacity, resource.getAmount()));
            return fluid.getAmount();
        }
        int filled = capacity - fluid.getAmount();
        if (resource.getAmount() < filled)
        {
            fluid.grow(resource.getAmount());
            filled = resource.getAmount();
        }
        else
            fluid.setAmount(capacity);
        return filled;
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action)
    {
        if (resource.isEmpty() || !resource.isFluidEqual(fluid))
            return FluidStack.EMPTY;
        return drain(resource.getAmount(), action);
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action)
    {
        int drained = maxDrain;
        if (fluid.getAmount() < drained)
            drained = fluid.getAmount();
        FluidStack stack = new FluidStack(fluid, drained);
        if (action.execute() && drained > 0)
            fluid.shrink(drained);
        return stack;
    }
}
