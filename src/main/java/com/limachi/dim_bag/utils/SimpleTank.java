package com.limachi.dim_bag.utils;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class SimpleTank implements IFluidTank, IFluidHandler {

    public static final SimpleTank NULL_TANK = new SimpleTank(0);

    protected FluidStack content = FluidStack.EMPTY;
    protected int capacity;

    public SimpleTank(int capacity) { this.capacity = capacity; }

    public SimpleTank(int capacity, FluidStack stack) {
        this.capacity = capacity;
        content = stack.copy();
        if (content.getAmount() > capacity)
            content.setAmount(capacity);
    }

    @Override
    public @NotNull FluidStack getFluid() { return content; }

    @Override
    public int getFluidAmount() { return content.getAmount(); }

    @Override
    public int getCapacity() { return capacity; }

    @Override
    public boolean isFluidValid(FluidStack stack) { return !stack.isEmpty() && (content.isEmpty() || content.isFluidEqual(stack)); }

    @Override
    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        if (!isFluidValid(resource)) return 0;
        int amount = Math.min(resource.getAmount(), Math.max(capacity - content.getAmount(), 0));
        if (amount != 0 && action.execute()) {
            if (content.isEmpty()) {
                content = resource.copy();
                content.setAmount(amount);
            } else
                content.setAmount(content.getAmount() + amount);
        }
        return amount;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        if (maxDrain <= 0 || content.isEmpty()) return FluidStack.EMPTY;
        int amount = Math.min(maxDrain, content.getAmount());
        if (amount > 0) {
            FluidStack out = content.copy();
            if (action.execute())
                content.setAmount(content.getAmount() - amount);
            out.setAmount(amount);
            return out;
        }
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        if (!isFluidValid(resource) || content.isEmpty()) return FluidStack.EMPTY;
        int amount = Math.min(resource.getAmount(), content.getAmount());
        if (amount > 0) {
            FluidStack out = resource.copy();
            if (action.execute())
                content.setAmount(content.getAmount() - amount);
            out.setAmount(amount);
            return out;
        }
        return FluidStack.EMPTY;
    }

    @Override
    public int getTanks() { return 1; }

    @Override
    @Nonnull
    public FluidStack getFluidInTank(int tank) { return content; }

    @Override
    public int getTankCapacity(int tank) { return capacity; }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) { return isFluidValid(stack); }
}
