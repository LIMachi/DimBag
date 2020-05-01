package com.limachi.dimensional_bags.common.data.container;

import com.limachi.dimensional_bags.DimensionalBagsMod;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.data.container.slot.UpgradeConsumerSlot;
import com.limachi.dimensional_bags.common.init.Registries;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.network.PacketBuffer;

import static com.limachi.dimensional_bags.common.references.GUIs.ScreenParts.*;
import static com.limachi.dimensional_bags.common.references.GUIs.UpgradeScreen.*;

public class UpgradeContainer extends BaseContainer {

    EyeData data;

    public UpgradeContainer(int windowId, PlayerInventory inventory, EyeData data) {
        super(Registries.UPGRADE_CONTAINER.get(), windowId, inventory, data.upgrades);
//        data.upgrades.setParent(this);
        this.data = data;
        this.reAddSlots();
        /*
        for (int y = 0; y < 2; ++y)
            for (int x = 0; x < 9; ++x)
                if (x + y * 9 < data.upgrades.getSizeInventory())
                    this.addSlot(new UpgradeConsumerSlot(data.upgrades, x + y * 9, FIRST_SLOT_X + x * SLOT_SIZE_X + 1, FIRST_SLOT_Y + y * SLOT_SIZE_X + 1, this.data));
        this.addPlayerSlotsContainer(inventory, 0, PLAYER_INVENTORY_PART_Y);
        */
    }

    protected void addContainerSlots(int ix, int iy) {
        for (int y = 0; y < this.inventory.getRows(); ++y)
            for (int x = 0; x < this.inventory.getColumns(); ++x)
                if (x + y * this.inventory.getColumns() < this.inventory.getSizeInventory())
                    this.addSlot(new UpgradeConsumerSlot(this.inventory, x + y * inventory.getColumns(), ix + SLOT_SIZE_X * x, iy + SLOT_SIZE_Y * y, this.data));
    }

    public void reAddSlots() {
        this.addSlots(FIRST_SLOT_X + 1, FIRST_SLOT_Y + 1, true, 0, PLAYER_INVENTORY_PART_Y);
    }

    public UpgradeContainer(int windowId, PlayerInventory inventory, PacketBuffer buff) {
        this(windowId, inventory, new EyeData(buff));
    }
}
