package com.limachi.dimensional_bags.lib.common.fluids;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nonnull;

public class EmptySimpleFluidHandlerSerializable implements ISimpleFluidHandlerSerializable {

    public static final IFluidTank EMPTY_TANK = new Tank(0);

    @Override
    public void readFromBuff(PacketBuffer buff) {}

    @Override
    public void writeToBuff(PacketBuffer buff) {}

    @Override
    public CompoundNBT serializeNBT() {return null;}

    @Override
    public void deserializeNBT(CompoundNBT nbt) {}

    @Override
    public int getSelectedTank() { return 0; }

    @Override
    public void selectTank(int tank) {}

    @Override
    public IFluidTank getTank(int tank) { return EMPTY_TANK; }

    @Override
    public int getTanks() { return 0; }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) { return FluidStack.EMPTY; }

    @Override
    public int getTankCapacity(int tank) { return 0; }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) { return false; }

    @Override
    public int fill(FluidStack resource, FluidAction action) { return 0; }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) { return FluidStack.EMPTY; }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) { return FluidStack.EMPTY; }
}
