package com.limachi.dim_bag.commands;

import com.limachi.dim_bag.entities.BagEntity;
import com.limachi.dim_bag.items.BagItem;
import com.limachi.lim_lib.commands.CommandManager;
import com.limachi.lim_lib.commands.arguments.BlockPosArg;
import com.limachi.lim_lib.commands.arguments.IntArg;
import com.limachi.lim_lib.registries.StaticInit;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@StaticInit
public class Bag {

    public static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.summon.failed"));
    public static final SimpleCommandExceptionType ERROR_DUPLICATE_UUID = new SimpleCommandExceptionType(Component.translatable("commands.summon.failed.uuid"));
    public static final SimpleCommandExceptionType INVALID_POSITION = new SimpleCommandExceptionType(Component.translatable("commands.summon.invalidPosition"));

    static {
        CommandManager.registerCmd(Bag.class, "getBag", s->s.hasPermission(2), "/bag get <id>", new IntArg(1));
        CommandManager.registerCmd(Bag.class, "spawnBag", s->s.hasPermission(2), "/bag spawn <id> <pos>", new IntArg(1), new BlockPosArg());
    }

    public static int getBag(CommandContext<CommandSourceStack> ctx, int id) {
        if (ctx.getSource().getEntity() instanceof Player player) {
            ItemStack stack = BagItem.bag(id);
            if (!player.getInventory().add(stack)) {
                ItemEntity e = player.drop(stack, false);
                if (e != null) {
                    e.setNoPickUpDelay();
                    e.setOwner(player.getUUID());
                }
            }
            return 1;
        }
        return 0;
    }

    public static int spawnBag(CommandContext<CommandSourceStack> ctx, int id, BlockPos pos) throws CommandSyntaxException {
        if (!Level.isInSpawnableBounds(pos))
            throw INVALID_POSITION.create();
        else {
            CompoundTag tag = new CompoundTag();
            tag.putString("id", "dim_bag:bag_entity");
            ServerLevel serverlevel = ctx.getSource().getLevel();
            Entity entity = EntityType.loadEntityRecursive(tag, serverlevel, ne -> {
                ne.moveTo(pos.getX(), pos.getY(), pos.getZ(), ne.getYRot(), ne.getXRot());
                return ne;
            });
            if (entity == null) {
                throw ERROR_FAILED.create();
            } else {
                if (!serverlevel.tryAddFreshEntityWithPassengers(entity)) {
                    throw ERROR_DUPLICATE_UUID.create();
                } else {
                    entity.getPersistentData().put(BagEntity.ITEM_KEY, BagItem.bag(id).serializeNBT());
                    ctx.getSource().sendSuccess(Component.translatable("commands.summon.success", entity.getDisplayName()), true);
                    return 1;
                }
            }
        }
    }
}
