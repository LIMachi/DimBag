package com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2;

import com.limachi.dimensional_bags.lib.common.inventory.ISimpleItemHandlerSerializable;
import com.limachi.dimensional_bags.common.bagDimensionOnly.bagSlot.SlotInventory;
import com.limachi.dimensional_bags.lib.utils.StackUtils;
import com.limachi.dimensional_bags.lib.utils.UUIDUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.UUIDCodec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class InventoryData extends WorldSavedDataManager.EyeWorldSavedData implements ISimpleItemHandlerSerializable {

    protected final ArrayList<UUID> pillarsOrder = new ArrayList<>();
    protected final ArrayList<SlotInventory> pillars = new ArrayList<>();

    public InventoryData() { super("inventory_data", 0, true, false); }

    public InventoryData(String suffix, int id, boolean client) {
        super(suffix, id, client, false);
    }

    public void addPillar(SlotInventory inv) {
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
        inv.notifyDirt = this::setDirty;
        setDirty();
    }

    public void removePillar(UUID id) {
        SlotInventory inv = (SlotInventory)getPillarInventory(id);
        inv.notifyDirt = null;
        pillars.remove(inv);
        setDirty();
    }

    public ISimpleItemHandlerSerializable getPillarInventory(@Nullable UUID id) {
        if (id == null || id.equals(UUIDUtils.NULL_UUID)) return this;
        for (SlotInventory inv : pillars)
            if (inv.getId().equals(id))
                return inv;
        return null;
    }

    static public InventoryData getInstance(int id) { return WorldSavedDataManager.getInstance(InventoryData.class, id); }

    static public <T> T execute(int id, Function<InventoryData, T> executable, T onErrorReturn) { return WorldSavedDataManager.execute(InventoryData.class, id, executable, onErrorReturn); }

    static public boolean execute(int id, Consumer<InventoryData> executable) { return WorldSavedDataManager.execute(InventoryData.class, id, data->{executable.accept(data); return true;}, false); }

    @Override
    public void readFromBuff(PacketBuffer buff) {
        pillars.clear();
        pillarsOrder.clear();
        int np = buff.readInt();
        int no = buff.readInt();
        for (int i = 0; i < no; ++i)
            pillarsOrder.add(buff.readUUID());
        for (int i = 0; i < np; ++i) {
            SlotInventory inv = new SlotInventory();
            inv.readFromBuff(buff);
            inv.notifyDirt = this::setDirty;
            pillars.add(inv);
        }
    }

    @Override
    public void writeToBuff(PacketBuffer buff) {
        buff.writeInt(pillars.size());
        buff.writeInt(pillarsOrder.size());
        for (UUID id : pillarsOrder)
            buff.writeUUID(id);
        for (SlotInventory inv : pillars)
            inv.writeToBuff(buff);
    }

    @Override
    public void load(CompoundNBT nbt) {
        pillars.clear();
        pillarsOrder.clear();
        ListNBT ord = nbt.getList("Order", 11);
        for (int i = 0; i < ord.size(); ++i)
            pillarsOrder.add(UUIDCodec.uuidFromIntArray(ord.getIntArray(i)));
        ListNBT pil = nbt.getList("Pillars", 10);
        for (int i = 0; i < pil.size(); ++i) {
            SlotInventory inv = new SlotInventory();
            inv.deserializeNBT(pil.getCompound(i));
            inv.notifyDirt = this::setDirty;
            pillars.add(inv);
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT compound) {
        ListNBT ord = new ListNBT();
        for (UUID id : pillarsOrder)
            ord.add(new IntArrayNBT(UUIDCodec.uuidToIntArray(id)));
        compound.put("Order", ord);
        ListNBT pil = new ListNBT();
        for (SlotInventory inv : pillars)
            pil.add(inv.serializeNBT());
        compound.put("Pillars", pil);
        return compound;
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        if (slot < 0 || slot >= pillars.size()) return;
        pillars.get(slot).setStackInSlot(0, stack);
    }

    public void tickOnPlayer(PlayerEntity player, @Nullable EquipmentSlotType targetSlot) {
        boolean changed = false;
        for (int i = 0; i < pillars.size(); ++i) {
            ItemStack original = pillars.get(i).getStackInSlot(0);
            ItemStack rep = StackUtils.tickOnPlayer(original, player, targetSlot);
            if (!original.equals(rep, false))
                changed = true;
            pillars.get(i).silentSetStackInSlot(0, rep);
        }
        if (changed)
            setDirty();
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
