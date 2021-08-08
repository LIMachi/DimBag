package com.limachi.dimensional_bags.common.container;

import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.InventoryData;
import com.limachi.dimensional_bags.common.inventory.ISimpleItemHandlerSerializable;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.UUID;

public class PillarSettings extends BaseEyeContainer<PillarSettings> {

    public static final String NAME = "pillar_settings";

    static {
        Registries.registerContainer(NAME, PillarSettings::new);
    }

    public PillarSettings(int windowId, PlayerInventory playerInv, PacketBuffer extraData) {
        super(Registries.getContainerType(NAME), windowId, playerInv, extraData);
    }

    public PillarSettings(int windowId, PlayerInventory playerInv, int eye, @Nullable UUID pillarId) {
        super(Registries.getContainerType(NAME), windowId, playerInv, eye);
        loadPillarFromUUID(pillarId);
    }

    protected ISimpleItemHandlerSerializable inv;
    protected UUID invId;

    void loadPillarFromUUID(@Nullable UUID id) {
        inv = InventoryData.execute(eyeId, data->data.getPillarInventory(id), null);
        invId = id;
//        if (inv != null) {
//            rows = (int) Math.ceil((double) inv.getSlots() / (double) 9);
//            columns = (int) Math.ceil((double) inv.getSlots() / (double) rows);
//        } else {
//            rows = 0;
//            columns = 0;
//        }
//        addSlots();
    }

    @Override
    public ITextComponent getDisplayName() { return new TranslationTextComponent("container.display_name." + NAME); }
}
