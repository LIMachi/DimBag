package com.limachi.dimensional_bags.lib.common.network.packets;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.lib.common.network.PacketHandler;
import com.limachi.dimensional_bags.lib.utils.NBTUtils;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class PlayerPersistentDataAction extends PacketHandler.Message {

    public enum Actions {
        OVERRIDE,
        MERGE,
        REMOVE_ENTRIES
    }

    private CompoundNBT nbt;
    private Actions action;

    public PlayerPersistentDataAction(PacketBuffer buffer) {
        this.action = Actions.values()[buffer.readByte()];
        this.nbt = buffer.readNbt();
    }

    public PlayerPersistentDataAction(Actions action, CompoundNBT nbt) {
        this.action = action;
        this.nbt = nbt;
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeByte(action.ordinal());
        buffer.writeNbt(nbt);
    }

    private void runAction(CompoundNBT target) {
        switch (action) {
            case OVERRIDE: NBTUtils.clear(target).merge(nbt); break;
            case MERGE: target.merge(nbt); break;
            case REMOVE_ENTRIES:
                for (String k : nbt.getAllKeys())
                    target.remove(k);
                break;
        }
    }

    @Override
    public void clientWork() {
        ClientPlayerEntity player = (ClientPlayerEntity) DimBag.getPlayer();
        if (player != null) runAction(player.getPersistentData());
    }

    @Override
    public void serverWork(ServerPlayerEntity player) {
        if (player != null) runAction(player.getPersistentData());
    }
}
