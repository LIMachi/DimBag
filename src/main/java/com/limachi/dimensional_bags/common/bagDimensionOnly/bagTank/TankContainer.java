package com.limachi.dimensional_bags.common.bagDimensionOnly.bagTank;

import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.references.Registries;
import com.limachi.dimensional_bags.lib.common.container.BaseContainer;
import com.limachi.dimensional_bags.lib.common.container.BaseEyeContainer;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.TankData;
import com.limachi.dimensional_bags.lib.common.fluids.ISimpleFluidHandlerSerializable;
import com.limachi.dimensional_bags.common.references.GUIs;
import com.limachi.dimensional_bags.lib.utils.UUIDUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

import static com.limachi.dimensional_bags.common.references.GUIs.ScreenParts.*;

@StaticInit
public class TankContainer extends BaseEyeContainer<TankContainer> {

    public static final String NAME = "tank";

    static {
        Registries.registerContainer(NAME, TankContainer::new);
    }

    public static void open(PlayerEntity player, int eye, @Nullable UUID fountainId) {
        if (player instanceof ServerPlayerEntity)
            BaseContainer.open(player, new TankContainer(((ServerPlayerEntity)player).containerCounter + 1, player.inventory, eye, fountainId));
    }

    public TankContainer(int windowId, PlayerInventory playerInv, PacketBuffer extraData) {
        super(Registries.getContainerType(NAME), windowId, playerInv, extraData);
    }

    private TankContainer(int windowId, PlayerInventory playerInv, int eye, @Nullable UUID fountainId) {
        super(Registries.getContainerType(NAME), windowId, playerInv, eye);
        loadFountainFromUUID(fountainId);
    }

    protected ISimpleFluidHandlerSerializable inv;
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
                    if (si < inv.getTanks())
                        this.addSlot(inv.createSlot(si++, sx + SLOT_SIZE_X * x, sy + SLOT_SIZE_Y * y));
        }
    }

    void loadFountainFromUUID(@Nullable UUID id) {
        inv = TankData.execute(bagId, data->data.getFountainTank(id), null);
        invId = id;
        if (inv != null) {
            rows = (int) Math.ceil((double) inv.getTanks() / (double) 9);
            columns = (int) Math.ceil((double) inv.getTanks() / (double) rows);
        } else {
            rows = 0;
            columns = 0;
        }
        addSlots();
    }

    @Override
    public void readFromBuff(PacketBuffer buff) {
        super.readFromBuff(buff);
        loadFountainFromUUID(buff.readUUID());
    }

    @Override
    public void writeToBuff(PacketBuffer buff) {
        super.writeToBuff(buff);
        buff.writeUUID(invId == null ? UUIDUtils.NULL_UUID : invId);
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() { return new TranslationTextComponent("container.display_name." + NAME); }
}
