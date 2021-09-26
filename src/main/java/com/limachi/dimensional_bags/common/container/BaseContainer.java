package com.limachi.dimensional_bags.common.container;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.limachi.dimensional_bags.ConfigManager.Config;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.client.render.screen.SimpleContainerScreen;
import com.limachi.dimensional_bags.common.container.slot.FluidSlot;
import com.limachi.dimensional_bags.common.container.widgets.BaseContainerWidget;
import com.limachi.dimensional_bags.common.inventory.IPacketSerializable;
import com.limachi.dimensional_bags.common.network.PacketHandler;
import com.limachi.dimensional_bags.common.network.packets.SetSlotPacket;
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
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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

public abstract class BaseContainer<I extends BaseContainer<I>> extends Container implements IPacketSerializable {
    public static final class NullPlayerInventory extends PlayerInventory {
        public static final NullPlayerInventory NULL_PLAYER_CONTAINER = new NullPlayerInventory();

        public NullPlayerInventory() { super(null); }

        @Override
        public boolean add(int slotIn, ItemStack stack) { return false; }

        @Override
        public void placeItemBackInInventory(World worldIn, ItemStack stack) {}

        @Override
        public void hurtArmor(DamageSource p_234563_1_, float p_234563_2_) {}

        @Override
        public void dropAll() {}

        @Override
        public boolean stillValid(PlayerEntity player) { return true; }
    }

    public static abstract class NullContainer extends BaseContainer<NullContainer> {
        public static final NullContainer NULL_CONTAINER = new NullContainer(null, -1, NullPlayerInventory.NULL_PLAYER_CONTAINER){};

        protected NullContainer(ContainerType<?> containerType, int windowId, PlayerInventory playerInv) { super(containerType, windowId, playerInv); }
        protected NullContainer(ContainerType<?> containerType, int windowId, PlayerInventory playerInv, @Nullable PacketBuffer buffer) { super(containerType, windowId, playerInv, buffer); }

        @Override
        final public ITextComponent getDisplayName() { return StringTextComponent.EMPTY; }

        @Override
        final public boolean stillValid(PlayerEntity playerIn) { return true; }

        @Override
        final public void readFromBuff(PacketBuffer buff) {}

        @Override
        final public void writeToBuff(PacketBuffer buff) {}
    }

    @OnlyIn(Dist.CLIENT)
    public SimpleContainerScreen<I> screen;

    protected final NonNullList<ItemStack> lastSlots = NonNullList.create();
    private BaseContainerWidget<I> focusedWidget = null;
    protected final List<IntReferenceHolder> dataSlots = Lists.newArrayList();

    @Override
    public Slot getSlot(int slot) {
        return slots.get(slot);
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
    protected void addDataSlots(IIntArray arrayIn) {
        for(int i = 0; i < arrayIn.getCount(); ++i)
            this.addDataSlot(IntReferenceHolder.forContainer(arrayIn, i));
    }

    protected final PlayerInventory playerInv;
    public final boolean isClient;

    public abstract ITextComponent getDisplayName();

    protected static <I extends BaseContainer<I>> void open(PlayerEntity player, I container) {
        if (player instanceof ServerPlayerEntity)
            NetworkHooks.openGui((ServerPlayerEntity) player, new INamedContainerProvider() {
                @Override
                public ITextComponent getDisplayName() {
                    return container.getDisplayName();
                }

                @Nullable
                @Override
                public Container createMenu(int p_createMenu_1_, PlayerInventory p_createMenu_2_, PlayerEntity p_createMenu_3_) {
                    return container;
                }
            }, container::writeToBuff);
    }

    protected BaseContainer(ContainerType<?> containerType, int windowId, PlayerInventory playerInv/*, ISimpleItemHandlerSerializable inv, ISimpleFluidHandlerSerializable tanks, int[] slots, int columnLimit, int rowLimit, boolean isClient*/) {
        super(containerType, windowId);
        this.isClient = !DimBag.isServer(null);
        this.playerInv = playerInv;
    }

    protected BaseContainer(ContainerType<?> containerType, int windowId, PlayerInventory playerInv, @Nullable PacketBuffer buffer) {
        this(containerType, windowId, playerInv);
        if (buffer != null)
            readFromBuff(buffer);
    }

    public void readFromBuff(PacketBuffer buff) {
    }

    public void writeToBuff(PacketBuffer buff) {
    }

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

    @Override
    protected Slot addSlot(Slot slotIn) {
        slotIn.index = this.slots.size();
        this.slots.add(slotIn);
        this.lastSlots.add(ItemStack.EMPTY);
        return slotIn;
    }

    protected void clearSlots() {
        this.slots.clear();
        this.lastSlots.clear();
    }

    @Override
    protected IntReferenceHolder addDataSlot(IntReferenceHolder intIn) {
        this.dataSlots.add(intIn);
        return intIn;
    }

    @Override
    public void setData(int id, int data) { this.dataSlots.get(id).set(data); }

    @Override
    public void broadcastChanges() {
        for(int i = 0; i < this.slots.size(); ++i) {
            ItemStack itemstack = this.slots.get(i).getItem();
            ItemStack itemstack1 = this.lastSlots.get(i);
            if (!ItemStack.matches(itemstack1, itemstack)) {
                boolean clientStackChanged = !itemstack1.equals(itemstack, true);
                ItemStack itemstack2 = itemstack.copy();
                this.lastSlots.set(i, itemstack2);

                if (clientStackChanged)
                    for(IContainerListener icontainerlistener : this.containerListeners) {
                        if (icontainerlistener instanceof ServerPlayerEntity && itemstack2.getCount() > 64) //added to sync slot size overrides
                            PacketHandler.toClient((ServerPlayerEntity) icontainerlistener, new SetSlotPacket(this.containerId, i, itemstack2));
                        else
                            icontainerlistener.slotChanged(this, i, itemstack2);
                    }
            }
        }

        for(int j = 0; j < this.dataSlots.size(); ++j) { //could be upgraded to sync other things than just int truncated to shorts IMO
            IntReferenceHolder intreferenceholder = this.dataSlots.get(j);
            if (intreferenceholder.checkAndClearUpdateFlag()) {
                for(IContainerListener icontainerlistener1 : this.containerListeners) {
                    icontainerlistener1.setContainerData(this, j, intreferenceholder.get());
                }
            }
        }
    }

    /**
     * should only be called by the associated screen client side
     */
    public void detectAndSendWidgetChanges() {
//        for (int i = 0; i < widgets.size(); ++i) {
//            CompoundNBT d = widgets.get(i).getDiffOrData(widgetData.get(i));
//            if (d.getBoolean("IsDiff"))
//                NBTUtils.applyDiff(widgetData.get(i), d);
//            else
//                widgetData.set(i, d.copy());
//            if (!d.isEmpty())
//                PacketHandler.toServer(new WidgetDataPacket(windowId, i, d));
//        }
    }

    public void receiveWidgetChange(PlayerEntity player, int widget, CompoundNBT data) {
//        if (player instanceof ServerPlayerEntity) //mirror changes on other clients if we are server side
//            for(IContainerListener icontainerlistener1 : this.containerListeners)
//                if (!icontainerlistener1.equals(player) && icontainerlistener1 instanceof ServerPlayerEntity)
//                    PacketHandler.toClient((ServerPlayerEntity)icontainerlistener1, new WidgetDataPacket(containerId, widget, data));
//        BaseContainer<I> container = (BaseContainer<I>)player.openContainer;
//        container.widgets.get(widget).loadDiffOrData(data);
//        container.widgetData.set(widget, container.widgets.get(widget).getLocalData().copy());
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
        Slot slotO = this.slots.get(original);
        Slot slotT = this.slots.get(target);
        if (slotO instanceof SlotItemHandler && slotT instanceof SlotItemHandler && ((SlotItemHandler)slotO).getItemHandler() == ((SlotItemHandler)slotT).getItemHandler())
            return false;
        if (subPlayerInventories && slotO.container instanceof PlayerInventory && slotO.container == slotT.container) {
            int o = slotO.getSlotIndex();
            int t = slotT.getSlotIndex();
            if ((o > 8 ? o > 36 ? o > 40 ? 3 : 2 : 1 : 0) != (t > 8 ? t > 36 ? t > 40 ? 3 : 2 : 1 : 0))
                return true;
        }
        if (slotO.container.getContainerSize() != 0 && slotT.container.getContainerSize() != 0 && slotO.container == slotT.container)
            return false;
        if ((slotT.isSameInventory(slotO) || slotO.isSameInventory(slotT)) && slotO.getSlotIndex() == slotT.getSlotIndex())
            return false;
        return true;
    }

    public static boolean areStackable(ItemStack s1, ItemStack s2) {
        return ItemStack.isSame(s1, s2) && ItemStack.tagMatches(s1, s2);
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity player, int position) //should be rewritten to respect the insert/extract rights of the IItemHandlerModifiable
    {
        Slot slot = this.slots.get(position);
        boolean reverse = (slot.container instanceof PlayerInventory);
        for (int pass = 0; pass < 2; ++pass)
            for (int targetIndex = reverse ? this.slots.size() - 1 : 0; reverse ? targetIndex >= 0 : targetIndex < this.slots.size(); targetIndex += reverse ? -1 : 1) {
                Slot target = this.slots.get(targetIndex);
                if (slot.getItem().isEmpty()) return ItemStack.EMPTY; //nothing else to transfer
                if ((pass == 0) == (target.getItem().isEmpty())) continue; //the first pass will try to merge first, that is, will skip the empty slots
                ItemStack slotStack = slot.getItem();
                if (!isTransferValid(position, target.index, false) || !target.mayPlace(slotStack)) continue; //test if we have the rights to transfer to the target slot, this does not check the validity of the merge
                ItemStack targetStack = target.getItem();
                if (!targetStack.isEmpty() && !areStackable(targetStack, slotStack)) continue; //invalid merge, the target slot isn't empty and does not match the original stack
                int maxInput = Math.min(slotStack.getCount(), getRealSlotInputLimit(target, slotStack));
                if (maxInput <= 0) continue; //no more place to input
                ItemStack newSlotStack = slotStack.copy();
                newSlotStack.shrink(maxInput);
                slot.set(newSlotStack);
                ItemStack newTargetStack;
                if (targetStack.isEmpty()) {
                    newTargetStack = slotStack.copy();
                    newTargetStack.setCount(maxInput);
                } else {
                    newTargetStack = targetStack.copy();
                    newTargetStack.grow(maxInput);
                }
                target.set(newTargetStack);
            }
        return ItemStack.EMPTY;
    }

    private int quickcraftStatus = 0;
    private int quickcraftType = -1;
    private final Set<Slot> quickcraftSlots = Sets.newHashSet();

    @Override
    protected void resetQuickCraft() {
        this.quickcraftStatus = 0;
        this.quickcraftSlots.clear();
    }

    @Override
    public ItemStack clicked(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
        try {
            return this.clickedInternal(slotId, dragType, clickTypeIn, player);
        } catch (Exception exception) {
            CrashReport crashreport = CrashReport.forThrowable(exception, "Container click");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Click info");
            crashreportcategory.setDetail("Menu Type", () -> this.getType() != null ? Registry.MENU.getKey(this.getType()).toString() : "<no type>");
            crashreportcategory.setDetail("Menu Class", () -> this.getClass().getCanonicalName());
            crashreportcategory.setDetail("Slot Count", this.slots.size());
            crashreportcategory.setDetail("Slot", slotId);
            crashreportcategory.setDetail("Button", dragType);
            crashreportcategory.setDetail("Type", clickTypeIn);
            throw new ReportedException(crashreport);
        }
    }

    protected ItemStack clickedInternal(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
        PlayerInventory playerinventory = player.inventory;
        if (slotId >= 0 && slots.get(slotId) instanceof FluidSlot) //special case: FluidSlot change how we interact with them
            return clickedFluid(playerinventory, slotId, clickTypeIn, player);
        else if (clickTypeIn == ClickType.QUICK_CRAFT)
            return clickedQuickCraft(playerinventory, slotId, dragType, player);
        else if (this.quickcraftStatus != 0)
            this.resetQuickCraft();
        else if ((clickTypeIn == ClickType.PICKUP || clickTypeIn == ClickType.QUICK_MOVE) && (dragType == 0 || dragType == 1))
            return clickedPickUp(playerinventory, slotId, dragType, clickTypeIn, player);
        else if (clickTypeIn == ClickType.SWAP)
            return clickedSwap(playerinventory, slotId, dragType, player);
        else if (clickTypeIn == ClickType.CLONE && player.isCreative() && playerinventory.getCarried().isEmpty() && slotId >= 0)
            return clickedClone(playerinventory, slotId);
        else if (clickTypeIn == ClickType.THROW && playerinventory.getCarried().isEmpty() && slotId >= 0)
            return clickedThrow(slotId, dragType, player);
        else if (clickTypeIn == ClickType.PICKUP_ALL && slotId >= 0)
            return clickedPickupAll(playerinventory, slotId, dragType, player);
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

    protected ItemStack clickedFluid(PlayerInventory playerinventory, int slotId, ClickType clickTypeIn, PlayerEntity player) {
        if (clickTypeIn != ClickType.PICKUP) return ItemStack.EMPTY; //only accept simple clicks
        FluidSlot slot = (FluidSlot)slots.get(slotId);
        slot.selectTank();
        if (CAN_FILL_AND_EMPTY_ITEM_TANKS)
            playerinventory.setCarried(stackInteraction(playerinventory.getCarried(), slot.getHandler(), playerinventory));
        return ItemStack.EMPTY;
    }

    protected ItemStack clickedQuickCraft(PlayerInventory playerinventory, int slotId, int dragType, PlayerEntity player) {
        int i1 = this.quickcraftStatus;
        this.quickcraftStatus = getQuickcraftHeader(dragType);
        if ((i1 != 1 || this.quickcraftStatus != 2) && i1 != this.quickcraftStatus) {
            this.resetQuickCraft();
        } else if (playerinventory.getCarried().isEmpty()) {
            this.resetQuickCraft();
        } else if (this.quickcraftStatus == 0) {
            this.quickcraftType = getQuickcraftType(dragType);
            if (isValidQuickcraftType(this.quickcraftType, player)) {
                this.quickcraftStatus = 1;
                this.quickcraftSlots.clear();
            } else {
                this.resetQuickCraft();
            }
        } else if (this.quickcraftStatus == 1) {
            Slot slot7 = this.slots.get(slotId);
            ItemStack itemstack12 = playerinventory.getCarried();
            if (slot7 != null && canItemQuickReplace(slot7, itemstack12, true) && slot7.mayPlace(itemstack12) && (this.quickcraftType == 2 || itemstack12.getCount() > this.quickcraftSlots.size()) && this.canDragTo(slot7)) {
                this.quickcraftSlots.add(slot7);
            }
        } else if (this.quickcraftStatus == 2) {
            if (!this.quickcraftSlots.isEmpty()) {
                ItemStack itemstack10 = playerinventory.getCarried().copy();
                int k1 = playerinventory.getCarried().getCount();

                for (Slot slot8 : this.quickcraftSlots) {
                    ItemStack itemstack13 = playerinventory.getCarried();
                    if (slot8 != null && canItemQuickReplace(slot8, itemstack13, true) && slot8.mayPlace(itemstack13) && (this.quickcraftType == 2 || itemstack13.getCount() >= this.quickcraftSlots.size()) && this.canDragTo(slot8)) {
                        ItemStack itemstack14 = itemstack10.copy();
                        int j3 = slot8.hasItem() ? slot8.getItem().getCount() : 0;
                        getQuickCraftSlotCount(this.quickcraftSlots, this.quickcraftType, itemstack14, j3);
                        int k3 = Math.min(itemstack14.getMaxStackSize(), slot8.getMaxStackSize(itemstack14));
                        if (itemstack14.getCount() > k3) {
                            itemstack14.setCount(k3);
                        }

                        k1 -= itemstack14.getCount() - j3;
                        slot8.set(itemstack14);
                    }
                }

                itemstack10.setCount(k1);
                playerinventory.setCarried(itemstack10);
            }

            this.resetQuickCraft();
        }
        return ItemStack.EMPTY;
    }

    /**
     * simulate a slot insertion and return how many items from stack that can be inserted
     */
    protected int getRealSlotInputLimit(Slot slot, ItemStack stack) {
        ItemStack s = slot.getItem();
        if (slot instanceof SlotItemHandler) {
            SlotItemHandler ih = (SlotItemHandler) slot;
            ItemStack rem = ih.getItemHandler().insertItem(slot.getSlotIndex(), stack, true);
            return stack.getCount() - rem.getCount();
        } else {
            int t = stack.getCount();
            if (t > slot.getMaxStackSize(stack) - s.getCount()) {
                t = slot.getMaxStackSize(stack) - s.getCount();
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
    protected ItemStack clickedPickUp(PlayerInventory playerinventory, int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
        ItemStack itemstack = ItemStack.EMPTY;
        if (slotId == -999) { //click outside the screen
            if (!playerinventory.getCarried().isEmpty()) {
                if (dragType == 0) { //probably left click, drop entire stask
                    player.drop(playerinventory.getCarried(), true);
                    playerinventory.setCarried(ItemStack.EMPTY);
                }

                if (dragType == 1) { //probably right click, drop only one item
                    player.drop(playerinventory.getCarried().split(1), true);
                }
            }
        } else if (clickTypeIn == ClickType.QUICK_MOVE) { //Shift-Click, be careful there
            if (slotId < 0) return ItemStack.EMPTY;

            Slot slot5 = this.slots.get(slotId);
            if (slot5 == null || !slot5.mayPickup(player)) return ItemStack.EMPTY;

            for(ItemStack itemstack8 = this.quickMoveStack(player, slotId); !itemstack8.isEmpty() && areStackable(slot5.getItem(), itemstack8); itemstack8 = this.quickMoveStack(player, slotId)) {
                itemstack = itemstack8.copy();
            }
        } else {
            if (slotId < 0) {
                return ItemStack.EMPTY;
            }

            Slot slot6 = this.slots.get(slotId);
            if (slot6 != null) {
                ItemStack itemstack9 = slot6.getItem();
                ItemStack itemstack11 = playerinventory.getCarried();
                if (!itemstack9.isEmpty()) {
                    itemstack = itemstack9.copy();
                }

                if (itemstack9.isEmpty()) {
                    if (!itemstack11.isEmpty() && slot6.mayPlace(itemstack11)) {
                        int j2 = dragType == 0 ? itemstack11.getCount() : 1;
                        if (j2 > slot6.getMaxStackSize(itemstack11)) {
                            j2 = slot6.getMaxStackSize(itemstack11);
                        }

                        slot6.set(itemstack11.split(j2));
                    }
                } else if (slot6.mayPickup(player)) {
                    if (itemstack11.isEmpty()) {
                        if (itemstack9.isEmpty()) {
                            slot6.set(ItemStack.EMPTY);
                            playerinventory.setCarried(ItemStack.EMPTY);
                        } else {
                            int k2 = dragType == 0 ? itemstack9.getCount() : (itemstack9.getCount() + 1) / 2;
                            if (k2 > itemstack9.getMaxStackSize()) k2 = itemstack9.getMaxStackSize(); //quick fix to prevent grab of more than a stack from a slot
                            playerinventory.setCarried(slot6.remove(k2));
                            if (itemstack9.isEmpty()) {
                                slot6.set(ItemStack.EMPTY);
                            }

                            slot6.onTake(player, playerinventory.getCarried());
                        }
                    } else if (slot6.mayPlace(itemstack11)) {
                        if (consideredTheSameItem(itemstack9, itemstack11)) {

                            int l2 = getRealSlotInputLimit(slot6, itemstack11);

                            itemstack11.shrink(l2);
                            itemstack9.grow(l2);
                        } else if (itemstack11.getCount() <= slot6.getMaxStackSize(itemstack11)) {
                            slot6.set(itemstack11);
                            playerinventory.setCarried(itemstack9);
                        }
                    } else if (itemstack11.getMaxStackSize() > 1 && consideredTheSameItem(itemstack9, itemstack11) && !itemstack9.isEmpty()) {
                        int i3 = itemstack9.getCount();
                        if (i3 + itemstack11.getCount() <= itemstack11.getMaxStackSize()) {
                            itemstack11.grow(i3);
                            itemstack9 = slot6.remove(i3);
                            if (itemstack9.isEmpty()) {
                                slot6.set(ItemStack.EMPTY);
                            }

                            slot6.onTake(player, playerinventory.getCarried());
                        }
                    }
                }

                slot6.setChanged();
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
    protected ItemStack clickedSwap(PlayerInventory playerinventory, int slotId, int dragType, PlayerEntity player) {
        Slot slot = this.slots.get(slotId);
        ItemStack itemstack1 = playerinventory.getItem(dragType);
        ItemStack itemstack2 = slot.getItem();
        if (!itemstack1.isEmpty() || !itemstack2.isEmpty()) {
            if (itemstack1.isEmpty()) {
                if (slot.mayPickup(player)) {
                    playerinventory.setItem(dragType, itemstack2);
                    ((ContainerSlotOverrides)slot).onSwapCraft(itemstack2.getCount());
                    slot.set(ItemStack.EMPTY);
                    slot.onTake(player, itemstack2);
                }
            } else if (itemstack2.isEmpty()) {
                if (slot.mayPlace(itemstack1)) {
                    int i = slot.getMaxStackSize(itemstack1);
                    if (itemstack1.getCount() > i) {
                        slot.set(itemstack1.split(i));
                    } else {
                        slot.set(itemstack1);
                        playerinventory.setItem(dragType, ItemStack.EMPTY);
                    }
                }
            } else if (slot.mayPickup(player) && slot.mayPlace(itemstack1)) {
                int l1 = slot.getMaxStackSize(itemstack1);
                if (itemstack1.getCount() > l1) {
                    slot.set(itemstack1.split(l1));
                    slot.onTake(player, itemstack2);
                    if (!playerinventory.add(itemstack2)) {
                        player.drop(itemstack2, true);
                    }
                } else {
                    slot.set(itemstack1);
                    playerinventory.setItem(dragType, itemstack2);
                    slot.onTake(player, itemstack2);
                }
            }
        }
        return ItemStack.EMPTY;
    }

    protected ItemStack clickedClone(PlayerInventory playerInventory, int slotId) {
        Slot slot4 = this.slots.get(slotId);
        if (slot4 != null && slot4.hasItem()) {
            ItemStack itemstack7 = slot4.getItem().copy();
            itemstack7.setCount(itemstack7.getMaxStackSize());
            playerInventory.setCarried(itemstack7);
        }
        return ItemStack.EMPTY;
    }

    protected ItemStack clickedThrow(int slotId, int dragType, PlayerEntity player) {
        Slot slot3 = this.slots.get(slotId);
        if (slot3 != null && slot3.hasItem() && slot3.mayPickup(player)) {
            ItemStack itemstack6 = slot3.remove(dragType == 0 ? 1 : slot3.getItem().getCount());
            slot3.onTake(player, itemstack6);
            player.drop(itemstack6, true);
        }
        return ItemStack.EMPTY;
    }

    protected ItemStack clickedPickupAll(PlayerInventory playerInventory, int slotId, int dragType, PlayerEntity player) {
        Slot slot2 = this.slots.get(slotId);
        ItemStack itemstack5 = playerInventory.getCarried();
        if (!itemstack5.isEmpty() && (slot2 == null || !slot2.hasItem() || !slot2.mayPickup(player))) {
            int j1 = dragType == 0 ? 0 : this.slots.size() - 1;
            int i2 = dragType == 0 ? 1 : -1;

            for(int j = 0; j < 2; ++j) {
                for(int k = j1; k >= 0 && k < this.slots.size() && itemstack5.getCount() < itemstack5.getMaxStackSize(); k += i2) {
                    Slot slot1 = this.slots.get(k);
                    if (slot1.hasItem() && canItemQuickReplace(slot1, itemstack5, true) && slot1.mayPickup(player) && this.canTakeItemForPickAll(itemstack5, slot1)) {
                        ItemStack itemstack3 = slot1.getItem();
                        if (j != 0 || itemstack3.getCount() != itemstack3.getMaxStackSize()) {
                            int l = Math.min(itemstack5.getMaxStackSize() - itemstack5.getCount(), itemstack3.getCount());
                            ItemStack itemstack4 = slot1.remove(l);
                            itemstack5.grow(l);
                            if (itemstack4.isEmpty()) {
                                slot1.set(ItemStack.EMPTY);
                            }

                            slot1.onTake(player, itemstack4);
                        }
                    }
                }
            }
        }
        this.broadcastChanges();
        return ItemStack.EMPTY;
    }

}
