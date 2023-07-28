package com.limachi.dim_bag.bag_data;

import com.limachi.dim_bag.items.BagItem;
import com.limachi.dim_bag.items.VirtualBagItem;
import com.limachi.dim_bag.utils.SimpleTank;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;

public class TankData implements IFluidHandlerItem {
    public static final Component DEFAULT_TANK_LABEL = Component.translatable("block.dim_bag.tank_module");
    public static final int DEFAULT_CAPACITY = 8000;
    private LazyOptional<TankData> handle = LazyOptional.of(()->this);

    public static class TankEntry extends SimpleTank {
        private final BlockPos pos;
        private Component label;

        public TankEntry(CompoundTag data) {
            super(data.contains("capacity") ? data.getInt("capacity") : DEFAULT_CAPACITY, FluidStack.loadFluidStackFromNBT(data));
            pos = BlockPos.of(data.getLong(BagInstance.POSITION));
            label = Component.Serializer.fromJson(data.getString("label"));
            if (label == null)
                label = DEFAULT_TANK_LABEL;
        }

        public CompoundTag serialize() {
            CompoundTag out = content.writeToNBT(new CompoundTag());
            out.putInt("capacity", capacity);
            out.putLong(BagInstance.POSITION, pos.asLong());
            out.putString("label", Component.Serializer.toJson(label));
            return out;
        }
    }

    private final int bag;
    private final ArrayList<TankEntry> tanks = new ArrayList<>();
    private final HashMap<BlockPos, LazyOptional<TankEntry>> handles = new HashMap<>();

    private ItemStack container = null;
    private final Supplier<CompoundTag> mode;

    protected TankData(int bag, ListTag tanks, Supplier<CompoundTag> mode) {
        this.bag = bag;
        for (int i = 0; i < tanks.size(); ++i)
            this.tanks.add(new TankEntry(tanks.getCompound(i)));
        this.mode = mode;
    }

    public int getSelected() {
        long selected = mode.get().getLong("selected");
        if (selected != 0L)
            return getTank(BlockPos.of(selected));
        return -1;
    }

    public void select(int index) {
        if (index >= 0 && index < tanks.size())
            mode.get().putLong("selected", tanks.get(index).pos.asLong());
    }

    public TankData setContainer(@Nullable ItemStack container) {
        this.container = container;
        return this;
    }

    @Override
    public @Nonnull ItemStack getContainer() {
        if (container != null)
            return container;
        ItemStack out = new ItemStack(VirtualBagItem.R_ITEM.get());
        out.getOrCreateTag().putInt(BagItem.BAG_ID_KEY, bag);
        return out;
    }

    public int getTank(BlockPos slot) {
        for (int i = 0; i < tanks.size(); ++i)
            if (tanks.get(i).pos.equals(slot))
                return i;
        return -1;
    }

    public BlockPos getTank(int slot) {
        if (slot < 0 || slot >= tanks.size()) return null;
        return tanks.get(slot).pos;
    }

    public LazyOptional<SimpleTank> getTankHandle(BlockPos pos) {
        if (pos == null)
            return null;
        return handles.computeIfAbsent(pos, k->{
            final int tank = getTank(pos);
            return tank != -1 ? LazyOptional.of(()->tanks.get(tank)) : LazyOptional.empty();
        }).cast();
    }

    public Component getTankLabel(BlockPos pos) {
        int slot = getTank(pos);
        if (slot != -1)
            return tanks.get(slot).label;
        return DEFAULT_TANK_LABEL;
    }

    public void setTankLabel(BlockPos pos, Component label) {
        int slot = getTank(pos);
        if (slot != -1) {
            tanks.get(slot).label = label;
            handles.remove(pos).invalidate();
        }
    }

    public CompoundTag uninstallTank(BlockPos pos) {
        int i = getTank(pos);
        if (i != -1) {
            handles.remove(pos).invalidate();
            CompoundTag out = tanks.remove(i).serialize();
            out.remove(BagInstance.POSITION);
            invalidate();
            return out;
        }
        return new CompoundTag();
    }

    public void installTank(BlockPos pos, CompoundTag data) {
        if (handles.containsKey(pos)) { //should never happen
            LazyOptional<TankEntry> prev = handles.remove(pos);
            tanks.remove(getTank(pos));
            prev.invalidate();
        }
        data.putLong(BagInstance.POSITION, pos.asLong());
        tanks.add(new TankEntry(data));
        if (handle != null)
            handle.invalidate(); //we invalidate the global handle to force all global inventories to reload
        handle = null;
    }

    public void invalidate() {
        for (LazyOptional<TankEntry> handle : handles.values())
            handle.invalidate();
        handles.clear();
        if (handle != null)
            handle.invalidate();
        handle = null;
    }

    public LazyOptional<TankData> getHandle() {
        if (handle == null)
            handle = LazyOptional.of(()->this);
        return handle;
    }

    protected ListTag serialize() {
        ListTag out = new ListTag();
        for (TankEntry entry : tanks)
            out.add(entry.serialize());
        return out;
    }

    @Override
    public int getTanks() { return tanks.size(); }

    @Override
    @Nonnull
    public FluidStack getFluidInTank(int tank) {
        return tank >= 0 && tank < tanks.size() ? tanks.get(tank).getFluid() : FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        return tank >= 0 && tank < tanks.size() ? tanks.get(tank).getCapacity() : 0;
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
        return tank >= 0 && tank < tanks.size() && tanks.get(tank).isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        int selected = getSelected();
        if (selected != -1)
            return tanks.get(selected).fill(resource, action);
        int filled = 0;
        if (!resource.isEmpty()) {
            FluidStack stack = resource.copy();
            for (TankEntry entry : tanks) {
                int t = entry.fill(stack, action);
                if (t > 0) {
                    stack.shrink(t);
                    filled += t;
                    if (stack.isEmpty())
                        break;
                }
            }
        }
        return filled;
    }

    @Override
    @Nonnull
    public FluidStack drain(FluidStack resource, FluidAction action) {
        int selected = getSelected();
        if (selected != -1)
            return tanks.get(selected).drain(resource, action);
        FluidStack drained = FluidStack.EMPTY;
        if (!resource.isEmpty()) {
            FluidStack stack = resource.copy();
            for (TankEntry entry : tanks) {
                FluidStack t = entry.drain(stack, action);
                if (!t.isEmpty()) {
                    stack.shrink(t.getAmount());
                    if (drained.isEmpty())
                        drained = t.copy();
                    else
                        drained.grow(t.getAmount());
                    if (stack.isEmpty())
                        break;
                }
            }
        }
        return drained;
    }

    @Override
    @Nonnull
    public FluidStack drain(int maxDrain, FluidAction action) {
        int selected = getSelected();
        if (selected != -1)
            return tanks.get(selected).drain(maxDrain, action);
        if (maxDrain <= 0) return FluidStack.EMPTY;
        for (TankEntry entry : tanks)
            if (!entry.getFluid().isEmpty()) {
                FluidStack target = entry.getFluid().copy();
                target.setAmount(maxDrain);
                return drain(target, action);
            }
        return FluidStack.EMPTY;
    }
}
