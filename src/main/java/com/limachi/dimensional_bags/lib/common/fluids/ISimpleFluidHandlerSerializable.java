package com.limachi.dimensional_bags.lib.common.fluids;

import com.limachi.dimensional_bags.lib.common.container.slot.FluidSlot;
import com.limachi.dimensional_bags.lib.common.inventory.IPacketSerializable;
import com.limachi.dimensional_bags.lib.common.inventory.ISlotProvider;
import net.minecraft.inventory.container.Slot;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

public interface ISimpleFluidHandlerSerializable extends IFluidHandler, INBTSerializable<CompoundNBT>, IPacketSerializable, ISlotProvider {
    //extended fluid handler interface
    /**
     * get the tank that drain will be using and fill will be using first
     */
    int getSelectedTank();
    /**
     * select the tank used for drain and fill
     * @param tank
     */
    void selectTank(int tank);

    /**
     * get the underlying tank
     * @param tank
     * @return
     */
    IFluidTank getTank(int tank);

    default Slot createSlot(int index, int x, int y) { return new FluidSlot(this, index, x, y); }
}
