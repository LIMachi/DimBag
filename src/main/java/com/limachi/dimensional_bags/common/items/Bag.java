package com.limachi.dimensional_bags.common.items;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.data.DimBagData;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.items.entity.BagEntityItem;
import com.limachi.dimensional_bags.common.managers.Upgrade;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Bag extends DimBagCommonItem {

    public static final String ID_KEY = "dim_bag_eye_id";
    public static final String OWNER_KEY = "dim_bag_eye_owner";

    public Bag() { super(new Properties().group(DimBag.ITEM_GROUP).maxStackSize(1)); }

    public static ItemStack stackWithId(int id) {
        ItemStack stack = new ItemStack(new Bag());
        CompoundNBT tag = new CompoundNBT();
        tag.putInt(ID_KEY, id);
        stack.setTag(tag);
        return stack;
    }

    public static int getId(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getInt(ID_KEY) : 0;
    }

    public static int getId(PlayerEntity player, int slot) {
        ItemStack stack = getItem(player, slot);
        if (stack != null && stack.getItem() instanceof Bag)
            return getId(stack);
        return 0;
    }

    private static ItemStack getItem(PlayerEntity player, int slot) {
        if (slot < 0) return null;
        if (slot < player.inventory.mainInventory.size()) return player.inventory.mainInventory.get(slot);
        if (slot < player.inventory.mainInventory.size() + player.inventory.offHandInventory.size()) return player.inventory.offHandInventory.get(slot - player.inventory.mainInventory.size());
        if (slot < player.inventory.mainInventory.size() + player.inventory.offHandInventory.size() + player.inventory.armorInventory.size()) return player.inventory.armorInventory.get(slot - player.inventory.mainInventory.size() - player.inventory.offHandInventory.size());
        return null;
    }

    public static String getOwner(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getString(OWNER_KEY) : "Unavailable";
    }

    public static int getFirstValidBag(PlayerEntity player) { //will search in order: the main hand, the off hand, the armor then the rest of the inventory, returns -1 if no bag was found
        ItemStack stack = player.inventory.mainInventory.get(player.inventory.currentItem);
        if (stack.getItem() instanceof Bag) return player.inventory.currentItem;
        if (player.inventory.offHandInventory.get(0).getItem() instanceof Bag) return player.inventory.mainInventory.size();
        for (int i = 0; i < player.inventory.armorInventory.size(); ++i)
            if (player.inventory.armorInventory.get(i).getItem() instanceof Bag) return player.inventory.mainInventory.size() + player.inventory.offHandInventory.size() + i;
        for (int i = 0; i < player.inventory.mainInventory.size(); ++i)
            if (player.inventory.mainInventory.get(i).getItem() instanceof Bag) return i;
        return -1;
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
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        int id = getId(stack);
        EyeData data = null;
        if (id == 0 || (data = EyeData.get(world.getServer(), id)) == null)
            tooltip.add(new TranslationTextComponent("tooltip.bag.missing_id"));
        else {
            tooltip.add(new TranslationTextComponent("tooltip.bag.mode", new TranslationTextComponent("bag.mode." + data.modeManager().getSelectedMode())));
            tooltip.add(new TranslationTextComponent("tooltip.bag.id", id, getOwner(stack)));
        }
        super.addInformation(stack, world, tooltip, flagIn);
        if (Screen.hasShiftDown() && data != null) {
            tooltip.add(new TranslationTextComponent("tooltip.bag.usable_slots", Math.min(data.getColumns() * data.getRows(), data.getInventory().getSlots()), Math.max(data.getColumns() * data.getRows(), data.getInventory().getSlots())));
            for (String upgrade : data.getUpgrades()) {
                Upgrade up = UpgradeManager.getUpgrade(upgrade);
                tooltip.add(new TranslationTextComponent("tooltip.bag.upgrade_count", up.getBaseName(), up.getCount(data), up.getLimit()));
            }
        }
        else
            tooltip.add(new TranslationTextComponent("tooltip.shift_for_info"));
    }

    public static void changeModeRequest(ServerPlayerEntity player, int slot, boolean up) {
        int id = getId(player, slot);
        if (id == 0) return;
        EyeData data = EyeData.get(player.server, id);
        if (data == null) return;
        ArrayList<String> modes = data.modeManager().getInstalledModes();
        for (int i = 0; i < modes.size(); ++i) {
            if (!modes.get(i).equals(data.modeManager().getSelectedMode())) continue;
            data.modeManager().selectMode((i + (up ? 1 : modes.size() - 1)) % modes.size());
            player.sendStatusMessage(new TranslationTextComponent("notification.bag.changed_mode", new TranslationTextComponent("bag.mode." + data.modeManager().getSelectedMode())), true);
            return;
        }
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
                nbt.putString(OWNER_KEY, entityIn.getName().getString());
                stack.setTag(nbt);
            } else
                data = EyeData.get(worldIn.getServer(), id);
            data.setUser(entityIn);
            data.modeManager().inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
        }
        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        int id;
        EyeData data;
        if (!DimBag.isServer(context.getWorld()) || (id = getId(context.getItem())) == 0 || (data = EyeData.get(context.getWorld().getServer(), id)) == null) return ActionResultType.PASS;
        return data.modeManager().onItemUse(context);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        int id;
        EyeData data;
        if (!DimBag.isServer(world) || (id = getId(player.getHeldItem(hand))) == 0 || (data = EyeData.get(world.getServer(), id)) == null) return super.onItemRightClick(world, player, hand);
        return data.modeManager().onItemRightClick(world, player, hand);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) {
        int id;
        EyeData data;
        if (!DimBag.isServer(player.world) || (id = getId(stack)) == 0 || (data = EyeData.get(player.world.getServer(), id)) == null) return false;
        return data.modeManager().onAttack(stack, player, entity).isSuccessOrConsume();
    }
}
