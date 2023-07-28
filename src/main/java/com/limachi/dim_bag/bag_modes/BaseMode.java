package com.limachi.dim_bag.bag_modes;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.bag_data.BagInstance;
import com.limachi.dim_bag.bag_modules.ParadoxModule;
import com.limachi.dim_bag.items.BagItem;
import com.limachi.dim_bag.menus.BagMenu;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.lim_lib.KeyMapController;
import com.limachi.lim_lib.scrollSystem.IScrollItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public abstract class BaseMode implements IScrollItem {

    public final String name;
    @Nullable
    public final String requiredModule;

    public BaseMode(String name, @Nullable String requiredModule) {
        this.name = name;
        this.requiredModule = requiredModule;
    }

    public boolean canDisable() { return !name.equals(ModesRegistry.DEFAULT.name()); }

    public CompoundTag initialData(BagInstance bag) { return new CompoundTag(); }

    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {}

    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) { return false; }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (KeyMapController.SNEAK.getState(player)) {
            int bag = BagItem.getBagId(player.getItemInHand(hand));
            if (BagsData.runOnBag(bag, b->{
                if (b.getModeData(SettingsMode.NAME).getBoolean("quick_enter") && !(player.level().dimension().equals(DimBag.BAG_DIM) && b.isInRoom(player.blockPosition()))) {
                    if (!b.isModulePresent(ParadoxModule.PARADOX_KEY))
                        BagItem.unequipBags(player, b.bagId(), player.level(), player.blockPosition(), false);
                    b.enter(player, false);
                    return true;
                }
                return false;
            }, false))
                return InteractionResultHolder.success(player.getItemInHand(hand));
            if (bag > 0) {
                BagItem.unequipBags(player, bag, player.level(), player.blockPosition(), true);
                return InteractionResultHolder.success(player.getItemInHand(hand));
            }
        }
        BagMenu.open(player, BagItem.getBagId(player.getItemInHand(hand)), 0);
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    public boolean onLeftClickBlock(ItemStack stack, Player player, BlockPos pos) { return false; }
    public boolean onLeftClickEmpty(ItemStack stack, Player player) { return false; }

    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) { return false; }

    public InteractionResult useOn(UseOnContext ctx) {
        ItemStack bag = ctx.getItemInHand();
        Player player = ctx.getPlayer();
        if (player instanceof ServerPlayer && bag.getItem() instanceof BagItem) {
            int id = BagItem.getBagId(bag);
            if (id > 0) {
                BlockPos at = ctx.getClickedPos().relative(ctx.getClickedFace());
                if (KeyMapController.SNEAK.getState(player)) {
                    if (BagsData.runOnBag(bag, b->{
                        if (b.getModeData(SettingsMode.NAME).getBoolean("quick_enter") && !(player.level().dimension().equals(DimBag.BAG_DIM) && b.isInRoom(player.blockPosition()))) {
                            if (!b.isModulePresent(ParadoxModule.PARADOX_KEY))
                                BagItem.unequipBags(player, id, player.level(), at, false);
                            b.enter(player, false);
                            return true;
                        }
                        return false;
                    }, false))
                        return InteractionResult.SUCCESS;
                    if (at.distSqr(player.blockPosition()) <= 9) {
                        BagItem.unequipBags(player, id, player.level(), at, true);
                        return InteractionResult.SUCCESS;
                    }
                }
                BagMenu.open(player, BagItem.getBagId(player.getItemInHand(ctx.getHand())), 0);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void scroll(Player player, int i, int i1) {}

    @Override
    public void scrollFeedBack(Player player, int i, int i1) {}

    @Override
    public boolean canScroll(Player player, int i) { return false; }
}
