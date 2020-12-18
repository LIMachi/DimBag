package com.limachi.dimensional_bags.common.inventory;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.function.Consumer;
import java.util.function.Function;

public class IORightsWrappedItemHandler implements InventoryUtils.IFormatAwareItemHandler {

    public static class WrappedEntityInventory extends IORightsWrappedItemHandler {

        protected WeakReference<Entity> entityRef;

        public WrappedEntityInventory(Entity entity) { this(entity, null); }

        public WrappedEntityInventory(Entity entity, InventoryUtils.ItemStackIORights[] rights) {
            super(entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).orElse(new ItemStackHandler(0)), rights, entity instanceof PlayerEntity ? InventoryUtils.ItemHandlerFormat.PLAYER : InventoryUtils.ItemHandlerFormat.ENTITY);
            entityRef = new WeakReference<>(entity);
        }

        public void reloadEntityRef(Entity entity, InventoryUtils.ItemStackIORights[] rights) {
            entityRef = new WeakReference<>(entity);
            reloadHandler(entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).orElse(new ItemStackHandler(0)), rights, entity instanceof PlayerEntity ? InventoryUtils.ItemHandlerFormat.PLAYER : InventoryUtils.ItemHandlerFormat.ENTITY);
        }

        @Override
        public CompoundNBT asNBT() {
            CompoundNBT out = super.asNBT();
            out.putString("EntityName", entityRef.get() != null ? entityRef.get().getScoreboardName() : "None");
            if (entityRef.get() != null)
                out.putUniqueId("EntityUUID", entityRef.get().getUniqueID());
            return out;
        }
    }

    protected WeakReference<IItemHandler> handler;
    protected InventoryUtils.ItemStackIORights[] rights;
    protected InventoryUtils.ItemHandlerFormat format;

    public IORightsWrappedItemHandler(IItemHandler handler, InventoryUtils.ItemStackIORights[] rights, InventoryUtils.ItemHandlerFormat format) {
        reloadHandler(handler, rights, format);
    }

    public void reloadHandler(IItemHandler handler, InventoryUtils.ItemStackIORights[] rights, InventoryUtils.ItemHandlerFormat format) {
        this.handler = new WeakReference<>(handler);
        if (rights == null)
            rights = new InventoryUtils.ItemStackIORights[0];
        if (handler.getSlots() == rights.length)
            this.rights = rights;
        else {
            this.rights = new InventoryUtils.ItemStackIORights[handler.getSlots()];
            for (int i = 0; i < this.rights.length; ++i)
                this.rights[i] = i < rights.length ? rights[i] : new InventoryUtils.ItemStackIORights();
        }
        this.format = format;
    }

    protected <T> T runOnHandler(Function<IItemHandler, T> function, T def) {
        IItemHandler h = handler.get();
        if (h != null)
            return function.apply(h);
        return def;
    }

    protected void runOnHandler(Consumer<IItemHandler> function) {
        IItemHandler h = handler.get();
        if (h != null)
            function.accept(h);
    }

    public CompoundNBT asNBT() {
        return runOnHandler(h->{
            CompoundNBT out = new CompoundNBT();
            ListNBT list = new ListNBT();
            for (int i = 0; i < h.getSlots(); ++i) {
                ItemStack stack = h.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    CompoundNBT entry = stack.write(new CompoundNBT());
                    entry.putInt("Slot", i);
                    list.add(entry);
                }
            }
            out.put("Items", list);
            out.putInt("Size", h.getSlots());
            out.putInt("Columns", getColumns());
            out.putInt("Rows", getRows());
            out.putInt( "Format", format.ordinal());
            list = new ListNBT();
            for (int i = 0; i < rights.length; ++i)
                if (!rights[i].isVanilla()) {
                    CompoundNBT entry = rights[i].writeNBT(new CompoundNBT());
                    entry.putInt("Slot", i);
                    list.add(entry);
                }
            out.put("Rights", list);
            return out;
        }, new CompoundNBT());
    }

    @Override
    public int getRows() {
        if (rights.length != 0 && format == InventoryUtils.ItemHandlerFormat.CHEST) {
            if (rights.length <= 2) return 1;
            if (rights.length <= 4) return 2;
            return 3 * (((rights.length - 1) / 27) + 1);
        }
        return 0;
    }

    @Override
    public int getColumns() {
        int rows = getRows();
        if (rows == 0) return 0;
        if (rows == 1) return rights.length;
        if (rows == 2) return 2;
        return (rights.length + 2) / rows;
    }

    @Override
    public InventoryUtils.ItemHandlerFormat getFormat() { return format; }

    @Override
    public void setRows(int rows) {}

    @Override
    public void setColumns(int columns) {}

    @Override
    public void setFormat(InventoryUtils.ItemHandlerFormat format) { this.format = format; }

    @Nonnull
    @Override
    public InventoryUtils.ItemStackIORights getRightsInSlot(int slot) {
        return runOnHandler(h->{
            if (slot < 0 || slot >= h.getSlots()) return InventoryUtils.ItemStackIORights.INVALID;
            return slot < rights.length ? rights[slot] : InventoryUtils.ItemStackIORights.VANILLA;
        }, InventoryUtils.ItemStackIORights.INVALID);
    }

    @Override
    public void setRightsInSlot(int slot, InventoryUtils.ItemStackIORights right) {
        runOnHandler(h->{
            if (slot < 0 || slot >= h.getSlots() || slot >= rights.length) return ;
            rights[slot] = right;
        });
    }

    @Override
    public int getSlots() { return runOnHandler(IItemHandler::getSlots, 0); }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) { return runOnHandler(h->h.getStackInSlot(slot), ItemStack.EMPTY); }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) { return runOnHandler(h->h.insertItem(slot, stack, simulate), stack); }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) { return runOnHandler(h->h.extractItem(slot, amount, simulate), ItemStack.EMPTY); }

    @Override
    public int getSlotLimit(int slot) { return runOnHandler(h->h.getSlotLimit(slot), 0); }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) { return runOnHandler(h->h.isItemValid(slot, stack), false); }
}
