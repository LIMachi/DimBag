package com.limachi.dim_bag.saveData;

import com.limachi.lim_lib.itemHandlers.ISimpleItemHandlerSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.UUID;
/*
@SaveData.RegisterSaveData(sync = SaveData.Sync.SERVER_TO_CLIENT)
public class InventoryData extends SaveData.SyncSaveData implements ISimpleItemHandlerSerializable {

    protected final ArrayList<UUID> pillarsOrder = new ArrayList<>();
    protected final ArrayList<SlotInventory> pillars = new ArrayList<>();

    public InventoryData(String name, SaveData.Sync sync) {
        super(name, sync);
    }

    @NotNull
    @Override
    public CompoundTag save(CompoundTag nbt) {
        return null;
    }

    @Override
    public void load(CompoundTag nbt) {
        pillars.clear();
        pillarsOrder.clear();
        ListTag ord = nbt.getList("Order", 11);
        for (int i = 0; i < ord.size(); ++i)
            pillarsOrder.add(UUIDCodec.uuidFromIntArray(ord.getIntArray(i)));
        ListTag pil = nbt.getList("Pillars", 10);
        for (int i = 0; i < pil.size(); ++i) {
            SlotInventory inv = new SlotInventory();
            inv.deserializeTag(pil.getCompound(i));
            inv.notifyDirt = this::setDirty;
            pillars.add(inv);
        }
    }

    @Override
    public void readFromBuff(FriendlyByteBuf buff) {

    }

    @Override
    public void writeToBuff(FriendlyByteBuf buff) {

    }

    @Override
    public CompoundTag serializeNBT() {
        return null;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {

    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {

    }

    @Override
    public int getSlots() {
        return 0;
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return null;
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return null;
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return null;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 0;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return false;
    }
}
*/