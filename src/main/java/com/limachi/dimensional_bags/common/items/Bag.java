package com.limachi.dimensional_bags.common.items;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.data.DimBagData;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.network.Network;
import net.minecraft.client.util.ITooltipFlag;
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

import java.util.List;

public class Bag extends Item {

    private final String ID_KEY = "dim_bag_eye_id";

    public Bag() {
        super(new Properties().group(DimBag.ITEM_GROUP).maxStackSize(1));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        int id = 0;
        if (stack.getTag() != null)
            id = stack.getTag().getInt(ID_KEY);
        if (id == 0)
            tooltip.add(new TranslationTextComponent("tooltip.bag.missing_id"));
        else
            tooltip.add(new TranslationTextComponent("tooltip.bag.id", id));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        if (!DimBag.isServer(world)) return super.onItemRightClick(world, player, hand);
        ItemStack stack = player.getHeldItem(hand);
        int id = 0;
        CompoundNBT tag = stack.getTag();
        if (tag != null)
            id = tag.getInt(ID_KEY);
        EyeData data;
        DimBagData dataManager = DimBagData.get(DimBag.getServer(world));
        if (id == 0) {
            data = dataManager.newEye((ServerPlayerEntity) player);
            id = data.getId();
            if (tag == null) {
                CompoundNBT nbt = new CompoundNBT();
                nbt.putInt(ID_KEY, id);
                stack.setTag(nbt);
            } else
                tag.putInt(ID_KEY, id);
        }
        else
            data = EyeData.get(world.getServer(), id);
        if (!player.isCrouching())
            Network.openEyeInventory((ServerPlayerEntity) player, data.getInventory());
        else
            Network.openEyeUpgrades((ServerPlayerEntity) player, data);
        return new ActionResult<>(ActionResultType.SUCCESS, player.getHeldItem(hand));
    }

    /*
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT tag) {

    }
    */
}
