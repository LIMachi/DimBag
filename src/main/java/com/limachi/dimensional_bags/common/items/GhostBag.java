package com.limachi.dimensional_bags.common.items;

import com.limachi.dimensional_bags.CuriosIntegration;
import com.limachi.dimensional_bags.KeyMapController;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.ClientDataManager;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.managers.modes.Default;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
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

import com.limachi.dimensional_bags.StaticInit;

@StaticInit
public class GhostBag extends Bag {

    public static final String NAME = "ghost_bag";
    public static final String BAG_ID_KEY = "targeted_bag_id";

    static {
        Registries.registerItem(NAME, GhostBag::new);
    }

    public static final String ORIGINAL_STACK_KEY = "original_stack";

    public static int getTargetedBag(ItemStack stack, Entity holder) {
        if (!(holder instanceof PlayerEntity) || stack.getTag() == null || stack.getTag().getInt(BAG_ID_KEY) == 0) return 0;
        CuriosIntegration.ProxyItemStackModifier res = CuriosIntegration.searchItem(holder, Bag.class, o->Bag.getEyeId(o) == stack.getTag().getInt(BAG_ID_KEY));
        if (res != null)
            return Bag.getEyeId(res.get());
        return SubRoomsManager.getEyeId(holder.world, holder.getPosition(), false);
    }

    public static ItemStack ghostBagFromStack(ItemStack stack, PlayerEntity holder) {
        int eyeId = Bag.getBag(holder, 0);
        if (eyeId == 0)
            eyeId = SubRoomsManager.getEyeId(holder.world, holder.getPosition(), false);
        if (eyeId <= 0) return stack;
        ItemStack out = new ItemStack(Registries.getItem(NAME));
        if (!out.hasTag())
            out.setTag(new CompoundNBT());
        out.getTag().put(ORIGINAL_STACK_KEY, stack.write(new CompoundNBT()));
        out.getTag().putInt(BAG_ID_KEY, eyeId);
        ClientDataManager.getInstance(eyeId).store(out);
        if (!stack.isEmpty()) {
            String name = out.getDisplayName().getString() + "(" + (stack.getCount() != 1 ? stack.getCount() + "x " : "") + stack.getDisplayName().getString() + ")";
            CompoundNBT display = new CompoundNBT();
            display.putString("Name", "{\"text\":\"" + name + "\",\"italic\":false}");
            out.getTag().put("display", display);
        }
        return out;
    }

    /*
     * item behavior
     */

    @Override
    public boolean hasCustomEntity(ItemStack stack) { return true; }

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
        if (!(entityIn instanceof PlayerEntity) || !isSelected || !KeyMapController.KeyBindings.BAG_KEY.getState((PlayerEntity) entityIn)) {
            entityIn.replaceItemInInventory(itemSlot, getOriginalStack(stack));
            return;
        }
        if (getTargetedBag(stack, entityIn) <= 0) {
            entityIn.replaceItemInInventory(itemSlot, getOriginalStack(stack));
            return;
        }
    }

    protected static boolean isGhostBagValid(LivingEntity entity, Hand hand) {
        if (getTargetedBag(entity.getHeldItem(hand), entity) <= 0) {
            entity.setHeldItem(hand, getOriginalStack(entity.getHeldItem(hand)));
            return false;
        }
        return true;
    }

    //updates the context to make sure the world and itemstack are correct
    protected static ItemUseContext rebuildContext(ItemUseContext original) {
        return new ItemUseContext(original.getPlayer(), original.getHand(), new BlockRayTraceResult(original.getHitVec(), original.getFace(), original.getPos(), original.isInside()));
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        if (context.getPlayer() == null) return ActionResultType.FAIL;
        if (isGhostBagValid(context.getPlayer(), context.getHand()))
            return Bag.INSTANCE.get().onItemUseFirst(stack, context);
        return super.onItemUseFirst(context.getPlayer().getHeldItem(context.getHand()), rebuildContext(context));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        if (context.getPlayer() == null) return ActionResultType.FAIL;
        if (isGhostBagValid(context.getPlayer(), context.getHand()))
            return Bag.INSTANCE.get().onItemUse(context);
//        return Bag.onItemUse(context.getWorld(), context.getPlayer(), Bag.getEyeId(context.getItem()), new BlockRayTraceResult(context.getHitVec(), context.getFace(), context.getPos(), context.isInside()));
        return super.onItemUse(rebuildContext(context));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        if (getTargetedBag(player.getHeldItem(hand), player) <= 0) {
            player.setHeldItem(hand, getOriginalStack(player.getHeldItem(hand)));
            return ActionResult.resultFail(player.getHeldItem(hand));
        }
        int id = Bag.getEyeId(player.getHeldItem(hand));
        if (SubRoomsManager.getEyeId(player.world, player.getPosition(), false) == id && KeyMapController.KeyBindings.SNEAK_KEY.getState(player) && ClientDataManager.getInstance(player.getHeldItem(hand)).getModeManager().getSelectedMode().equals(Default.ID)) {
            SubRoomsManager.execute(id, sm->sm.leaveBag(player, false, null, null));
            player.setHeldItem(hand, getOriginalStack(player.getHeldItem(hand)));
            return ActionResult.resultFail(player.getHeldItem(hand));
        }
//        ActionResult<ItemStack> out = Bag.onItemRightClick(world, player, IDimBagCommonItem.slotFromHand(player, hand), id);
        ActionResult<ItemStack> out = super.onItemRightClick(world, player, hand);
        return (out.getResult().getItem() instanceof Bag || out.getResult().getItem() instanceof GhostBag) ? new ActionResult<>(out.getType(), player.getHeldItem(hand)) : out;
    }

//    @Override
//    public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) {
//        return Bag.onLeftClickEntity(Bag.getEyeId(stack), player, entity);
//        return Bag.INSTANCE.get().onLeftClickEntity(stack, player, entity);
//    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) { return false; }
}
