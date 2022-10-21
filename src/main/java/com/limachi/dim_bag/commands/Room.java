package com.limachi.dim_bag.commands;

import com.limachi.dim_bag.Constants;
import com.limachi.dim_bag.rooms.Rooms;
import com.limachi.dim_bag.rooms.SubRoom;
import com.limachi.lim_lib.Sides;
import com.limachi.lim_lib.World;
import com.limachi.lim_lib.commands.CommandManager;
import com.limachi.lim_lib.commands.arguments.IntArg;
import com.limachi.lim_lib.commands.arguments.LiteralArg;
import com.limachi.lim_lib.registries.StaticInit;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;

@StaticInit
public class Room {
    static {
        CommandManager.registerCmd(Room.class, "build", s->s.hasPermission(2), "/bag room build <id> <sub_room>", new IntArg(1), new IntArg(0));
        CommandManager.registerCmd(Room.class, "enter", "/bag room <enter> <id> <sub_room?>", new LiteralArg(false), new IntArg(1), new IntArg(0));
        CommandManager.registerCmd(Room.class, "enter", "/bag room <admin> enter <id> <sub_room?>", new LiteralArg(false).requirePerm(2), new IntArg(1), new IntArg(0));
        CommandManager.registerCmd(Room.class, "module", "/bag room test module <id> <qty?>", new IntArg(1), new IntArg(1));
    }

    public static int build(CommandContext<CommandSourceStack> ctx, int id, int subRoom) {
        Rooms.buildRoom(World.getLevel(Constants.BAG_DIM), new SubRoom(id, 5, subRoom, new Vec3i(0, 0, 0)), 0, 0, 0);
        return 1;
    }

    public static int enter(CommandContext<CommandSourceStack> ctx, String t, int id, int subRoom) {
        Entity e = ctx.getSource().getEntity();
        if (!(e instanceof Player)) return 0;
        if ("admin".equals(t) || Rooms.validateRoomOwnership((Player)e, id, subRoom))
            World.teleportEntity(e, Constants.BAG_DIM, Rooms.getRoomCenter(id, subRoom));
        return 1;
    }

    public static int module(CommandContext<CommandSourceStack> ctx, int id, Integer qty) {
        if (qty == null) qty = 1;
        if (!Sides.isLogicalClient()) {
            for (int i = 0; i < qty; ++i) {
                BlockPos pos = Rooms.getNewModulePlacement(id);
                if (pos != null)
                    World.getLevel(Constants.BAG_DIM).setBlock(pos, Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
            }
        }
        return 0;
    }
}
