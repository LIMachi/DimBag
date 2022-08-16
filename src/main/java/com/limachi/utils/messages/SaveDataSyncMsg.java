package com.limachi.utils.messages;

import com.limachi.utils.ModBase;
import com.limachi.utils.Network;
import com.limachi.utils.SaveData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

@Network.RegisterMessage(value = 4, modId = ModBase.COMMON_ID)
public class SaveDataSyncMsg extends Network.Message {
    public final String name;
    public final boolean isDiff;
    public final CompoundTag nbt;

    public SaveDataSyncMsg(String name, boolean isDiff, CompoundTag nbt) {
        this.name = name;
        this.isDiff = isDiff;
        this.nbt = nbt;
    }

    public SaveDataSyncMsg(FriendlyByteBuf buffer) {
        name = buffer.readUtf();
        isDiff = buffer.readBoolean();
        nbt = buffer.readNbt();
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUtf(name);
        buffer.writeBoolean(isDiff);
        buffer.writeNbt(nbt);
    }

    @Override
    public void clientWork(Player player) { SaveData.clientUpdate(name, nbt, isDiff); }

    @Override
    public void serverWork(Player player) { SaveData.serverUpdate(player, name, nbt, isDiff); }
}
