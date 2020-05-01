package com.limachi.dimensional_bags.common.data.container;

import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.init.Registries;
import com.limachi.dimensional_bags.common.references.GUIs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.network.PacketBuffer;

import static com.limachi.dimensional_bags.common.references.GUIs.ScreenParts.*;

public class DimBagContainer extends BaseContainer {

    public DimBagContainer(int windowId, PlayerInventory inventory, EyeData data) {
        super(Registries.BAG_CONTAINER.get(), windowId, inventory, data.items);
//        data.items.setParent(this);
        this.reAddSlots();
    }

    protected void addContainerSlots(int ix, int iy) {
        for (int y = 0; y < this.inventory.getRows(); ++y)
            for (int x = 0; x < this.inventory.getColumns(); ++x)
                if (x + y * this.inventory.getColumns() < this.inventory.getSizeInventory())
                    this.addSlot(new Slot(this.inventory, x + y * this.inventory.getColumns(), ix + SLOT_SIZE_X * x, iy + SLOT_SIZE_Y * y));
    }

    public void reAddSlots() {
        int sx = GUIs.BagScreen.calculateShiftLeft(this.inventory.getColumns());
        int sy = GUIs.BagScreen.calculateYSize(this.inventory.getRows());
        this.addSlots(PART_SIZE_X + 1 + (sx < 0 ? -sx * SLOT_SIZE_X : 0),PART_SIZE_Y * 2 + 1, true, (sx > 0 ? sx * SLOT_SIZE_X : 0), sy - PLAYER_INVENTORY_Y);
    }

    public DimBagContainer(int windowId, PlayerInventory inventory, PacketBuffer buff) {
        this(windowId, inventory, new EyeData(buff));
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) { //FIXME: for now, set it to true, will have to implement logic later
        return true;
    }
}
