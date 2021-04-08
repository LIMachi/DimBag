package com.limachi.dimensional_bags.common.data.EyeDataMK2;

import com.limachi.dimensional_bags.common.inventory.EmptySimpleFluidHandlerSerializable;
import com.limachi.dimensional_bags.common.inventory.FountainTank;
import com.limachi.dimensional_bags.common.inventory.ISimpleFluidHandlerSerializable;
import com.limachi.dimensional_bags.utils.UUIDUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.UUIDCodec;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class TankData extends WorldSavedDataManager.EyeWorldSavedData implements ISimpleFluidHandlerSerializable {
    protected final ArrayList<UUID> fountainsOrder = new ArrayList<>();
    protected final ArrayList<FountainTank> fountains = new ArrayList<>();
    protected int selectedTank = 0;
    protected boolean tryFill = true;
    protected boolean tryDrain = true;
    protected boolean autoSelect = true;

    public TankData() { super("tank_data", 0, true, false); }

    public TankData(String suffix, int id, boolean client) { super(suffix, id, client, false); }

    public void addFountain(FountainTank tank) {
        if (fountainsOrder.contains(tank.getId())) {
            int o = 0;
            int p = 0;
            while (true) {
                if (fountainsOrder.get(o).equals(tank.getId())) {
                    fountains.add(p, tank);
                    break;
                }
                if (p < fountains.size() && fountainsOrder.get(o).equals(fountains.get(p).getId()))
                    ++p;
                ++o;
            }
        } else {
            fountainsOrder.add(tank.getId());
            fountains.add(tank);
        }
        tank.notifyDirt = this::markDirty;
        markDirty();
    }

    public void removeFountain(UUID id) {
        FountainTank font = (FountainTank)getFountainTank(id);
        font.notifyDirt = null;
        fountains.remove(font);
        if (selectedTank >= fountains.size())
            selectedTank = 0;
        markDirty();
    }

    public ISimpleFluidHandlerSerializable getFountainTank(@Nullable UUID id) {
        if (id == null || id.equals(UUIDUtils.NULL_UUID)) return this;
        for (FountainTank inv : fountains)
            if (inv.getId().equals(id))
                return inv;
        return null;
    }

    static public TankData getInstance(int id) {
        return WorldSavedDataManager.getInstance(TankData.class, null, id);
    }

    static public <T> T execute(int id, Function<TankData, T> executable, T onErrorReturn) {
        return WorldSavedDataManager.execute(TankData.class, null, id, executable, onErrorReturn);
    }

    static public boolean execute(int id, Consumer<TankData> executable) {
        return WorldSavedDataManager.execute(TankData.class, null, id, data->{executable.accept(data); return true;}, false);
    }

    @Override
    public void readFromBuff(PacketBuffer buff) {
        fountains.clear();
        fountainsOrder.clear();
        int np = buff.readInt();
        int no = buff.readInt();
        selectedTank = buff.readInt();
        tryFill = buff.readBoolean();
        tryDrain = buff.readBoolean();
        autoSelect = buff.readBoolean();
        for (int i = 0; i < no; ++i)
            fountainsOrder.add(buff.readUniqueId());
        for (int i = 0; i < np; ++i) {
            FountainTank tank = new FountainTank();
            tank.readFromBuff(buff);
            tank.notifyDirt = this::markDirty;
            fountains.add(tank);
        }
    }

    @Override
    public void writeToBuff(PacketBuffer buff) {
        buff.writeInt(fountains.size());
        buff.writeInt(fountainsOrder.size());
        buff.writeInt(selectedTank);
        buff.writeBoolean(tryFill);
        buff.writeBoolean(tryDrain);
        buff.writeBoolean(autoSelect);
        for (UUID id : fountainsOrder)
            buff.writeUniqueId(id);
        for (FountainTank tank : fountains)
            tank.writeToBuff(buff);
    }

    @Override
    public void read(CompoundNBT nbt) {
        fountains.clear();
        fountainsOrder.clear();
        ListNBT ord = nbt.getList("Order", 11);
        for (int i = 0; i < ord.size(); ++i)
            fountainsOrder.add(UUIDCodec.decodeUUID(ord.getIntArray(i)));
        ListNBT pil = nbt.getList("Fountains", 10);
        for (int i = 0; i < pil.size(); ++i) {
            FountainTank tank = new FountainTank();
            tank.deserializeNBT(pil.getCompound(i));
            tank.notifyDirt = this::markDirty;
            fountains.add(tank);
        }
        selectedTank = nbt.getInt("Selected");
        tryFill = nbt.getBoolean("TryFill");
        tryDrain = nbt.getBoolean("TryDrain");
        autoSelect = nbt.getBoolean("AutoSelect");
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ListNBT ord = new ListNBT();
        for (UUID id : fountainsOrder)
            ord.add(NBTUtil.func_240626_a_(id));
        compound.put("Order", ord);
        ListNBT pil = new ListNBT();
        for (FountainTank inv : fountains)
            pil.add(inv.serializeNBT());
        compound.put("Fountains", pil);
        compound.putInt("Selected", selectedTank);
        compound.putBoolean("TryFill", tryFill);
        compound.putBoolean("TryDrain", tryDrain);
        compound.putBoolean("AutoSelect", autoSelect);
        return compound;
    }

    @Override
    public int getSelectedTank() { return selectedTank; }

    @Override
    public void selectTank(int tank) {
        if (tank != selectedTank) {
            if (tank >= 0 && tank < fountains.size())
                selectedTank = tank;
            markDirty();
        }
    }

    public void setTryFillState(boolean state) {
        if (state != tryFill) {
            tryFill = state;
            markDirty();
        }
    }

    public void setTryDrainState(boolean state) {
        if (state != tryDrain) {
            tryDrain = state;
            markDirty();
        }
    }

    public void setAutoSelectState(boolean state) {
        if (state != autoSelect) {
            autoSelect = state;
            markDirty();
        }
    }

    @Override
    public IFluidTank getTank(int tank) {
        if (tank >= 0 && tank < fountains.size())
            return fountains.get(tank);
        return EmptySimpleFluidHandlerSerializable.EMPTY_TANK;
    }

    @Override
    public int getTanks() { return fountains.size(); }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) {
        if (tank >= 0 && tank < fountains.size())
            return fountains.get(tank).getFluid();
        return FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        if (tank >= 0 && tank < fountains.size())
            return fountains.get(tank).getCapacity();
        return 0;
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
        if (tank >= 0 && tank < fountains.size())
            return fountains.get(tank).isFluidValid(stack);
        return false;
    }

    @Override //try to fill the selected tank thirst, then if not valid, try to fill all the tanks one by one except the selected one, only trying to fill a single tank, even if there is still fluids available
    public int fill(FluidStack resource, FluidAction action) {
        if (!tryFill) return fountains.get(selectedTank).fill(resource, action);
        int total = 0;
        for (int i = -1; i < fountains.size(); ++i) {
            if (i == selectedTank) continue;
            if (i == -1)
                total = fountains.get(selectedTank).fill(resource, action);
            else
                total = fountains.get(i).fill(resource, action);
            if (total != 0) {
                if (autoSelect && action.execute())
                    selectTank(i);
                return total;
            }
        }
        return total;
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (!tryDrain) return fountains.get(selectedTank).drain(resource, action);
        FluidStack total = FluidStack.EMPTY;
        for (int i = -1; i < fountains.size(); ++i) {
            if (i == selectedTank) continue;
            if (i == -1)
                total = fountains.get(selectedTank).drain(resource, action);
            else
                total = fountains.get(i).drain(resource, action);
            if (!total.isEmpty()) {
                if (autoSelect && action.execute())
                    selectTank(i);
                return total;
            }
        }
        return total;
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (!tryDrain) return fountains.get(selectedTank).drain(maxDrain, action);
        FluidStack total = FluidStack.EMPTY;
        for (int i = -1; i < fountains.size(); ++i) {
            if (i == selectedTank) continue;
            if (i == -1)
                total = fountains.get(selectedTank).drain(maxDrain, action);
            else
                total = fountains.get(i).drain(maxDrain, action);
            if (!total.isEmpty()) {
                if (autoSelect && action.execute())
                    selectTank(i);
                return total;
            }
        }
        return total;
    }
}