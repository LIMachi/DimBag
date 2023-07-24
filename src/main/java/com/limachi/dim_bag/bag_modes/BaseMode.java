package com.limachi.dim_bag.bag_modes;

import com.limachi.dim_bag.bag_modules.ParadoxModule;
import com.limachi.dim_bag.items.BagItem;
import com.limachi.dim_bag.menus.BagMenu;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.lim_lib.KeyMapController;
import com.limachi.lim_lib.scrollSystem.IScrollItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public abstract class BaseMode implements IScrollItem {
    public final boolean autoInstall;
    public final String name;

    public BaseMode(String name, boolean autoInstall) {
        this.name = name;
        this.autoInstall = autoInstall;
    }

    public CompoundTag initialData() { return new CompoundTag(); }

    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {}

    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) { return false; }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        BagMenu.open(player, BagItem.getBagId(player.getItemInHand(hand)), 0);
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    public boolean onLeftClickBlock(ItemStack stack, Player player, BlockPos pos) { return false; }
    public boolean onLeftClickEmpty(ItemStack stack, Player player) { return false; }

    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) { return false; }

    public InteractionResult useOn(UseOnContext ctx) {
        ItemStack bag = ctx.getItemInHand();
        Player player = ctx.getPlayer();
        if (player != null && bag.getItem() instanceof BagItem) {
            int id = BagItem.getBagId(bag);
            if (id > 0) {
                BlockPos at = ctx.getClickedPos().relative(ctx.getClickedFace());
                if (KeyMapController.SNEAK.getState(player)) {
                    if (BagsData.runOnBag(bag, b->{
                        if (b.getModeData("Settings").map(t->t.getBoolean("quick_enter")).orElse(false))
                        {
                            if (!b.isPresent(ParadoxModule.PARADOX_KEY))
                                BagItem.unequipBags(player, id, player.level(), at);
                            b.enter(player, false);
                            return true;
                        }
                        return false;
                    }, false))
                        return InteractionResult.SUCCESS;
                    if (at.distSqr(player.blockPosition()) <= 9) {
                        BagItem.unequipBags(player, id, player.level(), at);
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
