package com.limachi.dimensional_bags.common.bag;

import com.limachi.dimensional_bags.common.upgrades.BagUpgradeManager;
import com.limachi.dimensional_bags.lib.CuriosIntegration;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.KeyMapController;
import com.limachi.dimensional_bags.common.references.Registries;
import com.limachi.dimensional_bags.lib.common.blocks.IBagWrenchable;
import com.limachi.dimensional_bags.lib.common.blocks.IHasBagSettings;
import com.limachi.dimensional_bags.lib.common.items.IDimBagCommonItem;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.OwnerData;
import com.limachi.dimensional_bags.lib.common.worldData.EyeDataMK2.SettingsData;
import com.limachi.dimensional_bags.lib.common.worldData.IBagIdHolder;
import com.limachi.dimensional_bags.common.upgrades.BaseUpgradeBag;
import com.limachi.dimensional_bags.common.bag.modes.ModeManager;
import com.limachi.dimensional_bags.common.bag.modes.Settings;
import com.limachi.dimensional_bags.lib.utils.SyncUtils;
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
import net.minecraft.network.play.server.SSetSlotPacket;
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
public class BagItem extends Item {

    public static WeakReference<FakePlayer> getFakePlayer(int eye, ServerWorld world) { return new WeakReference<>(FakePlayerFactory.get(world, new GameProfile(null, "[DimBag: " + eye + "]"))); }

    public static final String NAME = "bag";

    public static final Supplier<BagItem> INSTANCE = Registries.registerItem(NAME, BagItem::new);

    public BagItem() { super(DimBag.DEFAULT_PROPERTIES.stacksTo(1)); }

    public static ItemStack bag(int id) {
        ItemStack out = new ItemStack(INSTANCE.get());
        if (out.getTag() == null)
            out.setTag(new CompoundNBT());
        out.getTag().putInt(IBagIdHolder.EYE_ID_KEY, id);
        return out;
    }

    public static void giveBag(int id, PlayerEntity player) {
        ItemStack bag = bag(id);
        if (!player.addItem(bag))
            player.drop(bag, false);
    }

    public static boolean hasBag(int id, Entity entity) {
        return CuriosIntegration.searchItem(entity, BagItem.class, stack-> BagItem.getbagId(stack) == id) != null;
    }

    public static boolean equipBagOnCuriosSlot(ItemStack bag, LivingEntity player) {
        return CuriosIntegration.equipOnFirstValidSlot(player, CuriosIntegration.BAG_CURIOS_SLOT, bag);
    }

    public static int isEquippedOnCuriosSlot(LivingEntity entity, int eye_id) {
        Optional<ImmutableTriple<String, Integer, ItemStack>> ois = CuriosApi.getCuriosHelper().findEquippedCurio(stack->
            stack.getItem() instanceof BagItem && BagItem.getbagId(stack) != 0 && (eye_id == 0 || BagItem.getbagId(stack) == eye_id)
        , entity);
        return ois.isPresent() ? BagItem.getbagId(ois.get().getRight()) : 0;
    }

    public static ArrayList<BagEntity> unequipBags(LivingEntity entity, int bagId, @Nullable BlockPos posIn, @Nullable World worldIn) {
        World world = worldIn != null ? worldIn : entity.level;
        BlockPos pos = posIn != null ? posIn : entity.blockPosition();
        ArrayList<BagEntity> spawned = new ArrayList<>();
        CuriosIntegration.searchItem(entity, BagItem.class, o->(!(o.getItem() instanceof GhostBagItem) && BagItem.getbagId(o) == bagId), true).forEach(p -> {
            spawned.add(BagEntity.spawn(world, pos, p.get()));
            p.set(ItemStack.EMPTY);
        });
        return spawned;
    }


    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        Block block = state.getBlock();
        if ((block instanceof IBagWrenchable || block instanceof IHasBagSettings) && ModeManager.execute(getbagId(stack), mm -> mm.getSelectedMode(context.getPlayer()).equals(Settings.ID), false)) {
            if (block instanceof IBagWrenchable && KeyMapController.KeyBindings.SNEAK_KEY.getState(context.getPlayer())) //if the player is crouching and using the bag, then use it as a wrench
                return ((IBagWrenchable) block).wrenchWithBag(context.getLevel(), context.getClickedPos(), state, context.getClickedFace());
            if (block instanceof IHasBagSettings)
                return ((IHasBagSettings) block).openSettings(context.getPlayer(), context.getClickedPos());
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
        return new BagProxyInventory().setbagId(BagItem.getbagId(stack));
    }

    public static int getbagId(ItemStack stack) {
        if (stack.getTag() != null)
            return stack.getTag().getInt(IBagIdHolder.EYE_ID_KEY);
        return 0;
    }

//    public static ClientDataManager getClientData(ItemStack stack) { return ClientDataManager.getInstance(stack); }


    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        CompoundNBT nbt = stack.getOrCreateTag();
        String mode = ModeManager.execute(getbagId(stack), mm->mm.getSelectedMode(entity), "Default");
        if (!nbt.getString("OVERRIDE_MODE_PROPERTY").equals(mode)) {
            nbt.putString("OVERRIDE_MODE_PROPERTY", mode);
            if (entity instanceof ServerPlayerEntity)
                SyncUtils.resyncPlayerSlot((ServerPlayerEntity)entity, slot);
        }
    }

    //FIXME: for visuals, mode property is loaded from the stack nbt (and so should be sync by item ticking)
    public static float getModeProperty(ItemStack stack, World world, Entity entity) {
        CompoundNBT nbt = stack.getTag();
        if (nbt != null && nbt.contains("OVERRIDE_MODE_PROPERTY"))
            return ModeManager.getModeIndex(nbt.getString("OVERRIDE_MODE_PROPERTY"));
//        ModeManager modeManager = ClientDataManager.getInstance(stack).getModeManager();
        ModeManager modeManager = ModeManager.getInstance(getbagId(stack));
        if (modeManager != null)
            return ModeManager.getModeIndex(modeManager.getSelectedMode(entity));
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
        int eye = getbagId(stack);
        if (eye <= 0)
            tooltip.add(new TranslationTextComponent("tooltip.bag.missing_id"));
        else {
            String ownerName = OwnerData.execute(eye, OwnerData::getPlayerName, "Missing Server Data");
//            String selectedMode = ModeManager.execute(eye, ModeManager::getSelectedMode, "missing_server_data");
//            tooltip.add(new TranslationTextComponent("tooltip.bag.mode", new TranslationTextComponent("bag.mode." + selectedMode)));
            tooltip.add(new TranslationTextComponent("tooltip.bag.id", eye, ownerName));
        }
//        ModeManager.execute(eye, mm->mm.onAddInformation(stack, world, tooltip, flagIn));
        super.appendHoverText(stack, world, tooltip, flagIn);
        if (Screen.hasShiftDown() && eye > 0) {
            BagUpgradeManager upData = BagUpgradeManager.getInstance(eye);
            ArrayList<String> installedUpgrades = BagUpgradeManager.execute(eye, BagUpgradeManager::getInstalledUpgrades, new ArrayList<>());
            for (String upgrade : installedUpgrades) {
                BaseUpgradeBag up = BagUpgradeManager.getUpgrade(upgrade);
                tooltip.add(new TranslationTextComponent("tooltip.bag.upgrade_count", up.upgradeName(), up.getCount(upData), up.getMaxCount()));
            }
        }
        else
            tooltip.add(new TranslationTextComponent("tooltip.shift_for_info"));
    }

    /**
     * retrieve a bag on an entity, if 'bagId' is not 0, will only try to find the given bagId, if 'realBagOnly' is true, ignore ghost bags
     * @return found id or 0 if not found
     */
    public static int getBag(LivingEntity entity, int bagId, boolean realBagOnly, boolean equipOnly) {
        if (equipOnly) {
            Optional<ImmutableTriple<String, Integer, ItemStack>> s = CuriosApi.getCuriosHelper().findEquippedCurio(t->(!realBagOnly || !(t.getItem() instanceof GhostBagItem)) && t.getTag() != null && t.getTag().getInt(IBagIdHolder.EYE_ID_KEY) != 0 && (bagId == 0 || t.getTag().getInt(IBagIdHolder.EYE_ID_KEY) == bagId), entity);
            return s.isPresent() ? getbagId(s.get().getRight()) : 0;
        }
        CuriosIntegration.ProxySlotModifier res = CuriosIntegration.searchItem(entity, BagItem.class, t->(!realBagOnly || !(t.getItem() instanceof GhostBagItem)) && t.getTag() != null && t.getTag().getInt(IBagIdHolder.EYE_ID_KEY) != 0 && (bagId == 0 || t.getTag().getInt(IBagIdHolder.EYE_ID_KEY) == bagId));
        return res != null ? getbagId(res.get()) : 0;
    }

    @Override
    public ITextComponent getName(ItemStack stack) {
        return new StringTextComponent(SettingsData.execute(getbagId(stack), SettingsData::getBagName, super.getName(stack).getString()));
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
        return onItemUse(context.getLevel(), context.getPlayer(), getbagId(context.getItemInHand()), new BlockRayTraceResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), context.isInside()));
    }

    public static ActionResultType onItemUse(World world, PlayerEntity player, int bagId, BlockRayTraceResult ray) {
        return ModeManager.execute(bagId, modeManager -> modeManager.onItemUse(world, player, ray), ActionResultType.PASS);
    }

    public static ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, int slot, int bagId) {
        return new ActionResult<>(ModeManager.execute(bagId, modeManager -> modeManager.onItemRightClick(world, player), ActionResultType.PASS), player.inventory.getItem(slot));
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        return onItemRightClick(world, player, IDimBagCommonItem.slotFromHand(player, hand), getbagId(player.getItemInHand(hand)));
    }

    /**
     * static helper function that might also be called by a ghost bag
     */
    public static boolean onLeftClickEntity(int bagId, PlayerEntity player, Entity entity) {
        return ModeManager.execute(bagId, modeManager -> modeManager.onAttack(player, entity).consumesAction(), false);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) {
        return onLeftClickEntity(getbagId(stack), player, entity);
    }
}
