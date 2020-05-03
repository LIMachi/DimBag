package com.limachi.dimensional_bags.common.data.container;

import com.google.common.collect.Sets;
import com.limachi.dimensional_bags.DimensionalBagsMod;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.data.container.slot.BaseSlot;
import com.limachi.dimensional_bags.common.data.inventory.BaseInventory;
import com.limachi.dimensional_bags.common.references.GUIs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.NonNullList;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.Set;

import static com.limachi.dimensional_bags.common.references.GUIs.ScreenParts.SLOT_SIZE_X;
import static com.limachi.dimensional_bags.common.references.GUIs.ScreenParts.SLOT_SIZE_Y;

public abstract class BaseContainer extends Container {

    public final BaseInventory inventory;
    public final PlayerInventory playerInv;

    private final PlayerEntity player;

    private int dragEvent; //overwrite of Container private/protected variables
    private int dragMode = -1; //overwrite of Container private/protected variables
    private final Set<Slot> dragSlots = Sets.newHashSet(); //overwrite of Container private/protected variables

//    private final NonNullList<ItemStack> inventoryItemStacks = NonNullList.create();

    protected BaseContainer(@Nullable ContainerType<?> type, int id, PlayerInventory playerInv, BaseInventory inventoryIn) {
        super(type, id);
        this.playerInv = playerInv;
        this.inventory = inventoryIn;
        this.player = playerInv.player;
        trackIntArray(new IIntArray() { //tracker/updater for the size of the container (total, rows, columns)
            @Override
            public int get(int index) {
                switch (index) {
                    case 0: return inventory.getSizeInventory();
                    case 1: return inventory.getRows();
                    case 2: return inventory.getColumns();
                }
                return 0;
            }

            @Override
            public void set(int index, int value) {
                if (index >= 0 && index <= 2 && value != get(index)) {
                    inventory.resizeInventory(index == 0 ? value : inventory.getSizeInventory(),
                            index == 1 ? value : inventory.getRows(),
                            index == 2 ? value : inventory.getColumns());
//                    if (DimensionalBagsMod.isServer(playerInv.player.world)) {
//                        DimensionalBagsMod.LOGGER.info("Inventory size changed, rebuilding slots server side");
//                        reAddSlots();
//                    }
                }
            }

            @Override
            public int size() {
                return 3;
            }
        });
    }

    public abstract void reAddSlots();
    protected abstract void addContainerSlots(int ix, int iy);

    protected void addSlots(int ix, int iy, boolean p, int px, int py) {
        this.inventorySlots.clear();
//        this.inventoryItemStacks.clear();
        this.addContainerSlots(ix, iy);
        if (p) {
            int dx = px + GUIs.ScreenParts.PLAYER_INVENTORY_FIRST_SLOT_X + 1;
            int dy = py + GUIs.ScreenParts.PLAYER_INVENTORY_FIRST_SLOT_Y + 1;
            for (int y = 0; y < GUIs.ScreenParts.PLAYER_INVENTORY_ROWS; ++y)
                for (int x = 0; x < GUIs.ScreenParts.PLAYER_INVENTORY_COLUMNS; ++x)
                    this.addSlot(new Slot(playerInv, x + (y + 1) * GUIs.ScreenParts.PLAYER_INVENTORY_COLUMNS, dx + x * GUIs.ScreenParts.SLOT_SIZE_X, dy + y * GUIs.ScreenParts.SLOT_SIZE_Y));
            dy = py + GUIs.ScreenParts.PLAYER_BELT_FIRST_SLOT_Y + 1;
            for (int x = 0; x < GUIs.ScreenParts.PLAYER_INVENTORY_COLUMNS; ++x)
                this.addSlot(new Slot(playerInv, x, dx + x * GUIs.ScreenParts.SLOT_SIZE_X, dy));
        }
    }

    /*
    protected void addPlayerSlotsContainer(PlayerInventory inventory, int sx, int sy) {
        int dx = sx + GUIs.ScreenParts.PLAYER_INVENTORY_FIRST_SLOT_X + 1;
        int dy = sy + GUIs.ScreenParts.PLAYER_INVENTORY_FIRST_SLOT_Y + 1;
        for (int y = 0; y < GUIs.ScreenParts.PLAYER_INVENTORY_ROWS; ++y)
            for (int x = 0; x < GUIs.ScreenParts.PLAYER_INVENTORY_COLUMNS; ++x)
                this.addSlot(new Slot(inventory, x + (y + 1) * GUIs.ScreenParts.PLAYER_INVENTORY_COLUMNS, dx + x * GUIs.ScreenParts.SLOT_SIZE_X, dy + y * GUIs.ScreenParts.SLOT_SIZE_Y));
        dy = sy + GUIs.ScreenParts.PLAYER_BELT_FIRST_SLOT_Y + 1;
        for (int x = 0; x < GUIs.ScreenParts.PLAYER_INVENTORY_COLUMNS; ++x)
            this.addSlot(new Slot(inventory, x, dx + x * GUIs.ScreenParts.SLOT_SIZE_X, dy));
    }
    */

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) { return true; } //by default, all containers of this mod can interact with the player

    private void slotClickQuickCraft(int slotId, int dragType, PlayerEntity player) {
        int j1 = this.dragEvent;
        this.dragEvent = getDragEvent(dragType);
        if ((j1 != 1 || this.dragEvent != 2) && j1 != this.dragEvent) {
            this.resetDrag();
        } else if (player.inventory.getItemStack().isEmpty()) {
            this.resetDrag();
        } else if (this.dragEvent == 0) {
            this.dragMode = extractDragMode(dragType);
            if (isValidDragMode(this.dragMode, player)) {
                this.dragEvent = 1;
                this.dragSlots.clear();
            } else {
                this.resetDrag();
            }
        } else if (this.dragEvent == 1) {
            Slot slot7 = this.inventorySlots.get(slotId);
            ItemStack itemstack12 = player.inventory.getItemStack();
            if (slot7 != null && canAddItemToSlot(slot7, itemstack12, true) && slot7.isItemValid(itemstack12) && (this.dragMode == 2 || itemstack12.getCount() > this.dragSlots.size()) && this.canDragIntoSlot(slot7)) {
                this.dragSlots.add(slot7);
            }
        } else if (this.dragEvent == 2) {
            if (!this.dragSlots.isEmpty()) {
                ItemStack itemstack9 = player.inventory.getItemStack().copy();
                int k1 = player.inventory.getItemStack().getCount();

                for(Slot slot8 : this.dragSlots) {
                    ItemStack itemstack13 = player.inventory.getItemStack();
                    if (slot8 != null && canAddItemToSlot(slot8, itemstack13, true) && slot8.isItemValid(itemstack13) && (this.dragMode == 2 || itemstack13.getCount() >= this.dragSlots.size()) && this.canDragIntoSlot(slot8)) {
                        ItemStack itemstack14 = itemstack9.copy();
                        int j3 = slot8.getHasStack() ? slot8.getStack().getCount() : 0;
                        computeStackSize(this.dragSlots, this.dragMode, itemstack14, j3);
                        int k3 = Math.min(itemstack14.getMaxStackSize(), slot8.getItemStackLimit(itemstack14));
                        if (itemstack14.getCount() > k3) {
                            itemstack14.setCount(k3);
                        }

                        k1 -= itemstack14.getCount() - j3;
                        slot8.putStack(itemstack14);
                    }
                }

                itemstack9.setCount(k1);
                player.inventory.setItemStack(itemstack9);
            }

            this.resetDrag();
        } else {
            this.resetDrag();
        }
    }

    public ItemStack slotClickPickup(int slotId, int dragType, PlayerEntity player) {

        ItemStack out = ItemStack.EMPTY;

        Slot slot = this.inventorySlots.get(slotId);
        boolean input = BaseSlot.getInputRights(slot);
        boolean output = BaseSlot.getOuputRights(slot);
        if (slot != null) {
            ItemStack slotStack = slot.getStack();
            ItemStack heldStack = player.inventory.getItemStack();
            if (!slotStack.isEmpty()) {
                out = slotStack.copy();
            }

            if (slotStack.isEmpty()) {
                if (!heldStack.isEmpty() && slot.isItemValid(heldStack)) {
                    int splitQuantity = dragType == 0 ? heldStack.getCount() : 1;
                    if (splitQuantity > slot.getItemStackLimit(heldStack)) {
                        splitQuantity = slot.getItemStackLimit(heldStack);
                    }

                    slot.putStack(heldStack.split(splitQuantity));
                }
            } else if (slot.canTakeStack(player)) { //the important thing to change is there for click
                if (heldStack.isEmpty()) { //take mode
                    if (output) { //only accept if the slot can output items
                        if (slotStack.isEmpty()) { //reset empty the slot, vanilla code
                            slot.putStack(ItemStack.EMPTY);
                            player.inventory.setItemStack(ItemStack.EMPTY);
                        } else { //try to remove all or half the stack, based on button type
                            int k2 = dragType == 0 ? slotStack.getCount() : (slotStack.getCount() + 1) / 2;
                            player.inventory.setItemStack(slot.decrStackSize(k2));
                            if (slotStack.isEmpty()) {
                                slot.putStack(ItemStack.EMPTY);
                            }

                            slot.onTake(player, player.inventory.getItemStack());
                        }
                    }
                } else if (slot.isItemValid(heldStack)) { //test if the slot can accept the heldStack
                    if (areItemsAndTagsEqual(slotStack, heldStack) && input) { //try to merge (need input right)
                        int l2 = dragType == 0 ? heldStack.getCount() : 1;
                        if (l2 > slot.getItemStackLimit(heldStack) - slotStack.getCount()) {
                            l2 = slot.getItemStackLimit(heldStack) - slotStack.getCount();
                        }

                        if (l2 > heldStack.getMaxStackSize() - slotStack.getCount()) {
                            l2 = heldStack.getMaxStackSize() - slotStack.getCount();
                        }

                        heldStack.shrink(l2);
//                        slotStack.grow(l2); changed to a slot method for better tracking
                        ItemStack cpy = slotStack.copy();
                        cpy.grow(l2);
                        /*
                        if (slot.inventory instanceof BaseInventory)
                            slot.inventory.setInventorySlotContents(slotId, cpy); //should fix tracking issuese
                        else*/
                            slot.putStack(cpy);
                    } else if (heldStack.getCount() <= slot.getItemStackLimit(heldStack) && input && output) { //swap stacks (need all IO rights)
                        slot.putStack(heldStack);
                        player.inventory.setItemStack(slotStack);
                    }
                } else if (heldStack.getMaxStackSize() > 1 && !slotStack.isEmpty() && areItemsAndTagsEqual(slotStack, heldStack) && output) { //remove from slot to fill held item, need output right
                    int i3 = slotStack.getCount();
                    if (i3 + heldStack.getCount() <= heldStack.getMaxStackSize()) {
                        heldStack.grow(i3);
                        slotStack = slot.decrStackSize(i3);
                        if (slotStack.isEmpty()) {
                            slot.putStack(ItemStack.EMPTY);
                        }

                        slot.onTake(player, player.inventory.getItemStack());
                    }
                }
            }

            slot.onSlotChanged();
        }
        return out;
    }

    @Override
    public void resetDrag() {
        this.dragEvent = 0;
        this.dragSlots.clear();
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
        if (slotId != -999) //special slot id, usually used for dropping items
            if (slotId < 0 || slotId >= this.getInventory().size()) //not my index? not my concern
                return ItemStack.EMPTY;
        ItemStack itemstack = ItemStack.EMPTY;
//        PlayerInventory playerinventory = player.inventory;
        if (clickTypeIn == ClickType.QUICK_CRAFT) {
            slotClickQuickCraft(slotId, dragType, player);
            /*
            int j1 = this.dragEvent;
            this.dragEvent = getDragEvent(dragType);
            if ((j1 != 1 || this.dragEvent != 2) && j1 != this.dragEvent) {
                this.resetDrag();
            } else if (playerinventory.getItemStack().isEmpty()) {
                this.resetDrag();
            } else if (this.dragEvent == 0) {
                this.dragMode = extractDragMode(dragType);
                if (isValidDragMode(this.dragMode, player)) {
                    this.dragEvent = 1;
                    this.dragSlots.clear();
                } else {
                    this.resetDrag();
                }
            } else if (this.dragEvent == 1) {
                Slot slot7 = this.inventorySlots.get(slotId);
                ItemStack itemstack12 = playerinventory.getItemStack();
                if (slot7 != null && canAddItemToSlot(slot7, itemstack12, true) && slot7.isItemValid(itemstack12) && (this.dragMode == 2 || itemstack12.getCount() > this.dragSlots.size()) && this.canDragIntoSlot(slot7)) {
                    this.dragSlots.add(slot7);
                }
            } else if (this.dragEvent == 2) {
                if (!this.dragSlots.isEmpty()) {
                    ItemStack itemstack9 = playerinventory.getItemStack().copy();
                    int k1 = playerinventory.getItemStack().getCount();

                    for(Slot slot8 : this.dragSlots) {
                        ItemStack itemstack13 = playerinventory.getItemStack();
                        if (slot8 != null && canAddItemToSlot(slot8, itemstack13, true) && slot8.isItemValid(itemstack13) && (this.dragMode == 2 || itemstack13.getCount() >= this.dragSlots.size()) && this.canDragIntoSlot(slot8)) {
                            ItemStack itemstack14 = itemstack9.copy();
                            int j3 = slot8.getHasStack() ? slot8.getStack().getCount() : 0;
                            computeStackSize(this.dragSlots, this.dragMode, itemstack14, j3);
                            int k3 = Math.min(itemstack14.getMaxStackSize(), slot8.getItemStackLimit(itemstack14));
                            if (itemstack14.getCount() > k3) {
                                itemstack14.setCount(k3);
                            }

                            k1 -= itemstack14.getCount() - j3;
                            slot8.putStack(itemstack14);
                        }
                    }

                    itemstack9.setCount(k1);
                    playerinventory.setItemStack(itemstack9);
                }

                this.resetDrag();
            } else {
                this.resetDrag();
            }
        */} else if (this.dragEvent != 0) {
            this.resetDrag();
        } else if ((clickTypeIn == ClickType.PICKUP || clickTypeIn == ClickType.QUICK_MOVE) && (dragType == 0 || dragType == 1)) {
            if (slotId == -999) {
                if (!player.inventory.getItemStack().isEmpty()) {
                    if (dragType == 0) {
                        player.dropItem(player.inventory.getItemStack(), true);
                        player.inventory.setItemStack(ItemStack.EMPTY);
                    }

                    if (dragType == 1) {
                        player.dropItem(player.inventory.getItemStack().split(1), true);
                    }
                }
            } else if (clickTypeIn == ClickType.QUICK_MOVE) {
                if (slotId < 0) {
                    return ItemStack.EMPTY;
                }

                Slot slot5 = this.inventorySlots.get(slotId);
                if (slot5 == null || !slot5.canTakeStack(player)) {
                    return ItemStack.EMPTY;
                }

                for(ItemStack itemstack7 = this.transferStackInSlot(player, slotId); !itemstack7.isEmpty() && ItemStack.areItemsEqual(slot5.getStack(), itemstack7); itemstack7 = this.transferStackInSlot(player, slotId)) { //FIXME: vanilla code might go in an infinite loop there on shift click, need to be reworked
                    itemstack = itemstack7.copy();
                }
            } else { //ClickType.PICKUP
                if (slotId < 0) {
                    return ItemStack.EMPTY;
                }
                itemstack = slotClickPickup(slotId, dragType, player);
                /*
                Slot slot6 = this.inventorySlots.get(slotId);
                if (slot6 != null) {
                    ItemStack itemstack8 = slot6.getStack();
                    ItemStack itemstack11 = player.inventory.getItemStack();
                    if (!itemstack8.isEmpty()) {
                        itemstack = itemstack8.copy();
                    }

                    if (itemstack8.isEmpty()) {
                        if (!itemstack11.isEmpty() && slot6.isItemValid(itemstack11)) {
                            int j2 = dragType == 0 ? itemstack11.getCount() : 1;
                            if (j2 > slot6.getItemStackLimit(itemstack11)) {
                                j2 = slot6.getItemStackLimit(itemstack11);
                            }

                            slot6.putStack(itemstack11.split(j2));
                        }
                    } else if (slot6.canTakeStack(player)) {
                        if (itemstack11.isEmpty()) {
                            if (itemstack8.isEmpty()) {
                                slot6.putStack(ItemStack.EMPTY);
                                player.inventory.setItemStack(ItemStack.EMPTY);
                            } else {
                                int k2 = dragType == 0 ? itemstack8.getCount() : (itemstack8.getCount() + 1) / 2;
                                player.inventory.setItemStack(slot6.decrStackSize(k2));
                                if (itemstack8.isEmpty()) {
                                    slot6.putStack(ItemStack.EMPTY);
                                }

                                slot6.onTake(player, player.inventory.getItemStack());
                            }
                        } else if (slot6.isItemValid(itemstack11)) {
                            if (areItemsAndTagsEqual(itemstack8, itemstack11)) {
                                int l2 = dragType == 0 ? itemstack11.getCount() : 1;
                                if (l2 > slot6.getItemStackLimit(itemstack11) - itemstack8.getCount()) {
                                    l2 = slot6.getItemStackLimit(itemstack11) - itemstack8.getCount();
                                }

                                if (l2 > itemstack11.getMaxStackSize() - itemstack8.getCount()) {
                                    l2 = itemstack11.getMaxStackSize() - itemstack8.getCount();
                                }

                                itemstack11.shrink(l2);
                                itemstack8.grow(l2);
                            } else if (itemstack11.getCount() <= slot6.getItemStackLimit(itemstack11)) {
                                slot6.putStack(itemstack11);
                                player.inventory.setItemStack(itemstack8);
                            }
                        } else if (itemstack11.getMaxStackSize() > 1 && areItemsAndTagsEqual(itemstack8, itemstack11) && !itemstack8.isEmpty()) {
                            int i3 = itemstack8.getCount();
                            if (i3 + itemstack11.getCount() <= itemstack11.getMaxStackSize()) {
                                itemstack11.grow(i3);
                                itemstack8 = slot6.decrStackSize(i3);
                                if (itemstack8.isEmpty()) {
                                    slot6.putStack(ItemStack.EMPTY);
                                }

                                slot6.onTake(player, player.inventory.getItemStack());
                            }
                        }
                    }

                    slot6.onSlotChanged();
                }
            */}
        } else if (clickTypeIn == ClickType.SWAP && dragType >= 0 && dragType < 9) {
            Slot slot4 = this.inventorySlots.get(slotId);
            ItemStack itemstack6 = player.inventory.getStackInSlot(dragType);
            ItemStack itemstack10 = slot4.getStack();
            if (!itemstack6.isEmpty() || !itemstack10.isEmpty()) {
                if (itemstack6.isEmpty()) {
                    if (slot4.canTakeStack(player)) {
                        player.inventory.setInventorySlotContents(dragType, itemstack10);
                        ((BaseSlot)slot4).onSwapCraft(itemstack10.getCount()); //cast is used there to access protected method Slot#onSwapCraft as public (exposed by BaseSlot#onSwapCraft)
                        slot4.putStack(ItemStack.EMPTY);
                        slot4.onTake(player, itemstack10);
                    }
                } else if (itemstack10.isEmpty()) {
                    if (slot4.isItemValid(itemstack6)) {
                        int l1 = slot4.getItemStackLimit(itemstack6);
                        if (itemstack6.getCount() > l1) {
                            slot4.putStack(itemstack6.split(l1));
                        } else {
                            slot4.putStack(itemstack6);
                            player.inventory.setInventorySlotContents(dragType, ItemStack.EMPTY);
                        }
                    }
                } else if (slot4.canTakeStack(player) && slot4.isItemValid(itemstack6)) {
                    int i2 = slot4.getItemStackLimit(itemstack6);
                    if (itemstack6.getCount() > i2) {
                        slot4.putStack(itemstack6.split(i2));
                        slot4.onTake(player, itemstack10);
                        if (!player.inventory.addItemStackToInventory(itemstack10)) {
                            player.dropItem(itemstack10, true);
                        }
                    } else {
                        slot4.putStack(itemstack6);
                        player.inventory.setInventorySlotContents(dragType, itemstack10);
                        slot4.onTake(player, itemstack10);
                    }
                }
            }
        } else if (clickTypeIn == ClickType.CLONE && player.abilities.isCreativeMode && player.inventory.getItemStack().isEmpty() && slotId >= 0) {
            Slot slot3 = this.inventorySlots.get(slotId);
            if (slot3 != null && slot3.getHasStack()) {
                ItemStack itemstack5 = slot3.getStack().copy();
                itemstack5.setCount(itemstack5.getMaxStackSize());
                player.inventory.setItemStack(itemstack5);
            }
        } else if (clickTypeIn == ClickType.THROW && player.inventory.getItemStack().isEmpty() && slotId >= 0) {
            Slot slot2 = this.inventorySlots.get(slotId);
            if (slot2 != null && slot2.getHasStack() && slot2.canTakeStack(player) && BaseSlot.getOuputRights(slot2)) {
                ItemStack itemstack4 = slot2.decrStackSize(dragType == 0 ? 1 : slot2.getStack().getCount());
                slot2.onTake(player, itemstack4);
                player.dropItem(itemstack4, true);
            }
        } else if (clickTypeIn == ClickType.PICKUP_ALL && slotId >= 0) { //almost sure this is the double click detection
            Slot slot = this.inventorySlots.get(slotId);
            ItemStack itemstack1 = player.inventory.getItemStack();
            if (!itemstack1.isEmpty() && (slot == null || !slot.getHasStack() || !slot.canTakeStack(player))) {
                int i = dragType == 0 ? 0 : this.inventorySlots.size() - 1;
                int j = dragType == 0 ? 1 : -1;

                for(int k = 0; k < 2; ++k) {
                    for(int l = i; l >= 0 && l < this.inventorySlots.size() && itemstack1.getCount() < itemstack1.getMaxStackSize(); l += j) {
                        Slot slot1 = this.inventorySlots.get(l);
                        if (!BaseSlot.getOuputRights(slot1)) continue;
                        if (slot1.getHasStack() && canAddItemToSlot(slot1, itemstack1, true) && slot1.canTakeStack(player) && this.canMergeSlot(itemstack1, slot1)) {
                            ItemStack itemstack2 = slot1.getStack();
                            if (k != 0 || itemstack2.getCount() != itemstack2.getMaxStackSize()) {
                                int i1 = Math.min(itemstack1.getMaxStackSize() - itemstack1.getCount(), itemstack2.getCount());
                                ItemStack itemstack3 = slot1.decrStackSize(i1);
                                itemstack1.grow(i1);
                                if (itemstack3.isEmpty()) {
                                    slot1.putStack(ItemStack.EMPTY);
                                }

                                slot1.onTake(player, itemstack3);
                            }
                        }
                    }
                }
            }

            this.detectAndSendChanges();
        }

        return itemstack;
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);
        boolean state = false;

        if (slot != null && slot.getHasStack() && BaseSlot.getOuputRights(slot)) {
            copy = slot.getStack().copy();
            if (index < this.inventory.getSizeInventory()) { //from the container (to any slot, hotbar tried first, then inventory)
                state = mergeItemStack(copy, inventorySlots.size() - 9, inventorySlots.size(), false);
                if (!state)
                    state = mergeItemStack(copy, this.inventory.getSizeInventory(), inventorySlots.size() - 9, false);
            } else if (index >= inventorySlots.size() - 9) { //hotbar (to container first, or to inventory)
                state = mergeItemStack(copy, 0, inventorySlots.size() - 9, false); //one call to mergeItemStack is enough, thanks to the slots order
            } else { //player inventory (to container first, or to hotbar)
                state = mergeItemStack(copy, 0, this.inventory.getSizeInventory(), false);
                if (!state)
                    state = mergeItemStack(copy, inventorySlots.size() - 9, inventorySlots.size(), false);
            }
            /*
            if (slot.inventory instanceof BaseInventory)
                slot.inventory.setInventorySlotContents(index, copy);
            else*/
                slot.putStack(copy);
        }
        return /*state ? copy :*/ ItemStack.EMPTY;
    }

    @Override //overide of merge stack to respect IO rules and send proper updates to slots, original code is from vanilla Container#mergeItemStack
    protected boolean mergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        boolean flag = false;
        int i = reverseDirection ? endIndex - 1 : startIndex;

        if (stack.isStackable()) {
            while(!stack.isEmpty()) {
                if (reverseDirection) {
                    if (i < startIndex) {
                        break;
                    }
                } else if (i >= endIndex) {
                    break;
                }

                Slot slot = this.inventorySlots.get(i);
                ItemStack itemstack = slot.getStack();
                if (!itemstack.isEmpty() && BaseSlot.getInputRights(slot) && areItemsAndTagsEqual(stack, itemstack)) { //valid slot for merge (same item, and accept items)
                    int j = itemstack.getCount() + stack.getCount();
                    int maxSize = Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize());
                    if (j <= maxSize) {
                        stack.setCount(0);
                        itemstack.setCount(j);
                        slot.onSlotChanged();
                        flag = true;
                    } else if (itemstack.getCount() < maxSize) {
                        stack.shrink(maxSize - itemstack.getCount());
                        itemstack.setCount(maxSize);
                        slot.onSlotChanged();
                        flag = true;
                    }
                }

                if (reverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        if (!stack.isEmpty()) { //still items to process
            i = reverseDirection ? endIndex - 1 : startIndex;
            while (reverseDirection ? (i >= startIndex) : (i < endIndex)) {

                Slot slot1 = this.inventorySlots.get(i);
                ItemStack itemstack1 = slot1.getStack();
                if (itemstack1.isEmpty() && BaseSlot.getInputRights(slot1) && slot1.isItemValid(stack)) { //valid slot if empty, can input and accept item
                    if (stack.getCount() > slot1.getSlotStackLimit()) {
                        slot1.putStack(stack.split(slot1.getSlotStackLimit()));
                    } else {
                        slot1.putStack(stack.split(stack.getCount()));
                    }

                    slot1.onSlotChanged();
                    flag = true;
                    break;
                }

                if (reverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        return flag;
    }

    /*
    @Override
    public Slot addSlot(Slot slotIn) {
        slotIn.slotNumber = this.inventorySlots.size();
        this.inventorySlots.add(slotIn);
        this.inventoryItemStacks.add(ItemStack.EMPTY);
        return slotIn;
    }*/
/*
    @Override
    public void detectAndSendChanges() {
//        ServerLifecycleHooks.getCurrentServer()
//        DimensionalBagsMod.LOGGER.info("container tick");
        for(int i = 0; i < this.inventorySlots.size(); ++i) {
            ItemStack itemstack = this.inventorySlots.get(i).getStack();
            ItemStack itemstack1 = this.inventoryItemStacks.get(i);
            if (!ItemStack.areItemStacksEqual(itemstack1, itemstack)) {
                boolean clientStackChanged = !itemstack1.equals(itemstack, true);
                itemstack1 = itemstack.copy();
                this.inventoryItemStacks.set(i, itemstack1);

                if (clientStackChanged)
                    for(IContainerListener icontainerlistener : this.listeners) {
                        icontainerlistener.sendSlotContents(this, i, itemstack1);
                    }
            }
        }

        for(int j = 0; j < this.trackedIntReferences.size(); ++j) {
            IntReferenceHolder intreferenceholder = this.trackedIntReferences.get(j);
            if (intreferenceholder.isDirty()) {
                for(IContainerListener icontainerlistener1 : this.listeners) {
                    icontainerlistener1.sendWindowProperty(this, j, intreferenceholder.get());
                }
            }
        }
//        super.detectAndSendChanges();
    }
    */

    /*
    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        if (this.getSlot(index) instanceof
    }
    */

    /*
    @Override
    public boolean canMergeSlot(ItemStack stack, Slot slot) {
        return BaseSlot.getOuputRights(slot) && super.canMergeSlot(stack, slot);
    }
    */
}
