package com.limachi.dimensional_bags.common.items;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.data.IMarkDirty;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public interface IDimBagCommonItem {
    String onTickCommands = "pending";
    String onCreateCommands = "crafting";

    static String[] getStringList(ItemStack stack, String key) {
        if (!stack.hasTag())
            return new String[]{""};
        ListNBT list = stack.getTag().getList(key, 8);
        String[] out = new String[list.size()];
        for (int i = 0; i < list.size(); ++i)
            out[i] = list.get(i).getString();
        return out;
    }

    static ItemStack addToStringList(ItemStack stack, String key, String toAdd) {
        if (!stack.hasTag())
            stack.setTag(new CompoundNBT());
        ListNBT list = stack.getTag().getList(key, 8);
        list.add(StringNBT.valueOf(toAdd));
        stack.getTag().put(key, list);
        return stack;
    }

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

    static int getFirstValidItemFromBag(EyeData data, Class<? extends Item> clazz, Predicate<? super ItemStack> predicate) {
        IItemHandler inv = data.getInventory();
        for (int i = 0; i < inv.getSlots(); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (clazz.isInstance(stack.getItem()) && predicate.test(stack))
                return i;
        }
        return -1;
    }

    static void _recursiveSearchItem(ItemSearchResult search, int depth, Class<? extends Item> clazz, Predicate<? super ItemStack> predicate) {
        ArrayList<ItemStack> pending = new ArrayList<>();
        for (int i = 0; i < search.searchedItemHandler.getSlots(); ++i) {
            search.stack = search.searchedItemHandler.getStackInSlot(i);
            search.index = i;
            if (clazz.isInstance(search.stack.getItem()) && predicate.test(search.stack)) return ;
            if (depth > 0 && search.stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()) pending.add(search.stack);
        }
        for (int i = 0; i < pending.size(); ++i) {
            search.stack = pending.get(i);
            search.searchedItemHandler = search.stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
            if (search.searchedItemHandler == null) continue;
            search.stacks[depth - 1] = search.stack;
            _recursiveSearchItem(search, depth - 1, clazz, predicate);
            if (search.index != -1) return ;
        }
        search.index = -1;
    }

    static int slotFromHand(PlayerEntity player, Hand hand) {
        return hand == Hand.OFF_HAND ? player.inventory.getSizeInventory() - player.inventory.offHandInventory.size() : player.inventory.currentItem;
    }

    static ItemSearchResult searchItem(PlayerEntity player, int depth, Class<? extends Item> clazz, Predicate<? super ItemStack> predicate) {
        ItemSearchResult search = new ItemSearchResult();
        search.searchedEntity = player;
        search.searchedInventory = player.inventory;
        search.searchedItemHandler = null;
        search.stacks = new ItemStack[depth];
        ArrayList<ItemStack> pending = new ArrayList<>();
        search.index = player.inventory.currentItem;
        search.stack = player.inventory.mainInventory.get(player.inventory.currentItem);
        if (clazz.isInstance(search.stack.getItem()) && predicate.test(search.stack)) return search;
        if (depth > 0 && search.stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()) pending.add(search.stack);
        int s = player.inventory.offHandInventory.size();
        int d = player.inventory.getSizeInventory() - s;
        for (int i = 0; i < s; ++i) {
            search.index = d + i;
            search.stack = player.inventory.getStackInSlot(search.index);
            if (clazz.isInstance(search.stack.getItem()) && predicate.test(search.stack)) return search;
            if (depth > 0 && search.stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()) pending.add(search.stack);
        }
        s = player.inventory.armorInventory.size();
        d = player.inventory.getSizeInventory() - player.inventory.offHandInventory.size() - s;
        for (int i = 0; i < s; ++i) {
            search.index = d + i;
            search.stack = player.inventory.getStackInSlot(search.index);
            if (clazz.isInstance(search.stack.getItem()) && predicate.test(search.stack)) return search;
            if (depth > 0 && search.stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()) pending.add(search.stack);
        }
        s = player.inventory.mainInventory.size();
        for (int i = 0; i < s; ++i) {
            if (i == player.inventory.currentItem) continue;
            search.index = i;
            search.stack = player.inventory.getStackInSlot(search.index);
            if (clazz.isInstance(search.stack.getItem()) && predicate.test(search.stack)) return search;
            if (depth > 0 && search.stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()) pending.add(search.stack);
        }
        if (depth > 0)
            for (int i = 0; i < pending.size(); ++i) {
                search.stack = pending.get(i);
                search.searchedItemHandler = search.stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
                if (search.searchedItemHandler == null) continue;
                search.stacks[depth - 1] = search.stack;
                _recursiveSearchItem(search, depth - 1, clazz, predicate);
                if (search.index != -1)
                    return search;
            }
        return null;
    }

    class ItemSearchResult {
        public Entity searchedEntity;
        public IInventory searchedInventory;
        public IItemHandler searchedItemHandler;
        public int index;
        public ItemStack stack;
        public ItemStack[] stacks;

        public void setStackDirty() {
            if (searchedItemHandler != null) {
                if (searchedItemHandler instanceof IMarkDirty)
                    ((IMarkDirty) searchedItemHandler).markDirty();
            } else if (searchedInventory != null)
                searchedInventory.markDirty();
        }
    }

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
            EyeData data = EyeData.get(worldIn.getServer(), Bag.getId(stack));
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
}
