package com.limachi.dim_bag.bag_modes;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.items.BagItem;
import com.limachi.dim_bag.menus.BagMenu;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.lim_lib.Configs;
import com.limachi.lim_lib.Events;
import com.limachi.lim_lib.KeyMapController;
import com.limachi.lim_lib.World;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Capture extends BaseMode {

    @Configs.Config(cmt = "list of mobs (ressource style, regex compatible), that should not be able to be captured")
    public static String[] BLACK_LIST_MOB_CAPTURE = {"minecraft:ender_dragon", "minecraft:wither"};

    public Capture() { super("Capture", false); }

    @Override //open general info screen (bag screen index 3)
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
        BagsData.runOnBag(BagItem.getBagId(stack), b->b.enter(entity, false));
        return true;
    }

    public static Pair<Component, Entity> getSelectedTarget(int id) {
        return ((Optional<Pair<Component, Entity>>)BagsData.runOnBag(id, bag->
            bag.getModeData("Capture").map(capture->{
                BlockPos at = BlockPos.of(capture.getLong("selected"));
                CompoundTag teleporter = bag.getInstalledCompound("teleport", at);
                Component label = Component.Serializer.fromJson(teleporter.getString("label"));
                if (teleporter.getBoolean("active")) {
                    List<Entity> found = World.getLevel(DimBag.BAG_DIM).getEntities((Entity)null, new AABB(at.relative(Direction.UP)), e->true); //FIXME: should apply filter here
                    if (found.size() > 0)
                        return new Pair<>(label, found.get(0));
                }
                return new Pair<>(label, null);
            }), Optional.empty())).orElse(null);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (selected && !level.isClientSide && entity instanceof Player player && Events.tick % 10 == 0) {
            Pair<Component, Entity> target = getSelectedTarget(BagItem.getBagId(stack));
            if (target != null)
                player.displayClientMessage(Component.translatable("modules.capture.selected_entity", target.getFirst(), target.getSecond() != null ? target.getSecond().getDisplayName() : Component.translatable("modules.capture.no_entity")), true);
        }
    }

    @Override
    public void scroll(Player player, int slot, int amount) {
        BagsData.runOnBag(BagItem.getBagId(player.getInventory().getItem(slot)), bag->
            bag.getModeData("Capture").ifPresent(capture->{
                long from = capture.getLong("selected");
                CompoundTag raw = bag.unsafeRawAccess();
                ListTag teleporters = raw.getCompound("modules").getList("teleport", Tag.TAG_COMPOUND);
                if (teleporters.size() > 0) {
                    int i = 0;
                    for (; i < teleporters.size(); ++i)
                        if (teleporters.getCompound(i).getLong("position") == from)
                            break;
                    i -= amount;
                    while (i < 0)
                        i += teleporters.size();
                    while (i >= teleporters.size())
                        i -= teleporters.size();
                    capture.putLong("selected", teleporters.getCompound(i).getLong("position"));
                }
            }));
    }

    @Override
    public boolean canScroll(Player player, int i) { return KeyMapController.SNEAK.getState(player); }

    @Override //FIXME: sometimes freezes the game
    public InteractionResult useOn(UseOnContext ctx) {
        if (ctx.getLevel().isClientSide) return InteractionResult.SUCCESS;
        int id = BagItem.getBagId(ctx.getItemInHand());
        Pair<Component, Entity> target = getSelectedTarget(id);
        if (target != null && target.getSecond() != null)
            World.teleportEntity(target.getSecond(), ctx.getPlayer().level().dimension(), ctx.getClickedPos().offset(ctx.getClickedFace().getNormal()));
        return InteractionResult.SUCCESS;
    }
}
