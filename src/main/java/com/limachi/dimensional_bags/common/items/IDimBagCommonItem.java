package com.limachi.dimensional_bags.common.items;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.data.IMarkDirty;
import com.limachi.dimensional_bags.common.inventory.NBTStoredItemHandler;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

//import static com.limachi.dimensional_bags.common.inventory.Wrapper.IORights.*;

public interface IDimBagCommonItem {
    /*
    String onTickCommands = "pending";
    String onCreateCommands = "crafting";
     */

    /*
    static String[] getStringList(ItemStack stack, String key) {
        if (!stack.hasTag())
            return new String[]{""};
        ListNBT list = stack.getTag().getList(key, 8);
        String[] out = new String[list.size()];
        for (int i = 0; i < list.size(); ++i)
            out[i] = list.get(i).getString();
        return out;
    }
     */

    /*
    static ItemStack addToStringList(ItemStack stack, String key, String toAdd) {
        if (!stack.hasTag())
            stack.setTag(new CompoundNBT());
        ListNBT list = stack.getTag().getList(key, 8);
        list.add(StringNBT.valueOf(toAdd));
        stack.getTag().put(key, list);
        return stack;
    }
     */

    static int getFirstValidItemFromPlayer(PlayerEntity player, Class<? extends Item> clazz, Predicate<? super ItemStack> predicate) { //will search in order: the main hand, the off hand, the armor then the rest of the inventory, returns -1 if no bag was found
        ItemStack stack = player.inventory.mainInventory.get(player.inventory.currentItem);
        if (clazz.isInstance(stack.getItem()) && predicate.test(stack)) return player.inventory.currentItem;
        stack = player.inventory.offHandInventory.get(0);
        if (clazz.isInstance(stack.getItem()) && predicate.test(stack)) return player.inventory.mainInventory.size();
        for (int i = 0; i < player.inventory.armorInventory.size(); ++i) {
            stack = player.inventory.armorInventory.get(i);
            if (clazz.isInstance(stack.getItem()) && predicate.test(stack))
                return player.inventory.mainInventory.size() + player.inventory.offHandInventory.size() + i;
        }
        for (int i = 0; i < player.inventory.mainInventory.size(); ++i) {
            stack = player.inventory.mainInventory.get(i);
            if (clazz.isInstance(stack.getItem()) && predicate.test(stack))
                return i;
        }
        return -1;
    }

    static ItemStack getItemFromPlayer(PlayerEntity player, int slot) {
        if (slot < 0) return null;
        if (slot < player.inventory.mainInventory.size()) return player.inventory.mainInventory.get(slot);
        if (slot < player.inventory.mainInventory.size() + player.inventory.offHandInventory.size()) return player.inventory.offHandInventory.get(slot - player.inventory.mainInventory.size());
        if (slot < player.inventory.mainInventory.size() + player.inventory.offHandInventory.size() + player.inventory.armorInventory.size()) return player.inventory.armorInventory.get(slot - player.inventory.mainInventory.size() - player.inventory.offHandInventory.size());
        return null;
    }

//    static int getFirstValidItemFromBag(EyeData data, Class<? extends Item> clazz, Predicate<? super ItemStack> predicate) {
//        IItemHandler inv = data.getInventory();
//        for (int i = 0; i < inv.getSlots(); ++i) {
//            ItemStack stack = inv.getStackInSlot(i);
//            if (clazz.isInstance(stack.getItem()) && predicate.test(stack))
//                return i;
//        }
//        return -1;
//    }

    static boolean _recursiveSearchItem(ItemSearchResult search, int depth, Class<? extends Item> clazz, Predicate<? super ItemStack> predicate, boolean continueAfterOne) {
        int ls = search.stackList.size();
        ArrayList<IItemHandler> pending = new ArrayList<>();
        for (int i = 0; i < search.searchedItemHandler.getSlots(); ++i) {
            search.stack = search.searchedItemHandler.getStackInSlot(i);
            search.index = i;
            if (clazz.isInstance(search.stack.getItem()) && predicate.test(search.stack)) {
                if (continueAfterOne)
                    search.stackList.add(search.stack);
                else
                    return true;
            }
            if (depth > 0) {
                IItemHandler iItemHandler = getItemHandlerFromStack(search.stack, search);
                if (iItemHandler != null)
                    pending.add(iItemHandler);
            }
        }
        for (int i = 0; i < pending.size(); ++i) {
            search.searchedItemHandler = pending.get(i);
            search.stacks[depth - 1] = search.stack;
            if (_recursiveSearchItem(search, depth - 1, clazz, predicate, continueAfterOne) && !continueAfterOne)
                return true;
        }
        return search.stackList.size() != ls;
    }

    static IItemHandler getItemHandlerFromStack(ItemStack stack, ItemSearchResult search) {
        return stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(stack.getItem() instanceof BlockItem && ((BlockItem)stack.getItem()).getBlock() instanceof ShulkerBoxBlock ? new NBTStoredItemHandler.ShulkerBoxItemHandler(stack, (search.searchedItemHandler instanceof IMarkDirty) ? (IMarkDirty)search.searchedItemHandler : null) : null);
    }

    static int slotFromHand(PlayerEntity player, Hand hand) {
        return hand == Hand.OFF_HAND ? player.inventory.getSizeInventory() - player.inventory.offHandInventory.size() : player.inventory.currentItem;
    }

    static ItemSearchResult searchItem(PlayerEntity player, int depth, Class<? extends Item> clazz, Predicate<? super ItemStack> predicate, boolean continueAfterOne) {
        ItemSearchResult search = new ItemSearchResult();
        search.searchedEntity = player;
        search.searchedInventory = player.inventory;
        search.searchedItemHandler = null;
        search.stacks = new ItemStack[depth];
        search.stackList = new ArrayList<>();
        ArrayList<IItemHandler> pending = new ArrayList<>();
        search.index = player.inventory.currentItem;
        search.stack = player.inventory.mainInventory.get(player.inventory.currentItem);
        if (clazz.isInstance(search.stack.getItem()) && predicate.test(search.stack)) {
            if (continueAfterOne)
                search.stackList.add(search.stack);
            else
                return search;
        }
        if (depth > 0) {
            IItemHandler iItemHandler = getItemHandlerFromStack(search.stack, search);
            if (iItemHandler != null)
                pending.add(iItemHandler);
        }
        int s = player.inventory.offHandInventory.size();
        int d = player.inventory.getSizeInventory() - s;
        for (int i = 0; i < s; ++i) {
            search.index = d + i;
            search.stack = player.inventory.getStackInSlot(search.index);
            if (clazz.isInstance(search.stack.getItem()) && predicate.test(search.stack)) {
                if (continueAfterOne)
                    search.stackList.add(search.stack);
                else
                    return search;
            }
            if (depth > 0) {
                IItemHandler iItemHandler = getItemHandlerFromStack(search.stack, search);
                if (iItemHandler != null)
                    pending.add(iItemHandler);
            }
        }
        s = player.inventory.armorInventory.size();
        d = player.inventory.getSizeInventory() - player.inventory.offHandInventory.size() - s;
        for (int i = 0; i < s; ++i) {
            search.index = d + i;
            search.stack = player.inventory.getStackInSlot(search.index);
            if (clazz.isInstance(search.stack.getItem()) && predicate.test(search.stack)) {
                if (continueAfterOne)
                    search.stackList.add(search.stack);
                else
                    return search;
            }
            if (depth > 0) {
                IItemHandler iItemHandler = getItemHandlerFromStack(search.stack, search);
                if (iItemHandler != null)
                    pending.add(iItemHandler);
            }
        }
        s = player.inventory.mainInventory.size();
        for (int i = 0; i < s; ++i) {
            if (i == player.inventory.currentItem) continue;
            search.index = i;
            search.stack = player.inventory.getStackInSlot(search.index);
            if (clazz.isInstance(search.stack.getItem()) && predicate.test(search.stack)) {
                if (continueAfterOne)
                    search.stackList.add(search.stack);
                else
                    return search;
            }
            if (depth > 0) {
                IItemHandler iItemHandler = getItemHandlerFromStack(search.stack, search);
                if (iItemHandler != null)
                    pending.add(iItemHandler);
            }
        }
        if (depth > 0)
            for (int i = 0; i < pending.size(); ++i) {
                search.searchedItemHandler = pending.get(i);
                search.stacks[depth - 1] = search.stack;
                if (_recursiveSearchItem(search, depth - 1, clazz, predicate, continueAfterOne) && !continueAfterOne)
                    return search;
            }
        return search.stackList.size() != 0 ? search : null;
    }

    /*class ShulkerBoxItemHandler implements IItemHandler, IMarkDirty {

        protected final ItemStack shulkerBox;
        protected final ItemStack[] stacks;
        protected final IMarkDirty parentDirty;

        public ShulkerBoxItemHandler(ItemStack shulkerBox, IMarkDirty parentDirty) {
            this.shulkerBox = shulkerBox;
            stacks = new ItemStack[27];
            for (int i = 0; i < 27; ++i)
                stacks[i] = ItemStack.EMPTY;
            if (shulkerBox.getTag() != null) {
                ListNBT list = shulkerBox.getTag().getCompound("BlockEntityTag").getList("Items", 10);
                for (int i = 0; i < list.size(); ++i) {
                    CompoundNBT nbt = list.getCompound(i);
                    stacks[nbt.getInt("Slot")] = ItemStack.read(nbt);
                }
            }
            this.parentDirty = parentDirty;
        }

        @Override
        public void markDirty() {
            write();
            if (parentDirty != null)
                parentDirty.markDirty();
        }

        protected void write() {
            ListNBT list = new ListNBT();
            for (int i = 0; i < 27; ++i)
                if (!stacks[i].isEmpty()) {
                    CompoundNBT nbt = stacks[i].write(new CompoundNBT());
                    nbt.putInt("Slot", i);
                    list.add(nbt);
                }
            if (shulkerBox.getTag() != null) {
                shulkerBox.setTag(new CompoundNBT());
                shulkerBox.getTag().put("BlockEntityTag", new CompoundNBT());
            }
            shulkerBox.getTag().getCompound("BlockEntityTag").put("Items", list);
        }

        @Override
        public int getSlots() {
            return 27;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot < 0 || slot >= 27 ? ItemStack.EMPTY : stacks[slot];
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (stack.isEmpty()) return ItemStack.EMPTY;
            if (!isItemValid(slot, stack)) return stack;
            ItemStack stackInSlot = getStackInSlot(slot);

            int stackLimit = Math.min(stack.getMaxStackSize(), getSlotLimit(slot));

            if (!stackInSlot.isEmpty())
            {
                if (stackInSlot.getCount() >= stackLimit) return stack; //limit already reached (note: since we merge stacks, there is no need to test the size of the stack already in the slot)
                if (!ItemHandlerHelper.canItemStacksStack(stack, stackInSlot)) return stack; //those stacks can't be merged
                stackLimit -= stackInSlot.getCount(); //how much can be inserted (tacking into acount the number of items already in the slot)
                if (stack.getCount() <= stackLimit) { //there is enough room to insert the entirety of the stack
                    if (!simulate) {
                        ItemStack copy = stack.copy();
                        copy.grow(stackInSlot.getCount());
                        stacks[slot] = copy;
                        markDirty();
                    }
                    return ItemStack.EMPTY;
                } else {//not enough room, we will return a truncated stack
                    stack = stack.copy();
                    if (!simulate)
                    {
                        ItemStack copy = stack.split(stackLimit);
                        copy.grow(stackInSlot.getCount());
                        stacks[slot] = copy;
                        markDirty();
                        return stack;
                    } else {
                        stack.shrink(stackLimit);
                        return stack;
                    }
                }
            } else { //the slot we want to insert the stack into is empty
                if (stackLimit < stack.getCount()) {
                    stack = stack.copy();
                    if (!simulate) {
                        stacks[slot] = stack.split(stackLimit);
                        markDirty();
                        return stack;
                    } else {
                        stack.shrink(stackLimit);
                        return stack;
                    }
                } else {
                    if (!simulate) {
                        stacks[slot] = stack;
                        markDirty();
                    }
                    return ItemStack.EMPTY;
                }
            }
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (amount == 0) return ItemStack.EMPTY; //nothing requested
            ItemStack stackInSlot = getStackInSlot(slot);
            if (stackInSlot.isEmpty()) return ItemStack.EMPTY; //nothing to extract

            amount = Math.min(amount, Math.max(0, stackInSlot.getCount()));
            if (amount == 0) return ItemStack.EMPTY; //minimum limit reached, can't remove more items

            if (simulate) {
                if (stackInSlot.getCount() < amount)
                    return stackInSlot.copy();
                else {
                    ItemStack copy = stackInSlot.copy();
                    copy.setCount(amount);
                    return copy;
                }
            } else {
                ItemStack decrStackSize = amount > 0 ? stacks[slot].split(amount) : ItemStack.EMPTY;
                markDirty();
                return decrStackSize;
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return slot >= 0 && slot < 27 && (stacks[slot].isEmpty() || (stacks[slot].isItemEqual(stack) && ItemStack.areItemStackTagsEqual(stacks[slot], stack)));
        }
    }*/

    class ItemSearchResult {
        public Entity searchedEntity;
        public IInventory searchedInventory;
        public IItemHandler searchedItemHandler;
        public int index;
        public ItemStack stack;
        public ItemStack[] stacks;
        public ArrayList<ItemStack> stackList;

        public void setStackDirty() {
            if (searchedItemHandler != null) {
                if (searchedItemHandler instanceof IMarkDirty)
                    ((IMarkDirty) searchedItemHandler).markDirty();
            } else if (searchedInventory != null)
                searchedInventory.markDirty();
        }
    }

    /*
    static void resetStringList(ItemStack stack, String key) {
        if (!stack.hasTag()) return;
        stack.getTag().put(key, new ListNBT());
    }

    static void sInventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (DimBag.isServer(worldIn) && entityIn instanceof ServerPlayerEntity) {
            String[] pending = getStringList(stack, onTickCommands);
            for (String s : pending)
                if (s.startsWith("cmd."))
                    executePendingCommand(s.substring(4), stack, worldIn, entityIn, itemSlot, isSelected);
            resetStringList(stack, onTickCommands);
        }
    }

    default void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        sInventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
    }

    static void executePendingCommand(String s, ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (worldIn.isRemote()) return;
        if (s.startsWith("upgrade.") && stack.getItem() instanceof Bag) {
            EyeData data = Bag.getData(stack, true);
            UpgradeManager.getUpgrade(s.substring(8)).upgradeCrafted(data, stack, worldIn, entityIn);
        }
        if (s.startsWith("add.") && entityIn instanceof ServerPlayerEntity) {
            int qty = 0;
            if (s.substring(4).startsWith("random.")) { //format: random.9:27 random.<min>:<max>
                String[] ss = s.substring(11).split(":");
                double r1 = Integer.parseInt(ss[0]);
                double r2 = Integer.parseInt(ss[1]);
                qty = (int) (Math.random() * (r2 - r1 + 1D) + r1);
            } else
                qty = Integer.parseInt(s.substring(4));
            if (qty > 0) {
                ItemStack cpy = stack.copy();
                cpy.setCount(qty);
                resetStringList(cpy, onCreateCommands);
                ((ServerPlayerEntity) entityIn).addItemStackToInventory(cpy);
            }
        }
        if (s.startsWith("multiply.") && entityIn instanceof ServerPlayerEntity) {
            int qty = 0;
            if (s.substring(9).startsWith("random.")) { //format: random.9:27 random.<min>:<max>
                String[] ss = s.substring(16).split(":");
                double r1 = Integer.parseInt(ss[0]);
                double r2 = Integer.parseInt(ss[1]);
                qty = (int) (Math.random() * (r2 - r1 + 1D) + r1);
            } else
                qty = Integer.parseInt(s.substring(9));
            qty = (qty - 1) * stack.getCount();
            if (qty > 0) {
                ItemStack cpy = stack.copy();
                cpy.setCount(qty);
                resetStringList(cpy, onCreateCommands);
                ((ServerPlayerEntity) entityIn).addItemStackToInventory(cpy);
            }
        }
    }

    default void onCreated(ItemStack stack, World worldIn, PlayerEntity playerIn) {
        if (DimBag.isServer(worldIn)) {
            String[] pending = getStringList(stack, onCreateCommands);
            for (String s : pending)
                if (s.startsWith("cmd."))
                    executePendingCommand(s.substring(4), stack, worldIn, playerIn, -1, false);
        }
        resetStringList(stack, onCreateCommands);
    }

    @OnlyIn(Dist.CLIENT)
    default void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        String[] pending = getStringList(stack, onTickCommands);
        for (String s : pending)
            if (s.startsWith("msg.")) {
                if (s.substring(4).startsWith("translate."))
                    tooltip.add(new TranslationTextComponent(s.substring(14)));
                else
                    tooltip.add(new StringTextComponent(s.substring(4)));
            }
    }
     */
}
