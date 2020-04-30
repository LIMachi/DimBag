package com.limachi.dimensional_bags.common.data.container.slot;

import com.limachi.dimensional_bags.common.data.EyeData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;

public class BaseSlot extends Slot {
    protected final EyeData data;

    private final boolean canInput;
    private final boolean canOutput;

    public BaseSlot(IInventory inventoryIn, int index, int xPosition, int yPosition, boolean canInput, boolean canOutput, EyeData data) {
        super(inventoryIn, index, xPosition, yPosition);
        this.canInput = canInput;
        this.canOutput = canOutput;
        this.data = data;
    }

    public static boolean getInputRights(Slot slot) {
        return (!(slot instanceof BaseSlot)) || ((BaseSlot)slot).canInput;
    }

    public static boolean getOuputRights(Slot slot) {
        return (!(slot instanceof BaseSlot)) || ((BaseSlot)slot).canOutput;
    }

    public static boolean getIORights(Slot slot) {
        return (!(slot instanceof BaseSlot)) || (((BaseSlot)slot).canInput && ((BaseSlot)slot).canOutput);
    }

    @Override
    public boolean canTakeStack(PlayerEntity playerIn) {
        return /*canOutput &&*/ super.canTakeStack(playerIn);
    }

    public void onSwapCraft(int p_190900_1_) { //trick to allow call of onSwapCraft by BaseContainer#slotClick
        super.onSwapCraft(p_190900_1_);
    }
}
