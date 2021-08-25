package com.limachi.dimensional_bags.common.inventory;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nonnull;

/*
public class PlayerInvWrapper extends Wrapper {
    public PlayerInvWrapper(PlayerInventory inv) { super(inv); }

    public PlayerInvWrapper(PlayerInventory inv, Runnable setChanged) {
        super(inv, baseRights(inv.getContainerSize()), setChanged);
    }

    public PlayerInvWrapper(PlayerInventory inv, IORights[] IO, Runnable setChanged) { super(inv, IO, setChanged); }

    public PlayerInvWrapper(PlayerInventory inv, PacketBuffer buffer) { super(inv, buffer); }

    public PlayerInvWrapper(PacketBuffer buffer) { super(buffer); }

    public PlayerInventory getPlayerInventory() { return inv instanceof PlayerInventory ? (PlayerInventory)inv : null; }

    @Override
    public boolean mayPlace(int slot, @Nonnull ItemStack stack) {
        if (slot >= 36 && slot <= 39) { //armor slots
            EquipmentSlotType type = stack.getEquipmentSlot(); //forge might have been overiden by a modded item
            if (type == null) { //was not overiden, using vanilla armor behavior
                if (!(stack.getItem() instanceof ArmorItem))
                    return false;
                type = ((ArmorItem)stack.getItem()).getEquipmentSlot(stack);
            }
            if (type.getType() != EquipmentSlotType.Group.ARMOR || type.getIndex() != slot - 36)
                return false;
        }
        return super.mayPlace(slot, stack);
    }
}
*/