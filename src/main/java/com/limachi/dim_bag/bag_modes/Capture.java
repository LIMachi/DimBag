package com.limachi.dim_bag.bag_modes;

import com.limachi.dim_bag.items.BagItem;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.lim_lib.Configs;
import com.limachi.lim_lib.Events;
import com.limachi.lim_lib.KeyMapController;
import com.limachi.lim_lib.World;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;

public class Capture extends BaseMode {

    @Configs.Config(cmt = "list of mobs (ressource style, regex compatible), that should not be able to be captured")
    public static String[] BLACK_LIST_MOB_CAPTURE = {"minecraft:ender_dragon", "minecraft:wither"};

    public Capture() { super("Capture", true); }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (player.level().isClientSide) return true;
        String entityRes = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString();
        if (Arrays.stream(BLACK_LIST_MOB_CAPTURE).anyMatch(entityRes::matches))
            return true;
        BagsData.runOnBag(BagItem.getBagId(stack), b->b.enter(entity, false));
        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (selected && !level.isClientSide && entity instanceof Player player && Events.tick % 10 == 0) {
//            Teleporters t = Teleporters.getInstance(BagItem.getBagId(stack));
//            if (t != null) {
//                Entity target = t.getSelectedTeleporterTarget();
//                player.displayClientMessage(Component.translatable("modules.capture.selected_entity", t.getSelectedTeleporterLabel().getString(), (target != null ? target.getDisplayName() : Component.translatable("modules.capture.no_entity")).getString()), true);
//            }
        }
    }

    @Override
    public void scroll(Player player, int i, int i1) {
//        Teleporters t = Teleporters.getInstance(BagItem.getBagId(player.getInventory().getItem(i)));
//        if (t != null)
//            t.select(i1);
    }

    @Override
    public boolean canScroll(Player player, int i) {
        return KeyMapController.SNEAK.getState(player);
    }

    @Override //FIXME: sometimes freezes the game
    public InteractionResult useOn(UseOnContext ctx) {
        if (ctx.getPlayer() != null) {
//            Teleporters t = Teleporters.getInstance(BagItem.getBagId(ctx.getItemInHand()));
//            if (t != null) {
//                World.teleportEntity(t.getSelectedTeleporterTarget(), ctx.getPlayer().level().dimension(), ctx.getClickedPos().offset(ctx.getClickedFace().getNormal()));
//                return InteractionResult.SUCCESS;
//            }
        }
        return InteractionResult.PASS;
    }
}
