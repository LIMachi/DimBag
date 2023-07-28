package com.limachi.dim_bag.commands;

import com.limachi.lim_lib.commands.CommandManager;
import com.limachi.lim_lib.commands.arguments.*;
import com.limachi.lim_lib.registries.StaticInit;

@StaticInit
public class Register {
    static {
        CommandManager.registerCmd(TpInOut.class, "enterBag", s->s.hasPermission(2), "/dim_bag enter <id> <entities?> <proxy?>", new IntArg(1), new EntitiesArg(true), new BoolArg());
        CommandManager.registerCmd(TpInOut.class, "leaveBag", s->s.hasPermission(2), "/dim_bag leave <entities?>", new EntitiesArg(true));
        CommandManager.registerCmd(CheatIn.class, "spawnBag", s->s.hasPermission(2), "/dim_bag spawn <id:0=new> <pos?>", new IntArg(0), new BlockPosArg());
        CommandManager.registerCmd(CheatIn.class, "giveBag", s->s.hasPermission(2), "/dim_bag give <id:0=new> <players?>", new IntArg(0), new PlayersArg(true));
        CommandManager.registerCmd(CheatIn.class, "fillEnergy", s->s.hasPermission(2), "/dim_bag energy <id> add <amount>", new IntArg(1), new LongArg(0));
        CommandManager.registerCmd(CheatIn.class, "extractEnergy", s->s.hasPermission(2), "/dim_bag energy <id> remove <amount>", new IntArg(1), new LongArg(0));
    }
}
