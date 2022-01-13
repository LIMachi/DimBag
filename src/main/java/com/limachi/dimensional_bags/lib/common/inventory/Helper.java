package com.limachi.dimensional_bags.lib.common.inventory;

/*
public class Helper {
    public static ItemStack getItemStack(EquipmentSlotType slot, Entity entity) {
        Iterator<ItemStack> it = Collections.emptyIterator();
        if (slot.getSlotType() == EquipmentSlotType.Group.HAND)
            it = entity.getHandSlots().iterator();
        else if (slot.getSlotType() == EquipmentSlotType.Group.ARMOR)
            it = entity.getArmorSlots().iterator();
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
*/