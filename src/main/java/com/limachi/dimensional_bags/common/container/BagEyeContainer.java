package com.limachi.dimensional_bags.common.container;

import com.limachi.dimensional_bags.common.init.Registries;
import com.limachi.dimensional_bags.common.tileentity.BagEyeTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;

import java.util.Objects;

public class BagEyeContainer extends Container {

    public final BagEyeTileEntity tileEntity;
    private final IWorldPosCallable canIntereactWithCallable;

    public BagEyeContainer(final int windowId, final PlayerInventory inventory, final BagEyeTileEntity tileEntity) {
        super(Registries.BAG_CONTAINER.get(), windowId);
        this.tileEntity = tileEntity;
        this.canIntereactWithCallable = IWorldPosCallable.of(tileEntity.getWorld(), tileEntity.getPos());

        //Main Inventory
        int startX = 8; //default chest first slot
        int startY = 18; //default chest first slot
        int slotInterval = 18; //default chest slot size
        for(int row = 0; row < tileEntity.getRows(); ++row)
            for (int column = 0; column < tileEntity.getColumns(); ++column)
                this.addSlot(new Slot(tileEntity, row * tileEntity.getColumns() + column, startX + column * slotInterval, startY + row * slotInterval));

        //Main Player Inventory
        int startYp = 102; //default chest player first slot
        for (int row = 0; row < 3; ++row)
            for (int column = 0; column < 9; ++column)
                this.addSlot(new Slot(inventory, column + row * 9 + 9, startX + column * slotInterval, startYp + row * slotInterval));

        //Player Hotbar
        int startYhb = 160; //default chest hotbar first slot
        for (int column = 0; column < 9; ++column)
            this.addSlot(new Slot(inventory, column, startX + column * slotInterval, startYhb));
    }

    private static BagEyeTileEntity getTileEntity(final PlayerInventory inventory, final PacketBuffer data) {
        Objects.requireNonNull(inventory, "inventory cannot be null");
        Objects.requireNonNull(data, "data cannot be null");
        final TileEntity tileAtPos = inventory.player.world.getTileEntity(data.readBlockPos());
        if (tileAtPos instanceof BagEyeTileEntity) {
            return (BagEyeTileEntity)tileAtPos;
        }
        throw new IllegalStateException("Tile entity is not correct" + tileAtPos);
    }

    public BagEyeContainer(final int windowId, final PlayerInventory inventory, final PacketBuffer data) {
        this(windowId, inventory, getTileEntity(inventory, data));
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return isWithinUsableDistance(canIntereactWithCallable, playerIn, Registries.BAG_EYE_BLOCK.get());
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemStack1 = slot.getStack();
            itemStack = itemStack1.copy();
            if (index < tileEntity.getSizeInventory()) {
                if (!this.mergeItemStack(itemStack1, tileEntity.getSizeInventory(), this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
                else if (!this.mergeItemStack(itemStack1, 0, tileEntity.getSizeInventory(), false)) {
                    return ItemStack.EMPTY;
                }
                if (itemStack1.isEmpty()) {
                    slot.putStack(ItemStack.EMPTY);
                } else {
                    slot.onSlotChanged();
                }
            }
        }
        return itemStack;
    }
}
