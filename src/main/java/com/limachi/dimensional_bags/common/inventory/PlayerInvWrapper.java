package com.limachi.dimensional_bags.common.inventory;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nonnull;

public class PlayerInvWrapper extends Wrapper {
    public PlayerInvWrapper(PlayerInventory inv) { super(inv); }

    public PlayerInvWrapper(PlayerInventory inv, Runnable markDirty) {
        super(inv, baseRights(inv.getSizeInventory()), markDirty);
    }

    public PlayerInvWrapper(PlayerInventory inv, IORights[] IO, Runnable markDirty) { super(inv, IO, markDirty); }

    public PlayerInvWrapper(PlayerInventory inv, PacketBuffer buffer) { super(inv, buffer); }

    public PlayerInvWrapper(PacketBuffer buffer) { super(buffer); }

    public PlayerInventory getPlayerInventory() { return inv instanceof PlayerInventory ? (PlayerInventory)inv : null; }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        if (slot >= 36 && slot <= 39) { //armor slots
            EquipmentSlotType type = stack.getEquipmentSlot(); //forge might have been overiden by a modded item
            if (type == null) { //was not overiden, using vanilla armor behavior
                if (!(stack.getItem() instanceof ArmorItem))
                    return false;
                type = ((ArmorItem)stack.getItem()).getEquipmentSlot();
            }
            if (type.getSlotType() != EquipmentSlotType.Group.ARMOR || type.getIndex() != slot - 36)
                return false;
        }
        return super.isItemValid(slot, stack);
    }
}
