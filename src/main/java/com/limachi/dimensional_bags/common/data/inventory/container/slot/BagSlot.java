package com.limachi.dimensional_bags.common.data.inventory.container.slot;

import com.limachi.dimensional_bags.common.items.Bag;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class BagSlot extends SyncSlot {

    public BagSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack) { //prevent shift-clic of bag inside bag slots
        return !(stack.getItem() instanceof Bag) && super.isItemValid(stack);
    }
}
