package com.limachi.dim_bag.moduleSystem;

import com.limachi.dim_bag.items.BagItem;
import com.limachi.dim_bag.rooms.Rooms;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.VanillaGameEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.level.SaplingGrowTreeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ModuleEvents {
    @SubscribeEvent
    public static void entityEventForward(EntityEvent event) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            int id = BagItem.getBag(livingEntity, 0, true, false);
            if (id == 0) return;
            ModuleManager mm = ModuleManager.getInstance(id);
            if (mm == null) return;
            mm.event(event);
        }
    }

    @SubscribeEvent
    public static void blockEventForward(BlockEvent event) {
        int id = Rooms.getbagId((Level)event.getLevel(), event.getPos(), false);
        if (id == 0) return;
        ModuleManager mm = ModuleManager.getInstance(id);
        if (mm == null) return;
        mm.event(event);
    }

    @SubscribeEvent
    public static void explosionEventForward(ExplosionEvent event) {
        int id = Rooms.getbagId(event.getLevel(), new BlockPos(event.getExplosion().getPosition()), false);
        if (id == 0) return;
        ModuleManager mm = ModuleManager.getInstance(id);
        if (mm == null) return;
        mm.event(event);
    }

    @SubscribeEvent
    public static void saplingGrowEventForward(SaplingGrowTreeEvent event) {
        int id = Rooms.getbagId((Level)event.getLevel(), event.getPos(), false);
        if (id == 0) return;
        ModuleManager mm = ModuleManager.getInstance(id);
        if (mm == null) return;
        mm.event(event);
    }

    @SubscribeEvent
    public static void vanillaEventForward(VanillaGameEvent event) {
        int id = Rooms.getbagId(event.getLevel(), new BlockPos(event.getEventPosition()), false);
        if (id == 0) return;
        ModuleManager mm = ModuleManager.getInstance(id);
        if (mm == null) return;
        mm.event(event);
    }
}
