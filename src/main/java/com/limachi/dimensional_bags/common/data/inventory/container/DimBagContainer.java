package com.limachi.dimensional_bags.common.data.inventory.container;

import com.limachi.dimensional_bags.common.data.DimBagData;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.init.Registries;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nullable;

public class DimBagContainer extends Container {

    private final EyeData data;
    private final PlayerInventory inventory;

    protected DimBagContainer(@Nullable ContainerType<? extends DimBagContainer> type, int id, PlayerInventory inventory, EyeData data) {
        super(type, id);
        this.data = data;
        this.inventory = inventory;
    }

    public DimBagContainer(int id, PlayerInventory inventory, PacketBuffer buff) {
        super(Registries.BAG_CONTAINER.get(), id);
        this.inventory = inventory;
        int eye = buff.readInt();
        this.data = DimBagData.get(inventory.player.getServer()).getEyeData(eye);
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) { //FIXME: for now, set it to true, will have to implement logic later
        return true;
    }
}
