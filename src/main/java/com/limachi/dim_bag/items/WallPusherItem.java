package com.limachi.dim_bag.items;

import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.lim_lib.registries.annotations.RegisterItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;

public class WallPusherItem extends Item {

    @RegisterItem
    public static RegistryObject<BlockItem> R_ITEM;

    public WallPusherItem() { super(new Properties()); }

    @Override
    @Nonnull
    public InteractionResult useOn(@Nonnull UseOnContext ctx) {
        if (ctx.getPlayer() instanceof ServerPlayer player && BagsData.runOnBag(ctx.getLevel(), ctx.getClickedPos(), b->b.pushWall(ctx.getClickedPos()), false)) {
            if (!player.isCreative()) {
                ItemStack stack = ctx.getItemInHand();
                stack.shrink(1);
                player.setItemInHand(ctx.getHand(), stack);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
