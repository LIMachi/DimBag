package com.limachi.dimensional_bags.common.entities;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.KeyMapController;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.container.PillarContainer;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.HolderData;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.SubRoomsManager;
import com.limachi.dimensional_bags.common.data.IEyeIdHolder;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.managers.ModeManager;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import static com.limachi.dimensional_bags.DimBag.MOD_ID;

import com.limachi.dimensional_bags.StaticInit;

import java.util.Collections;
import java.util.List;

@StaticInit
public class BagEntity extends MobEntity implements IEyeIdHolder {

    public static final String NAME = "bag_entity";

    static {
        Registries.registerEntityType(NAME, () -> EntityType.Builder.<BagEntity>of(BagEntity::new, EntityClassification.MISC).sized(0.5f, 1f).build(new ResourceLocation(MOD_ID, "bag_entity").toString()));
    }

    private static final List<ItemStack> EMPTY_EQUIPMENT = Collections.emptyList();

    public static String ITEM_KEY = "BagItemStack";

    public BagEntity(EntityType<? extends MobEntity> type, World world) {
        super(type, world);
    }

    public static AttributeModifierMap.MutableAttribute getAttributeMap() {
        return LivingEntity.createLivingAttributes().add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    public void die(DamageSource cause) {
        LOGGER.error("Something tried to remove this bag without authorization: " + getEyeId() + " (cause: " + cause + ")");
    }

    @Override
    public ITextComponent getName() {
        ItemStack stack = getBagItem();
        if (stack.getItem() instanceof Bag)
            return new TranslationTextComponent(stack.getItem().getDescriptionId(stack));
        return super.getName();
    }

    @Override
    protected void outOfWorld() {}

    public static BagEntity spawn(World world, BlockPos position, ItemStack bag) {
        BagEntity out = new BagEntity(Registries.getEntityType(NAME), world);
        out.setPos(position.getX() + 0.5d, position.getY() + 0.5d, position.getZ() + 0.5d);
        out.setInvulnerable(true);
        out.requiresCustomPersistence();
        out.getPersistentData().put(ITEM_KEY, bag.save(new CompoundNBT()));
        out.setCustomName(out.getName());
        out.setCustomNameVisible(true);
        world.addFreshEntity(out);
        return out;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) { return false; }

    @Override
    public boolean hurt(DamageSource source, float amount) { return false; }

    @Override
    public boolean ignoreExplosion() { return true; }

    @Override
    public ItemStack getPickedResult(RayTraceResult target) { return getBagItem(); }

    @Override
    public boolean shouldRiderSit() { return false; }

    @Override
    public boolean canRiderInteract() { return true; }

    @Override
    public boolean canBeRiddenInWater(Entity rider) { return true; }

    @Override
    public EntityClassification getClassification(boolean forSpawnCount) { return null; }

    @Override
    protected void pickUpItem(ItemEntity itemEntity) { return; }

    @Override
    public boolean checkSpawnRules(IWorld worldIn, SpawnReason spawnReasonIn) { return false; }

    public static List<ItemStack> getEmptyEquipment() {
        return EMPTY_EQUIPMENT;
    }

    @Override
    public Iterable<ItemStack> getHandSlots() { return EMPTY_EQUIPMENT; }

    @Override
    public Iterable<ItemStack> getArmorSlots() { return EMPTY_EQUIPMENT; }

    @Override
    public ItemStack getItemBySlot(EquipmentSlotType slotIn) { return ItemStack.EMPTY; }

    @Override
    public void setItemSlot(EquipmentSlotType slotIn, ItemStack stack) {}

    @Override
    public boolean canPickUpLoot() { return false; }

    @Override
    public boolean canHoldItem(ItemStack p_175448_1_) { return false; }

    @Override
    public boolean canCollideWith(Entity entity) { return !(entity instanceof PlayerEntity) && super.canCollideWith(entity); }

    @Override
    public boolean setSlot(int inventorySlot, ItemStack itemStackIn) { return false; }

    @Override
    public void tick() {
        super.tick();
        int eyeId = getEyeId();
        if (eyeId > 0) {
            if (DimBag.isServer(level))
                HolderData.execute(eyeId, holderData->holderData.setHolder(this));
            ModeManager.execute(eyeId, modeManager -> modeManager.inventoryTick(level, this, true));
            UpgradeManager.execute(eyeId, upgradeManager -> upgradeManager.inventoryTick(level, this));
        }
    }

    @Override
    protected ActionResultType mobInteract(PlayerEntity player, Hand hand) {
        if (player.level.isClientSide()) return ActionResultType.PASS;
        int id = getEyeId();
        if (id == 0) return ActionResultType.PASS;
        if (KeyMapController.KeyBindings.SNEAK_KEY.getState(player)) {
            SubRoomsManager.execute(id, sm->sm.enterBag(player));
        } else
            PillarContainer.open(player, getEyeId(), null);
        return ActionResultType.SUCCESS;
    }

    public ItemStack getBagItem() { return ItemStack.of(getPersistentData().getCompound(ITEM_KEY)); }

    @Override
    public int getEyeId() { return Bag.getEyeId(getBagItem()); }
}
