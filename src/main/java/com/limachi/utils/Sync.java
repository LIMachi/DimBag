package com.limachi.utils;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Sync {
    public static void resyncPlayerHands(@Nonnull ServerPlayer player, boolean main, boolean off) {
        List<Pair<EquipmentSlot, ItemStack>> hands = new ArrayList<>();
        if (main)
            hands.add(new Pair<>(EquipmentSlot.MAINHAND, player.getMainHandItem()));
        if (off)
            hands.add(new Pair<>(EquipmentSlot.OFFHAND, player.getOffhandItem()));
        player.connection.send(new ClientboundSetEquipmentPacket(player.getId(), hands));
    }

    public static void resyncPlayerSlot(@Nonnull ServerPlayer player, int slot) {
        if (slot >= 0 && slot < PlayerUtils.MAX_SLOT)
            player.connection.send(new ClientboundContainerSetSlotPacket(player.containerMenu.containerId, player.containerMenu.incrementStateId(), slot, player.containerMenu.getSlot(slot).getItem()));
    }

    public static class XPSnapShot {
        public final float xp;
        public final int lvl;
        public final int total;

        public static final XPSnapShot ZERO = new XPSnapShot(0, 0, 0);

        public XPSnapShot(float xp, int lvl, int total) {
            this.xp = xp;
            this.lvl = lvl;
            this.total = total;
        }

        public XPSnapShot(Player player) {
            xp = player.experienceProgress;
            lvl = player.experienceLevel;
            total = player.totalExperience;
        }
    }

    public static void resyncXP(@Nonnull ServerPlayer player, XPSnapShot xp) {
        player.connection.send(new ClientboundSetExperiencePacket(xp.xp, xp.total, xp.lvl));
    }
}
