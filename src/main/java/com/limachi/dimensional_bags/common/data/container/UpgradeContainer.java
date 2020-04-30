package com.limachi.dimensional_bags.common.data.container;

import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.data.container.slot.UpgradeConsumerSlot;
import com.limachi.dimensional_bags.common.init.Registries;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;

import static com.limachi.dimensional_bags.common.references.GUIs.ScreenParts.*;
import static com.limachi.dimensional_bags.common.references.GUIs.UpgradeScreen.*;

public class UpgradeContainer extends BaseContainer {

    public UpgradeContainer(int windowId, PlayerInventory inventory, EyeData data) {
        super(Registries.UPGRADE_CONTAINER.get(), windowId, data, data.upgrades.getSizeInventory());
        for (int y = 0; y < 2; ++y)
            for (int x = 0; x < 9; ++x)
                if (x + y * 9 < data.upgrades.getSizeInventory())
                    this.addSlot(new UpgradeConsumerSlot(data.upgrades, x + y * 9, FIRST_SLOT_X + x * SLOT_SIZE_X + 1, FIRST_SLOT_Y + y * SLOT_SIZE_X + 1, this.data));
        this.addPlayerSlotsContainer(inventory, 0, PLAYER_INVENTORY_PART_Y);
    }

    public UpgradeContainer(int windowId, PlayerInventory inventory, PacketBuffer buff) {
        this(windowId, inventory, new EyeData(buff));
    }
}
