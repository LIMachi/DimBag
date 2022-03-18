package com.limachi.dimensional_bags.common.bag;

import com.limachi.dimensional_bags.lib.CuriosIntegration;
import com.limachi.dimensional_bags.KeyMapController;
import com.limachi.dimensional_bags.common.references.Registries;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.SettingsData;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.lib.common.worldData.IBagIdHolder;
import com.limachi.dimensional_bags.common.bag.modes.Default;
import com.limachi.dimensional_bags.common.bag.modes.ModeManager;
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
public class GhostBagItem extends BagItem {

    public static final String NAME = "ghost_bag";

    static {
        Registries.registerItem(NAME, GhostBagItem::new);
    }

    public static final String ORIGINAL_STACK_KEY = "original_stack";

    public static int getTargetedBag(ItemStack stack, Entity holder) {
        if (!(holder instanceof PlayerEntity) || stack.getTag() == null || stack.getTag().getInt(IBagIdHolder.BAG_ID) == 0) return 0;
        CuriosIntegration.ProxySlotModifier res = CuriosIntegration.searchItem(holder, BagItem.class, o-> BagItem.getbagId(o) == stack.getTag().getInt(IBagIdHolder.BAG_ID));
        if (res != null)
            return BagItem.getbagId(res.get());
        return SubRoomsManager.getbagId(holder.level, holder.blockPosition(), false);
    }

    public static ItemStack ghostBagFromStack(ItemStack stack, PlayerEntity holder) {
        int bagId = BagItem.getBag(holder, 0, true, false);
        if (bagId == 0)
            bagId = SubRoomsManager.getbagId(holder.level, holder.blockPosition(), false);
        if (bagId <= 0) return stack;
        ItemStack out = new ItemStack(Registries.getItem(NAME));
        if (!out.hasTag())
            out.setTag(new CompoundNBT());
        out.getTag().put(ORIGINAL_STACK_KEY, stack.save(new CompoundNBT()));
        out.getTag().putInt(IBagIdHolder.BAG_ID, bagId);
        if (!stack.isEmpty()) {
            String name = out.getHoverName().getString() + "(" + (stack.getCount() != 1 ? stack.getCount() + "x " : "") + stack.getHoverName().getString() + ")";
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
    public ITextComponent getName(ItemStack stack) { return new StringTextComponent(SettingsData.execute(getbagId(stack), sd->new TranslationTextComponent("item.dim_bag.ghost_bag.prefix").append(sd.getBagName()), super.getName(stack)).getString()); }

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
            return BagItem.INSTANCE.get().onItemUseFirst(stack, context);
        return super.onItemUseFirst(context.getPlayer().getItemInHand(context.getHand()), rebuildContext(context));
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        if (context.getPlayer() == null) return ActionResultType.FAIL;
        if (isGhostBagValid(context.getPlayer(), context.getHand()))
            return BagItem.INSTANCE.get().useOn(context);
        return super.useOn(rebuildContext(context));
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (getTargetedBag(player.getItemInHand(hand), player) <= 0) {
            player.setItemInHand(hand, getOriginalStack(player.getItemInHand(hand)));
            return ActionResult.fail(player.getItemInHand(hand));
        }
        int id = BagItem.getbagId(player.getItemInHand(hand));
        if (SubRoomsManager.getbagId(player.level, player.blockPosition(), false) == id && KeyMapController.KeyBindings.SNEAK_KEY.getState(player) && /*ClientDataManager.getInstance(player.getItemInHand(hand)).getModeManager().getSelectedMode().equals(Default.ID)*/ModeManager.execute(id, mm->mm.getSelectedMode(player).equals(Default.ID), false)) {
            SubRoomsManager.execute(id, sm->sm.leaveBag(player));
            player.setItemInHand(hand, getOriginalStack(player.getItemInHand(hand)));
            return ActionResult.fail(player.getItemInHand(hand));
        }
        ActionResult<ItemStack> out = super.use(world, player, hand);
        return (out.getObject().getItem() instanceof BagItem || out.getObject().getItem() instanceof GhostBagItem) ? new ActionResult<>(out.getResult(), player.getItemInHand(hand)) : out;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) { return false; }

    /*
    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        if (!super.shouldCauseBlockBreakReset(oldStack, newStack)) return false;
        ItemStack s = getOriginalStack(newStack);
        return s.getItem().shouldCauseBlockBreakReset(oldStack, s);
    }

    @Nonnull
    @Override
    public Set<ToolType> getToolTypes(@Nonnull ItemStack stack) {
        ItemStack s = getOriginalStack(stack);
        return s.getItem().getToolTypes(s);
    }

    @Override
    public float getDestroySpeed(@Nonnull ItemStack stack, @Nonnull BlockState state) {
        ItemStack s = getOriginalStack(stack);
        return s.getItem().getDestroySpeed(s, state);
    }

    @Override
    public int getHarvestLevel(@Nonnull ItemStack stack, @Nonnull ToolType tool, @Nullable PlayerEntity player, @Nullable BlockState blockState) {
        ItemStack s = getOriginalStack(stack);
        return s.getItem().getHarvestLevel(s, tool, player, blockState);
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        ItemStack s = getOriginalStack(stack);
        return s.getItem().isDamageable(s);
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
        ItemStack s = getOriginalStack(stack);
        if (entity.getItemInHand(Hand.MAIN_HAND).equals(stack))
            entity.setItemInHand(Hand.MAIN_HAND, s);
        else if (entity.getItemInHand(Hand.OFF_HAND).equals(stack))
            entity.setItemInHand(Hand.OFF_HAND, s);
        else
            return 0;
        return s.getItem().damageItem(s, amount, entity, onBroken);
    }

    @Override
    public boolean mineBlock(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity entity) {
        ItemStack s = getOriginalStack(stack);
        if (entity.getItemInHand(Hand.MAIN_HAND).equals(stack))
            entity.setItemInHand(Hand.MAIN_HAND, s);
        else if (entity.getItemInHand(Hand.OFF_HAND).equals(stack))
            entity.setItemInHand(Hand.OFF_HAND, s);
        else
            return false;
        return s.getItem().mineBlock(s, world, state, pos, entity);
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState state) {
        ItemStack s = getOriginalStack(stack);
        return super.isCorrectToolForDrops(state);
    }*/
}
