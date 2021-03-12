package com.limachi.dimensional_bags.common.data.EyeDataMK2;

import com.limachi.dimensional_bags.common.data.IMarkDirty;
import com.limachi.dimensional_bags.common.inventory.ISimpleItemHandler;
import com.limachi.dimensional_bags.common.inventory.PillarInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.UUIDCodec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class InventoryData extends WorldSavedDataManager.EyeWorldSavedData implements IMarkDirty, ISimpleItemHandler {

    protected final ArrayList<UUID> pillarsOrder = new ArrayList<>();
    protected final ArrayList<PillarInventory> pillars = new ArrayList<>();

    public InventoryData() {
        super("inventory_data", 0, true);
    }

    public InventoryData(String suffix, int id, boolean client) {
        super(suffix, id, client);
    }

    public void addPillar(PillarInventory inv) {
        if (pillarsOrder.contains(inv.getId())) {
            int o = 0;
            int p = 0;
            while (true) {
                if (pillarsOrder.get(o).equals(inv.getId())) {
                    pillars.add(p, inv);
                    break;
                }
                if (p < pillars.size() && pillarsOrder.get(o).equals(pillars.get(p).getId()))
                    ++p;
                ++o;
            }
        } else {
            pillarsOrder.add(inv.getId());
            pillars.add(inv);
        }
        inv.notifyDirt = this;
        markDirty();
    }

    public void removePillar(UUID id) {
        PillarInventory inv = (PillarInventory)getPillarInventory(id);
        inv.notifyDirt = null;
        pillars.remove(inv);
        markDirty();
    }

    public ISimpleItemHandler getPillarInventory(@Nullable UUID id) {
        if (id == null) return this;
        for (PillarInventory inv : pillars)
            if (inv.getId().equals(id))
                return inv;
        return null;
    }

    static public InventoryData getInstance(int id) {
        return WorldSavedDataManager.getInstance(InventoryData.class, null, id);
    }

    static public <T> T execute(int id, Function<InventoryData, T> executable, T onErrorReturn) {
        return WorldSavedDataManager.execute(InventoryData.class, null, id, executable, onErrorReturn);
    }

    static public boolean execute(int id, Consumer<InventoryData> executable) {
        return WorldSavedDataManager.execute(InventoryData.class, null, id, data->{executable.accept(data); return true;}, false);
    }

    @Override
    public void readFromBuff(PacketBuffer buff) {
        pillars.clear();
        pillarsOrder.clear();
        int np = buff.readInt();
        int no = buff.readInt();
        for (int i = 0; i < no; ++i)
            pillarsOrder.add(buff.readUniqueId());
        for (int i = 0; i < np; ++i) {
            PillarInventory inv = new PillarInventory();
            inv.readFromBuff(buff);
            inv.notifyDirt = this;
            pillars.add(inv);
        }
    }

    @Override
    public void writeToBuff(PacketBuffer buff) {
        buff.writeInt(pillars.size());
        buff.writeInt(pillarsOrder.size());
        for (UUID id : pillarsOrder)
            buff.writeUniqueId(id);
        for (PillarInventory inv : pillars)
            inv.writeToBuff(buff);
    }

    @Override
    public void read(CompoundNBT nbt) {
        pillars.clear();
        pillarsOrder.clear();
        ListNBT ord = nbt.getList("Order", 11);
        for (int i = 0; i < ord.size(); ++i)
            pillarsOrder.add(UUIDCodec.decodeUUID(ord.getIntArray(i)));
        ListNBT pil = nbt.getList("Pillars", 10);
        for (int i = 0; i < pil.size(); ++i) {
            PillarInventory inv = new PillarInventory();
            inv.deserializeNBT(pil.getCompound(i));
            inv.notifyDirt = this;
            pillars.add(inv);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ListNBT ord = new ListNBT();
        for (UUID id : pillarsOrder)
            ord.add(NBTUtil.func_240626_a_(id));
        compound.put("Order", ord);
        ListNBT pil = new ListNBT();
        for (PillarInventory inv : pillars)
            pil.add(inv.serializeNBT());
        compound.put("Pillars", pil);
        return compound;
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        if (slot < 0 || slot >= pillars.size()) return;
        pillars.get(slot).setStackInSlot(0, stack);
    }

    @Override
    public int getSlots() {
        return pillars.size();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot < 0 || slot >= pillars.size()) return ItemStack.EMPTY;
        return pillars.get(slot).getStackInSlot(0);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (slot < 0 || slot >= pillars.size()) return stack;
        return pillars.get(slot).insertItem(0, stack, simulate);
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot < 0 || slot >= pillars.size()) return ItemStack.EMPTY;
        return pillars.get(slot).extractItem(0, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        if (slot < 0 || slot >= pillars.size()) return 0;
        return pillars.get(slot).getSlotLimit(0);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        if (slot < 0 || slot >= pillars.size()) return false;
        return pillars.get(slot).isItemValid(0, stack);
    }
}
