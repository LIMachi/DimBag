package com.limachi.dimensional_bags.common.container;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.limachi.dimensional_bags.ConfigManager.Config;
import com.limachi.dimensional_bags.common.EventManager;
import com.limachi.dimensional_bags.common.container.slot.FluidSlot;
import com.limachi.dimensional_bags.common.inventory.IPacketSerializable;
import com.limachi.dimensional_bags.common.network.PacketHandler;
import com.limachi.dimensional_bags.common.network.SyncCompoundNBT;
import com.limachi.dimensional_bags.common.network.packets.SetSlotPacket;
import com.limachi.dimensional_bags.utils.ReflectionUtils;
import io.netty.buffer.Unpooled;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.*;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nullable;
import java.util.*;

import static com.limachi.dimensional_bags.common.managers.modes.Tank.stackInteraction;
import static com.limachi.dimensional_bags.common.references.GUIs.ScreenParts.*;

/**
 * simple container that accepts any ISimpleItemHandler
 * overrides most of the vanilla container to allow finer control of behavior
 */

public abstract class BaseContainer extends Container implements IPacketSerializable, INamedContainerProvider {

    protected final NonNullList<ItemStack> inventoryItemStacks = NonNullList.create();
    protected final List<IntReferenceHolder> trackedIntReferences = Lists.newArrayList();

    private final UUIDIntArray sci = new UUIDIntArray();
    public SyncCompoundNBT sc;
    {
        sci.set(UUID.randomUUID());
        trackIntArray(sci);
        EventManager.delayedTask(1, ()->sc = SyncCompoundNBT.create(sci.get(), true, true)); //mad scientist is back baby
    }

    @Nullable
    @Override
    public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity player) {
        ReflectionUtils.setField(this, "windowId", "field_75152_c", windowId);
        return this;
    }

    public static class UUIDIntArray extends IntArray {
        public UUIDIntArray() { super(8); }

        public void set(UUID id) {
            for (int i = 0; i < 8; ++i)
                super.set(i, i < 4 ? (short)(id.getLeastSignificantBits() >> (16 * i)) : (short)(id.getMostSignificantBits() >> (16 * (i - 4))));
        }

        public UUID get() {
            long least = 0;
            long most = 0;
            for (int i = 0; i < 8; ++i)
                if (i < 4)
                    least |= (long)get(i) << (16 * i);
                else
                    most |= (long)get(i) << (16 * (i - 4));
            return new UUID(most, least);
        }
    }

    @Override
    protected void trackIntArray(IIntArray arrayIn) {
        for(int i = 0; i < arrayIn.size(); ++i)
            this.trackInt(IntReferenceHolder.create(arrayIn, i));
    }
/*
    public static final String NAME = "simple_container";

    static {
        Registries.registerContainer(NAME, (windowId, playerInv, extraData)->{
            String invClassName = extraData.readString();
            ISimpleItemHandlerSerializable inv;
            try {
                inv = (ISimpleItemHandlerSerializable)Class.forName(invClassName).newInstance();
                inv.readFromBuff(extraData);
            } catch (Exception e) {
                DimBag.LOGGER.error("Failed to load inventory: " + invClassName + " for container, reason: " + e);
                inv = new EmptySimpleItemHandlerSerializable();
            }
            String tanksClassName = extraData.readString();
            ISimpleFluidHandlerSerializable tanks;
            try {
                tanks = (ISimpleFluidHandlerSerializable)Class.forName(tanksClassName).newInstance();
                tanks.readFromBuff(extraData);
            } catch (Exception e) {
                DimBag.LOGGER.error("Failed to load tanks: " + tanksClassName + " for container, reason: " + e);
                tanks = new EmptySimpleFluidHandlerSerializable();
            }
            return new SimpleContainer(windowId, playerInv, inv, tanks, extraData.readVarIntArray(), extraData.readInt(), extraData.readInt(), p->true, true);
        });
    }
*/
    public void open(PlayerEntity player) { if (player instanceof ServerPlayerEntity) NetworkHooks.openGui((ServerPlayerEntity)player, this, this::writeToBuff); }
    public void close() { playerInv.player.closeScreen(); }

    protected final PlayerInventory playerInv;
//    protected final ISimpleItemHandlerSerializable inv;
//    protected final ISimpleFluidHandlerSerializable tanks;
//    protected final int[] slots;
//    protected final boolean scrollBar;
//    protected final int columnLimit;
//    protected final int rowLimit;
//    protected final int columns;
//    protected final int rows;
//    protected int scroll = 0;
//    protected final boolean isClient;

    /*
    static public void open(ServerPlayerEntity playerEntity, ITextComponent name, @Nullable ISimpleItemHandlerSerializable inv, @Nullable ISimpleFluidHandlerSerializable tanks, @Nullable ArrayList<Integer> slots, int columnLimit, int rowLimit, @Nonnull Predicate<PlayerEntity> canInteractWithPred) {
        int[] slotArray = new int[slots != null ? slots.size() : 0];
        if (slots != null)
            for (int i = 0; i < slots.size(); ++i)
                slotArray[i] = slots.get(i);
        if (inv == null)
            inv = new EmptySimpleItemHandlerSerializable();
        if (tanks == null)
            tanks = new EmptySimpleFluidHandlerSerializable();
        final ISimpleItemHandlerSerializable finv = inv;
        final ISimpleFluidHandlerSerializable ftanks = tanks;
        NetworkHooks.openGui(playerEntity, new INamedContainerProvider() {
            @Override
            public ITextComponent getDisplayName() { return name; }

            @Nullable
            @Override
            public Container createMenu(int windowId, PlayerInventory playerInv, PlayerEntity player) {
                return new SimpleContainer(windowId, playerInv, finv, ftanks, slotArray, columnLimit, rowLimit, canInteractWithPred, false);
            }
        }, buffer->{
            buffer.writeString(finv.getClass().getName());
            finv.writeToBuff(buffer);
            buffer.writeString(ftanks.getClass().getName());
            ftanks.writeToBuff(buffer);
            buffer.writeVarIntArray(slotArray);
            buffer.writeInt(columnLimit);
            buffer.writeInt(rowLimit);
        });
    }*/

    /*
    static public void open(ServerPlayerEntity playerEntity, ITextComponent name, @Nullable ISimpleItemHandlerSerializable inv, @Nullable ISimpleFluidHandlerSerializable tanks) {
        open(playerEntity, name, inv, tanks, null, 9, 6, p->true);
    }
    */

    protected BaseContainer(ContainerType<?> containerType, int windowId, PlayerInventory playerInv/*, ISimpleItemHandlerSerializable inv, ISimpleFluidHandlerSerializable tanks, int[] slots, int columnLimit, int rowLimit, boolean isClient*/) {
        super(containerType, windowId);
//        this.isClient = isClient;
        this.playerInv = playerInv;
//        this.inv = inv;
//        this.tanks = tanks;
//        if (slots.length > 0)
//            this.slots = slots;
//        else {
//            this.slots = new int[inv.getSlots() + tanks.getTanks()];
//            for (int i = 0; i < this.slots.length; ++i)
//                this.slots[i] = i;
//        }
//        this.columnLimit = columnLimit;
//        this.rowLimit = rowLimit;
//        this.rows = (int)Math.ceil((double)this.slots.length / (double)columnLimit);
//        this.columns = (int)Math.ceil((double)this.slots.length / (double)this.rows);
//        this.scrollBar = rows > rowLimit;
    }

    protected BaseContainer(ContainerType<?> containerType, int windowId, PlayerInventory playerInv, PacketBuffer buffer) {
        this(containerType, windowId, playerInv);
        readFromBuff(buffer);
    }

//    public int getRows() { return rows; }
//    public int getColumns() { return columns; }
//    public int getSlotCount() { return slots.length; }

    protected void addPlayerSlots(int px, int py) {
        int dx = px + PLAYER_INVENTORY_FIRST_SLOT_X + 1;
        int dy = py + PLAYER_BELT_FIRST_SLOT_Y + 1;
        for (int x = 0; x < PLAYER_INVENTORY_COLUMNS; ++x)
            addSlot(new Slot(playerInv, x, dx + x * SLOT_SIZE_X, dy));
        dy = py + PLAYER_INVENTORY_FIRST_SLOT_Y + 1;
        for (int y = 0; y < PLAYER_INVENTORY_ROWS; ++y)
            for (int x = 0; x < PLAYER_INVENTORY_COLUMNS; ++x)
                addSlot(new Slot(playerInv, x + (y + 1) * PLAYER_INVENTORY_COLUMNS, dx + x * SLOT_SIZE_X, dy + y * SLOT_SIZE_Y));
    }

    protected static final Inventory EMPTY_INVENTORY = new Inventory(0);

//    protected Slot createSlot(int index, int x, int y) {
//        if (index < inv.getSlots())
//            return inv.createSlot(index, x, y);
//        if (index < inv.getSlots() + tanks.getTanks())
//            return tanks.createSlot(index - inv.getSlots(), x, y);
//        return new DisabledSlot(EMPTY_INVENTORY, 0, x, y);
//    }
//
//    protected void addSlots() {
//        int sx = GUIs.BagScreen.calculateShiftLeft(columns);
//        int sy = GUIs.BagScreen.calculateYSize(rows);
//        addPlayerSlots(sx > 0 ? sx * SLOT_SIZE_X : 0, sy - PLAYER_INVENTORY_Y);
//        sx = PART_SIZE_X + 1 + (sx < 0 ? -sx * SLOT_SIZE_X : 0);
//        sy = PART_SIZE_Y * 2 + 1;
//        int si = 0;
//        for (int y = 0; y < rows; ++y)
//            for (int x = 0; x < columns; ++x)
//                if (si < slots.length)
//                    this.addSlot(createSlot(slots[si++], sx + SLOT_SIZE_X * x, sy + SLOT_SIZE_Y * y));
//    }

    @Override
    protected Slot addSlot(Slot slotIn) {
        slotIn.slotNumber = this.inventorySlots.size();
        this.inventorySlots.add(slotIn);
        this.inventoryItemStacks.add(ItemStack.EMPTY);
        return slotIn;
    }

    protected void clearSlots() {
        this.inventorySlots.clear();
        this.inventoryItemStacks.clear();
    }

    @Override
    protected IntReferenceHolder trackInt(IntReferenceHolder intIn) {
        this.trackedIntReferences.add(intIn);
        return intIn;
    }

    @Override
    public void updateProgressBar(int id, int data) { this.trackedIntReferences.get(id).set(data); }

    @Override
    public void detectAndSendChanges() {
        for(int i = 0; i < this.inventorySlots.size(); ++i) {
            ItemStack itemstack = this.inventorySlots.get(i).getStack();
            ItemStack itemstack1 = this.inventoryItemStacks.get(i);
            if (!ItemStack.areItemStacksEqual(itemstack1, itemstack)) {
                boolean clientStackChanged = !itemstack1.equals(itemstack, true);
                ItemStack itemstack2 = itemstack.copy();
                this.inventoryItemStacks.set(i, itemstack2);

                if (clientStackChanged)
                    for(IContainerListener icontainerlistener : this.listeners) {
                        if (icontainerlistener instanceof ServerPlayerEntity && itemstack2.getCount() > 64) //added to sync slot size overrides
                            PacketHandler.toClient((ServerPlayerEntity) icontainerlistener, new SetSlotPacket(windowId, i, itemstack2));
                        else
                            icontainerlistener.sendSlotContents(this, i, itemstack2);
                    }
            }
        }

        for(int j = 0; j < this.trackedIntReferences.size(); ++j) { //could be upgraded to sync other things than just int truncated to shorts IMO
            IntReferenceHolder intreferenceholder = this.trackedIntReferences.get(j);
            if (intreferenceholder.isDirty()) {
                for(IContainerListener icontainerlistener1 : this.listeners) {
                    icontainerlistener1.sendWindowProperty(this, j, intreferenceholder.get());
                }
            }
        }

//        CompoundNBT diff = NBTUtils.extractDiff(widgetData, prevWidgetData);
//        if (!diff.isEmpty()) {
//            for (IContainerListener icontainerlistener : this.listeners)
//                if (icontainerlistener instanceof ServerPlayerEntity) {
//
//                }
//        }
    }

    /**
     * test that a shift click between those 2 slots is valid (by default, enforce that the two slots are pointing to different inventories
     * or different sub inventories for player inventories (belt, inv, armor, off hand)
     * @param original
     * @param target
     * @return
     */
    public boolean isTransferValid(int original, int target, boolean subPlayerInventories) {
        if (original == target) return false;
        Slot slotO = this.inventorySlots.get(original);
        Slot slotT = this.inventorySlots.get(target);
        if (slotO instanceof SlotItemHandler && slotT instanceof SlotItemHandler && ((SlotItemHandler)slotO).getItemHandler() == ((SlotItemHandler)slotT).getItemHandler())
            return false;
        if (subPlayerInventories && slotO.inventory instanceof PlayerInventory && slotO.inventory == slotT.inventory) {
            int o = slotO.getSlotIndex();
            int t = slotT.getSlotIndex();
            if ((o > 8 ? o > 36 ? o > 40 ? 3 : 2 : 1 : 0) != (t > 8 ? t > 36 ? t > 40 ? 3 : 2 : 1 : 0))
                return true;
        }
        if (slotO.inventory.getSizeInventory() != 0 && slotT.inventory.getSizeInventory() != 0 && slotO.inventory == slotT.inventory)
            return false;
        return true;
    }

    public static boolean areStackable(ItemStack s1, ItemStack s2) {
        return ItemStack.areItemsEqual(s1, s2) && ItemStack.areItemStackTagsEqual(s1, s2);
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int position) //should be rewritten to respect the insert/extract rights of the IItemHandlerModifiable
    {
        Slot slot = this.inventorySlots.get(position);
        boolean reverse = (slot.inventory instanceof PlayerInventory);
        for (int pass = 0; pass < 2; ++pass)
            for (int targetIndex = reverse ? this.inventorySlots.size() - 1 : 0; reverse ? targetIndex >= 0 : targetIndex < this.inventorySlots.size(); targetIndex += reverse ? -1 : 1) {
                Slot target = this.inventorySlots.get(targetIndex);
                if (slot.getStack().isEmpty()) return ItemStack.EMPTY; //nothing else to transfer
                if ((pass == 0) == (target.getStack().isEmpty())) continue; //the first pass will try to merge first, that is, will skip the empty slots
                ItemStack slotStack = slot.getStack();
                if (!isTransferValid(position, target.slotNumber, false) || !target.isItemValid(slotStack)) continue; //test if we have the rights to transfer to the target slot, this does not check the validity of the merge
                ItemStack targetStack = target.getStack();
                if (!targetStack.isEmpty() && !areStackable(targetStack, slotStack)) continue; //invalid merge, the target slot isn't empty and does not match the original stack
                int maxInput = Math.min(slotStack.getCount(), getRealSlotInputLimit(target, slotStack));
                if (maxInput <= 0) continue; //no more place to input
                ItemStack newSlotStack = slotStack.copy();
                newSlotStack.shrink(maxInput);
                slot.putStack(newSlotStack);
                ItemStack newTargetStack;
                if (targetStack.isEmpty()) {
                    newTargetStack = slotStack.copy();
                    newTargetStack.setCount(maxInput);
                } else {
                    newTargetStack = targetStack.copy();
                    newTargetStack.grow(maxInput);
                }
                target.putStack(newTargetStack);
            }
        return ItemStack.EMPTY;
    }

    private int dragEvent = 0;
    private int dragMode = -1;
    private final Set<Slot> dragSlots = Sets.newHashSet();

    @Override
    protected void resetDrag() {
        this.dragEvent = 0;
        this.dragSlots.clear();
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
        try {
            return this.slotClickInternal(slotId, dragType, clickTypeIn, player);
        } catch (Exception exception) {
            CrashReport crashreport = CrashReport.makeCrashReport(exception, "Container click");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Click info");
            crashreportcategory.addDetail("Menu Type", () -> this.getType() != null ? Registry.MENU.getKey(this.getType()).toString() : "<no type>");
            crashreportcategory.addDetail("Menu Class", () -> this.getClass().getCanonicalName());
            crashreportcategory.addDetail("Slot Count", this.inventorySlots.size());
            crashreportcategory.addDetail("Slot", slotId);
            crashreportcategory.addDetail("Button", dragType);
            crashreportcategory.addDetail("Type", clickTypeIn);
            throw new ReportedException(crashreport);
        }
    }

    protected ItemStack slotClickInternal(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
        PlayerInventory playerinventory = player.inventory;
        if (slotId >= 0 && inventorySlots.get(slotId) instanceof FluidSlot) //special case: FluidSlot change how we interact with them
            return slotClickFluid(playerinventory, slotId, clickTypeIn, player);
        else if (clickTypeIn == ClickType.QUICK_CRAFT)
            return slotClickQuickCraft(playerinventory, slotId, dragType, player);
        else if (this.dragEvent != 0)
            this.resetDrag();
        else if ((clickTypeIn == ClickType.PICKUP || clickTypeIn == ClickType.QUICK_MOVE) && (dragType == 0 || dragType == 1))
            return slotClickPickUp(playerinventory, slotId, dragType, clickTypeIn, player);
        else if (clickTypeIn == ClickType.SWAP)
            return slotClickSwap(playerinventory, slotId, dragType, player);
        else if (clickTypeIn == ClickType.CLONE && player.abilities.isCreativeMode && playerinventory.getItemStack().isEmpty() && slotId >= 0)
            return slotClickClone(playerinventory, slotId);
        else if (clickTypeIn == ClickType.THROW && playerinventory.getItemStack().isEmpty() && slotId >= 0)
            return slotClickThrow(slotId, dragType, player);
        else if (clickTypeIn == ClickType.PICKUP_ALL && slotId >= 0)
            return slotClickPickupAll(playerinventory, slotId, dragType, player);
        return ItemStack.EMPTY;
    }

    /**
     * used to access protected methods and fields of slots
     */
    protected abstract static class ContainerSlotOverrides extends Slot {

        public ContainerSlotOverrides(IInventory inventoryIn, int index, int xPosition, int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
        }

        @Override
        public void onSwapCraft(int numItemsCrafted) { super.onSwapCraft(numItemsCrafted); }
    }

    @Config(cmt = "does clicking a fluid slot with a tank item (bucket, glass bottle, etc...) should empty or fill it in addition to selecting the slot (set to false if you want players to be forced to use strict fluid mechanics)")
    public static final boolean CAN_FILL_AND_EMPTY_ITEM_TANKS = true;

    protected ItemStack slotClickFluid(PlayerInventory playerinventory, int slotId, ClickType clickTypeIn, PlayerEntity player) {
        if (clickTypeIn != ClickType.PICKUP) return ItemStack.EMPTY; //only accept simple clicks
        FluidSlot slot = (FluidSlot)inventorySlots.get(slotId);
        slot.selectTank();
        if (CAN_FILL_AND_EMPTY_ITEM_TANKS)
            playerinventory.setItemStack(stackInteraction(playerinventory.getItemStack(), slot.getHandler(), playerinventory));
        return ItemStack.EMPTY;
    }

    protected ItemStack slotClickQuickCraft(PlayerInventory playerinventory, int slotId, int dragType, PlayerEntity player) {
        int i1 = this.dragEvent;
        this.dragEvent = getDragEvent(dragType);
        if ((i1 != 1 || this.dragEvent != 2) && i1 != this.dragEvent) {
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
                ItemStack itemstack10 = playerinventory.getItemStack().copy();
                int k1 = playerinventory.getItemStack().getCount();

                for (Slot slot8 : this.dragSlots) {
                    ItemStack itemstack13 = playerinventory.getItemStack();
                    if (slot8 != null && canAddItemToSlot(slot8, itemstack13, true) && slot8.isItemValid(itemstack13) && (this.dragMode == 2 || itemstack13.getCount() >= this.dragSlots.size()) && this.canDragIntoSlot(slot8)) {
                        ItemStack itemstack14 = itemstack10.copy();
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

                itemstack10.setCount(k1);
                playerinventory.setItemStack(itemstack10);
            }

            this.resetDrag();
        }
        return ItemStack.EMPTY;
    }

    /**
     * simulate a slot insertion and return how many items from stack that can be inserted
     */
    protected int getRealSlotInputLimit(Slot slot, ItemStack stack) {
        ItemStack s = slot.getStack();
        if (slot instanceof SlotItemHandler) {
            SlotItemHandler ih = (SlotItemHandler) slot;
            ItemStack rem = ih.getItemHandler().insertItem(slot.getSlotIndex(), stack, true);
            return stack.getCount() - rem.getCount();
        } else {
            int t = stack.getCount();
            if (t > slot.getItemStackLimit(stack) - s.getCount()) {
                t = slot.getItemStackLimit(stack) - s.getCount();
            }

            if (t > stack.getMaxStackSize() - s.getCount()) {
                t = stack.getMaxStackSize() - s.getCount();
            }
            return t;
        }
    }

    /**
     * should be single click
     * @param playerinventory
     * @param slotId
     * @param dragType
     * @param clickTypeIn
     * @param player
     * @return
     */
    protected ItemStack slotClickPickUp(PlayerInventory playerinventory, int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
        ItemStack itemstack = ItemStack.EMPTY;
        if (slotId == -999) { //click outside the screen
            if (!playerinventory.getItemStack().isEmpty()) {
                if (dragType == 0) { //probably left click, drop entire stask
                    player.dropItem(playerinventory.getItemStack(), true);
                    playerinventory.setItemStack(ItemStack.EMPTY);
                }

                if (dragType == 1) { //probably right click, drop only one item
                    player.dropItem(playerinventory.getItemStack().split(1), true);
                }
            }
        } else if (clickTypeIn == ClickType.QUICK_MOVE) { //Shift-Click, be careful there
            if (slotId < 0) return ItemStack.EMPTY;

            Slot slot5 = this.inventorySlots.get(slotId);
            if (slot5 == null || !slot5.canTakeStack(player)) return ItemStack.EMPTY;

            for(ItemStack itemstack8 = this.transferStackInSlot(player, slotId); !itemstack8.isEmpty() && areStackable(slot5.getStack(), itemstack8); itemstack8 = this.transferStackInSlot(player, slotId)) {
                itemstack = itemstack8.copy();
            }
        } else {
            if (slotId < 0) {
                return ItemStack.EMPTY;
            }

            Slot slot6 = this.inventorySlots.get(slotId);
            if (slot6 != null) {
                ItemStack itemstack9 = slot6.getStack();
                ItemStack itemstack11 = playerinventory.getItemStack();
                if (!itemstack9.isEmpty()) {
                    itemstack = itemstack9.copy();
                }

                if (itemstack9.isEmpty()) {
                    if (!itemstack11.isEmpty() && slot6.isItemValid(itemstack11)) {
                        int j2 = dragType == 0 ? itemstack11.getCount() : 1;
                        if (j2 > slot6.getItemStackLimit(itemstack11)) {
                            j2 = slot6.getItemStackLimit(itemstack11);
                        }

                        slot6.putStack(itemstack11.split(j2));
                    }
                } else if (slot6.canTakeStack(player)) {
                    if (itemstack11.isEmpty()) {
                        if (itemstack9.isEmpty()) {
                            slot6.putStack(ItemStack.EMPTY);
                            playerinventory.setItemStack(ItemStack.EMPTY);
                        } else {
                            int k2 = dragType == 0 ? itemstack9.getCount() : (itemstack9.getCount() + 1) / 2;
                            if (k2 > itemstack9.getMaxStackSize()) k2 = itemstack9.getMaxStackSize(); //quick fix to prevent grab of more than a stack from a slot
                            playerinventory.setItemStack(slot6.decrStackSize(k2));
                            if (itemstack9.isEmpty()) {
                                slot6.putStack(ItemStack.EMPTY);
                            }

                            slot6.onTake(player, playerinventory.getItemStack());
                        }
                    } else if (slot6.isItemValid(itemstack11)) {
                        if (areItemsAndTagsEqual(itemstack9, itemstack11)) {

                            int l2 = getRealSlotInputLimit(slot6, itemstack11);

                            itemstack11.shrink(l2);
                            itemstack9.grow(l2);
                        } else if (itemstack11.getCount() <= slot6.getItemStackLimit(itemstack11)) {
                            slot6.putStack(itemstack11);
                            playerinventory.setItemStack(itemstack9);
                        }
                    } else if (itemstack11.getMaxStackSize() > 1 && areItemsAndTagsEqual(itemstack9, itemstack11) && !itemstack9.isEmpty()) {
                        int i3 = itemstack9.getCount();
                        if (i3 + itemstack11.getCount() <= itemstack11.getMaxStackSize()) {
                            itemstack11.grow(i3);
                            itemstack9 = slot6.decrStackSize(i3);
                            if (itemstack9.isEmpty()) {
                                slot6.putStack(ItemStack.EMPTY);
                            }

                            slot6.onTake(player, playerinventory.getItemStack());
                        }
                    }
                }

                slot6.onSlotChanged();
            }
        }
        return itemstack;
    }

    /**
     * should be when you press the hotbar keys while hovering on a slot
     * @param playerinventory
     * @param slotId
     * @param dragType
     * @param player
     * @return
     */
    protected ItemStack slotClickSwap(PlayerInventory playerinventory, int slotId, int dragType, PlayerEntity player) {
        Slot slot = this.inventorySlots.get(slotId);
        ItemStack itemstack1 = playerinventory.getStackInSlot(dragType);
        ItemStack itemstack2 = slot.getStack();
        if (!itemstack1.isEmpty() || !itemstack2.isEmpty()) {
            if (itemstack1.isEmpty()) {
                if (slot.canTakeStack(player)) {
                    playerinventory.setInventorySlotContents(dragType, itemstack2);
                    ((ContainerSlotOverrides)slot).onSwapCraft(itemstack2.getCount());
                    slot.putStack(ItemStack.EMPTY);
                    slot.onTake(player, itemstack2);
                }
            } else if (itemstack2.isEmpty()) {
                if (slot.isItemValid(itemstack1)) {
                    int i = slot.getItemStackLimit(itemstack1);
                    if (itemstack1.getCount() > i) {
                        slot.putStack(itemstack1.split(i));
                    } else {
                        slot.putStack(itemstack1);
                        playerinventory.setInventorySlotContents(dragType, ItemStack.EMPTY);
                    }
                }
            } else if (slot.canTakeStack(player) && slot.isItemValid(itemstack1)) {
                int l1 = slot.getItemStackLimit(itemstack1);
                if (itemstack1.getCount() > l1) {
                    slot.putStack(itemstack1.split(l1));
                    slot.onTake(player, itemstack2);
                    if (!playerinventory.addItemStackToInventory(itemstack2)) {
                        player.dropItem(itemstack2, true);
                    }
                } else {
                    slot.putStack(itemstack1);
                    playerinventory.setInventorySlotContents(dragType, itemstack2);
                    slot.onTake(player, itemstack2);
                }
            }
        }
        return ItemStack.EMPTY;
    }

    protected ItemStack slotClickClone(PlayerInventory playerInventory, int slotId) {
        Slot slot4 = this.inventorySlots.get(slotId);
        if (slot4 != null && slot4.getHasStack()) {
            ItemStack itemstack7 = slot4.getStack().copy();
            itemstack7.setCount(itemstack7.getMaxStackSize());
            playerInventory.setItemStack(itemstack7);
        }
        return ItemStack.EMPTY;
    }

    protected ItemStack slotClickThrow(int slotId, int dragType, PlayerEntity player) {
        Slot slot3 = this.inventorySlots.get(slotId);
        if (slot3 != null && slot3.getHasStack() && slot3.canTakeStack(player)) {
            ItemStack itemstack6 = slot3.decrStackSize(dragType == 0 ? 1 : slot3.getStack().getCount());
            slot3.onTake(player, itemstack6);
            player.dropItem(itemstack6, true);
        }
        return ItemStack.EMPTY;
    }

    protected ItemStack slotClickPickupAll(PlayerInventory playerInventory, int slotId, int dragType, PlayerEntity player) {
        Slot slot2 = this.inventorySlots.get(slotId);
        ItemStack itemstack5 = playerInventory.getItemStack();
        if (!itemstack5.isEmpty() && (slot2 == null || !slot2.getHasStack() || !slot2.canTakeStack(player))) {
            int j1 = dragType == 0 ? 0 : this.inventorySlots.size() - 1;
            int i2 = dragType == 0 ? 1 : -1;

            for(int j = 0; j < 2; ++j) {
                for(int k = j1; k >= 0 && k < this.inventorySlots.size() && itemstack5.getCount() < itemstack5.getMaxStackSize(); k += i2) {
                    Slot slot1 = this.inventorySlots.get(k);
                    if (slot1.getHasStack() && canAddItemToSlot(slot1, itemstack5, true) && slot1.canTakeStack(player) && this.canMergeSlot(itemstack5, slot1)) {
                        ItemStack itemstack3 = slot1.getStack();
                        if (j != 0 || itemstack3.getCount() != itemstack3.getMaxStackSize()) {
                            int l = Math.min(itemstack5.getMaxStackSize() - itemstack5.getCount(), itemstack3.getCount());
                            ItemStack itemstack4 = slot1.decrStackSize(l);
                            itemstack5.grow(l);
                            if (itemstack4.isEmpty()) {
                                slot1.putStack(ItemStack.EMPTY);
                            }

                            slot1.onTake(player, itemstack4);
                        }
                    }
                }
            }
        }
        this.detectAndSendChanges();
        return ItemStack.EMPTY;
    }
}
