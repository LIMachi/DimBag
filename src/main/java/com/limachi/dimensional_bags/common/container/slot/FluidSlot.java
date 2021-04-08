package com.limachi.dimensional_bags.common.container.slot;

import com.limachi.dimensional_bags.common.inventory.ISimpleFluidHandlerSerializable;
import com.limachi.dimensional_bags.common.items.FluidItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;

public class FluidSlot extends Slot {

    private final ISimpleFluidHandlerSerializable fluidHandler;

    public FluidSlot(ISimpleFluidHandlerSerializable fluidHandler, int index, int xPosition, int yPosition) {
        super(DisabledSlot.EMPTY_INVENTORY, index, xPosition, yPosition);
        this.fluidHandler = fluidHandler;
    }

    @Override
    public void putStack(ItemStack stack) {
        if (stack.getItem() instanceof FluidItem) {
            FluidStack fluid = FluidStack.loadFluidStackFromNBT(stack.getTag());
            int ps = fluidHandler.getSelectedTank();
            fluidHandler.selectTank(getSlotIndex());
            FluidStack local = fluidHandler.getFluidInTank(getSlotIndex());
            if (fluid.isFluidEqual(local)) {
                if (fluid.getAmount() > local.getAmount()) {
                    fluid.setAmount(fluid.getAmount() - local.getAmount());
                    fluidHandler.fill(fluid, IFluidHandler.FluidAction.EXECUTE);
                } else if (fluid.getAmount() < local.getAmount())
                    fluidHandler.drain(local.getAmount() - fluid.getAmount(), IFluidHandler.FluidAction.EXECUTE);
            } else {
                if (!local.isEmpty())
                    fluidHandler.drain(local.getAmount(), IFluidHandler.FluidAction.EXECUTE);
                if (!fluid.isEmpty())
                    fluidHandler.fill(fluid, IFluidHandler.FluidAction.EXECUTE);
            }
            fluidHandler.selectTank(ps);
        }
        this.onSlotChanged();
    }

    @Nonnull
    @Override
    public ItemStack getStack() {
        FluidStack fluid = getFluid();
        if (fluid.isEmpty())
            return ItemStack.EMPTY;
        return FluidItem.createStack(fluid);
    }

    public void selectTank() { fluidHandler.selectTank(getSlotIndex()); }

    public boolean isSelected() { return fluidHandler.getSelectedTank() == getSlotIndex(); }

    @Override
    public boolean getHasStack() { return !fluidHandler.getFluidInTank(getSlotIndex()).isEmpty(); }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) { return false; }

    @Override
    public boolean canTakeStack(PlayerEntity playerIn) { return false; }

    @Nonnull
    @Override
    public ItemStack decrStackSize(int amount) { return ItemStack.EMPTY; }

    @Nonnull
    public FluidStack getFluid() { return fluidHandler.getFluidInTank(getSlotIndex()); }

    public int getCapacity() { return fluidHandler.getTankCapacity(getSlotIndex()); }

    public ISimpleFluidHandlerSerializable getHandler() { return fluidHandler; }
}
