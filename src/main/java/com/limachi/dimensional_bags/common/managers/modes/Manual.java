package com.limachi.dimensional_bags.common.managers.modes;

import com.limachi.dimensional_bags.client.render.screen.ManualScreen;
import com.limachi.dimensional_bags.common.managers.Mode;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;

public class Manual extends Mode {

    @ObjectHolder("patchouli:guide_book")
    public static final Item manual_item = null;
    public static ItemStack manual = null;

    public static final String ID = "Manual";

    public Manual() { super(ID, false, true); }

    @Override
    public ActionResultType onItemRightClick(int eyeId, World world, PlayerEntity player) {
        if (manual_item != null) {
            if (manual == null) {
                manual = new ItemStack(manual_item);
                CompoundNBT t = new CompoundNBT();
                t.putString("patchouli:book", "dim_bag:dim_bag_manual");
                manual.setTag(t);
            }
            ItemStack p = player.getItemInHand(Hand.MAIN_HAND);
            player.setItemInHand(Hand.MAIN_HAND, manual);
            manual.use(world, player, Hand.MAIN_HAND);
            player.setItemInHand(Hand.MAIN_HAND, p);
        }
        else
            ManualScreen.open();
        return ActionResultType.SUCCESS;
    }
}