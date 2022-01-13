package com.limachi.dimensional_bags.lib.common.inventory;

import com.limachi.dimensional_bags.common.upgrades.BaseUpgradeInventory;
import com.limachi.dimensional_bags.lib.utils.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.function.Supplier;

public class UpgradesInventory implements ISimpleItemHandlerSerializable {

    protected final ArrayList<ItemStack> upgrades = new ArrayList<>();
    protected final BaseUpgradeInventory.UpgradeTarget target;
    protected final Supplier<Boolean> runOnUpgradeSetChange;

    public boolean applyUpgrades(Object target) {
        boolean ok = true;
        for (ItemStack i : upgrades)
            if (!i.isEmpty())
                if (!((BaseUpgradeInventory)i.getItem()).applySequentialUpgrades(i.getCount(), target))
                    ok = false;
        return ok;
    }

    public UpgradesInventory(BaseUpgradeInventory.UpgradeTarget target, int upgrade_count, Supplier<Boolean> runOnUpgradeSetChange) {
        this.runOnUpgradeSetChange = runOnUpgradeSetChange;
        for (int i = 0; i < upgrade_count; ++i)
            upgrades.add(ItemStack.EMPTY);
        this.target = target;
    }

    @Override
    public void readFromBuff(PacketBuffer buff) {
        int c = buff.readInt();
        upgrades.clear();
        for (int i = 0; i < c; ++i)
            upgrades.add(buff.readItem());
        runOnUpgradeSetChange.get();
    }

    @Override
    public void writeToBuff(PacketBuffer buff) {
        buff.writeInt(upgrades.size());
        upgrades.forEach(i->buff.writeItemStack(i, false));
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT out = new CompoundNBT();
        ListNBT list = new ListNBT();
        upgrades.forEach(i->list.add(StackUtils.writeAsCompound(i)));
        out.put("Stacks", list);
        return out;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        ListNBT list = nbt.getList("Stacks", 10);
        upgrades.clear();
        list.forEach(n->upgrades.add(ItemStack.of((CompoundNBT)n)));
        runOnUpgradeSetChange.get();
    }

    /**
     * try to install the upgrade in the first valid slot available, similar to a shift click in an open inventory
     * returns the remainder (all the stack if it could not be installed, some of it if the maximum amount of upgrades is reached or empty if it could fully be installed)
     */
    public ItemStack installUpgrades(ItemStack stack) {
        if (stack.getCount() < 1 || !(stack.getItem() instanceof BaseUpgradeInventory) || ((BaseUpgradeInventory)stack.getItem()).target != target) return stack;
        int oc = stack.getCount();
        for (int pass = 0; pass < 2 && stack.getCount() > 0; ++pass) {
            for (int s = 0; s < upgrades.size() && stack.getCount() > 0; ++s) {
                ItemStack c = upgrades.get(s);
                if ((pass == 0 && c.isEmpty()) || !(c.isEmpty() || c.getItem().equals(stack.getItem()))) continue;
                //if we made it there, the slot is valid, we can try to fill it
                int toInsert = Math.min(stack.getCount(), stack.getMaxStackSize() - c.getCount());
                if (toInsert <= 0) continue; //can't add more, skip
                upgrades.set(s, new ItemStack(stack.getItem(), toInsert + c.getCount()));
                stack.shrink(toInsert);
            }
        }
        if (oc != stack.getCount())
            runOnUpgradeSetChange.get();
        return stack;
    }

    protected boolean validSlot(int slot) { return slot >= 0 && slot < upgrades.size(); }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        if (isItemValid(slot, stack)) {
            ItemStack os = upgrades.get(slot).copy();
            upgrades.set(slot, stack);
            if (!os.equals(stack, false))
                runOnUpgradeSetChange.get();
        }
    }

    @Override
    public int getSlots() { return upgrades.size(); }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) { return validSlot(slot) ? upgrades.get(slot) : ItemStack.EMPTY; }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || !isItemValid(slot, stack)) return stack;
        int space = stack.getMaxStackSize() - upgrades.get(slot).getCount();
        if (space <= 0) return stack;
        int toAdd = Math.min(stack.getCount(), space);
        ItemStack out = stack.copy();
        out.setCount(stack.getCount() - toAdd);
        if (!simulate) {
            ItemStack n = stack.copy();
            n.setCount(upgrades.get(slot).getCount() + toAdd);
            upgrades.set(slot, n);
            runOnUpgradeSetChange.get();
        }
        return out;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0 || !validSlot(slot) || upgrades.get(slot).isEmpty()) return ItemStack.EMPTY;
        ItemStack s = upgrades.get(slot);
        int rem = Math.min(s.getCount(), amount);
        ItemStack out = s.copy();
        out.setCount(rem);
        if (!simulate) {
            s.shrink(rem);
            runOnUpgradeSetChange.get();
        }
        return out;
    }

    @Override
    public int getSlotLimit(int slot) { return 64; }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) { return validSlot(slot) && stack.getItem() instanceof BaseUpgradeInventory && ((BaseUpgradeInventory)stack.getItem()).target == target && (upgrades.get(slot).isEmpty() || (upgrades.get(slot).getItem() == stack.getItem() && ItemStack.tagMatches(upgrades.get(slot), stack))); }
}
