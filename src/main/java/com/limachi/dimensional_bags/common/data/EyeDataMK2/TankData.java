package com.limachi.dimensional_bags.common.data.EyeDataMK2;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.WorldUtils;
import com.limachi.dimensional_bags.common.inventory.Tank;
import com.limachi.dimensional_bags.common.inventory.Wrapper;
import net.minecraft.nbt.ByteArrayNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;

public class TankData extends WorldSavedDataManager.EyeWorldSavedData implements IFluidHandler {

//    private final int id;
    private ArrayList<Tank> tanks = new ArrayList<>();
    private ArrayList<Byte> rights = new ArrayList<>();
    private int selected = 0;

    public TankData(String suffix, int id, boolean client) {
        super(suffix, id, client);
//        super(DimBag.MOD_ID + "_eye_" + id + "_tank_data");
//        this.id = id;
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

//    static public TankData getInstance(@Nullable ServerWorld world, int id) {
//        if (id <= 0) return null;
//        if (world == null)
//            world = WorldUtils.getOverWorld();
//        if (world != null)
//            return world.getSavedData().getOrCreate(()->new TankData(id), DimBag.MOD_ID + "_eye_" + id + "_tank_data");
//        return null;
//    }

    static public TankData getInstance(@Nullable ServerWorld world, int id) {
        return WorldSavedDataManager.getInstance(TankData.class, world, id);
    }
}
