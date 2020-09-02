package com.limachi.dimensional_bags.common.container;

public class UpgradeContainer /*extends BaseWrappedInventoryContainer*/ {/*

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
        for (int y = 0; y < 2; ++y)
            for (int x = 0; x < 9; ++x)
                if (x + y * 9 < openInv.getSlots())
                        this.addSlot(new InvWrapperUpgradeSlot(openInv, x + y * 9, sx + SLOT_SIZE_X * x, sy + SLOT_SIZE_Y * y, data));
    }
*/}
