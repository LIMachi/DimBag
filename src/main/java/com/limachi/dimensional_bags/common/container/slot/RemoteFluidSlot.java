package com.limachi.dimensional_bags.common.container.slot;

import com.limachi.dimensional_bags.common.data.EyeDataMK2.TankData;
import com.limachi.dimensional_bags.common.inventory.Wrapper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class RemoteFluidSlot extends Slot implements IIORightsSlot {
    protected final TankData fluidHandler;
    protected final int slotIndex;

    public RemoteFluidSlot(TankData fluidHandler, int index, int xPosition, int yPosition) {
        super(new Inventory(0), index, xPosition, yPosition);
        this.fluidHandler = fluidHandler;
        this.slotIndex = index;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        LazyOptional<IFluidHandlerItem> cap = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
        if (cap.isPresent()) {
            IFluidHandlerItem handler = cap.orElse(null);
            return fluidHandler.isFluidValid(slotIndex, handler.drain(FluidAttributes.BUCKET_VOLUME, TankData.FluidAction.SIMULATE));
        }
        return false;
    }

    @Override
    public boolean canTakeStack(PlayerEntity playerIn) { return false; }

    public TankData getFluidHandler() { return fluidHandler; }

    @Override
    public void onSlotChanged() {}

    public FluidStack getFluidStack() { return this.fluidHandler.getFluidInTank(slotIndex); }

    @Override
    public boolean isSameInventory(Slot other) { return other instanceof RemoteFluidSlot && ((RemoteFluidSlot)other).fluidHandler == fluidHandler; }

    public ItemStack onSlotClick(PlayerEntity player, ClickType clickType) {
        return ItemStack.EMPTY;
    }

    @Override
    public byte getRights() {
        return 3; //FIXME: for now the rights are always true for input and output
    }

    @Override
    public void setRightsFlag(byte rights) {} //FIXME: for now rights cannot be changed

    @Override
    public void setRights(Wrapper.IORights rights) {} //FIXME: for now rights cannot be changed
}
