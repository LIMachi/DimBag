package com.limachi.utils;

import com.google.common.collect.ArrayListMultimap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber
public class Tick {
    public static int tick = 0;
    private static final ArrayListMultimap<Integer, Runnable> pendingTasks = ArrayListMultimap.create();
    /**
     * queue a delayed task to be run in X ticks (minimum 1 tick)
     */
    public static void delayedTask(int ticksToWait, Runnable run) { if (ticksToWait <= 0) ticksToWait = 1; pendingTasks.put(ticksToWait + tick, run); }
    /**
     * handles delayed tasks on the TickEvent server side, if you need something delayed client side, please use the same function but in the client package
     */
    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            List<Runnable> tasks = pendingTasks.get(tick);
            for (Runnable task : tasks)
                task.run();
        } else if (event.phase == TickEvent.Phase.END) {
            pendingTasks.removeAll(tick);
            ++tick;
        }
    }
}
