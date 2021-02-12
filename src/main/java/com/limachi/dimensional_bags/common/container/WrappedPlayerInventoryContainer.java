package com.limachi.dimensional_bags.common.container;

import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.container.slot.InvWrapperSlot;
import com.limachi.dimensional_bags.common.inventory.PlayerInvWrapper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;

import static com.limachi.dimensional_bags.common.references.GUIs.ScreenParts.*;
import static com.limachi.dimensional_bags.common.references.GUIs.PlayerInterface.*;

import com.limachi.dimensional_bags.StaticInit;

@StaticInit
public class WrappedPlayerInventoryContainer extends BaseContainer {

    public static final String NAME = "pillar";

    static {
        Registries.registerContainer(NAME, WrappedPlayerInventoryContainer::new);
    }

    private final PlayerInvWrapper targetInventory;
    private String localUserName;

    public WrappedPlayerInventoryContainer(int windowId, PlayerInventory playerInventory, TileEntity tileEntity, PlayerInvWrapper targetInventory) {
        super(Registries.getContainerType(NAME), windowId, playerInventory, ContainerConnectionType.TILE_ENTITY, tileEntity, 0);
        this.targetInventory = targetInventory;
        init();
    }

    public WrappedPlayerInventoryContainer(int windowId, PlayerInventory playerInventory, PacketBuffer data) {
        super(Registries.getContainerType(NAME), windowId, playerInventory, data);
        targetInventory = data.readBoolean() ? new PlayerInvWrapper(player.inventory, data) : new PlayerInvWrapper(data);
        init();
    }

    protected void init() {
        addPlayerInventory(PLAYER_INVENTORY_FIRST_SLOT_X + 1, PLAYER_INVENTORY_PART_Y + PLAYER_INVENTORY_FIRST_SLOT_Y + 1);
        for (int x = 0; x < 9; ++x)
            addSlot(new InvWrapperSlot(targetInventory, x, BELT_X + 1 + x * SLOT_SIZE_X, BELT_Y + 1));
        for (int y = 0; y < 3; ++y)
            for (int x = 0; x < 9; ++x)
                addSlot(new InvWrapperSlot(targetInventory, x + 9 * (y + 1), MAIN_INVENTORY_X + 1 + x * SLOT_SIZE_X, MAIN_INVENTORY_Y + 1 + y * SLOT_SIZE_Y));
        for (int x = 0; x < 4; ++x)
            addSlot(new InvWrapperSlot(targetInventory, 36 + x, ARMOR_SLOTS_X + 1 + x * SLOT_SIZE_X, SPECIAL_SLOTS_Y + 1));
        addSlot(new InvWrapperSlot(targetInventory, 40, OFF_HAND_SLOT_X + 1, SPECIAL_SLOTS_Y + 1));
        trackIntArray(targetInventory.rightsAsIntArray());
        trackString(new StringReferenceHolder() {
            @Override
            public String get() {
                return localUserName;
            }

            @Override
            public void set(String value) {
                localUserName = value;
            }
        });
        if (isClient)
            localUserName = "Unavailable";
        else
            localUserName = targetInventory.getPlayerInventory().player.getName().getString();
    }

    public static PacketBuffer writeParameters(PacketBuffer buffer, TileEntity te, boolean isUserTheTarget, PlayerInvWrapper targetInventory) {
        BaseContainer.writeBaseParameters(buffer, ContainerConnectionType.TILE_ENTITY, te, 0);
        buffer.writeBoolean(isUserTheTarget);
        targetInventory.sizeAndRightsToBuffer(buffer);
        return buffer;
    }

    public String getLocalUserName() { return localUserName; }
}
