package com.limachi.dim_bag.items;

import com.limachi.dim_bag.Constants;
import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.blocks.IBagWrenchable;
import com.limachi.dim_bag.blocks.IHasBagSettings;
import com.limachi.dim_bag.entities.BagEntity;
import com.limachi.dim_bag.saveData.Test;
import com.limachi.lim_lib.*;
import com.limachi.lim_lib.integration.CuriosIntegration;
import com.limachi.lim_lib.registries.annotations.RegisterItem;
import com.limachi.lim_lib.saveData.SaveDataManager;
import com.limachi.lim_lib.scrollSystem.IScrollItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import top.theillusivec4.curios.api.CuriosApi;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BagItem extends Item implements IScrollItem {

    @RegisterItem
    public static RegistryObject<BagItem> R_ITEM;

    public BagItem() { super(DimBag.INSTANCE.defaultProps().stacksTo(1)); }

    public static ItemStack bag(int id) {
        ItemStack out = new ItemStack(R_ITEM.get());
        if (out.getTag() == null)
            out.setTag(new CompoundTag());
        out.getTag().putInt(Constants.BAG_ID_TAG_KEY, id);
        return out;
    }

    public static void giveBag(int id, Player player) {
        ItemStack bag = bag(id);
        if (!player.addItem(bag))
            player.drop(bag, false);
    }

    public static boolean hasBag(int id, Entity entity) {
        return CuriosIntegration.searchItem(entity, BagItem.class, stack-> BagItem.getbagId(stack) == id) != null;
    }

    public static boolean equipBagOnCuriosSlot(ItemStack bag, LivingEntity player) {
        return CuriosIntegration.equipOnFirstValidSlot(player, Constants.BAG_CURIO_SLOT, bag);
    }

    public static int isEquippedOnCuriosSlot(LivingEntity entity, int eye_id) {
        Optional<ImmutableTriple<String, Integer, ItemStack>> ois = CuriosApi.getCuriosHelper().findEquippedCurio(stack->
                        stack.getItem() instanceof BagItem && BagItem.getbagId(stack) != 0 && (eye_id == 0 || BagItem.getbagId(stack) == eye_id)
                , entity);
        return ois.isPresent() ? BagItem.getbagId(ois.get().getRight()) : 0;
    }

    public static ArrayList<BagEntity> unequipBags(LivingEntity entity, int bagId, @Nullable BlockPos posIn, @Nullable Level worldIn) {
        Level world = worldIn != null ? worldIn : entity.level;
        BlockPos pos = posIn != null ? posIn : entity.blockPosition();
        ArrayList<BagEntity> spawned = new ArrayList<>();
        CuriosIntegration.searchItem(entity, BagItem.class, o->(!(o.getItem() instanceof GhostBagItem) && BagItem.getbagId(o) == bagId), true).forEach(p -> {
            spawned.add(BagEntity.spawn(world, pos, p.get()));
            p.set(ItemStack.EMPTY);
        });
        return spawned;
    }

    public static int getbagId(ItemStack stack) {
        if (stack.getTag() != null)
            return stack.getTag().getInt(Constants.BAG_ID_TAG_KEY);
        return 0;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        Block block = state.getBlock();
        if ((block instanceof IBagWrenchable || block instanceof IHasBagSettings) && true /*FIXME ModeManager.execute(getbagId(stack), mm -> mm.getSelectedMode(context.getPlayer()).equals(Settings.ID), false)*/) {
            if (block instanceof IBagWrenchable b && KeyMapController.SNEAK.getState(context.getPlayer()))
                return b.wrenchWithBag(context.getLevel(), context.getClickedPos(), state, context.getClickedFace());
            if (block instanceof IHasBagSettings b)
                return b.openSettings(context.getPlayer(), context.getClickedPos());
        }
        return super.onItemUseFirst(stack, context);
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) { return true; }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        return itemStack.copy();
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) { return 0; }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
//        return new BagProxyInventory().setbagId(BagItem.getbagId(stack)); FIXME
        return null;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        CompoundTag nbt = stack.getOrCreateTag();
        //FIXME 1.16.5 BagItem:159
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flags) {
        int id = getbagId(stack);
        if (id <= 0)
            tooltip.add(Component.translatable("tooltip.bag.missing_id"));
        else {
            String owner = /*FIXME OwnerData.execute(eye, OwnerData::getPlayerName, "Missing Server Data")*/ "Missing Server Data";
            tooltip.add(Component.translatable("tooltip.bag.id", id, owner));
        }
        super.appendHoverText(stack, level, tooltip, flags);
    }

    public static int getBag(LivingEntity entity, int bagId, boolean realBagOnly, boolean equippedOnly) {
        if (equippedOnly) {
            Optional<ImmutableTriple<String, Integer, ItemStack>> s = CuriosApi.getCuriosHelper().findEquippedCurio(t->(!realBagOnly || !(t.getItem() instanceof GhostBagItem)) && t.getTag() != null && t.getTag().getInt(Constants.BAG_ID_TAG_KEY) != 0 && (bagId == 0 || t.getTag().getInt(Constants.BAG_ID_TAG_KEY) == bagId), entity);
            return s.isPresent() ? getbagId(s.get().getRight()) : 0;
        }
        CuriosIntegration.ProxySlotModifier res = CuriosIntegration.searchItem(entity, BagItem.class, t->(!realBagOnly || !(t.getItem() instanceof GhostBagItem)) && t.getTag() != null && t.getTag().getInt(Constants.BAG_ID_TAG_KEY) != 0 && (bagId == 0 || t.getTag().getInt(Constants.BAG_ID_TAG_KEY) == bagId));
        return res != null ? getbagId(res.get()) : 0;
    }

    @Override
    public boolean isDamageable(ItemStack stack) { return false; }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) { return slotChanged || oldStack.getItem() != newStack.getItem(); }

    /**
     * expose an otherwise protected method
     */
    public static BlockHitResult rayTrace(Level level, Player player, ClipContext.Fluid fluidMode) {
        return getPlayerPOVHitResult(level, player, fluidMode);
    }

    @Override
    public void scroll(Player player, int slot, int delta) {
        Test test = SaveDataManager.getInstance("test:1");
        delta += test.getCounter();
        Log.warn("validated scroll: " + delta + " for slot " + slot);
        test.setCounter(delta);
    }

    @Override
    public void scrollFeedBack(Player player, int slot, int delta) {

    }

    @Override
    public boolean canScroll(Player player, int slot) { return Constants.ACTION_KEY.getState(player); }
}
