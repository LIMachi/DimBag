package com.limachi.dimensional_bags.common.container;

import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.container.slot.UpgradeConsumerSlot;
import com.limachi.dimensional_bags.common.data.EyeData;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;

import static com.limachi.dimensional_bags.common.references.GUIs.ScreenParts.SLOT_SIZE_X;
import static com.limachi.dimensional_bags.common.references.GUIs.ScreenParts.SLOT_SIZE_Y;
import static com.limachi.dimensional_bags.common.references.GUIs.UpgradeScreen.FIRST_SLOT_X;
import static com.limachi.dimensional_bags.common.references.GUIs.UpgradeScreen.FIRST_SLOT_Y;
import static com.limachi.dimensional_bags.common.references.GUIs.UpgradeScreen.PLAYER_INVENTORY_PART_Y;

public class UpgradeContainer extends BaseContainer {

    private EyeData data;

    public UpgradeContainer(int windowId, PlayerInventory playerInv, PacketBuffer extraData) {
        super(Registries.UPGRADE_CONTAINER.get(), windowId, playerInv, extraData);
        this.data = null;
        addSlots();
    }

    public UpgradeContainer(int windowId, ServerPlayerEntity player, EyeData data) {
        super(Registries.UPGRADE_CONTAINER.get(), windowId, player, data.getupgrades());
        this.data = data;
        addSlots();
    }

    private void addSlots() {
        addPlayerSlots(0, PLAYER_INVENTORY_PART_Y);
        int sx = FIRST_SLOT_X + 1;
        int sy = FIRST_SLOT_Y + 1;
        for (int y = 0; y < getRows(); ++y)
            for (int x = 0; x < getColumns(); ++x)
                if (x + y * getColumns() < openInv.getSize())
                        this.addSlot(new UpgradeConsumerSlot(openInv, x + y * getColumns(), sx + SLOT_SIZE_X * x, sy + SLOT_SIZE_Y * y, data));
    }
}