package com.limachi.dimensional_bags.common.inventory;

import com.limachi.dimensional_bags.ConfigManager.Config;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.UUID;

public class FountainTank implements ISimpleFluidHandlerSerializable, IFluidTank {

    @Config(min = "1000", max = "30000000", cmt = "initial size of a bag tank (fountain inside of the bag) in milli buckets (1000mb = 1 bucket, 333mb = 1 bottle, 114mb = 1 ingot)")
    public static int DEFAULT_SIZE_IN_MILLI_BUCKETS = 8000;

    protected UUID id = UUID.randomUUID(); //unique id used to create a group of fountains
    protected int capacity = DEFAULT_SIZE_IN_MILLI_BUCKETS; //how much items can be stored (in stacks, yes this means the real amount is dependent on 'referent.getMaxStackSize()')
    protected FluidStack fluid = FluidStack.EMPTY;
    protected boolean locked = false; //does this fountain need to keep it's last mb
    public Runnable notifyDirt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FountainTank that = (FountainTank) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public void markDirty() {
        if (notifyDirt != null)
            notifyDirt.run();
    }

    public void open(ServerPlayerEntity player) { //FIXME
//        ArrayList<Integer> list = new ArrayList<>();
//        list.add(0);
//        SimpleContainer.open(player, new TranslationTextComponent("inventory.fountain.name"), null, this, list, 1, 1, t -> true);
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT out = new CompoundNBT();
        out.putUniqueId("UUID", id);
        out.putInt("capacity", capacity);
        out.put("fluid", fluid.writeToNBT(new CompoundNBT()));
        return out;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        id = nbt.getUniqueId("UUID");
        capacity = nbt.getInt("capacity");
        fluid = FluidStack.loadFluidStackFromNBT(nbt.getCompound("fluid"));
    }

    public void setLockState(boolean lock) {
        if (lock != locked)
            markDirty();
        locked = lock;
    }

    public boolean getLockState() {
        return locked;
    }

    @Override
    public void readFromBuff(PacketBuffer buff) {
        id = buff.readUniqueId();
        capacity = buff.readInt();
        fluid = buff.readFluidStack();
        locked = buff.readBoolean();
    }

    @Override
    public void writeToBuff(PacketBuffer buff) {
        buff.writeUniqueId(id);
        buff.writeInt(capacity);
        buff.writeFluidStack(fluid);
        buff.writeBoolean(locked);
    }

    @Override
    public int getSelectedTank() { return 0; }

    @Override
    public void selectTank(int tank) {}

    @Override
    public IFluidTank getTank(int tank) { return this; }

    @Override
    public int getTanks() { return 1; }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) { return fluid; }

    @Override
    public int getTankCapacity(int tank) { return capacity; }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) { return fluid.isEmpty() || fluid.isFluidEqual(stack); }

    @Nonnull
    @Override
    public FluidStack getFluid() { return fluid; }

    @Override
    public int getFluidAmount() { return fluid.getAmount(); }

    @Override
    public int getCapacity() { return capacity; }

    @Override
    public boolean isFluidValid(FluidStack stack) { return fluid.isEmpty() || fluid.isFluidEqual(stack); }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || fluid.getAmount() >= capacity || !isFluidValid(resource)) return 0;
        int consumed = Integer.min(resource.getAmount(), capacity - fluid.getAmount());
        if (action.execute()) {
            if (fluid.isEmpty()) {
                fluid = resource.copy();
                fluid.setAmount(consumed);
            }
            else
                fluid.grow(consumed);
            markDirty();
        }
        return consumed;
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || fluid.isEmpty() || !fluid.isFluidEqual(resource)) return FluidStack.EMPTY;
        FluidStack out = fluid.copy();
        out.setAmount(Integer.min(resource.getAmount(), fluid.getAmount()));
        if (!out.isEmpty() && action.execute()) {
            fluid.shrink(out.getAmount());
            markDirty();
        }
        return out;
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (maxDrain <= 0 || fluid.isEmpty()) return FluidStack.EMPTY;
        FluidStack out = fluid.copy();
        out.setAmount(Integer.min(maxDrain, fluid.getAmount()));
        if (!out.isEmpty() && action.execute()) {
            fluid.shrink(out.getAmount());
            markDirty();
        }
        return out;
    }
}