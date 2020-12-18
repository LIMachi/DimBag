package com.limachi.dimensional_bags.common.inventory;

import com.limachi.dimensional_bags.common.NBTUtils;
import javafx.util.Pair;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class InventoryUtils {
    public static class ItemStackIORights {
        public static final ItemStackIORights VANILLA = new ItemStackIORights();
        public static final ItemStackIORights INVALID = new ItemStackIORights(false, false, 0, 0, true, true, true, false);

        public boolean canInput = true;
        public boolean canOutput = true;
        public byte minStack = 0;
        public byte maxStack = 64;
        public boolean whiteList = false;
        public boolean matchNBT = true;
        public boolean matchDamage = true;
        public boolean ignoreStackable = false;
        public ArrayList<ITag<Item>> tags = new ArrayList<>();
        public ArrayList<String> mods = new ArrayList<>();
        public ArrayList<ItemStack> stacks = new ArrayList<>();

        /**
         * default vanilla slot, no restriction, can contain between 0 and 64 items
         */
        public ItemStackIORights() {}

        /**
         * simple restricted slot: only input/output and size of stack are enforced
         */
        public ItemStackIORights(boolean canInput, boolean canOutput, int minStack, int maxStack) {
            this.canInput = canInput;
            this.canOutput = canOutput;
            this.minStack = (byte)MathHelper.clamp(minStack, 0, 64);
            this.maxStack = (byte)MathHelper.clamp(maxStack, 0, 64);
        }

        /**
         * advanced restricted slot: also sets the whitelist, nbt and damage flags
         */
        public ItemStackIORights(boolean canInput, boolean canOutput, int minStack, int maxStack, boolean whiteList, boolean matchNBT, boolean matchDamage, boolean ignoreStackable) {
            this(canInput, canOutput, minStack, maxStack);
            this.whiteList = whiteList;
            this.matchNBT = matchNBT;
            this.matchDamage = matchDamage;
            this.ignoreStackable = ignoreStackable;
        }

        public boolean isVanilla() {
            return flagsAsByte() == VANILLA.flagsAsByte() && minStack == VANILLA.minStack && maxStack == VANILLA.maxStack && tags.size() == 0 && mods.size() == 0 && stacks.size() == 0;
        }

        protected byte flagsAsByte() {
            int out = canInput ? 1 : 0;
            if (canOutput) out += 2;
            if (whiteList) out += 4;
            if (matchNBT) out += 8;
            if (matchDamage) out += 16;
            if (ignoreStackable) out += 32;
            return (byte)out;
        }

        protected void flagsFromByte(int flags) {
            canInput = (flags & 1) == 1;
            canOutput = (flags & 2) == 2;
            whiteList = (flags & 4) == 4;
            matchNBT = (flags & 8) == 8;
            matchDamage = (flags & 16) == 16;
            ignoreStackable = (flags & 32) == 32;
        }

        /**
         * store this ItemStackIORights in a CompoundNBT
         * @param nbt where to store the information (can be null, in that case, will create a new CompoundNBT)
         * @return the populated compound (might be empty for a vanilla ItemStackIORights)
         */
        public CompoundNBT writeNBT(CompoundNBT nbt) {
            if (nbt == null) nbt = new CompoundNBT();
            byte flags = flagsAsByte();
            if (flags != VANILLA.flagsAsByte()) nbt.putByte("Flags", flags);
            if (minStack != 0) nbt.putByte("Min", minStack);
            if (maxStack != 64) nbt.putByte("Max", maxStack);
            if (tags.size() != 0) nbt.put("Tags", NBTUtils.convertList(tags, null, t->NBTUtils.newCompound("TagResourceLocation", ItemTags.getCollection().getValidatedIdFromTag(t).toString())));
            if (mods.size() != 0) nbt.put("Mods", NBTUtils.convertList(mods, null, t->NBTUtils.newCompound("ModName", t)));
            if (stacks.size() != 0) nbt.put("Stacks", NBTUtils.convertList(stacks, null, t->t.write(new CompoundNBT())));
            return nbt;
        }

        /**
         * load a ItemStackIORights from a CompoundNBT
         * @param nbt the CompoundNBT to use, if null, this method will be equivalent to setting this ItemStackIORights to the vanilla default
         */
        public void readNBT(CompoundNBT nbt) {
            if (nbt == null) nbt = new CompoundNBT();
            flagsFromByte(NBTUtils.getOrDefault(nbt, "Flags", nbt::getByte, VANILLA.flagsAsByte()));
            minStack = NBTUtils.getOrDefault(nbt, "Min", nbt::getByte, VANILLA.minStack);
            maxStack = NBTUtils.getOrDefault(nbt, "Max", nbt::getByte, VANILLA.maxStack);
            ListNBT list = nbt.getList("Tags", 10);
            tags = new ArrayList<>();
            if (list.size() != 0) {
                tags.ensureCapacity(list.size());
                NBTUtils.populateList(list, tags, null, t -> ItemTags.getCollection().getTagByID(new ResourceLocation(t.getString("TagResourceLocation"))));
            }
            list = nbt.getList("Mods", 10);
            mods = new ArrayList<>();
            if (list.size() != 0) {
                mods.ensureCapacity(list.size());
                NBTUtils.populateList(list, mods, null, t -> t.getString("ModName"));
            }
            list = nbt.getList("Stacks", 10);
            stacks = new ArrayList<>();
            if (list.size() != 0) {
                stacks.ensureCapacity(list.size());
                NBTUtils.populateList(list, stacks, null, ItemStack::read);
            }
        }

        /**
         * test if at least one of the tags/mods/stacks list match the given stack
         */
        public boolean stackInList(ItemStack itemStack) {
            if (itemStack.isEmpty()) return false;
            Item item = itemStack.getItem();
            if (mods.size() > 0) {
                ResourceLocation reg = item.getRegistryName();
                String itemMod = reg == null || reg.getNamespace().equals("") ? "Minecraft" : reg.getNamespace();
                for (String mod : mods)
                    if (itemMod.equals(mod))
                        return true;
            }
            for (ITag<Item> tag : tags)
                if (tag.contains(item))
                    return true;
            for (ItemStack stack : stacks)
                if (stack.getItem() == itemStack.getItem()) {
                    if (matchDamage && stack.isDamageable() && stack.getDamage() != itemStack.getDamage()) continue;
                    if (matchNBT && stack.hasTag() && !ItemStack.areItemStackTagsEqual(stack, itemStack)) continue;
                    return true;
                }
            return false;
        }

        public boolean canMergeStacks(ItemStack s1, ItemStack s2) {
            if (s1.isEmpty() || s2.isEmpty()) return true;
            if (s1.getItem() != s2.getItem() || !ItemStack.areItemStackTagsEqual(s1, s2)) return false;
            return !ignoreStackable || s1.isStackable();
        }

        /**
         * test if we can add 'toMerge' to the slot containing 'inSlot' in accordance to this ItemStackIORights
         * @param inSlot the current state of the ItemStack of the linked slot
         * @param toMerge the stack we try to add to the slot
         * @return false if the slot cannot accept any items from 'toMerge' (either because of space, IO restriction or black/white list)
         */
        public boolean canInsert(ItemStack inSlot, ItemStack toMerge) {
            if (toMerge.isEmpty() || !canInput || inSlot.getCount() >= maxStack || (!inSlot.isEmpty() && canMergeStacks(inSlot, toMerge))) return false;
            return stackInList(toMerge) == whiteList;
        }

        /**
         * test if we can remove items from 'inSlot' and merge them with 'toMerge' in accordance to this ItemStackIORights
         * @param inSlot the current state of the ItemStack of the linked slot
         * @param toMerge the stack we try to merge, set to ItemStack.EMPTY to remove as mush as we can without merge restrictions
         * @return false if the slot cannot give any items (either because of space, IO restriction or black/white list)
         */
        public boolean canExtract(ItemStack inSlot, ItemStack toMerge) {
            return !(inSlot.isEmpty() || !canOutput || inSlot.getCount() <= minStack || (!toMerge.isEmpty() && canMergeStacks(inSlot, toMerge)));
        }

        /**
         * will try to transfer items from 'toMerge' to 'inSlot', without modifying them (return a pair of new stacks)
         * @param inSlot the current state of the ItemStack of the linked slot
         * @param toMerge the stack we try to add to the slot
         * @return a pair of new states for inSlot and toMerge (return.getKey() -> inSlot, return.getValue() -> toMerge)
         */
        public Pair<ItemStack, ItemStack> mergeIn(ItemStack inSlot, ItemStack toMerge) {
            if (!toMerge.isEmpty() && canInsert(inSlot, toMerge)) {
                int transfer = Math.min(Math.min(maxStack, toMerge.getMaxStackSize()) - inSlot.getCount(), toMerge.getCount());
                ItemStack slot = toMerge.copy();
                slot.setCount(inSlot.getCount() + transfer);
                ItemStack merge = toMerge.copy();
                merge.shrink(transfer);
                return new Pair<>(slot, merge);
            }
            return new Pair<>(inSlot.copy(), toMerge.copy());
        }

        /**
         * will try to transfer items from 'inSlot' to 'toMerge', without modifying them (return a pair of new stacks)
         * @param inSlot the current state of the ItemStack of the linked slot
         * @param qty: how many items we try to extract (set to -1 or 64 to extract as much as we can)
         * @param toMerge the stack we try to merge, set to ItemStack.EMPTY to remove without merge restrictions
         * @return a pair of new states for inSlot and toMerge (return.getKey() -> inSlot, return.getValue() -> toMerge)
         */
        public Pair<ItemStack, ItemStack> mergeOut(ItemStack inSlot, int qty, ItemStack toMerge) {
            if (!inSlot.isEmpty() && qty > 0 && canExtract(inSlot, toMerge)) {
                int transfer = Math.max(0, toMerge.getMaxStackSize() - toMerge.getCount() - Math.min(qty, inSlot.getCount()));
                ItemStack slot = inSlot.copy();
                slot.shrink(transfer);
                ItemStack merge = inSlot.copy();
                merge.setCount(toMerge.getCount() + transfer);
                return new Pair<>(slot, merge);
            }
            return new Pair<>(inSlot.copy(), toMerge.copy());
        }
    }

    public interface IIORIghtItemHandler extends IItemHandler {
        @Nonnull
        ItemStackIORights getRightsInSlot(int slot);

        void setRightsInSlot(int slot, InventoryUtils.ItemStackIORights right);
    }

    public enum ItemHandlerFormat {
        CHEST, //grid pattern, use rows and columns
        PLAYER, //player inventory pattern (hotbar + chest) (set rows to other than 0 to also include the current player hotbar + chest) (set columns to other than 0 to also include the armor and off hand)
        ENTITY //entity inventory pattern (main hand + off hand + armor)
    }

    public interface IFormatAwareItemHandler extends IIORIghtItemHandler {
        int getRows();
        int getColumns();
        ItemHandlerFormat getFormat();

        void setRows(int rows);
        void setColumns(int columns);
        void setFormat(ItemHandlerFormat format);
    }

    public static ItemStack mergeItemStack(List<Slot> inventorySlots, ItemStack stack, int startIndex, int endIndex, boolean reverseDirection, List<Integer> blackListSlot) {
        if (stack.isEmpty()) return stack;
        Pair<ItemStack, ItemStack> p;
        for (int pass = 0; pass < 2; ++pass) //do 2 passes, the first will skip empty slots to force the merge on already present stacks
            for (int i = reverseDirection ? endIndex - 1 : startIndex; !stack.isEmpty() && reverseDirection ? (i >= startIndex) : (i < endIndex); i += reverseDirection ? -1 : 1) {
                if (blackListSlot.contains(i)) continue;
                Slot slot = inventorySlots.get(i);
                if (pass == 0 && slot.getStack().isEmpty()) continue; //skip empty slots on the first pass, so we try to grow already present stacks first
                ItemStackIORights rights = ItemStackIORights.VANILLA; //default to the vanilla IO behavior
                if (slot instanceof SlotItemHandler && ((SlotItemHandler)slot).getItemHandler() instanceof IIORIghtItemHandler)
                    rights = ((IIORIghtItemHandler)((SlotItemHandler)slot).getItemHandler()).getRightsInSlot(slot.getSlotIndex());
                p = rights.mergeIn(slot.getStack(), stack);
                if (p.getValue().getCount() != stack.getCount()) { //the size of the stack changed, the merge worked
                    stack = p.getValue();
                    slot.putStack(p.getKey());
                    slot.onSlotChanged();
                }
            }
        return stack;
    }
}
