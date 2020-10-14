package com.limachi.dimensional_bags.common.data.EyeDataMK2;

import com.limachi.dimensional_bags.common.inventory.Tank;
import com.limachi.dimensional_bags.common.inventory.Wrapper;
import net.minecraft.nbt.ByteArrayNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

public class TankData extends WorldSavedDataManager.EyeWorldSavedData implements IFluidHandler {

    private ArrayList<Tank> tanks = new ArrayList<>();
    private ArrayList<Byte> rights = new ArrayList<>();
    private int selected = 0;

    public TankData(String suffix, int id, boolean client) {
        super(suffix, id, client);
    }

    public void selectTank(int tank) {
        if (tank >= 0 && tank < tanks.size()) {
            selected = tank;
            markDirty();
        }
    }

    public void increaseCapacity(int addCapacity) {
        for (Tank tank : tanks)
            tank.setCapacity(tank.getCapacity() + addCapacity);
        markDirty();
    }

    public void attachTank(Tank tank, byte right) {
        tanks.add(tank);
        rights.add(right);
        markDirty();
    }

    public int getSelectedTank() { return selected; }

    @Override
    public int getTanks() { return tanks.size(); }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) { return tanks.get(tank).getFluid(); }

    @Override
    public int getTankCapacity(int tank) { return tanks.get(tank).getCapacity(); }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) { return tanks.get(tank).isFluidValid(stack); }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || (rights.get(selected) & Wrapper.IORights.CANINPUT) == 0) return 0;
        int consumed = tanks.get(selected).fill(resource, action);
        if (consumed > 0 && action.execute())
            markDirty();
        return consumed;
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || (rights.get(selected) & Wrapper.IORights.CANOUTPUT) == 0) return FluidStack.EMPTY;
        FluidStack out = tanks.get(selected).drain(resource, action);
        if (!out.isEmpty() && action.execute())
            markDirty();
        return out;
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (maxDrain == 0 || (rights.get(selected) & Wrapper.IORights.CANOUTPUT) == 0) return FluidStack.EMPTY;
        FluidStack out = tanks.get(selected).drain(maxDrain, action);
        if (!out.isEmpty() && action.execute())
            markDirty();
        return out;
    }

    public void setFluid(int tank, FluidStack fluid) {
        tanks.get(tank).setFluid(fluid);
        markDirty();
    }

    @Override
    public void read(CompoundNBT nbt) {
        selected = nbt.getInt("Selected");
        tanks = new ArrayList<>();
        ListNBT list = nbt.getList("Tanks", 10);
        for (int i = 0; i < list.size(); ++i)
            tanks.add(new Tank(list.getCompound(i)));
        byte[] array = nbt.getByteArray("Rights");
        rights = new ArrayList<>();
        for (byte right : array)
            rights.add(right);
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        nbt.putInt("Selected", selected);
        ListNBT list = new ListNBT();
        for (Tank tank : tanks)
            list.add(tank.write(new CompoundNBT()));
        nbt.put("Tanks", list);
        nbt.put("Rights", new ByteArrayNBT(rights));
        return nbt;
    }

    static public TankData getInstance(int id) {
        return WorldSavedDataManager.getInstance(TankData.class, null, id);
    }

    static public <T> T execute(int id, Function<TankData, T> executable, T onErrorReturn) {
        return WorldSavedDataManager.execute(TankData.class, null, id, executable, onErrorReturn);
    }

    static public boolean execute(int id, Consumer<TankData> executable) {
        return WorldSavedDataManager.execute(TankData.class, null, id, data->{executable.accept(data); return true;}, false);
    }
}
