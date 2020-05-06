package com.limachi.dimensional_bags.common.items;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.EventManager;
import com.limachi.dimensional_bags.common.data.DimBagData;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.items.entity.BagEntityItem;
import com.limachi.dimensional_bags.common.network.Network;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class Bag extends Item {

    private static final String ID_KEY = "dim_bag_eye_id";
    private static final String OWNER_KEY = "dim_bag_eye_owner";

    public Bag() { super(new Properties().group(DimBag.ITEM_GROUP).maxStackSize(1)); }

    public static int getId(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getInt(ID_KEY) : 0;
    }

    public static String getOwner(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getString(OWNER_KEY) : "Unavailable";
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Nullable
    @Override
    public Entity createEntity(World world, Entity location, ItemStack itemstack) {
        BagEntityItem entity = new BagEntityItem(world, location.getPosX(), location.getPosY(), location.getPosZ(), itemstack);
        entity.setMotion(location.getMotion());
        return entity;
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (DimBag.isServer(worldIn) && entityIn instanceof ServerPlayerEntity) {
            int id = getId(stack);
            EyeData data;
            if (id == 0) {
                data = DimBagData.get(worldIn.getServer()).newEye((ServerPlayerEntity) entityIn);
                CompoundNBT nbt = stack.hasTag() ? stack.getTag() : new CompoundNBT();
                nbt.putInt(ID_KEY, data.getId());
                nbt.putString(OWNER_KEY, entityIn.getName().getFormattedText());
                stack.setTag(nbt);
            } else
                data = EyeData.get(worldIn.getServer(), id);
            data.setUser((ServerPlayerEntity) entityIn);
        }
        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        int id = getId(stack);
        if (id == 0)
            tooltip.add(new TranslationTextComponent("tooltip.bag.missing_id"));
        else
            tooltip.add(new TranslationTextComponent("tooltip.bag.id", id, getOwner(stack)));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        int id;
        if (!DimBag.isServer(world) || (id = getId(player.getHeldItem(hand))) == 0) return super.onItemRightClick(world, player, hand);
        EyeData data = EyeData.get(world.getServer(), id);
        if (!player.isCrouching())
            Network.openEyeInventory((ServerPlayerEntity) player, data.getInventory());
        else
            Network.openEyeUpgrades((ServerPlayerEntity) player, data);
        return new ActionResult<>(ActionResultType.SUCCESS, player.getHeldItem(hand));
    }
}
