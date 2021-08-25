package com.limachi.dimensional_bags.common.items;

import com.limachi.dimensional_bags.CuriosIntegration;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.KeyMapController;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.blocks.IBagWrenchable;
import com.limachi.dimensional_bags.common.blocks.IHasBagSettings;
import com.limachi.dimensional_bags.common.data.DimBagData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.HolderData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.OwnerData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SettingsData;
import com.limachi.dimensional_bags.common.data.IEyeIdHolder;
import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.limachi.dimensional_bags.common.inventory.BagProxy;
import com.limachi.dimensional_bags.common.items.entity.BagEntityItem;
import com.limachi.dimensional_bags.common.items.upgrades.BaseUpgrade;
import com.limachi.dimensional_bags.common.managers.ModeManager;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import com.limachi.dimensional_bags.common.managers.modes.Settings;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.limachi.dimensional_bags.StaticInit;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import top.theillusivec4.curios.api.CuriosApi;

@StaticInit
public class Bag extends Item {

    public static WeakReference<FakePlayer> getFakePlayer(int eye, ServerWorld world) { return new WeakReference<>(FakePlayerFactory.get(world, new GameProfile(null, "[DimBag: " + eye + "]"))); }

    public static final String NAME = "bag";

    public static final Supplier<Bag> INSTANCE = Registries.registerItem(NAME, Bag::new);

    public Bag() { super(DimBag.DEFAULT_PROPERTIES.stacksTo(1)); }

    public static ItemStack bag(int id) {
        ItemStack out = new ItemStack(INSTANCE.get());
        if (out.getTag() == null)
            out.setTag(new CompoundNBT());
        out.getTag().putInt(IEyeIdHolder.EYE_ID_KEY, id);
        return out;
    }

    public static void giveBag(int id, PlayerEntity player) {
        ItemStack bag = bag(id);
        if (!player.addItem(bag))
            player.drop(bag, false);
    }

    public static boolean hasBag(int id, Entity entity) {
        return CuriosIntegration.searchItem(entity, Bag.class, stack->Bag.getEyeId(stack) == id) != null;
    }

    public static boolean equipBagOnCuriosSlot(ItemStack bag, PlayerEntity player) {
        return CuriosIntegration.equipOnFirstValidSlot(player, CuriosIntegration.BAG_CURIOS_SLOT, bag);
    }

    public static int isEquippedOnCuriosSlot(LivingEntity entity, int eye_id) {
        Optional<ImmutableTriple<String, Integer, ItemStack>> ois = CuriosApi.getCuriosHelper().findEquippedCurio(stack->
            stack.getItem() instanceof Bag && Bag.getEyeId(stack) != 0 && (eye_id == 0 || Bag.getEyeId(stack) == eye_id)
        , entity);
        return ois.isPresent() ? Bag.getEyeId(ois.get().getRight()) : 0;
    }

    public static boolean unequippedBags(LivingEntity entity, int eyeId, @Nullable BlockPos pos) {
        List<CuriosIntegration.ProxySlotModifier> res = CuriosIntegration.searchItem(entity, Bag.class, o->(!(o.getItem() instanceof GhostBag) && Bag.getEyeId(o) == eyeId), true);
        boolean did_unequip = false;
        if (!res.isEmpty()) {
            for (CuriosIntegration.ProxySlotModifier p : res) {
                BagEntity.spawn(entity.level, pos == null ? entity.blockPosition() : pos, p.get());
                p.set(ItemStack.EMPTY);
                did_unequip = true;
            }
        }
        return did_unequip;
    }


    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        Block block = state.getBlock();
        if ((block instanceof IBagWrenchable || block instanceof IHasBagSettings) && ModeManager.execute(getEyeId(stack), mm -> mm.getSelectedMode().equals(Settings.ID), false)) {
            if (block instanceof IBagWrenchable && KeyMapController.KeyBindings.SNEAK_KEY.getState(context.getPlayer())) //if the player is crouching and using the bag, then use it as a wrench
                return ((IBagWrenchable) block).wrenchWithBag(context.getLevel(), context.getClickedPos(), state, context.getClickedFace());
            if (block instanceof IHasBagSettings)
                return ((IHasBagSettings) block).openSettings(context.getPlayer());
        }
        return super.onItemUseFirst(stack, context);
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) { return true; } //will not be consumed by a craft

    @Override
    public ItemStack getContainerItem(ItemStack stack) { //what is left in the crafting table if this item was used
        return stack.copy();
    }

    @Override
    public int getItemEnchantability(ItemStack stack) { return 0; }

    /*
     * bag behavior
     */

    @Override
    @Nullable
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return new BagProxy().setEyeID(Bag.getEyeId(stack));
    }

    public static int getEyeId(ItemStack stack) {
        if (stack.getTag() != null)
            return stack.getTag().getInt(IEyeIdHolder.EYE_ID_KEY);
        return 0;
    }

//    public static ClientDataManager getClientData(ItemStack stack) { return ClientDataManager.getInstance(stack); }

    public static float getModeProperty(ItemStack stack, World world, Entity entity) {
        CompoundNBT nbt = stack.getTag();
        if (nbt != null && nbt.contains("OVERRIDE_MODE_PROPERTY"))
            return nbt.getFloat("OVERRIDE_MODE_PROPERTY");
//        ModeManager modeManager = ClientDataManager.getInstance(stack).getModeManager();
        ModeManager modeManager = ModeManager.getInstance(getEyeId(stack));
        if (modeManager != null)
            return ModeManager.getModeIndex(modeManager.getSelectedMode());
        return ModeManager.getModeIndex("Default");
    }

    /*
     * item behavior
     */

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Nullable
    @Override
    public Entity createEntity(World world, Entity location, ItemStack itemstack) {
        BagEntityItem entity = new BagEntityItem(world, location.position().x, location.position().y, location.position().z, itemstack);
        entity.setDeltaMovement(location.getDeltaMovement());
        return entity;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        int eye = getEyeId(stack);
        if (eye <= 0)
            tooltip.add(new TranslationTextComponent("tooltip.bag.missing_id"));
        else {
            String ownerName = OwnerData.execute(eye, OwnerData::getPlayerName, "Missing Server Data");
            String selectedMode = ModeManager.execute(eye, ModeManager::getSelectedMode, "missing_server_data");
            tooltip.add(new TranslationTextComponent("tooltip.bag.mode", new TranslationTextComponent("bag.mode." + selectedMode)));
            tooltip.add(new TranslationTextComponent("tooltip.bag.id", eye, ownerName));
        }
        ModeManager.execute(eye, mm->mm.onAddInformation(stack, world, tooltip, flagIn));
        super.appendHoverText(stack, world, tooltip, flagIn);
        if (Screen.hasShiftDown() && eye > 0) {
            UpgradeManager upData = UpgradeManager.getInstance(eye);
            ArrayList<String> installedUpgrades = UpgradeManager.execute(eye, UpgradeManager::getInstalledUpgrades, new ArrayList<>());
            for (String upgrade : installedUpgrades) {
                BaseUpgrade up = UpgradeManager.getUpgrade(upgrade);
                tooltip.add(new TranslationTextComponent("tooltip.bag.upgrade_count", up.upgradeName(), up.getCount(upData), up.getMaxCount()));
            }
        }
        else
            tooltip.add(new TranslationTextComponent("tooltip.shift_for_info"));
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        int eyeId;
        if ((eyeId = getEyeId(stack)) == 0 && entityIn instanceof ServerPlayerEntity && DimBag.isServer(worldIn))
            eyeId = DimBagData.get().newEye((ServerPlayerEntity) entityIn, stack);
        tickEye(eyeId, worldIn, entityIn, isSelected);
    }

    static public void tickEye(int eye, World world, Entity entity, boolean isSelected) {
        if (eye > 0) {
            ModeManager.execute(eye, modeManager -> modeManager.inventoryTick(world, entity, isSelected));
            UpgradeManager.execute(eye, upgradeManager -> upgradeManager.inventoryTick(world, entity));
            if (DimBag.isServer(world) && !(entity instanceof FakePlayer)) //protection to make sure we don't override the last known position of a bag by a proxy
                HolderData.execute(eye, holderData -> holderData.setHolder(entity)); //FIXME: since this is what is used to determine a player's position when leaving a bag, we should add a system to exit a bag through a proxy (if we entered through a proxy), this fix only needs to be aplied to players, since the proxy can't absorb other entities
        }
    }

    /**
     * retrieve a bag on an entity, if 'eyeid' is not 0, will only try to find the given eyeid, if 'realBagOnly' is true, ignore ghost bags
     * @return found id or 0 if not found
     */
    public static int getBag(Entity entity, int eyeId, boolean realBagOnly) {
        CuriosIntegration.ProxySlotModifier res = CuriosIntegration.searchItem(entity, Bag.class, t->(!realBagOnly || !(t.getItem() instanceof GhostBag)) && t.getTag() != null && t.getTag().getInt(IEyeIdHolder.EYE_ID_KEY) != 0 && (eyeId == 0 || t.getTag().getInt(IEyeIdHolder.EYE_ID_KEY) == eyeId));
        return res != null ? res.get().getTag().getInt(IEyeIdHolder.EYE_ID_KEY) : 0;
    }

    @Override
    public ITextComponent getName(ItemStack stack) {
        return new StringTextComponent(SettingsData.execute(getEyeId(stack), SettingsData::getBagName, super.getName(stack).getString()));
    }

    @Override
    public boolean isDamageable(ItemStack stack) { return false; }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    public static BlockRayTraceResult rayTrace(World worldIn, PlayerEntity player, RayTraceContext.FluidMode fluidMode) {
        return getPlayerPOVHitResult(worldIn, player, fluidMode);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        return onItemUse(context.getLevel(), context.getPlayer(), getEyeId(context.getItemInHand()), new BlockRayTraceResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), context.isInside()));
    }

    public static ActionResultType onItemUse(World world, PlayerEntity player, int eyeId, BlockRayTraceResult ray) {
        return ModeManager.execute(eyeId, modeManager -> modeManager.onItemUse(world, player, ray), ActionResultType.PASS);
    }

    public static ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, int slot, int eyeId) {
        return new ActionResult<>(ModeManager.execute(eyeId, modeManager -> modeManager.onItemRightClick(world, player), ActionResultType.PASS), player.inventory.getItem(slot));
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        return onItemRightClick(world, player, IDimBagCommonItem.slotFromHand(player, hand), getEyeId(player.getItemInHand(hand)));
    }

    /**
     * static helper function that might also be called by a ghost bag
     */
    public static boolean onLeftClickEntity(int eyeId, PlayerEntity player, Entity entity) {
        return ModeManager.execute(eyeId, modeManager -> modeManager.onAttack(player, entity).consumesAction(), false);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) {
        return onLeftClickEntity(getEyeId(stack), player, entity);
    }
}
