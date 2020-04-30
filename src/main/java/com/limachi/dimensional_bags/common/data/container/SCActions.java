package com.limachi.dimensional_bags.common.data.container;

import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.init.Registries;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;

public class SCActions extends Container { //virtually empty container, only used to sync data with the server

    private final EyeData data;

    public SCActions(int id, EyeData data) {
        super(Registries.SC_ACTIONS.get(), id);
        this.data = data;
    }

    public SCActions(int id, PlayerInventory _, PacketBuffer buff) {
        this(id, new EyeData(buff));
    }

    public EyeData getData() {
        return this.data;
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return true;
    }
}
