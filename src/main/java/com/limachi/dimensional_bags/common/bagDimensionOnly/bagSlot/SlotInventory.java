package com.limachi.dimensional_bags.common.bagDimensionOnly.bagSlot;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.lib.ConfigManager.Config;
import com.limachi.dimensional_bags.lib.common.inventory.ISimpleItemHandlerSerializable;
import com.limachi.dimensional_bags.lib.common.inventory.UpgradesInventory;
import com.limachi.dimensional_bags.common.upgrades.BaseUpgradeInventory;
import com.limachi.dimensional_bags.lib.common.tileentities.TEWithUUID;
import com.limachi.dimensional_bags.lib.utils.StackUtils;

import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.UUID;

public class SlotInventory implements ISimpleItemHandlerSerializable {

    @Config(min = "1", max = "30000000", cmt = "initial size of a bag slot (pillar inside of the bag) in stacks")
    public static int DEFAULT_SIZE_IN_STACKS = 4;

    protected UUID id = new UUID(0, 0); //unique id used to create a group of pillar
    protected int size = DEFAULT_SIZE_IN_STACKS; //how much items can be stored (in stacks, yes this means the real amount is dependent on 'referent.getMaxStackSize()')
    protected ItemStack stack = ItemStack.EMPTY; //the count of this stack may go above a byte and should be communicated using StackUtils
    protected boolean locked = false; //does this pillar need to keep it's last item
    public Runnable notifyDirt;

    protected UpgradesInventory upgrades = new UpgradesInventory(BaseUpgradeInventory.UpgradeTarget.PILLAR, 4, this::applyUpgrades);
    protected String filter; //regex on name of item (could implement a behavior of: dropping item there would extract the name)

    public boolean addSize(int add, boolean mayFail) {
        int p = size;
        size = MathHelper.clamp(size + add, 0, 30000000);
        if (mayFail && p + add != size) {
            size = p;
            return false;
        }
        if (p != size && stack.getCount() > getSizeInItems())
            stack.setCount(getSizeInItems());
        return true;
    }

    public boolean mulSize(double f, boolean mayFail) {
        int p = size;
        size = MathHelper.clamp((int)(size * f), 0, 30000000);
        if (mayFail && (int)(p * f) != size) {
            size = p;
            return false;
        }
        if (p != size && stack.getCount() > getSizeInItems())
            stack.setCount(getSizeInItems());
        return true;
    }

    protected boolean applyUpgrades() {
        ItemStack pi = stack.copy();
        int ps = size;
        size = DEFAULT_SIZE_IN_STACKS;
        boolean pl = locked;
        locked = false;
        boolean r = upgrades.applyUpgrades(this);
        if (ps != size || pl != locked || !stack.equals(pi))
            setChanged();
        return r;
    }

    public UpgradesInventory getUpgradesInventory() { return upgrades; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SlotInventory that = (SlotInventory) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }

    public void setChanged() {
        if (notifyDirt != null)
            notifyDirt.run();
    }

    private static class PillarSlot extends SlotItemHandler {

        public PillarSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) { super(itemHandler, index, xPosition, yPosition); }

        @Override
        public int getMaxStackSize(@Nonnull ItemStack stack) {
            IItemHandler handler = this.getItemHandler();
            return handler.isItemValid(0, stack) ? handler.getSlotLimit(0) : 0;
        }
    }

    @Override
    public Slot createSlot(int index, int x, int y) { return new PillarSlot(this, index, x, y); }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT out = new CompoundNBT();
        out.putUUID("UUID", id);
        out.putInt("size", size);
        CompoundNBT ref = new CompoundNBT();
        out.put("stack", StackUtils.writeAsCompound(stack));
        out.put("upgrades", upgrades.serializeNBT());
        return out;
    }

    public UUID getId() { return id; }

    public void setId(UUID id) { this.id = id; setChanged(); }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        CompoundNBT t = nbt.getCompound("BlockEntityTag").getCompound("ForgeData");
        if (t.contains(TEWithUUID.NBT_KEY_UUID))
            id = t.getUUID(TEWithUUID.NBT_KEY_UUID);
        else
            id = nbt.getUUID("UUID");
        size = nbt.getInt("size");
        stack = StackUtils.readFromCompound(nbt.getCompound("stack"));
        upgrades.deserializeNBT(nbt.getCompound("upgrades"));
    }

    public void setLockState(boolean lock) {
        locked = lock;
    }

    public boolean getLockState() { return locked; }

    public int getUsageInStacks() { return stack.isEmpty() ? 0 : (int)Math.ceil((double)stack.getCount() / (double)stack.getMaxStackSize()); }

    public int getSizeInItems() { return size * stack.getMaxStackSize(); }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack input) {
        stack = input;
        setChanged();
    }

    public void silentSetStackInSlot(int slot, @Nonnull ItemStack input) { stack = input; }

    public void setCreativeStack() { stack.setCount(Integer.MAX_VALUE); }

    @Override
    public int getSlots() { return 1; }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) { return stack; }

    public boolean isVoid() {
//        for (ItemStack i : upgrades.upgrades)
//            if (!i.isEmpty() && ((BaseUpgradeInventory)i.getItem()).isVoid())
//                return true;
        return false;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack input, boolean simulate) {
        if (!isItemValid(slot, input)) return input;
        boolean isVoid = isVoid();
        int toInput = Math.min(input.getCount(), getSizeInItems() - stack.getCount());
        if (toInput <= 0) return isVoid ? ItemStack.EMPTY : input;
        ItemStack out = input.copy();
        out.shrink(toInput);
        if (!simulate) {
            if (stack.isEmpty()) {
                stack = input.copy();
                stack.setCount(toInput);
            } else
                stack.grow(toInput);
            setChanged();
        }
        return isVoid ? ItemStack.EMPTY : out;
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
            setChanged();
        }
        return out;
    }

    @Override
    public int getSlotLimit(int slot) { return size * stack.getMaxStackSize(); }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack test) { return !test.isEmpty() && (stack.isEmpty() || StackUtils.areStackable(stack, test)); }

    @Override
    public void readFromBuff(PacketBuffer buff) {
        id = buff.readUUID();
        size = buff.readInt();
        stack = buff.readItem();
        locked = buff.readBoolean();
        upgrades.readFromBuff(buff);
    }

    @Override
    public void writeToBuff(PacketBuffer buff) {
        buff.writeUUID(id);
        buff.writeInt(size);
        buff.writeItemStack(stack, false);
        buff.writeBoolean(locked);
        upgrades.writeToBuff(buff);
    }
}
