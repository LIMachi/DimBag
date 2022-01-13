package com.limachi.dimensional_bags.common.bagDimensionOnly.bagSlot;

import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.references.Registries;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.InventoryData;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.SettingsData;
import com.limachi.dimensional_bags.lib.common.inventory.ISimpleItemHandlerSerializable;
import com.limachi.dimensional_bags.common.references.GUIs;
import com.limachi.dimensional_bags.lib.common.container.BaseContainer;
import com.limachi.dimensional_bags.lib.common.container.BaseEyeContainer;
import com.limachi.dimensional_bags.lib.utils.UUIDUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

import static com.limachi.dimensional_bags.common.references.GUIs.ScreenParts.*;

@StaticInit
public class SlotContainer extends BaseEyeContainer<SlotContainer> {

    public static final String NAME = "slot";

    static {
        Registries.registerContainer(NAME, SlotContainer::new);
    }

    public static void open(PlayerEntity player, int eye, @Nullable UUID pillarId) {
        if (player instanceof ServerPlayerEntity)
            BaseContainer.open(player, new SlotContainer(((ServerPlayerEntity)player).containerCounter + 1, player.inventory, eye, pillarId));
    }

    protected SlotContainer(ContainerType<?> containerType, int windowId, PlayerInventory playerInv, PacketBuffer extraData) { super(containerType, windowId, playerInv, extraData); }
    protected SlotContainer(ContainerType<?> containerType, int windowId, PlayerInventory playerInv, int eye, @Nullable UUID pillarId) {
        super(containerType, windowId, playerInv, eye);
        loadPillarFromUUID(pillarId);
    }

    public SlotContainer(int windowId, PlayerInventory playerInv, PacketBuffer extraData) {
        super(Registries.getContainerType(NAME), windowId, playerInv, extraData);
    }

    private SlotContainer(int windowId, PlayerInventory playerInv, int eye, @Nullable UUID pillarId) {
        super(Registries.getContainerType(NAME), windowId, playerInv, eye);
        loadPillarFromUUID(pillarId);
    }

    protected ISimpleItemHandlerSerializable inv;
    protected UUID invId;
    protected int rows;
    protected int columns;

    protected void addSlots() {
        int sx = GUIs.BagScreen.calculateShiftLeft(columns);
        int sy = GUIs.BagScreen.calculateYSize(rows);
        addPlayerSlots(sx > 0 ? sx * SLOT_SIZE_X : 0, sy - PLAYER_INVENTORY_Y);
        if (inv != null) {
            sx = PART_SIZE_X + 1 + (sx < 0 ? -sx * SLOT_SIZE_X : 0);
            sy = PART_SIZE_Y * 2 + 1;
            int si = 0;
            for (int y = 0; y < rows; ++y)
                for (int x = 0; x < columns; ++x)
                    if (si < inv.getSlots())
                        this.addSlot(inv.createSlot(si++, sx + SLOT_SIZE_X * x, sy + SLOT_SIZE_Y * y));
        }
    }

    void loadPillarFromUUID(@Nullable UUID id) {
        inv = InventoryData.execute(bagId, data->data.getPillarInventory(id), null);
        invId = id;
        if (inv != null) {
            rows = (int) Math.ceil((double) inv.getSlots() / (double) 9);
            columns = (int) Math.ceil((double) inv.getSlots() / (double) rows);
        } else {
            rows = 0;
            columns = 0;
        }
        addSlots();
    }

    @Override
    public void readFromBuff(PacketBuffer buff) {
        super.readFromBuff(buff);
        loadPillarFromUUID(buff.readUUID());
    }

    @Override
    public void writeToBuff(PacketBuffer buff) {
        super.writeToBuff(buff);
        buff.writeUUID(invId == null ? UUIDUtils.NULL_UUID : invId);
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() { return invId == null ? new StringTextComponent(SettingsData.execute(bagId, SettingsData::getBagName, NAME)) : new TranslationTextComponent("container.display_name." + NAME); }
}
