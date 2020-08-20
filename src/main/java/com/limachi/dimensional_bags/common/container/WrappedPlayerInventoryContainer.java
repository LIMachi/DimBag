package com.limachi.dimensional_bags.common.container;

import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.container.slot.InvWrapperSlot;
import com.limachi.dimensional_bags.common.inventory.PlayerInvWrapper;
import com.limachi.dimensional_bags.common.inventory.Wrapper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.*;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIntArray;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import java.lang.reflect.Field;
import java.util.List;

import javax.annotation.Nullable;

import static com.limachi.dimensional_bags.common.references.GUIs.ScreenParts.*;
import static com.limachi.dimensional_bags.common.references.GUIs.PlayerInterface.*;

public class WrappedPlayerInventoryContainer extends Container {

    private final PlayerInvWrapper targetInventory;
    private final PlayerInventory playerInventory;
    private final boolean isClient;
    private String localUserName;

    protected static Field field = ObfuscationReflectionHelper.findField(Container.class, "field_75149_d");
    protected List<IContainerListener> listenersR;

    public static WrappedPlayerInventoryContainer createClient(int windowId, PlayerInventory playerInventory, PacketBuffer extraData) {
        return new WrappedPlayerInventoryContainer(Registries.PLAYER_CONTAINER.get(), windowId, playerInventory, extraData.readBoolean() ? new PlayerInvWrapper(playerInventory, extraData) : new PlayerInvWrapper(extraData), true, null);
    }

    public static WrappedPlayerInventoryContainer createServer(int windowId, PlayerInventory playerInventory, PlayerInvWrapper targetInventory, TileEntity te) {
        return new WrappedPlayerInventoryContainer(Registries.PLAYER_CONTAINER.get(), windowId, playerInventory, targetInventory, false, te);
    }

    protected void addPlayerSlots(int px, int py) {
        int dx = px + PLAYER_INVENTORY_FIRST_SLOT_X + 1;
        int dy = py + PLAYER_BELT_FIRST_SLOT_Y + 1;
        for (int x = 0; x < PLAYER_INVENTORY_COLUMNS; ++x)
            addSlot(new Slot(playerInventory, x, dx + x * SLOT_SIZE_X, dy));
        dy = py + PLAYER_INVENTORY_FIRST_SLOT_Y + 1;
        for (int y = 0; y < PLAYER_INVENTORY_ROWS; ++y)
            for (int x = 0; x < PLAYER_INVENTORY_COLUMNS; ++x)
                addSlot(new Slot(playerInventory, x + (y + 1) * PLAYER_INVENTORY_COLUMNS, dx + x * SLOT_SIZE_X, dy + y * SLOT_SIZE_Y));
    }

    private WrappedPlayerInventoryContainer(@Nullable ContainerType<? extends WrappedPlayerInventoryContainer> type, int windowId, PlayerInventory playerInventory, PlayerInvWrapper targetInventory, boolean isClient, TileEntity te) {
        super(type, windowId);
        try { this.listenersR = (List<IContainerListener>) field.get(this); } catch (Throwable e) {}
        this.isClient = isClient;
        this.targetInventory = targetInventory;
        this.playerInventory = playerInventory;
        addPlayerSlots(0, PLAYER_INVENTORY_PART_Y);
        for (int x = 0; x < 9; ++x)
            addSlot(new InvWrapperSlot(targetInventory, x, BELT_X + 1 + x * SLOT_SIZE_X, BELT_Y + 1));
        for (int y = 0; y < 3; ++y)
            for (int x = 0; x < 9; ++x)
                addSlot(new InvWrapperSlot(targetInventory, x + 9 * (y + 1), MAIN_INVENTORY_X + 1 + x * SLOT_SIZE_X, MAIN_INVENTORY_Y + 1 + y * SLOT_SIZE_Y));
        for (int x = 0; x < 4; ++x)
            addSlot(new InvWrapperSlot(targetInventory, 36 + x, ARMOR_SLOTS_X + 1 + x * SLOT_SIZE_X, SPECIAL_SLOTS_Y + 1));
        addSlot(new InvWrapperSlot(targetInventory, 40, OFF_HAND_SLOT_X + 1, SPECIAL_SLOTS_Y + 1));
        trackIntArray(targetInventory.rightsAsIntArray());
        if (isClient)
            localUserName = "Unavailable ";
        else {
            localUserName = targetInventory.getPlayerInventory().player.getName().getString();
            if (localUserName.length() != 12)
                localUserName = String.format("%1$-12s", localUserName);
        }
        trackIntArray(new IIntArray() {
            @Override
            public int get(int i) {
                final char[] chars = localUserName.toCharArray();
                return (chars[i]);
            }

            @Override
            public void set(int i, int var) {
                final char[] chars = localUserName.toCharArray();
                chars[i] = (char)var;
                localUserName = new String(chars);
            }

            @Override
            public int size() {
                return 12;
            }
        });
    }

    public String getLocalUserName() { return localUserName; }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
    }

    public void changeRights(int slot, Wrapper.IORights rights) {
        targetInventory.setRights(slot, rights);
        /*
        if (!isClient)
            targetInventory.markDirty();
        */
        /*
        if (!isClient) {
            data.markDirty();
            SlotIORightsChanged pack = new SlotIORightsChanged(slot, rights);
            for (IContainerListener listener : listenersR)
                if (listener instanceof PlayerEntity)
                    PacketHandler.toClient(pack, (ServerPlayerEntity) listener);
        }
        */
    }

    public Wrapper.IORights getRights(int slot) {
        if (slot < 36 || slot >= inventorySlots.size()) return new Wrapper.IORights();
        return targetInventory.getRights(slot - 36);
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
        if (clickTypeIn == ClickType.CLONE) { //middle click
            if (slotId < 36 || slotId >= inventorySlots.size()) return super.slotClick(slotId, dragType, clickTypeIn, player);
            slotId -= 36;
            Wrapper.IORights rights = targetInventory.getRights(slotId);
            byte flags = (byte)(((rights.flags & 3) + 1) & 3); //only work on the 2 first flags who correspond to Input and Output, increment and modulo it
            rights.flags = (byte)((rights.flags & ~3) | flags); //merge the new io flags with the previous flags
            changeRights(slotId, rights);
            return ItemStack.EMPTY;
        }
        return super.slotClick(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int position)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(position);

        if(slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if(position >= 36 && !Wrapper.mergeItemStack(inventorySlots, itemstack1, 0, 36, false, targetInventory.getPlayerInventory() == playerInventory ? position - 36 : -1))
                return ItemStack.EMPTY;
            else if(!Wrapper.mergeItemStack(inventorySlots, itemstack1, 36, this.inventorySlots.size(), false, targetInventory.getPlayerInventory() == playerInventory ? 36 + position : -1))
                return ItemStack.EMPTY;
            if(itemstack1.getCount() == 0)
                slot.putStack(ItemStack.EMPTY);
            else
                slot.onSlotChanged();
        }
        return itemstack;
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) { return true; }
}
