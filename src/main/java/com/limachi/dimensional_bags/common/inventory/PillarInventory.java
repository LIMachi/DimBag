package com.limachi.dimensional_bags.common.inventory;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.Config.Config;
import com.limachi.dimensional_bags.common.StackUtils;
import com.limachi.dimensional_bags.common.container.SimpleContainer;
import com.limachi.dimensional_bags.common.data.IMarkDirty;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class PillarInventory implements ISimpleItemHandler {

    @Config.IntRange(def = 4, min = 1, max = 30_000_000, cmt = "initial size of a bag slot (pillar inside of the bag) in stacks")
    public static int DEFAULT_SIZE_IN_STACKS;

    protected UUID id = UUID.randomUUID(); //unique id used to create a group of pillar
    protected int size = DEFAULT_SIZE_IN_STACKS; //how much items can be stored (in stacks, yes this means the real amount is dependent on 'referent.getMaxStackSize()')
    protected ItemStack stack = ItemStack.EMPTY; //the count of this stack may go above a byte and should be communicated using StackUtils
    protected boolean locked = false; //does this pillar need to keep it's last item
    public IMarkDirty notifyDirt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PillarInventory that = (PillarInventory) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }

    public void markDirty() {
        if (notifyDirt != null)
            notifyDirt.markDirty();
    }

    private static class PillarSlot extends SlotItemHandler {

        public PillarSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public int getItemStackLimit(@Nonnull ItemStack stack) {
            IItemHandler handler = this.getItemHandler();
            return handler.isItemValid(0, stack) ? handler.getSlotLimit(0) : 0;
        }
    }

    @Override
    public Slot createSlot(int index, int x, int y) {
        return new PillarSlot(this, index, x, y);
    }

    public void open(ServerPlayerEntity player) {
        ArrayList<Integer> list = new ArrayList<>();
        list.add(0);
        SimpleContainer.open(player, new TranslationTextComponent("inventory.pillar.name"), this, list, 1, 1, t->true);
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT out = new CompoundNBT();
        out.putUniqueId("UUID", id);
        out.putInt("size", size);
        CompoundNBT ref = new CompoundNBT();
        out.put("stack", StackUtils.writeAsCompound(stack));
        return out;
    }

    public UUID getId() { return id; }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        id = nbt.getUniqueId("UUID");
        size = nbt.getInt("size");
        stack = StackUtils.readFromCompound(nbt.getCompound("stack"));
    }

    public void setLockState(boolean lock) {
        if (lock != locked)
            markDirty();
        locked = lock;
    }

    public boolean getLockState() { return locked; }

    public int getUsageInStacks() { return stack.isEmpty() ? 0 : (int)Math.ceil((double)stack.getCount() / (double)stack.getMaxStackSize()); }

    public int getSizeInItems() { return size * stack.getMaxStackSize(); }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack input) {
        stack = input;
        markDirty();
    }

    @Override
    public int getSlots() { return 1; }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) { return stack; }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack input, boolean simulate) {
        if (!isItemValid(slot, input)) return input;
        int toInput = Math.min(input.getCount(), getSizeInItems() - stack.getCount());
        if (toInput == 0) return input;
        ItemStack out = input.copy();
        out.shrink(toInput);
        if (!simulate) {
            if (stack.isEmpty()) {
                stack = input.copy();
                stack.setCount(toInput);
            } else
                stack.grow(toInput);
            markDirty();
        }
        return out;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount > stack.getMaxStackSize()) { //this might occur if a mod is requesting the entirety of the inventory or does not check the max size of a stack before output, we reduce the amount of the request to prevent incompatibility with vanilla which could cause loss of items
            DimBag.LOGGER.error("watch out, we've got a badass here");
            amount = stack.getMaxStackSize();
        }
        if (amount <= 0 || stack.isEmpty()) return ItemStack.EMPTY;
        int toOutput = Math.min(stack.getCount(), amount);
        if (locked && toOutput > 0 && stack.getCount() - toOutput == 0)
            --toOutput;
        if (toOutput == 0) return ItemStack.EMPTY;
        ItemStack out = stack.copy();
        out.setCount(toOutput);
        if (!simulate) {
            stack.shrink(toOutput);
            markDirty();
        }
        return out;
    }

    @Override
    public int getSlotLimit(int slot) { return size * stack.getMaxStackSize(); }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack test) { return !test.isEmpty() && (stack.isEmpty() || SimpleContainer.areStackable(stack, test)); }

    @Override
    public void readFromBuff(PacketBuffer buff) {
        id = buff.readUniqueId();
        size = buff.readInt();
        stack = buff.readItemStack();
        locked = buff.readBoolean();
    }

    @Override
    public void writeToBuff(PacketBuffer buff) {
        buff.writeUniqueId(id);
        buff.writeInt(size);
        buff.writeItemStack(stack);
        buff.writeBoolean(locked);
    }
}
