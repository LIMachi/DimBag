package com.limachi.dimensional_bags.common.data.container.slot;

import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.upgradesManager.UpgradeManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class UpgradeConsumerSlot extends BaseSlot { //upgradesManager put in this slot will be removed from the game and stored in the eye definitely (the mod use this method as crafting the bag item with an upgrade would require to validate the craft on the server with methods that don't give a simple access to the server class)

    public UpgradeConsumerSlot(IInventory inventory, int index, int xPosition, int yPosition, EyeData data) {
        super(inventory, index, xPosition, yPosition, true, false, data);
    }

    @Override
    public int getSlotStackLimit() {
        return 127;
    } //by default, the maximum (the actual limit is calculated per item)

    @Override
    public int getItemStackLimit(ItemStack stack) {
        int id = UpgradeManager.getIdByStack(stack);
        if (id == -1) return 0; //not an upgrade
        if (id != this.getSlotIndex()) return 0; //invalid upgrade slot
        return UpgradeManager.getLimit(id);
        //int upgrade = this.data.upgrades.getStackInSlot(id).getCount(); //FIXME
        //return UpgradeManager.getLimit(id) - upgrade;
    }

    @Override
    public void putStack(ItemStack stack) {
        int id = UpgradeManager.getIdByStack(stack);
        int before = this.data.upgrades.getStackInSlot(id).getCount();
        super.putStack(stack);
        int after = this.data.upgrades.getStackInSlot(id).getCount();
        UpgradeManager.applyUpgrade(id, before, after, data);
    }

    @Override
    public boolean canTakeStack(PlayerEntity playerIn) {
        return super.canTakeStack(playerIn);
    }
}
