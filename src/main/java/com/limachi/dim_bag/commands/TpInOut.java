package com.limachi.dim_bag.commands;

import com.limachi.dim_bag.save_datas.BagsData;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.Collection;

public class TpInOut {
    public static int leaveBag(CommandSourceStack source, Collection<Entity> entities) throws CommandSyntaxException {
        if (entities == null) {
            entities = new ArrayList<>();
            entities.add(source.getEntityOrException());
        }
        final int[] count = {0};
        for (Entity entity : entities)
            BagsData.runOnBag(entity.level(), entity.blockPosition(), b-> count[0] += b.leave(entity) != null ? 1 : 0);
        return count[0];
    }

    public static int enterBag(CommandSourceStack source, int id, Collection<Entity> entities, boolean proxy) throws CommandSyntaxException {
        int res = BagsData.runOnBag(id, b->{
            if (entities == null || entities.isEmpty())
                return source.getPlayer() != null ? b.enter(source.getPlayer(), proxy) != null ? 1 : 0 : 0;
            else
                return (int)entities.stream().filter(e->b.enter(e, proxy) != null).count();
        }, -1);
        if (res == -1) {
            source.sendFailure(Component.translatable("command.error.bag_not_exist"));
            return 0;
        }
        return res;
    }
}
