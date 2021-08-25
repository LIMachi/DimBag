package com.limachi.dimensional_bags.utils;

import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SChangeBlockPacket;
import net.minecraft.network.play.server.SEntityEquipmentPacket;
import net.minecraft.network.play.server.SSetExperiencePacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class SyncUtils {
    public static void resyncPlayerHands(@Nonnull ServerPlayerEntity player, boolean main, boolean off) {
        List<Pair<EquipmentSlotType, ItemStack>> hands = new ArrayList<>();
        if (main)
            hands.add(new Pair<>(EquipmentSlotType.MAINHAND, player.getMainHandItem()));
        if (off)
            hands.add(new Pair<>(EquipmentSlotType.OFFHAND, player.getOffhandItem()));
        player.connection.send(new SEntityEquipmentPacket(player.getId(), hands));
    }

    public static void resyncBlock(@Nullable ServerPlayerEntity player, @Nonnull ServerWorld world, @Nonnull BlockPos pos) {
        if (player != null && !player.level.dimension().equals(world.dimension())) return; //we will not send a resync to a player that isn't in the dimension where the block was desync
        SChangeBlockPacket packet = new SChangeBlockPacket(pos, world.getBlockState(pos));
        if (player != null)
            player.connection.send(packet);
        else
            for (ServerPlayerEntity p: world.getPlayers(p->!(p instanceof FakePlayer)))
                p.connection.send(packet);
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
    }

    public static void resyncXP(@Nonnull ServerPlayerEntity player, XPSnapShot xp) {
        player.connection.send(new SSetExperiencePacket(xp.xp, xp.total, xp.lvl));
    }

//    public static void resyncWindowSlot(@Nonnull ServerPlayerEntity player, int slot) {
//        player.connection.sendPacket(new SSetSlotPacket());
//    }
}
