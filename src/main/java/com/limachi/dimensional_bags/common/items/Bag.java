package com.limachi.dimensional_bags.common.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.client.entity.layer.BagLayer;
import com.limachi.dimensional_bags.client.entity.model.BagLayerModel;
import com.limachi.dimensional_bags.client.entity.model.NullModel;
import com.limachi.dimensional_bags.common.data.DimBagData;
import com.limachi.dimensional_bags.common.data.EyeData;
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
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
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
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static com.limachi.dimensional_bags.common.data.EyeData.getCapabilityProvider;
import static net.minecraft.item.Items.AIR;

public class Bag extends ArmorItem implements IDimBagCommonItem {

    public static final String ID_KEY = "dim_bag_eye_id";
    public static final String OWNER_KEY = "dim_bag_eye_owner";

    public Bag() { super(ArmorMaterial.LEATHER, EquipmentSlotType.CHEST, new Properties().group(DimBag.ITEM_GROUP).maxStackSize(1)); DispenserBlock.registerDispenseBehavior(this, ArmorItem.DISPENSER_BEHAVIOR); }

    /*
     * elytra behavior
     */
    @Override
    public boolean canElytraFly(ItemStack stack, LivingEntity entity) { return stack.hasTag() && stack.getTag().getBoolean("ElytraAttached"); }

    @Override
    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) { return canElytraFly(stack, entity); }

    public static ItemStack stackWithId(int id) {
        ItemStack stack = new ItemStack(new Bag());
        CompoundNBT tag = new CompoundNBT();
        tag.putInt(ID_KEY, id);
        tag.putString("Mode", "Default");
        tag.putBoolean("ElytraAttached", true); //test
        stack.setTag(tag);
        return stack;
    }

    /*
     * armor behavior
     */

    public static ItemStack getChestPlate(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof Bag) || !stack.hasTag()) return ItemStack.EMPTY;
        ItemStack out = ItemStack.read(stack.getTag().getCompound("ChestPlate"));
        if (out.isEmpty() || !(out.getItem() instanceof ArmorItem) || ((ArmorItem)out.getItem()).getEquipmentSlot() != EquipmentSlotType.CHEST)
            return ItemStack.EMPTY;
        return out;
    }

    @OnlyIn(Dist.CLIENT)
    @Nullable
    @Override
    public <A extends BipedModel<?>> A getArmorModel(LivingEntity entityLiving, ItemStack stack, EquipmentSlotType armorSlot, A _default) {
        ItemStack chestPlate = getChestPlate(stack);
        if (!chestPlate.isEmpty())
            return chestPlate.getItem().getArmorModel(entityLiving, chestPlate, armorSlot, _default);
        return (A)new NullModel();
    }

    @Override
    public IArmorMaterial getArmorMaterial() {
        return super.getArmorMaterial();
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
    public void onArmorTick(ItemStack stack, World world, PlayerEntity player) {} //might be usefull someday, for now the tick method is enough

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
    public int getItemEnchantability(ItemStack stack) { return 0; } //this property should be read from the attached armor

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) { return 0; } //prevent damage to the bag

    @Override
    public int getDamageReduceAmount() { return 0; } //seem to only be used by mobs to switch armor, the attributes are used by damage calculation

    @Override
    public float func_234657_f_() { return 0; } //toughness, seem to only be used by mobs to switch armor

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot, ItemStack stack)
    {
        if (slot == this.slot) {
//            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
//            UUID uuid = ARMOR_MODIFIERS[slot.getIndex()];
            ItemStack chestplate = getChestPlate(stack);
            if (!chestplate.isEmpty())
                return ((ArmorItem)chestplate.getItem()).getAttributeModifiers(slot);
//            return builder.build();
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
                EyeData data = EyeData.get(Bag.getId(stack));
                return data != null ? data.getCapability(cap, side) : LazyOptional.empty();
            }
        };
    }

    public static int getId(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getInt(ID_KEY) : 0;
    }

    public static int getId(PlayerEntity player, int slot) {
        ItemStack stack = IDimBagCommonItem.getItemFromPlayer(player, slot);
        if (stack != null && stack.getItem() instanceof Bag)
            return getId(stack);
        return 0;
    }

    public static String getOwner(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getString(OWNER_KEY) : "Unavailable";
    }

    /*
     * modal behavior
     */

    public static float getModeProperty(ItemStack stack, World world, Entity entity) {
        if (stack.getTag() == null) return 0;
        return ModeManager.getModeIndex(stack.getTag().getString("Mode"));
    }

    public static void changeModeRequest(ServerPlayerEntity player, int slot, boolean up) {
        ItemStack stack = IDimBagCommonItem.getItemFromPlayer(player, slot);
        if (stack == null || !(stack.getItem() instanceof Bag)) return;
        int id = getId(stack);
        if (id == 0) return;
        EyeData data = EyeData.get(id);
        if (data == null) return;
        ArrayList<String> modes = data.modeManager().getInstalledModes();
        for (int i = 0; i < modes.size(); ++i) {
            if (!modes.get(i).equals(data.modeManager().getSelectedMode())) continue;
            data.modeManager().selectMode((i + (up ? 1 : modes.size() - 1)) % modes.size());
            CompoundNBT nbt = stack.hasTag() ? stack.getTag() : new CompoundNBT();
            nbt.putString("Mode", modes.get((i + (up ? 1 : modes.size() - 1)) % modes.size()));
            stack.setTag(nbt);
            player.sendStatusMessage(new TranslationTextComponent("notification.bag.changed_mode", new TranslationTextComponent("bag.mode." + data.modeManager().getSelectedMode())), true);
            return;
        }
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
        int id = getId(stack);
        EyeData data = null;
        if (id == 0 || (data = EyeData.get(id)) == null)
            tooltip.add(new TranslationTextComponent("tooltip.bag.missing_id"));
        else {
            tooltip.add(new TranslationTextComponent("tooltip.bag.mode", new TranslationTextComponent("bag.mode." + stack.getTag().getString("Mode"))));
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

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        IDimBagCommonItem.sInventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
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
                data = EyeData.get(id);
            if (!stack.getTag().getString("Mode").equals(data.modeManager().getSelectedMode()))
                stack.getTag().putString("Mode", data.modeManager().getSelectedMode());
            data.setUser(entityIn);
            data.modeManager().inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
        }
    }

    public static BlockRayTraceResult rayTrace(World worldIn, PlayerEntity player, RayTraceContext.FluidMode fluidMode) {
        return Items.AIR.rayTrace(worldIn, player, fluidMode);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        return onItemUse(context.getWorld(), context.getPlayer(), IDimBagCommonItem.slotFromHand(context.getPlayer(), context.getHand()), new BlockRayTraceResult(context.getHitVec(), context.getFace(), context.getPos(), context.isInside()));
    }

    public ActionResultType onItemUse(World world, PlayerEntity player, int slot, BlockRayTraceResult ray) {
        int id;
        EyeData data;
        if (!DimBag.isServer(world) || (id = getId(player.inventory.getStackInSlot(slot))) == 0 || (data = EyeData.get(id)) == null) return ActionResultType.PASS;
        return data.modeManager().onItemUse(world, player, slot, ray);
    }

    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, int slot) {
        int id;
        EyeData data;
        if (!DimBag.isServer(world) || (id = getId(player.inventory.getStackInSlot(slot))) == 0 || (data = EyeData.get(id)) == null) return new ActionResult<>(ActionResultType.PASS, player.inventory.getStackInSlot(slot));
        return data.modeManager().onItemRightClick(world, player, slot);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        return onItemRightClick(world, player, IDimBagCommonItem.slotFromHand(player, hand));
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) {
        int id;
        EyeData data;
        if (!DimBag.isServer(player.world) || (id = getId(stack)) == 0 || (data = EyeData.get(id)) == null) return false;
        return data.modeManager().onAttack(stack, player, entity).isSuccessOrConsume();
    }
}
