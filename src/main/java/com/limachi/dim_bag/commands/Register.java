package com.limachi.dim_bag.commands;

import com.limachi.lim_lib.commands.CommandManager;
import com.limachi.lim_lib.commands.arguments.*;
import com.limachi.lim_lib.registries.StaticInit;

@StaticInit
public class Register {
    static {
        CommandManager.registerCmd(TpInOut.class, "enterBag", s->s.hasPermission(2), "/dim_bag enter <id> <entities?> <proxy?>", new IntArg(1), new EntitiesArg(true), new BoolArg());
        CommandManager.registerCmd(TpInOut.class, "leaveBag", s->s.hasPermission(2), "/dim_bag leave <entities?>", new EntitiesArg(true));
        CommandManager.registerCmd(CheatIn.class, "spawnBag", s->s.hasPermission(2), "/dim_bag spawn <id?> <pos?>", new IntArg(1), new BlockPosArg());
        CommandManager.registerCmd(CheatIn.class, "giveBag", s->s.hasPermission(2), "/dim_bag give <id?> <players?>", new IntArg(1), new PlayersArg(true));
    }
}
