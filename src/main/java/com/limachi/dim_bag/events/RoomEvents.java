package com.limachi.dim_bag.events;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.lim_lib.Configs;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityMobGriefingEvent;
import net.minecraftforge.event.entity.living.LivingDestroyBlockEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;

@Mod.EventBusSubscriber(modid = DimBag.MOD_ID)
public class RoomEvents {
    @Configs.Config(cmt = "list of mobs (ressource style, regex compatible), that should not be able to grief (warning, the defaults are there because some vanilla mobs might be able to break walls)")
    public static String[] BLACK_LIST_MOB_GRIEF = {"minecraft:enderman", "minecraft:silverfish"};

    @SubscribeEvent
    public static void mobGrief(EntityMobGriefingEvent event) {
        if (event.getEntity().level().dimension() == DimBag.BAG_DIM) {
            String entityRes = ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType()).toString();
            if (Arrays.stream(BLACK_LIST_MOB_GRIEF).anyMatch(entityRes::matches))
                event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void howTFDidYouPutAWitherOrDragonInYourBagAndThoughtItWasAGoodIdea(LivingDestroyBlockEvent event) {
        if (BagsData.isWall(event.getEntity().level(), event.getPos()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void wallCannotBeMined(PlayerEvent.BreakSpeed event) {
        if (event.getEntity().level() instanceof ServerLevel level && event.getPosition().map(p->BagsData.isWall(level, p)).orElse(false))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void wallCannotBeBroken(BlockEvent.BreakEvent event) {
        if (!event.getPlayer().isCreative() && BagsData.isWall((Level)event.getLevel(), event.getPos()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void wallCannotBeExploded(ExplosionEvent.Detonate event) {
        if (event.getLevel().dimension() == DimBag.BAG_DIM) {
            Vec3 pos = event.getExplosion().getPosition();
            BagsData.runOnBag(event.getLevel(), new BlockPos((int)pos.x, (int)pos.y, (int)pos.z), bag->event.getAffectedBlocks().removeIf(bag::isWall));
        }
    }
}
