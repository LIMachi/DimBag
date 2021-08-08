package com.limachi.dimensional_bags.common.items;

import com.limachi.dimensional_bags.CuriosIntegration;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.KeyMapController;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.blocks.IBagWrenchable;
import com.limachi.dimensional_bags.common.blocks.IHasBagSettings;
import com.limachi.dimensional_bags.common.data.DimBagData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.ClientDataManager;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.HolderData;
import com.limachi.dimensional_bags.common.data.IEyeIdHolder;
import com.limachi.dimensional_bags.common.entities.BagEntity;
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
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
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

    public Bag() { super(new Properties().group(DimBag.ITEM_GROUP).maxStackSize(1)); }

    public static ItemStack bag(int id) {
        ItemStack out = new ItemStack(INSTANCE.get());
        if (out.getTag() == null)
            out.setTag(new CompoundNBT());
        out.getTag().putInt(IEyeIdHolder.EYE_ID_KEY, id);
        return out;
    }

    public static void giveBag(int id, PlayerEntity player) {
        ItemStack bag = bag(id);
        if (!player.addItemStackToInventory(bag))
            player.dropItem(bag, false);
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
        List<CuriosIntegration.ProxyItemStackModifier> res = CuriosIntegration.searchItem(entity, Bag.class, o->(!(o.getItem() instanceof GhostBag) && Bag.getEyeId(o) == eyeId), true);
        boolean did_unequip = false;
        if (!res.isEmpty()) {
            for (CuriosIntegration.ProxyItemStackModifier p : res) {
                BagEntity.spawn(entity.world, pos == null ? entity.getPosition() : pos, p.get());
                p.set(ItemStack.EMPTY);
                did_unequip = true;
            }
        }
        return did_unequip;
    }


    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        BlockState state = context.getWorld().getBlockState(context.getPos());
        Block block = state.getBlock();
        if ((block instanceof IBagWrenchable || block instanceof IHasBagSettings) && ModeManager.execute(getEyeId(stack), mm -> mm.getSelectedMode().equals(Settings.ID), false)) {
            if (block instanceof IBagWrenchable && KeyMapController.KeyBindings.SNEAK_KEY.getState(context.getPlayer())) //if the player is crouching and using the bag, then use it as a wrench
                return ((IBagWrenchable) block).wrenchWithBag(context.getWorld(), context.getPos(), state, context.getFace());
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
        return new ICapabilityProvider() {
            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
//                EyeData data = getData(stack, true);
                return /*data != null ? data.getCapability(cap, side) :*/ LazyOptional.empty();
            }
        };
    }

    public static int getEyeId(ItemStack stack) {
        if (stack.getTag() != null)
            return stack.getTag().getInt(IEyeIdHolder.EYE_ID_KEY);
        return 0;
    }

    public static ClientDataManager getClientData(ItemStack stack) {
        return ClientDataManager.getInstance(stack);
    }

    public static float getModeProperty(ItemStack stack, World world, Entity entity) {
        CompoundNBT nbt = stack.getTag();
        if (nbt != null && nbt.contains("OVERRIDE_MODE_PROPERTY"))
            return nbt.getFloat("OVERRIDE_MODE_PROPERTY");
        ModeManager modeManager = ClientDataManager.getInstance(stack).getModeManager();
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
        BagEntityItem entity = new BagEntityItem(world, location.getPosX(), location.getPosY(), location.getPosZ(), itemstack);
        entity.setMotion(location.getMotion());
        return entity;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        ClientDataManager data = getClientData(stack);
        if (data.getId() <= 0)
            tooltip.add(new TranslationTextComponent("tooltip.bag.missing_id"));
        else {
            tooltip.add(new TranslationTextComponent("tooltip.bag.mode", new TranslationTextComponent("bag.mode." + data.getModeManager().getSelectedMode())));
            tooltip.add(new TranslationTextComponent("tooltip.bag.id", data.getId(), data.getOwnerName()));
        }
        super.addInformation(stack, world, tooltip, flagIn);
        if (Screen.hasShiftDown() && data != null) {
            for (String upgrade : data.getUpgradeManager().getInstalledUpgrades()) {
                BaseUpgrade up = UpgradeManager.getUpgrade(upgrade);
                tooltip.add(new TranslationTextComponent("tooltip.bag.upgrade_count", up.upgradeName(), up.getCount(data.getUpgradeManager()), up.getMaxCount()));
            }
        }
        else
            tooltip.add(new TranslationTextComponent("tooltip.shift_for_info"));
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        int eyeId;
        if ((eyeId = getEyeId(stack)) == 0 && entityIn instanceof ServerPlayerEntity && DimBag.isServer(worldIn))
            eyeId = DimBagData.get(worldIn.getServer()).newEye((ServerPlayerEntity) entityIn, stack);
        tickEye(eyeId, worldIn, entityIn, isSelected);
    }

    static public void tickEye(int eye, World world, Entity entity, boolean isSelected) {
        if (eye > 0) {
            ModeManager.execute(eye, modeManager -> modeManager.inventoryTick(world, entity, isSelected));
            UpgradeManager.execute(eye, upgradeManager -> upgradeManager.inventoryTick(world, entity));
            if (DimBag.isServer(world))
                HolderData.execute(eye, holderData -> holderData.setHolder(entity));
        }
    }

    public static int getBag(Entity entity, int eyeId) {
        CuriosIntegration.ProxyItemStackModifier res = CuriosIntegration.searchItem(entity, Bag.class, t->t.getTag() != null && t.getTag().getInt(IEyeIdHolder.EYE_ID_KEY) != 0 && (eyeId == 0 || t.getTag().getInt(IEyeIdHolder.EYE_ID_KEY) == eyeId));
        return res != null ? res.get().getTag().getInt(IEyeIdHolder.EYE_ID_KEY) : 0;
    }

    @Override
    public boolean isDamageable() { return false; }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    public static BlockRayTraceResult rayTrace(World worldIn, PlayerEntity player, RayTraceContext.FluidMode fluidMode) {
        return Items.AIR.rayTrace(worldIn, player, fluidMode);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        return onItemUse(context.getWorld(), context.getPlayer(), getEyeId(context.getItem()), new BlockRayTraceResult(context.getHitVec(), context.getFace(), context.getPos(), context.isInside()));
    }

    public static ActionResultType onItemUse(World world, PlayerEntity player, int eyeId, BlockRayTraceResult ray) {
        return ModeManager.execute(eyeId, modeManager -> modeManager.onItemUse(world, player, ray), ActionResultType.PASS);
    }

    public static ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, int slot, int eyeId) {
        return new ActionResult<>(ModeManager.execute(eyeId, modeManager -> modeManager.onItemRightClick(world, player), ActionResultType.PASS), player.inventory.getStackInSlot(slot));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        return onItemRightClick(world, player, IDimBagCommonItem.slotFromHand(player, hand), getEyeId(player.getHeldItem(hand)));
    }

    /**
     * static helper function that might also be called by a ghost bag
     */
    public static boolean onLeftClickEntity(int eyeId, PlayerEntity player, Entity entity) {
        return ModeManager.execute(eyeId, modeManager -> modeManager.onAttack(player, entity).isSuccessOrConsume(), false);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) {
        return onLeftClickEntity(getEyeId(stack), player, entity);
    }
}
