package com.limachi.dim_bag.bag_modes;

import com.limachi.dim_bag.bag_data.BagInstance;
import com.limachi.dim_bag.bag_modules.TeleportModule;
import com.limachi.dim_bag.items.BagItem;
import com.limachi.dim_bag.menus.BagMenu;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.lim_lib.Configs;
import com.limachi.lim_lib.Events;
import com.limachi.lim_lib.KeyMapController;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;

public class CaptureMode extends BaseMode {

    public static final String NAME = "Capture";

    @Configs.Config(cmt = "list of mobs (ressource style, regex compatible), that should not be able to be captured, by default the Vanilla bosses are prevented (but feel free to enable them :P)")
    public static String[] BLACK_LIST_MOB_CAPTURE = {"minecraft:ender_dragon", "minecraft:wither"};

    public CaptureMode() { super(NAME, TeleportModule.NAME); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        BagMenu.open(player, BagItem.getBagId(player.getItemInHand(hand)), 3);
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (player.level().isClientSide) return true;
        String entityRes = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString();
        if (Arrays.stream(BLACK_LIST_MOB_CAPTURE).anyMatch(entityRes::matches))
            return true;
        BagsData.runOnBag(stack, b->b.enter(entity, false));
        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (selected && !level.isClientSide && entity instanceof Player player && Events.tick % 10 == 0) {
            TeleportModule.getSelectedTeleporterAndTarget(BagItem.getBagId(stack)).ifPresent(p->
                player.displayClientMessage(Component.translatable("modules.capture.selected_entity", p.getFirst(), p.getSecond().map(Entity::getDisplayName).orElse(Component.translatable("modules.capture.no_entity"))), true));
        }
    }

    @Override
    public void scroll(Player player, int slot, int amount) {
        BagsData.runOnBag(player.getInventory().getItem(slot), bag-> {
            CompoundTag captureMode = bag.getModeData(NAME);
            long from = captureMode.getLong("selected");
            CompoundTag raw = bag.unsafeRawAccess();
            ListTag teleporters = raw.getCompound(BagInstance.MODULE_STORAGE).getList("teleport", Tag.TAG_COMPOUND);
            if (teleporters.size() > 0) {
                int i = 0;
                for (; i < teleporters.size(); ++i)
                    if (teleporters.getCompound(i).getLong(BagInstance.POSITION) == from)
                        break;
                i -= amount;
                while (i < 0)
                    i += teleporters.size();
                while (i >= teleporters.size())
                    i -= teleporters.size();
                captureMode.putLong("selected", teleporters.getCompound(i).getLong(BagInstance.POSITION));
            }
        });
    }

    @Override
    public boolean canScroll(Player player, int i) { return KeyMapController.SNEAK.getState(player); }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (ctx.getLevel().isClientSide) return InteractionResult.SUCCESS;
        int id = BagItem.getBagId(ctx.getItemInHand());
        TeleportModule.teleportTargetTo(id, ctx.getLevel().dimension(), ctx.getClickedPos().offset(ctx.getClickedFace().getNormal()));
        return InteractionResult.SUCCESS;
    }
}
