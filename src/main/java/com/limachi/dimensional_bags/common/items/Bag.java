package com.limachi.dimensional_bags.common.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.client.entity.model.NullModel;
import com.limachi.dimensional_bags.common.data.DimBagData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.ClientDataManager;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.HolderData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.OwnerData;
import com.limachi.dimensional_bags.common.data.IEyeIdHolder;
import com.limachi.dimensional_bags.common.data.IMarkDirty;
import com.limachi.dimensional_bags.common.inventory.NBTStoredItemHandler;
import com.limachi.dimensional_bags.common.items.entity.BagEntityItem;
import com.limachi.dimensional_bags.common.managers.ModeManager;
import com.limachi.dimensional_bags.common.managers.Upgrade;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import net.minecraft.block.DispenserBlock;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class Bag extends ArmorItem implements IDimBagCommonItem {

    public Bag() { super(ArmorMaterial.LEATHER, EquipmentSlotType.CHEST, new Properties().group(DimBag.ITEM_GROUP).maxStackSize(1)); DispenserBlock.registerDispenseBehavior(this, ArmorItem.DISPENSER_BEHAVIOR); }

    public static NBTStoredItemHandler getArmorStandUpgradeInventory(ItemStack bag) {
        if (bag.getTag() == null)
            bag.setTag(new CompoundNBT());
        IMarkDirty syncEnchants = new IMarkDirty() {
            @Override
            public void markDirty() {
                bag.getTag().put("Enchantments", new ListNBT()); //clears the enchantments and also make sure the list for the enchantments exists
                bag.getEnchantmentTagList().addAll(getChestPlate(bag).getEnchantmentTagList()); //blunt copy of the enchantments of the chestplate
                bag.getEnchantmentTagList().addAll(getElytra(bag).getEnchantmentTagList()); //blunt copy of the enchantments of the chestplate
            }
        };
        return new NBTStoredItemHandler(()->bag.getTag().getCompound("ArmorStandUpgrade"), nbt->bag.getTag().put("ArmorStandUpgrade", nbt), syncEnchants).resize(2);
    }

    public static boolean equipBagOnChestSlot(ItemStack bag, PlayerEntity player) {
        ItemStack chestplate = player.getItemStackFromSlot(EquipmentSlotType.CHEST);
        if (!chestplate.isEmpty()) {
            NBTStoredItemHandler handler = getArmorStandUpgradeInventory(bag);
            if (chestplate.getItem() instanceof ElytraItem) {
                if (!handler.insertItem(1, chestplate, true).isEmpty())
                    return false;
                else
                    handler.insertItem(1, chestplate, false);
            } else {
                if (!handler.insertItem(0, chestplate, true).isEmpty())
                    return false;
                else
                    handler.insertItem(0, chestplate, false);
            }
            handler.markDirty();
        }
        player.setItemStackToSlot(EquipmentSlotType.CHEST, bag);
        return true;
    }

    public static ItemStack unequipBagOnChestSlot(PlayerEntity player) {
        ItemStack bag = player.getItemStackFromSlot(EquipmentSlotType.CHEST).copy();
        if (!(bag.getItem() instanceof Bag)) return ItemStack.EMPTY;
        NBTStoredItemHandler handler = getArmorStandUpgradeInventory(bag);
        for (int i = 0; i < handler.getSlots(); ++i)
            if (!handler.extractItem(i, 1, true).isEmpty()) {
                player.setItemStackToSlot(EquipmentSlotType.CHEST, handler.extractItem(i, 1, false));
                return bag;
            }
        player.setItemStackToSlot(EquipmentSlotType.CHEST, ItemStack.EMPTY);
        return bag;
    }

    /*
     * elytra behavior
     */
    @Override
    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        return getArmorStandUpgradeInventory(stack).getStackInSlot(1).canElytraFly(entity);
    }

    @Override
    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        NBTStoredItemHandler handler = getArmorStandUpgradeInventory(stack);
        ItemStack elytra = handler.getStackInSlot(1);
        boolean out = elytra.elytraFlightTick(entity, flightTicks);
        handler.markDirty();
        return out;
    }

    /*
     * armor behavior
     */

    @Override
    public boolean canEquip(ItemStack stack, EquipmentSlotType armorType, Entity entity) {
        return entity instanceof PlayerEntity && super.canEquip(stack, armorType, entity); //only a player can equip the bag
    }

    public static ItemStack getChestPlate(ItemStack stack) {
        return getArmorStandUpgradeInventory(stack).getStackInSlot(0);
    }

    public static ItemStack getElytra(ItemStack stack) {
        return getArmorStandUpgradeInventory(stack).getStackInSlot(1);
    }

    @OnlyIn(Dist.CLIENT)
    @Nullable
    @Override
    public <A extends BipedModel<?>> A getArmorModel(LivingEntity entityLiving, ItemStack stack, EquipmentSlotType armorSlot, A _default) {
        ItemStack chestPlate = getChestPlate(stack);
        if (!chestPlate.isEmpty())
            return chestPlate.getItem().getArmorModel(entityLiving, chestPlate, armorSlot, _default);
        return (A)new /*BagLayerModel(false)*/NullModel<>();
    }

    @Override
    public boolean makesPiglinsNeutral(ItemStack stack, LivingEntity wearer) {
        ItemStack chestPlate = getChestPlate(stack);
        if (!chestPlate.isEmpty())
            return chestPlate.makesPiglinsNeutral(wearer);
        return false;
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) { return true; } //will not be consumed by a craft

    @Override
    public ItemStack getContainerItem(ItemStack stack) { //what is left in the crafting table if this item was used
        return stack.copy();
    }

    @Override
    public void onArmorTick(ItemStack stack, World world, PlayerEntity player) {
        NBTStoredItemHandler handler = getArmorStandUpgradeInventory(stack);
        handler.getStackInSlot(0).onArmorTick(world, player);
        handler.markDirty();
    }

    @OnlyIn(Dist.CLIENT)
    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type) {
        ItemStack chestPlate = getChestPlate(stack);
        if (!chestPlate.isEmpty()) {
            String test = chestPlate.getItem().getArmorTexture(chestPlate, entity, slot, type);
            if (test == null) { //if the item did not use the forge hook to provide the texture, it might be a vanilla item
                //try the vanilla method
                String texture = ((ArmorItem)chestPlate.getItem()).getArmorMaterial().getName();
                test = String.format("%s:textures/models/armor/%s_layer_%d%s.png", "minecraft", texture, 1, type == null ? "" : String.format("_%s", type));
            }
            return test;
        }
        return null; //default armor texture (usually the white leather armor)
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void renderHelmetOverlay(ItemStack stack, PlayerEntity player, int width, int height, float partialTicks) {} //might be usefull someday, but for now only works on helmets

    @Override
    public int getItemEnchantability(ItemStack stack) { return 0; } //the bag might render the enchantments of the equiped chest plate, but is not itself enchentable

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
        NBTStoredItemHandler handler = getArmorStandUpgradeInventory(stack);
        ItemStack chestplate = handler.getStackInSlot(0);
        chestplate.damageItem(amount, entity, onBroken);
        handler.markDirty();
        return 0;
    } //prevent damage to the bag

    @Override
    public int getMaxDamage(ItemStack stack) {
        return getChestPlate(stack).getMaxDamage();
    }

    @Override
    public int getDamageReduceAmount() { return 0; } //seem to only be used by mobs to switch armor, the attributes are used by damage calculation

    @Override
    public float func_234657_f_() { return 0; } //toughness, seem to only be used by mobs to switch armor

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack)
    {
        if (slot == this.slot) {
            ItemStack chestplate = getChestPlate(stack);
            if (!chestplate.isEmpty())
                return ((ArmorItem)chestplate.getItem()).getAttributeModifiers(slot);
        }
        return ImmutableMultimap.of();
    }

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
//        return NBTUtils.getNbt(stack).getInt(IEyeIdHolder.EYE_ID_KEY);
        if (stack.getTag() != null)
            return stack.getTag().getInt(IEyeIdHolder.EYE_ID_KEY);
        return 0;
    }

    @Override
    public void onCreated(ItemStack stack, World worldIn, PlayerEntity playerIn) {
        DimBag.LOGGER.info("onCreated: " + stack + ", world: " + worldIn + " player: " + playerIn);
        super.onCreated(stack, worldIn, playerIn);
        ClientDataManager.getInstance(stack).syncToServer(stack);
    }

    public static ClientDataManager getClientData(ItemStack stack) {
        return ClientDataManager.getInstance(stack);
    }

    public static float getModeProperty(ItemStack stack, World world, Entity entity) {
//        if (stack.getTag() == null) return 0;
//        return ModeManager.getModeIndex(NBTUtils.getNbt(stack).getString("Mode"));
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
//            tooltip.add(new TranslationTextComponent("tooltip.bag.usable_slots", Math.min(data.getColumns() * data.getRows(), data.getInventory().getSlots()), Math.max(data.getColumns() * data.getRows(), data.getInventory().getSlots())));
            for (String upgrade : data.getUpgradeManager().getInstalledUpgrades()) {
                Upgrade up = UpgradeManager.getUpgrade(upgrade);
                tooltip.add(new TranslationTextComponent("tooltip.bag.upgrade_count", up.getBaseName(), up.getCount(data.getUpgradeManager()), up.getLimit()));
            }
        }
        else
            tooltip.add(new TranslationTextComponent("tooltip.shift_for_info"));
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
//        IDimBagCommonItem.sInventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
        if (DimBag.isServer(worldIn)) {
            int eyeId;
            if ((eyeId = getEyeId(stack)) == 0 && entityIn instanceof PlayerEntity) {
                eyeId = DimBagData.get(worldIn.getServer()).newEye((ServerPlayerEntity) entityIn);
                OwnerData ownerData = OwnerData.getInstance(null, eyeId);
                if (ownerData != null) {
                    ownerData.setPlayer((PlayerEntity)entityIn);
                    new ClientDataManager(eyeId, UpgradeManager.getInstance(null, eyeId), ModeManager.getInstance(null, eyeId), ownerData).store(stack);
                }
            }
            if (eyeId > 0) {
                ClientDataManager.getInstance(stack).syncToServer(stack);
                ModeManager modeManager = ModeManager.getInstance(null, eyeId);
                if (modeManager != null)
                    modeManager.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
                UpgradeManager upgradeManager = UpgradeManager.getInstance(null, eyeId);
                if (upgradeManager != null)
                    for (String upgrade : upgradeManager.getInstalledUpgrades())
                        UpgradeManager.getUpgrade(upgrade).upgradeEntityTick(eyeId, isSelected, stack, worldIn, entityIn, itemSlot);
                HolderData holderData = HolderData.getInstance(null, eyeId);
                if (holderData != null)
                    holderData.setHolder(entityIn);
            }
        }
    }

    @Override
    public boolean isDamageable() { return true; } //set to true by default since damage are meant to be sent to the chestplate

    @Override
    public boolean isDamaged(ItemStack stack) {
        return getChestPlate(stack).isDamaged();
    }

    @Override
    public int getDamage(ItemStack stack) {
        return getChestPlate(stack).getDamage();
    }

    @Override
    public float getXpRepairRatio(ItemStack stack) {
        return getChestPlate(stack).getXpRepairRatio();
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        ItemStack chestplate = getChestPlate(stack);
        return chestplate.getItem().isDamageable(chestplate);
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {
        NBTStoredItemHandler handler = getArmorStandUpgradeInventory(stack);
        ItemStack chestplate = handler.getStackInSlot(0);
        chestplate.setDamage(damage);
        handler.markDirty();
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    public static BlockRayTraceResult rayTrace(World worldIn, PlayerEntity player, RayTraceContext.FluidMode fluidMode) {
        return Items.AIR.rayTrace(worldIn, player, fluidMode);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        return onItemUse(context.getWorld(), context.getPlayer(), IDimBagCommonItem.slotFromHand(context.getPlayer(), context.getHand()), new BlockRayTraceResult(context.getHitVec(), context.getFace(), context.getPos(), context.isInside()));
    }

    public static ActionResultType onItemUse(World world, PlayerEntity player, int slot, BlockRayTraceResult ray) {
        ModeManager data;
        if (!DimBag.isServer(world) || (data = ModeManager.getInstance(null, getEyeId(player.inventory.getStackInSlot(slot)))) == null) return ActionResultType.PASS;
        return data.onItemUse(world, player, slot, ray);
    }

    public static ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, int slot) {
        ModeManager data;
        if (!DimBag.isServer(world) || (data = ModeManager.getInstance(null, getEyeId(player.inventory.getStackInSlot(slot)))) == null) return new ActionResult<>(ActionResultType.PASS, player.inventory.getStackInSlot(slot));
        return data.onItemRightClick(world, player, slot);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        return onItemRightClick(world, player, IDimBagCommonItem.slotFromHand(player, hand));
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) {
        ModeManager data;
        if (!DimBag.isServer(player.world) || (data = ModeManager.getInstance(null, getEyeId(stack))) == null) return false;
        return data.onAttack(stack, player, entity).isSuccessOrConsume();
    }
}
