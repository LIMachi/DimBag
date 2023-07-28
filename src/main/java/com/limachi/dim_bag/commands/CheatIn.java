package com.limachi.dim_bag.commands;

import com.limachi.dim_bag.entities.BagEntity;
import com.limachi.dim_bag.items.BagItem;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.lim_lib.PlayerUtils;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;

public class CheatIn {
    public static int spawnBag(CommandSourceStack source, int id, BlockPos pos) throws CommandSyntaxException {
        if (id <= 0)
            id = BagsData.newBagId();
        if (id <= 0 || id > BagsData.maxBagId()) {
            source.sendFailure(Component.translatable("command.error.bag_not_exist"));
            return 0;
        }
        if (pos == null)
            BagEntity.create(source.getLevel(), source.getPlayerOrException().getOnPos().offset(0,1,0), id);
        else
            BagEntity.create(source.getLevel(), pos, id);
        return 1;
    }

    public static int giveBag(CommandSourceStack source, int id, Collection<Player> players) throws CommandSyntaxException {
        if (id <= 0)
            id = BagsData.newBagId();
        if (id <= 0 || id > BagsData.maxBagId()) {
            source.sendFailure(Component.translatable("command.error.bag_not_exist"));
            return 0;
        }
        if (players == null || players.isEmpty()) {
            PlayerUtils.giveOrDrop(source.getPlayerOrException(), BagItem.create(id));
            return 1;
        }
        else {
            for (Player p : players)
                PlayerUtils.giveOrDrop(p, BagItem.create(id));
            return players.size();
        }
    }

    public static int fillEnergy(CommandSourceStack source, int id, long amount) {
        if (BagsData.runOnBag(id, bag->bag.energyHandle().ifPresent(d->d.addBatteryModule(amount))))
            return 1;
        source.sendFailure(Component.translatable("command.error.bag_not_exist"));
        return 0;
    }

    public static int extractEnergy(CommandSourceStack source, int id, long amount) {
        if (BagsData.runOnBag(id, bag->bag.energyHandle().ifPresent(d->d.addBatteryModule(-amount))))
            return 1;
        source.sendFailure(Component.translatable("command.error.bag_not_exist"));
        return 0;
    }
}
