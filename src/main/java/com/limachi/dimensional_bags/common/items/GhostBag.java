package com.limachi.dimensional_bags.common.items;

import com.limachi.dimensional_bags.CuriosIntegration;
import com.limachi.dimensional_bags.KeyMapController;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SettingsData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.data.IEyeIdHolder;
import com.limachi.dimensional_bags.common.managers.modes.Default;
import com.limachi.dimensional_bags.common.managers.ModeManager;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import com.limachi.dimensional_bags.StaticInit;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

@StaticInit
public class GhostBag extends Bag {

    public static final String NAME = "ghost_bag";

    static {
        Registries.registerItem(NAME, GhostBag::new);
    }

    public static final String ORIGINAL_STACK_KEY = "original_stack";

    public static int getTargetedBag(ItemStack stack, Entity holder) {
        if (!(holder instanceof PlayerEntity) || stack.getTag() == null || stack.getTag().getInt(IEyeIdHolder.EYE_ID_KEY) == 0) return 0;
        CuriosIntegration.ProxySlotModifier res = CuriosIntegration.searchItem(holder, Bag.class, o->Bag.getEyeId(o) == stack.getTag().getInt(IEyeIdHolder.EYE_ID_KEY));
        if (res != null)
            return Bag.getEyeId(res.get());
        return SubRoomsManager.getEyeId(holder.level, holder.blockPosition(), false);
    }

    public static ItemStack ghostBagFromStack(ItemStack stack, PlayerEntity holder) {
        int eyeId = Bag.getBag(holder, 0, true);
        if (eyeId == 0)
            eyeId = SubRoomsManager.getEyeId(holder.level, holder.blockPosition(), false);
        if (eyeId <= 0) return stack;
        ItemStack out = new ItemStack(Registries.getItem(NAME));
        if (!out.hasTag())
            out.setTag(new CompoundNBT());
        out.getTag().put(ORIGINAL_STACK_KEY, stack.save(new CompoundNBT()));
        out.getTag().putInt(IEyeIdHolder.EYE_ID_KEY, eyeId);
        if (!stack.isEmpty()) {
            String name = out.getDisplayName().getString() + "(" + (stack.getCount() != 1 ? stack.getCount() + "x " : "") + stack.getDisplayName().getString() + ")";
            CompoundNBT display = new CompoundNBT();
            display.putString("Name", "{\"text\":\"" + name + "\",\"italic\":false}");
            out.getTag().put("display", display);
        }
        return out;
    }

    /**
     * only the real bag has capabilities, yet this implements the "capabilities" of a bag without registering them (inheritance)
     */
    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) { return null; }

    @Override
    public ITextComponent getName(ItemStack stack) { return new StringTextComponent(SettingsData.execute(getEyeId(stack), sd->new TranslationTextComponent("item.dim_bag.ghost_bag.prefix").append(sd.getBagName()), super.getName(stack)).getString()); }

    @Override
    public boolean hasCustomEntity(ItemStack stack) { return true; }

    public static ItemStack getOriginalStack(ItemStack stack) {
        if (stack.getTag() != null)
            return ItemStack.of(stack.getTag().getCompound(ORIGINAL_STACK_KEY));
        return ItemStack.EMPTY;
    }

    @Nullable
    @Override
    public Entity createEntity(World world, Entity location, ItemStack itemstack) {
        ItemStack original = getOriginalStack(itemstack);
        Entity entity = original.getItem().createEntity(world, location, original);
        if (entity == null) {
            entity = new ItemEntity(world, location.position().x, location.position().y, location.position().z, getOriginalStack(itemstack));
            entity.setDeltaMovement(location.getDeltaMovement());
            ((ItemEntity)entity).setDefaultPickUpDelay();
        }
        return entity;
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (!(entityIn instanceof PlayerEntity) || !isSelected || !KeyMapController.KeyBindings.BAG_KEY.getState((PlayerEntity) entityIn)) {
            entityIn.setSlot(itemSlot, getOriginalStack(stack));
            return;
        }
        if (getTargetedBag(stack, entityIn) <= 0) {
            entityIn.setSlot(itemSlot, getOriginalStack(stack));
            return;
        }
    }

    protected static boolean isGhostBagValid(LivingEntity entity, Hand hand) {
        if (getTargetedBag(entity.getItemInHand(hand), entity) <= 0) {
            entity.setItemInHand(hand, getOriginalStack(entity.getItemInHand(hand)));
            return false;
        }
        return true;
    }

    //updates the context to make sure the world and itemstack are correct
    protected static ItemUseContext rebuildContext(ItemUseContext original) {
        return new ItemUseContext(original.getPlayer(), original.getHand(), new BlockRayTraceResult(original.getClickLocation(), original.getClickedFace(), original.getClickedPos(), original.isInside()));
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        if (context.getPlayer() == null) return ActionResultType.FAIL;
        if (isGhostBagValid(context.getPlayer(), context.getHand()))
            return Bag.INSTANCE.get().onItemUseFirst(stack, context);
        return super.onItemUseFirst(context.getPlayer().getItemInHand(context.getHand()), rebuildContext(context));
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        if (context.getPlayer() == null) return ActionResultType.FAIL;
        if (isGhostBagValid(context.getPlayer(), context.getHand()))
            return Bag.INSTANCE.get().useOn(context);
        return super.useOn(rebuildContext(context));
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (getTargetedBag(player.getItemInHand(hand), player) <= 0) {
            player.setItemInHand(hand, getOriginalStack(player.getItemInHand(hand)));
            return ActionResult.fail(player.getItemInHand(hand));
        }
        int id = Bag.getEyeId(player.getItemInHand(hand));
        if (SubRoomsManager.getEyeId(player.level, player.blockPosition(), false) == id && KeyMapController.KeyBindings.SNEAK_KEY.getState(player) && /*ClientDataManager.getInstance(player.getItemInHand(hand)).getModeManager().getSelectedMode().equals(Default.ID)*/ModeManager.execute(id, mm->mm.getSelectedMode().equals(Default.ID), false)) {
            SubRoomsManager.execute(id, sm->sm.leaveBag(player, false, null, null));
            player.setItemInHand(hand, getOriginalStack(player.getItemInHand(hand)));
            return ActionResult.fail(player.getItemInHand(hand));
        }
        ActionResult<ItemStack> out = super.use(world, player, hand);
        return (out.getObject().getItem() instanceof Bag || out.getObject().getItem() instanceof GhostBag) ? new ActionResult<>(out.getResult(), player.getItemInHand(hand)) : out;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) { return false; }
}
