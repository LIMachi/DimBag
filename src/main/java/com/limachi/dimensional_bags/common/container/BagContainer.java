package com.limachi.dimensional_bags.common.container;

import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.InventoryData;
import com.limachi.dimensional_bags.common.inventory.PlayerInvWrapper;
import com.limachi.dimensional_bags.common.inventory.Wrapper;
import com.limachi.dimensional_bags.common.references.GUIs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.SlotItemHandler;

import static com.limachi.dimensional_bags.common.references.GUIs.ScreenParts.*;

import com.limachi.dimensional_bags.StaticInit;

//@StaticInit
/*
public class BagContainer extends BaseWrappedInventoryContainer {

    public static final String NAME = "inventory";

    static {
        Registries.registerContainer(NAME, BagContainer::CreateClient);
    }

    private int rows;
    private int columns;
//    private int pillar;
//    private InventoryData data;

    private BagContainer(int windowId, PlayerInventory playerInv, int pillar, Wrapper openInv) {
        super(Registries.getContainerType(BagContainer.NAME), windowId);
        this.playerInv = new PlayerInvWrapper(playerInv);
        this.openInv = openInv;
//        this.pillar = pillar;
        if (pillar != -1) { //if this is an identified pillar, only view the related slot
            this.rows = 1;
            this.columns = 1;
        } else { //otherwise, show the entire inventory
            this.rows = (int)Math.ceil((double)openInv.getSlots() / 9.);
            this.columns = (int)Math.ceil((double)openInv.getSlots() / (double)this.rows);
        }
//        this.data = data;
        addSlots();
        trackIntArray(this.openInv.rightsAsIntArray());
    }

    public static BagContainer CreateClient(int windowId, PlayerInventory playerInv, PacketBuffer extraData) { //client
        return new BagContainer(windowId, playerInv, extraData.readInt(), new Wrapper(extraData), null);
    }

    public static BagContainer CreateServer(int windowId, PlayerInventory playerInv, InventoryData data, int slot) { //server
        return new BagContainer(windowId, playerInv, slot, data.getUserInventory(), data);
    }

    private void addSlots() {
        int sx = GUIs.BagScreen.calculateShiftLeft(columns);
        int sy = GUIs.BagScreen.calculateYSize(rows);
        addPlayerSlots(sx > 0 ? sx * SLOT_SIZE_X : 0, sy - PLAYER_INVENTORY_Y);
        sx = PART_SIZE_X + 1 + (sx < 0 ? -sx * SLOT_SIZE_X : 0);
        sy = PART_SIZE_Y * 2 + 1;
        for (int y = 0; y < rows; ++y)
            for (int x = 0; x < columns; ++x)
                if (x + y * columns < openInv.getSlots())
                    this.addSlot(new SlotItemHandler(openInv, x + y * columns, sx + SLOT_SIZE_X * x, sy + SLOT_SIZE_Y * y));
    }

    public int getRows() { return rows; }

    public int getColumns() { return columns; }

    public int getInventorySize() { return openInv.getSlots(); }

//    @Override
//    public void detectAndSendChanges() {
//        if (!client && data != null && data.getPillarDirtyState()) {//something changed with the pillar
//            PillarState s = data.getPillarState(pillar);
//            if (s == PillarState.)
//            Network.openEyeInventory((ServerPlayerEntity) playerInv.getPlayerInventory().player, data.getEyeId(), pillar);
//        } else
//            super.detectAndSendChanges();
//    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
        if (clickTypeIn == ClickType.CLONE) { //middle click
            if (slotId < 36 || slotId >= inventorySlots.size()) return super.slotClick(slotId, dragType, clickTypeIn, player);
            slotId -= 36;
            Wrapper.IORights rights = this.openInv.getRights(slotId);
            byte flags = (byte)(((rights.flags & 3) + 1) & 3); //only work on the 2 first flags who correspond to Input and Output, increment and modulo it
            rights.flags = (byte)((rights.flags & ~3) | flags); //merge the new io flags with the previous flags
            changeRights(slotId, rights);
            return ItemStack.EMPTY;
        }
        return super.slotClick(slotId, dragType, clickTypeIn, player);
    }
}*/