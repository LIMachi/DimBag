package com.limachi.dimensional_bags.common.items;

import com.limachi.dimensional_bags.DimensionalBagsMod;
import com.limachi.dimensional_bags.common.dimensions.BagDimension;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class Bag extends Item {

    public Bag() {
        super(new Properties().group(DimensionalBagsMod.ItemGroup.instance));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        if (world == null) return;
        int id;
        try {
            id = stack.getTag().getInt("ID");
        } catch (NullPointerException e) {
            id = -1;
        }
        if (id == -1)
            tooltip.add(new TranslationTextComponent("tooltip.bag.missing_id"));
        else
            tooltip.add(new StringTextComponent("ID: " + id));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        DimensionalBagsMod.LOGGER.info("activation of bag item with world status: " + world.isRemote());
        if (world.isRemote()) return super.onItemRightClick(world, player, hand);
        int id;
        try {
            id = player.getHeldItem(hand).getTag().getInt("ID");
        } catch (NullPointerException e) {
            id = -1;
        }
        if (id == -1) {//first time using the bag, bind it to a new room
            id = BagDimension.newRoom((ServerPlayerEntity) player);
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt("ID", id);
            player.getHeldItem(hand).setTag(nbt); //overkill, might destroy other nbt, but i don't give a damn
        }
        DimensionalBagsMod.LOGGER.info("player croushing: " + player.isCrouching() + " remote world: " + world.isRemote() + " dimension type: " + world.dimension.getType());
        if (player.isCrouching() && !world.isRemote && !(world.dimension instanceof BagDimension)) {
            DimensionalBagsMod.LOGGER.info("trying to tp to dimension");
            BagDimension.teleportToRoom((ServerPlayerEntity) player, id);
            return new ActionResult<>(ActionResultType.SUCCESS, player.getHeldItem(hand));
        }
        return super.onItemRightClick(world, player, hand);
    }
}
