package com.limachi.dimensional_bags.common.inventory;

import net.minecraft.nbt.ByteArrayNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class MultyTank {} /*implements IFluidHandler {

    protected Runnable dirty = null;
    protected ArrayList<Tank> tanks = new ArrayList<>();
    protected ArrayList<Byte> rights = new ArrayList<>();
    protected int selected = 0;

    public void attachListener(Runnable dirt) { dirty = dirt; }

    public CompoundNBT write(CompoundNBT nbt) {
        ListNBT list = new ListNBT();
        for (Tank tank : tanks)
            list.add(tank.write(new CompoundNBT()));
        nbt.put("Tanks", list);
        nbt.put("Rights", new ByteArrayNBT(rights));
        return nbt;
    }

    public void selectTank(int tank) {
        if (tank >= 0 && tank < tanks.size())
            selected = tank;
    }

    public void read(CompoundNBT nbt) {
        this.tanks = new ArrayList<>();
        ListNBT list = nbt.getList("Tanks", 10);
        for (int i = 0; i < list.size(); ++i)
            this.tanks.add(new Tank(list.getCompound(i)));
        byte[] array = nbt.getByteArray("Rights");
        this.rights = new ArrayList<>();
        for (byte right : array)
            this.rights.add(right);
    }

    public int attachTank(Tank tank, byte right) {
        tanks.add(tank);
        rights.add(right);
        onContentsChanged();
        return tanks.size() - 1;
    }

    public ArrayList<Byte> switchRights(ArrayList<Byte> rights) {
        ArrayList<Byte> out = this.rights;
        if (rights.size() == this.rights.size()) {
            this.rights = rights;
        }
        return out;
    }

    public void detachTank(int index) {
        tanks.remove(index);
        rights.remove(index);
        if (selected == tanks.size())
            selected = 0;
        onContentsChanged();
    }

    @Override
    public int getTanks() {
        return tanks.size();
    }

    public Tank getTank() {
        if (tanks.size() > 0)
            return tanks.get(selected);
        return null;
    }

    public void increaseCapacity(int increment) {
        for (Tank tank : tanks)
            tank.setCapacity(tank.getCapacity() + increment);
    }

    public int getSpace() {
        if (tanks.size() == 0)
            return 0;
        return tanks.get(selected).getCapacity() - tanks.get(selected).getFluidAmount();
    }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return tanks.get(tank).getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return tanks.get(tank).getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
        return tanks.get(tank).isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || (rights.get(selected) & Wrapper.IORights.CANINPUT) == 0) return 0;
        int consumed = tanks.get(selected).fill(resource, action);
        if (consumed > 0 && action.execute())
            onContentsChanged();
        return consumed;
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || (rights.get(selected) & Wrapper.IORights.CANOUTPUT) == 0) return FluidStack.EMPTY;
        FluidStack out = tanks.get(selected).drain(resource, action);
        if (!out.isEmpty() && action.execute())
            onContentsChanged();
        return out;
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (maxDrain == 0 || (rights.get(selected) & Wrapper.IORights.CANOUTPUT) == 0) return FluidStack.EMPTY;
        FluidStack out = tanks.get(selected).drain(maxDrain, action);
        if (!out.isEmpty() && action.execute())
            onContentsChanged();
        return out;
    }

    public void setFluid(int tank, FluidStack fluid) {
        tanks.get(tank).setFluid(fluid);
        onContentsChanged();
    }

    protected void onContentsChanged() {
        if (dirty != null)
            dirty.run();
    }
}*/
