package com.limachi.dim_bag.utils;

import com.limachi.dim_bag.menus.slots.BagSlot;
import com.limachi.dim_bag.menus.slots.BagTankSlot;
import com.limachi.dim_bag.menus.slots.TankSlot;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

public class Menus {
    public record SlotSection(int from, int toInclusive, boolean inverseInsertion, boolean searchUp){}

    public static Optional<ItemStack> interactWithFluidSlot(Player player, ItemStack stack, TankSlot slot, IItemHandler dropBackInventory) {
        FluidActionResult fluidActionResult = FluidUtil.tryFillContainerAndStow(stack, slot, dropBackInventory, Integer.MAX_VALUE, player, true);
        if (!fluidActionResult.isSuccess())
            fluidActionResult = FluidUtil.tryEmptyContainerAndStow(stack, slot, dropBackInventory, Integer.MAX_VALUE, player, true);
        if (fluidActionResult.isSuccess())
            return Optional.of(fluidActionResult.getResult());
        return Optional.empty();
    }

    public static boolean moveItemStackTo(Player player, int index, SlotSection section, NonNullList<Slot> slots) {
        boolean didMove = false;
        int i = section.inverseInsertion ? section.toInclusive : section.from;
        ItemStack stack = slots.get(index).getItem();

        if (stack.isEmpty()) return false;

        //first, try to insert in a slot that contains the same item (if stackable)
        if (stack.isStackable()) {
            while (!stack.isEmpty() && (section.inverseInsertion ? i >= section.from : i <= section.toInclusive)) {

                Slot slot = slots.get(i);
                ItemStack itemstack = slot.getItem();
                if (!itemstack.isEmpty() && ItemStack.isSameItemSameTags(stack, itemstack)) {
                    int j = itemstack.getCount() + stack.getCount();
                    int maxSize = Math.min(slot.getMaxStackSize(), stack.getMaxStackSize());
                    if (j <= maxSize) {
                        stack.setCount(0);
                        itemstack.setCount(j);
                        slot.setChanged();
                        didMove = true;
                    } else if (itemstack.getCount() < maxSize) {
                        stack.shrink(maxSize - itemstack.getCount());
                        itemstack.setCount(maxSize);
                        slot.setChanged();
                        didMove = true;
                    }
                }

                if (section.inverseInsertion)
                    --i;
                else
                    ++i;
            }
        }

        //if the first operation failed, try to interact with fluid slots (derived from FluidUtil#interactWithFluidHandler)
        if (!didMove && stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent()) {
            IItemHandler dropBackInventory = slots.get(index) instanceof SlotItemHandler sih ? sih.getItemHandler() : new InvWrapper(slots.get(index).container);
            i = section.inverseInsertion ? section.toInclusive : section.from;

            while (section.inverseInsertion ? i >= section.from : i <= section.toInclusive) {

                if (slots.get(i) instanceof TankSlot slot && slot.isActive() && interactWithFluidSlot(player, stack, slot, dropBackInventory).map(s->{slots.get(index).set(s); return true;}).orElse(false))
                    return true;

                if (section.inverseInsertion)
                    --i;
                else
                    ++i;
            }
        }

        //finally (even if the first operation succeeded, but never if the second succeeded) try to put the item in an empty slot
        if (!stack.isEmpty()) {
            i = section.inverseInsertion ? section.toInclusive : section.from;

            while (section.inverseInsertion ? i >= section.from : i <= section.toInclusive) {

                Slot slot1 = slots.get(i);
                ItemStack itemstack1 = slot1.getItem();
                if (itemstack1.isEmpty() && slot1.mayPlace(stack)) {
                    if (stack.getCount() > slot1.getMaxStackSize())
                        slot1.setByPlayer(stack.split(slot1.getMaxStackSize()));
                    else
                        slot1.setByPlayer(stack.split(stack.getCount()));

                    slot1.setChanged();
                    didMove = true;
                    break;
                }

                if (section.inverseInsertion)
                    --i;
                else
                    ++i;
            }
        }

        if (didMove)
            slots.get(index).set(stack);
        return didMove;
    }

    public static boolean similarContainer(Slot s1, Slot s2) {
        if (s1 instanceof BagSlot bs1 && s2 instanceof BagSlot bs2)
            return bs1.bag == bs2.bag;
        if (s1 instanceof BagSlot || s2 instanceof BagSlot)
            return false;
        if (s1 instanceof BagTankSlot bs1 && s2 instanceof BagTankSlot bs2)
            return bs1.bag == bs2.bag;
        if (s1 instanceof BagTankSlot || s2 instanceof BagTankSlot)
            return false;
        if (s1 instanceof SlotItemHandler sih1 && s2 instanceof SlotItemHandler sih2)
            return sih1.getItemHandler().equals(sih2.getItemHandler());
        if (s1 instanceof SlotItemHandler || s2 instanceof SlotItemHandler)
            return false;
        if (s1 instanceof TankSlot ts1 && s2 instanceof TankSlot ts2)
            return ts1.getTankHandler().equals(ts2.getTankHandler());
        if (s1 instanceof TankSlot || s2 instanceof TankSlot)
            return false;
        return s1.container.equals(s2.container);
    }

    /**
     * default behavior:
     * try to move the stack in a slot that does not have the same inventory (if no sections are found)
     * compatible with fluid slots (shift clicking a stack that can contain fluid will first try to stack it with existing item, then try to fill it/empty it, and only then try to find an empty slot to store it)
     */

    public static ItemStack quickMoveStack(AbstractContainerMenu menu, Player player, int slot, Collection<SlotSection> sections) {
        if (menu == null || menu.slots.isEmpty() || slot < 0 || slot >= menu.slots.size() || menu.slots.get(slot) instanceof TankSlot || !menu.slots.get(slot).hasItem()) return ItemStack.EMPTY; //for now, shift clicking a tank slot does nothing
        ArrayList<SlotSection> ls = new ArrayList<>(sections);
        if (ls.isEmpty()) {
            int from = 0;
            while (from < menu.slots.size()) {
                Slot t = menu.slots.get(from);
                int i = from + 1;
                for (; i < menu.slots.size(); ++i)
                    if (!similarContainer(t, menu.slots.get(i)))
                        break;
                ls.add(new SlotSection(from, i - 1, false, true));
                from = i;
            }
        }
        for (int i = 0; i < ls.size(); ++i) {
            SlotSection section = ls.get(i);
            if (slot >= section.from && slot <= section.toInclusive) {
                if (section.searchUp) {
                    int j = i == 0 ? ls.size() - 1 : i - 1;
                    for (; j != i; j = j == 0 ? ls.size() - 1 : j - 1)
                        if (moveItemStackTo(player, slot, ls.get(j), menu.slots))
                            return ItemStack.EMPTY;
                } else {
                    int j = i == ls.size() - 1 ? 0 : i + 1;
                    for (; j != i; j = j == ls.size() - 1 ? 0 : j + 1)
                        if (moveItemStackTo(player, slot, ls.get(j), menu.slots))
                            return ItemStack.EMPTY;
                }
                break;
            }
        }
        return menu.slots.get(slot).getItem();
    }
    public static ItemStack quickMoveStack(AbstractContainerMenu menu, Player player, int slot, SlotSection ... sections) {
        return quickMoveStack(menu, player, slot, Arrays.asList(sections));
    }
}
