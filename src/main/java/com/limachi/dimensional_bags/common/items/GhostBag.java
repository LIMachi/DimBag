package com.limachi.dimensional_bags.common.items;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.KeyMapController;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.ClientDataManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class GhostBag extends Item implements IDimBagCommonItem {

    public static final String ORIGINAL_STACK_KEY = "original_stack";
    public static final String TARGETED_BAG_INDEX_KEY = "targeted_bag_index";

    public static float getModeProperty(ItemStack stack, World world, Entity entity) {
        ItemStack bag = getTargetedBag(stack, entity);
        if (bag != null)
            return Bag.getModeProperty(bag, world, entity);
        return 0f;
    }

    public static ItemStack getTargetedBag(ItemStack stack, Entity holder) {
        if (!(holder instanceof PlayerEntity) || stack.getTag() == null) return null;
        ItemStack bag = ((PlayerEntity)holder).inventory.getStackInSlot(stack.getTag().getInt(TARGETED_BAG_INDEX_KEY));
        if (bag.getItem() instanceof Bag)
            return bag;
        return null;
    }

    public GhostBag() { super(new Properties().group(DimBag.ITEM_GROUP).maxStackSize(1)); }

    public static ItemStack ghostBagFromStack(ItemStack stack, PlayerEntity holder) {
        IDimBagCommonItem.ItemSearchResult res = IDimBagCommonItem.searchItem(holder, 0, Bag.class, o->true, false);
        if (res == null || res.index == -1)
            return ItemStack.EMPTY;
        ItemStack out = new ItemStack(Registries.GHOST_BAG_ITEM.get());
        if (!out.hasTag())
            out.setTag(new CompoundNBT());
        out.getTag().put(ORIGINAL_STACK_KEY, stack.write(new CompoundNBT()));
        out.getTag().putInt(TARGETED_BAG_INDEX_KEY, res.index);
        if (!stack.isEmpty()) {
            String name = out.getDisplayName().getString() + "(" + (stack.getCount() != 1 ? stack.getCount() + "x " : "") + stack.getDisplayName().getString() + ")";
            CompoundNBT display = new CompoundNBT();
            display.putString("Name", "{\"text\":\"" + name + "\",\"italic\":false}");
            out.getTag().put("display", display);
        }
        return out;
    }

    public static ClientDataManager getClientData(ItemStack stack, Entity holder) {
        if (stack.getItem() instanceof GhostBag)
            return Bag.getClientData(getTargetedBag(stack, holder));
        if (stack.getItem() instanceof Bag)
            return Bag.getClientData(stack);
        return null;
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) { return true; } //will not be consumed by a craft

    @Override
    public ItemStack getContainerItem(ItemStack stack) { //what is left in the crafting table if this item was used
        return stack.copy();
    }

    /*
     * item behavior
     */

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    public static ItemStack getOriginalStack(ItemStack stack) {
        if (stack.getTag() != null)
            return ItemStack.read(stack.getTag().getCompound(ORIGINAL_STACK_KEY));
        return ItemStack.EMPTY;
    }

    @Nullable
    @Override
    public Entity createEntity(World world, Entity location, ItemStack itemstack) {
        ItemStack original = getOriginalStack(itemstack);
        Entity entity = original.getItem().createEntity(world, location, original);
        if (entity == null) {
            entity = new ItemEntity(world, location.getPosX(), location.getPosY(), location.getPosZ(), getOriginalStack(itemstack));
            entity.setMotion(location.getMotion());
            ((ItemEntity)entity).setPickupDelay(10);
        }
        return entity;
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (!(entityIn instanceof PlayerEntity) || !isSelected || !KeyMapController.getKey((PlayerEntity)entityIn, KeyMapController.BAG_ACTION_KEY))
            entityIn.replaceItemInInventory(itemSlot, getOriginalStack(stack));
        IDimBagCommonItem.ItemSearchResult res = IDimBagCommonItem.searchItem((PlayerEntity)entityIn, 0, Bag.class, o->true, false);
        if (res == null || res.index == -1)
            entityIn.replaceItemInInventory(itemSlot, getOriginalStack(stack));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        if (context.getPlayer() == null) return ActionResultType.FAIL;
        ItemStack bag = getTargetedBag(context.getItem(), context.getPlayer());
        if (bag == null)
            return ActionResultType.FAIL;
        return Bag.onItemUse(context.getWorld(), context.getPlayer(), context.getItem().getTag().getInt(TARGETED_BAG_INDEX_KEY), new BlockRayTraceResult(context.getHitVec(), context.getFace(), context.getPos(), context.isInside()));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack bag = getTargetedBag(player.getHeldItem(hand), player);
        if (bag == null) {
            player.setHeldItem(hand, getOriginalStack(player.getHeldItem(hand)));
            return ActionResult.resultFail(player.getHeldItem(hand));
        }
        ActionResult<ItemStack> out = Bag.onItemRightClick(world, player, player.getHeldItem(hand).getTag().getInt(TARGETED_BAG_INDEX_KEY));
        return out.getResult().getItem() instanceof Bag ? new ActionResult<>(out.getType(), player.getHeldItem(hand)) : out;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) {
        IDimBagCommonItem.ItemSearchResult thisPos = IDimBagCommonItem.searchItem(player, 0, GhostBag.class, o->o.getItem() == this, false);
        if (thisPos == null || thisPos.index == -1) return false;
        ItemStack bag = getTargetedBag(stack, player);
        if (bag == null) {
            player.replaceItemInInventory(thisPos.index, getOriginalStack(stack));
            return false;
        }
        return bag.getItem().onLeftClickEntity(bag, player, entity);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) { return false; }
}
