package com.limachi.utils.messages;

import com.limachi.utils.ModBase;
import com.limachi.utils.Network;
import com.limachi.utils.scrollSystem.IScrollItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

@Network.RegisterMessage(value = 2, modId = ModBase.COMMON_ID)
public class ScrolledItem extends Network.Message {
    int slot;
    int delta;

    public ScrolledItem(int slot, int delta) {
        this.slot = slot;
        this.delta = delta;
    }

    public ScrolledItem(FriendlyByteBuf buffer) {
        slot = buffer.readInt();
        delta = buffer.readInt();
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(slot);
        buffer.writeInt(delta);
    }

    /**
     * should only work client -> server
     */
    @Override
    public void serverWork(Player player) {
        Item item = player.getInventory().getItem(slot).getItem();
        if (item instanceof IScrollItem)
            ((IScrollItem)item).scroll(player, slot, delta);
    }
}
