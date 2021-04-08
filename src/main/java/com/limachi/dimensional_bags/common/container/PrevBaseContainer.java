package com.limachi.dimensional_bags.common.container;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.utils.WorldUtils;
import com.limachi.dimensional_bags.common.container.slot.DisabledSlot;
//import com.limachi.dimensional_bags.common.container.slot.RemoteFluidSlot;
import com.limachi.dimensional_bags.common.container.slot.IIORightsSlot;
import com.limachi.dimensional_bags.common.container.slot.InvWrapperSlot;
import com.limachi.dimensional_bags.common.inventory.Wrapper;
import com.limachi.dimensional_bags.common.network.PacketHandler;
//import com.limachi.dimensional_bags.common.network.packets.TrackedStringSyncMsg;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import com.limachi.dimensional_bags.common.references.GUIs.ScreenParts;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.ArrayList;

/*
public class BaseContainer extends Container {

    public interface IStringArray {
        String get(int index);
        void set(int index, String value);
        int size();
    }

    public static abstract class StringReferenceHolder {
        private String lastKnownValue;

        public static StringReferenceHolder create(final IStringArray data, final int idx) {
            return new StringReferenceHolder() {
                public String get() { return data.get(idx); }
                public void set(String value) { data.set(idx, value); }
            };
        }

        public static StringReferenceHolder create(final String[] data, final int idx) {
            return new StringReferenceHolder() {
                public String get() { return data[idx]; }
                public void set(String value) { data[idx] = value; }
            };
        }

        public static StringReferenceHolder single() {
            return new StringReferenceHolder() {
                private String value;
                public String get() { return this.value; }
                public void set(String value) { this.value = value; }
            };
        }

        public abstract String get();
        public abstract void set(String value);

        protected void sync(String value) {
            set(value);
            lastKnownValue = new String(value.getBytes());
        }

        public boolean isDirty() {
            String s = this.get();
            if (s != null && this.lastKnownValue != null) {
                boolean flag = s.equals(this.lastKnownValue);
                this.lastKnownValue = new String(s.getBytes()); //make sure the last known value is not a reference but a deep copy
                return flag;
            }
            return s != null;
        }
    }

    public enum ContainerConnectionType {
        TILE_ENTITY, //the container reflects the state of a tile entity (like a chest/crafting interface/etc)
        ITEM, //the container reflects the state of an item in the player inventory (like a bag/chulkerbox/etc)
        GLOBAL, //the container reflects the state of a world accessible data (world config/dimensional bag/etc)
        MODAL, //the container isn't linked and is expected to have it's behavior dictated by overides/callback/network messages
        NONE //error/uninitialized container
    }

    protected boolean isClient;
    protected PlayerEntity player;

    private int inventoryStart = -1;
    private int armorStart = -1;
    private int offHandStart = -1;

//    public final List<RemoteFluidSlot> remoteFluidSlots = Lists.newArrayList();
    NonNullList<FluidStack> trackedFluidStacks = NonNullList.create();
    NonNullList<StringReferenceHolder> trackedString = NonNullList.create();
    public final ContainerConnectionType connectionType;
    @Nullable
    public final TileEntity tileEntity;
    @Nullable
    public final Integer itemSlot;

    public BaseContainer(ContainerType<? extends BaseContainer> type, int windowId, PlayerInventory playerInv, ContainerConnectionType connectionType, TileEntity tileEntity, int itemSlot) {
        super(type, windowId);
        this.isClient = false;
        this.player = playerInv.player;
        this.connectionType = connectionType;
        if (connectionType == ContainerConnectionType.TILE_ENTITY)
            this.tileEntity = tileEntity;
        else
            this.tileEntity = null;
        if (connectionType == ContainerConnectionType.ITEM)
            this.itemSlot = itemSlot;
        else
            this.itemSlot = null;
    }

    public BaseContainer(ContainerType<? extends BaseContainer> type, int windowId, PlayerInventory playerInv, PacketBuffer extraData) {
        super(type, windowId);
        this.isClient = true;
        this.player = playerInv.player;
        this.connectionType = ContainerConnectionType.values()[extraData.readInt()];
        switch (this.connectionType) {
            case TILE_ENTITY:
                this.tileEntity = WorldUtils.getWorld(DimBag.getServer(), extraData.readString()).getTileEntity(extraData.readBlockPos());
                this.itemSlot = null;
                break;
            case ITEM:
                this.tileEntity = null;
                this.itemSlot = extraData.readInt();
                break;
            default:
                this.tileEntity = null;
                this.itemSlot = null;
        }
    }

    public static PacketBuffer writeBaseParameters(PacketBuffer buffer, ContainerConnectionType connectionType, TileEntity tileEntity, int itemSlot) {
        buffer.writeInt(connectionType.ordinal());
        if (connectionType == ContainerConnectionType.TILE_ENTITY) {
            buffer.writeString(WorldUtils.worldRKToString(tileEntity.getWorld().getDimensionKey()));
            buffer.writeBlockPos(tileEntity.getPos());
        } else if (connectionType == ContainerConnectionType.ITEM)
            buffer.writeInt(itemSlot);
        return buffer;
    }

    protected ArrayList<Integer> disabledSlots() {
        ArrayList<Integer> out = new ArrayList<>();
        if (connectionType == ContainerConnectionType.ITEM) //by default, add the item linked to the container to the blacklisted slots
            out.add(itemSlot);
        return out;
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        if (connectionType == ContainerConnectionType.TILE_ENTITY) {
            if (tileEntity != null)
                return tileEntity.getWorld().getTileEntity(tileEntity.getPos()) == tileEntity; //does the tile entity in the world at the previous position is still the same
            return false; //missing tile entity reference, the logic of this container might break
        }
        return true;
    }

    public PlayerEntity getPlayer() { return this.player; }

    protected void addPlayerArmor(int x, int y, boolean horizontal) {
        this.armorStart = this.inventorySlots.size();
        if (horizontal) {
            for (int i = 0; i < 4; ++i)
                if (disabledSlots().contains(36 + i))
                    addSlot(new DisabledSlot(player.inventory, 36 + i, x + i * ScreenParts.SLOT_SIZE_X, y));
                else
                    addSlot(new Slot(player.inventory, 36 + i, x + i * ScreenParts.SLOT_SIZE_X, y));
        } else {
            for (int i = 0; i < 4; ++i)
                if (disabledSlots().contains(36 + i))
                    addSlot(new DisabledSlot(player.inventory, 36 + i, x, y + i * ScreenParts.SLOT_SIZE_Y));
                else
                    addSlot(new Slot(player.inventory, 36 + i, x, y + i * ScreenParts.SLOT_SIZE_Y));
        }
    }

    protected void addPlayerOffHand(int x, int y) {
        this.offHandStart = this.inventorySlots.size();
        addSlot(disabledSlots().contains(40) ? new DisabledSlot(player.inventory, 40, x, y) : new Slot(player.inventory, 40, x, y));
    }

    protected void addPlayerInventory(int x, int y) {
        this.inventoryStart = this.inventorySlots.size();
        for (int i = 0; i < 9; ++i)
            if (disabledSlots().contains(i))
                addSlot(new DisabledSlot(player.inventory, i, x + i * ScreenParts.SLOT_SIZE_X, y + 4 + 3 * ScreenParts.SLOT_SIZE_Y));
            else
                addSlot(new Slot(player.inventory, i, x + i * ScreenParts.SLOT_SIZE_X, y + 4 + 3 * ScreenParts.SLOT_SIZE_Y));
        for (int ty = 0; ty < 3; ++ty)
            for (int tx = 0; tx < 9; ++tx)
                if (disabledSlots().contains(x + y * 9 + 9))
                    addSlot(new DisabledSlot(player.inventory, tx + ty * 9 + 9, x + tx * ScreenParts.SLOT_SIZE_X, y + ty * ScreenParts.SLOT_SIZE_Y));
                else
                    addSlot(new Slot(player.inventory, tx + ty * 9 + 9, x + tx * ScreenParts.SLOT_SIZE_X, y + ty * ScreenParts.SLOT_SIZE_Y));
    }

    @Override
    protected Slot addSlot(Slot slotIn) {
        super.addSlot(slotIn);
//        if (slotIn instanceof RemoteFluidSlot) {
//            trackedFluidStacks.add(FluidStack.EMPTY);
//            remoteFluidSlots.add((RemoteFluidSlot)slotIn);
//            return slotIn;
//        }
        return slotIn;
    }

    @Override
    public boolean canMergeSlot(ItemStack stack, Slot slotIn) {
//        if (slotIn instanceof RemoteFluidSlot) return false;
        return super.canMergeSlot(stack, slotIn);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges(); //will do the int and ItemStacks for us
        if (player instanceof ServerPlayerEntity) {
//            for (int i = 0; i < this.remoteFluidSlots.size(); ++i) {
//                FluidStack fluid = this.remoteFluidSlots.get(i).getFluidStack();
//                if (!this.trackedFluidStacks.get(i).isFluidStackIdentical(fluid)) {
//                    this.trackedFluidStacks.set(i, fluid.copy());
//                    PacketHandler.toClient((ServerPlayerEntity) player, new FluidSlotSyncMsg(windowId, i, fluid));
//                }
//            }
            for (int i = 0; i < this.trackedString.size(); ++i) {
                StringReferenceHolder srh = this.trackedString.get(i);
                if (srh.isDirty())
                    PacketHandler.toClient((ServerPlayerEntity) player, new TrackedStringSyncMsg(windowId, i, srh.get()));
            }
        } else {
            for (int i = 0; i < this.trackedString.size(); ++i) {
                StringReferenceHolder srh = this.trackedString.get(i);
                if (srh.isDirty())
                    PacketHandler.toServer(new TrackedStringSyncMsg(windowId, i, srh.get()));
            }
        }
    }

    public void loadStringChange(int stringId, String data) { trackedString.get(stringId).sync(data); }

    public void loadFluidSlotChange(int slotIndex, FluidStack fluid) {
        trackedFluidStacks.set(slotIndex, fluid);
//        RemoteFluidSlot slot = remoteFluidSlots.get(slotIndex);
//        slot.getFluidHandler().setFluid(slot.slotNumber, fluid);
    }

    public StringReferenceHolder trackString(StringReferenceHolder stringIn) {
        this.trackedString.add(stringIn);
        return stringIn;
    }

    public void trackStringArray(IStringArray arrayIn) {
        for (int i = 0; i < arrayIn.size(); ++i)
            trackString(StringReferenceHolder.create(arrayIn, i));
    }

    protected boolean isPlayerSlot(int index) {
        if (inventoryStart != -1 && index >= inventoryStart && index < inventoryStart + 36) return true;
        if (armorStart != -1 && index >= armorStart && index < armorStart + 4) return true;
        return offHandStart != -1 && index == offHandStart;
    }

    protected ArrayList<Integer> playerSlots() {
        ArrayList<Integer> out = new ArrayList<>();
        for (int i = 0; i < inventorySlots.size(); ++i)
            if (isPlayerSlot(i))
                out.add(i);
        return out;
    }

    protected ArrayList<Integer> otherSlots() {
        ArrayList<Integer> out = new ArrayList<>();
        for (int i = 0; i < inventorySlots.size(); ++i)
            if (!isPlayerSlot(i))
                out.add(i);
        return out;
    }

    protected int getSameSlotIndex(Slot slot) {
        for (Slot test : inventorySlots) {
            if (test.slotNumber == slot.slotNumber) continue;
            if (test instanceof InvWrapperSlot) {
                if (slot instanceof InvWrapperSlot) {
                    if (!((InvWrapperSlot)test).getWrapper().matchInventory(((InvWrapperSlot)slot).getWrapper().getInventory()))
                        continue;
                } else {
                    if (!((InvWrapperSlot)test).getWrapper().matchInventory(slot.inventory))
                        continue;
                }
            } else if (slot instanceof InvWrapperSlot) {
                if (!((InvWrapperSlot)slot).getWrapper().matchInventory(test.inventory))
                    continue;
            } else if (slot.inventory != test.inventory)
                continue;
            if (test.getSlotIndex() == slot.getSlotIndex())
                return test.slotNumber;
        }
        return -1;
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = index >= 0 && index < inventorySlots.size() ? inventorySlots.get(index) : null;
        if (slot != null && slot.getHasStack()) {
            ItemStack slotStack = slot.getStack();
            itemStack = slotStack.copy();
            boolean playerSlot = isPlayerSlot(index);
            ArrayList<Integer> blackList = new ArrayList<>(disabledSlots());
            blackList.add(getSameSlotIndex(slot));
            if (playerSlot)
                blackList.addAll(playerSlots());
            else
                blackList.addAll(otherSlots());
            if (!Wrapper.mergeItemStack(inventorySlots, slotStack, 0, inventorySlots.size(), false, blackList))
                return ItemStack.EMPTY;
            if (slotStack.getCount() == 0)
                slot.putStack(ItemStack.EMPTY);
            else
                slot.onSlotChanged();
        }
        return itemStack;
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
        if (disabledSlots().contains(slotId)) return ItemStack.EMPTY; //clicking invalid slot
        if (clickTypeIn == ClickType.SWAP && disabledSlots().contains(dragType)) return ItemStack.EMPTY; //trying to swap invalid slot
        Slot slot = slotId >= 0 && slotId < inventorySlots.size() ? inventorySlots.get(slotId) : null;
        if (clickTypeIn == ClickType.CLONE && slot instanceof IIORightsSlot) {
            ((IIORightsSlot)slot).setRightsFlag((byte)((((IIORightsSlot)slot).getRights() + 1) & 3));
            return ItemStack.EMPTY;
        }
//        if (slot instanceof RemoteFluidSlot) return ((RemoteFluidSlot)slot).onSlotClick(player, clickTypeIn);
        return super.slotClick(slotId, dragType, clickTypeIn, player);
    }

    public Wrapper.IORights getRights(int index) {
        if (index < 0 || index >= inventorySlots.size()) return new Wrapper.IORights();
        Slot slot = inventorySlots.get(index);
        if (slot instanceof IIORightsSlot)
            return new Wrapper.IORights(((IIORightsSlot)slot).getRights(), (byte)0, (byte)64);
        return new Wrapper.IORights();
    }

    public void changeRights(int index, Wrapper.IORights rights) {
        if (index < 0 || index >= inventorySlots.size()) return;
        Slot slot = inventorySlots.get(index);
        if (slot instanceof IIORightsSlot)
            ((IIORightsSlot)slot).setRights(rights);
    }
}
*/