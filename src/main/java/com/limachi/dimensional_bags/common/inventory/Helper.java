package com.limachi.dimensional_bags.common.inventory;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.Iterator;


public class Helper {
    public static ItemStack getItemStack(EquipmentSlotType slot, Entity entity) {
        Iterator<ItemStack> it = Collections.emptyIterator();
        if (slot.getSlotType() == EquipmentSlotType.Group.HAND)
            it = entity.getHeldEquipment().iterator();
        else if (slot.getSlotType() == EquipmentSlotType.Group.ARMOR)
            it = entity.getArmorInventoryList().iterator();
        for (int i = 0; i < slot.getIndex(); ++i)
            if (it.hasNext())
                it.next();
        return it.hasNext() ? it.next() : ItemStack.EMPTY;
    }

    public static EquipmentSlotType slotTypeFromIndex(int index) {
        for (EquipmentSlotType t : EquipmentSlotType.values())
            if (t.getSlotIndex() == index)
                return t;
        return null;
    }
}
