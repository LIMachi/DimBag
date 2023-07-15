package com.limachi.dim_bag.bag_data;

import com.limachi.dim_bag.items.BagItem;
import com.limachi.lim_lib.StackUtils;
import com.limachi.lim_lib.containers.ISlotAccessContainer;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;

public class SlotData implements IItemHandlerModifiable {

    public static final SlotData EMPTY = new SlotData(0, new ListTag());
    private LazyOptional<SlotData> handle = LazyOptional.of(()->this);

    private static class SlotEntry implements ISlotAccessContainer {
        private BlockPos pos;
        private Component label;
        private ItemStack content;
        private final SlotAccess sa = new SlotAccess() {
            @Override
            public ItemStack get() {
                return content;
            }

            @Override
            public boolean set(ItemStack stack) {
                content = stack;
                return true;
            }
        };

        SlotEntry(CompoundTag data) {
            content = ItemStack.of(data);
            pos = BlockPos.of(data.getLong("position"));
            label = Component.Serializer.fromJson(data.getString("label"));
            if (label == null)
                label = Component.translatable("block.dim_bag.slot_module");
        }

        CompoundTag serialize() {
            CompoundTag out = content.serializeNBT();
            out.putLong("position", pos.asLong());
            out.putString("label", Component.Serializer.toJson(label));
            return out;
        }

        @Override
        public SlotAccess getSlotAccess(int i) {
            return sa;
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return true;
        }
    }

    public int getSlot(BlockPos slot) {
        for (int i = 0; i < stacks.size(); ++i)
            if (stacks.get(i).pos.equals(slot))
                return i;
        return -1;
    }

    public BlockPos getSlot(int slot) {
        if (slot < 0 || slot >= stacks.size()) return null;
        return stacks.get(slot).pos;
    }

    private final int bag;
    private final ArrayList<SlotEntry> stacks = new ArrayList<>();
    private final HashMap<BlockPos, LazyOptional<SlotEntry>> handles = new HashMap<>();

    public LazyOptional<IItemHandler> getSlotHandle(BlockPos pos) {
        if (pos == null)
            return null;
        return handles.computeIfAbsent(pos, k->{
            final int slot = getSlot(pos);
            LazyOptional<SlotEntry> opt = slot != -1 ? LazyOptional.of(()->stacks.get(slot)) : LazyOptional.empty();
            return opt;
        }).cast();
    }

    public CompoundTag uninstallSlot(BlockPos pos) {
        int i = getSlot(pos);
        if (i != -1) {
            handles.remove(pos).invalidate();
            CompoundTag out = stacks.remove(i).serialize();
            out.remove("position");
            invalidate();
            return out;
        }
        return new CompoundTag();
    }

    public void installSlot(BlockPos pos, CompoundTag data) {
        if (handles.containsKey(pos)) { //should never happen
            LazyOptional<SlotEntry> prev = handles.remove(pos);
            stacks.remove(getSlot(pos));
            prev.invalidate();
        }
        data.putLong("position", pos.asLong());
        stacks.add(new SlotEntry(data));
        handle.invalidate(); //we invalidate the global handle to force all global inventories to reload
        handle = null;
    }

    public void invalidate() {
        for (LazyOptional<SlotEntry> handle : handles.values())
            handle.invalidate();
        handles.clear();
        handle.invalidate();
        handle = null;
    }

    public LazyOptional<SlotData> getHandle() {
        if (handle == null)
            handle = LazyOptional.of(()->this);
        return handle;
    }

    protected SlotData(int bag, ListTag slots) {
        this.bag = bag;
        for (int i = 0; i < slots.size(); ++i)
            stacks.add(new SlotEntry(slots.getCompound(i)));
    }

    protected ListTag serialize() {
        ListTag out = new ListTag();
        for (SlotEntry entry : stacks)
            out.add(entry.serialize());
        return out;
    }

    @Override
    public int getSlots() { return stacks.size(); }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        if (slot >= 0 && slot < stacks.size())
            stacks.get(slot).content = stack;
    }

    @Override
    public @Nonnull ItemStack getStackInSlot(int slot) {
        return slot >= 0 && slot < stacks.size() ? stacks.get(slot).content : ItemStack.EMPTY;
    }

    @Override
    public @Nonnull ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || slot < 0 || slot >= stacks.size()) return stack;
        ItemStack to = stacks.get(slot).content;
        if (!StackUtils.canMerge(to, stack)) return stack;
        Pair<ItemStack, ItemStack> p = StackUtils.merge(stacks.get(slot).content, stack);
        if (!simulate)
            stacks.get(slot).content = p.getFirst();
        return p.getSecond();
    }

    @Override
    public @Nonnull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0 || slot < 0 || slot >= stacks.size()) return ItemStack.EMPTY;
        Pair<ItemStack, ItemStack> p = StackUtils.extract(stacks.get(slot).content, amount);
        if (!simulate)
            stacks.get(slot).content = p.getSecond();
        return p.getFirst();
    }

    @Override
    public int getSlotLimit(int slot) { return 64; }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return !(stack.getItem() instanceof BagItem) || BagItem.getBagId(stack) != bag;
    }
}
