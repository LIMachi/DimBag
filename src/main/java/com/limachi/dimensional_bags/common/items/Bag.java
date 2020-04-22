package com.limachi.dimensional_bags.common.items;

import com.limachi.dimensional_bags.DimensionalBagsMod;
import com.limachi.dimensional_bags.common.data.DimBagData;
import com.limachi.dimensional_bags.common.data.IdHandler;
import com.limachi.dimensional_bags.common.dimensions.BagDimension;
import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.limachi.dimensional_bags.common.init.Registries;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Override
    public Entity createEntity(World world, Entity location, ItemStack stack) { //should use this only if a player or dispenser used the item (will do later)
        Entity ent = new BagEntity(Registries.BAG_ENTITY.get(), world);
        ent.setPosition(location.getPosX(), location.getPosY(), location.getPosZ());
        ent.setInvulnerable(true); //make sure the entity can't be destroyed (except by player events)
        ((BagEntity)ent).enablePersistence(); //make sure the entity can't despawn
        IdHandler id = new IdHandler(stack);
        id.write((BagEntity)ent);
        return ent;
    }

    public static int getUpgrade(ItemStack item, String id) {
//        try {
//            return item.getTag().getCompound("dim_bag_upgrades").getInt(id);
//        } catch (NullPointerException e) {
//            return 0;
//        }
        return 0;
    }

    /*
    public static void addUpgrade(ItemStack item, String id) {
        try {
            item.getTag().getCompound("dim_bag_upgrades")
        } catch (NullPointerException e) {

        }
    }
    */

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        if (world == null) return;
        IdHandler id = new IdHandler(stack);
        if (id.getId() == 0)
            tooltip.add(new TranslationTextComponent("tooltip.bag.missing_id"));
        else
            tooltip.add(new TranslationTextComponent("tooltip.bag.id", id.getId()));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        if (world.isRemote()) return super.onItemRightClick(world, player, hand); //non non non, logic is server side
        IdHandler id = new IdHandler(player.getHeldItem(hand));
        if (id.getId() == 0) {//first time using the bag, bind it to a new room
            id = DimBagData.get(player.getServer()).newEye(player).getId();
            id.write(player.getHeldItem(hand));
        }
        if (player.isCrouching() && !world.isRemote && !(world.dimension instanceof BagDimension)) {
            BagDimension.teleportToRoom((ServerPlayerEntity) player, id.getId()); //redo too
            return new ActionResult<>(ActionResultType.SUCCESS, player.getHeldItem(hand));
        }
        return super.onItemRightClick(world, player, hand);
    }
}
