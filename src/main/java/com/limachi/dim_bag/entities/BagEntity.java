package com.limachi.dim_bag.entities;

import com.limachi.dim_bag.items.BagItem;
import com.limachi.utils.Log;
import com.limachi.utils.Registries;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class BagEntity extends Mob {

    @Registries.RegisterEntity(width = 0.5f)
    public static RegistryObject<EntityType<BagEntity>> R_TYPE;

    @Registries.EntityAttributeBuilder
    public static AttributeSupplier.Builder attributes() {
        return LivingEntity.createLivingAttributes().add(Attributes.FOLLOW_RANGE, 16.);
    }

    private static final List<ItemStack> EMPTY_EQUIPMENT = Collections.emptyList();

    public static String ITEM_KEY = "BagItemStack";

    public BagEntity(EntityType<BagEntity> bagEntityEntityType, Level level) {
        super(bagEntityEntityType, level);
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    public void die(DamageSource cause) {
        Log.error("Something tried to remove this bag without authorization: " + getbagId() + " (cause: " + cause + ")");
    }

    @Override
    public Component getName() {
        ItemStack stack = getBagItem();
        if (stack.getItem() instanceof BagItem)
            return stack.getItem().getName(stack);
        return super.getName();
    }

    @Override
    protected void outOfWorld() {}

    public static BagEntity spawn(Level level, BlockPos position, ItemStack bag) {
        BagEntity out = new BagEntity(R_TYPE.get(), level);
        out.setPos(position.getX() + 0.5d, position.getY() + 0.5d, position.getZ() + 0.5d);
        out.setInvulnerable(true);
        out.requiresCustomPersistence();
        out.getPersistentData().put(ITEM_KEY, bag.save(new CompoundTag()));
        out.setCustomName(out.getName());
        out.setCustomNameVisible(true);
        level.addFreshEntity(out);
        return out;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) { return false; }

    @Override
    public boolean hurt(DamageSource source, float amount) { return false; }

    @Override
    public boolean ignoreExplosion() { return true; }

    @Override
    public ItemStack getPickResult() { return getBagItem(); }

    @Override
    public boolean shouldRiderSit() { return false; }

    @Override
    public boolean canRiderInteract() { return true; }

    @Override
    public boolean canBeRiddenInWater(Entity rider) { return true; }

    @Override
    protected void pickUpItem(ItemEntity itemEntity) { return; }

    public static List<ItemStack> getEmptyEquipment() { return EMPTY_EQUIPMENT; }

    @Override
    public Iterable<ItemStack> getHandSlots() { return EMPTY_EQUIPMENT; }

    @Override
    public Iterable<ItemStack> getArmorSlots() { return EMPTY_EQUIPMENT; }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        if (CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.equals(cap))
            return LazyOptional.empty(); //FIXME: remap capability to the bag content (bag proxy cap)
        return super.getCapability(cap);
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slotIn) { return ItemStack.EMPTY; }

    @Override
    public void setItemSlot(EquipmentSlot slotIn, ItemStack stack) {}

    @Override
    public boolean canPickUpLoot() { return false; }

    @Override
    public boolean canHoldItem(ItemStack p_175448_1_) { return false; }

    @Override
    public boolean canCollideWith(Entity entity) { return !(entity instanceof Player) && super.canCollideWith(entity); }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player.level.isClientSide()) return InteractionResult.PASS;
        int id = getbagId();
        if (id == 0) return InteractionResult.PASS;
//        if (KeyMapController.SNEAK.getState(player)) {
//            SubRoomsManager.execute(id, sm->sm.enterBag(player));
//        } else
//            SlotContainer.open(player, getbagId(), null);
        return InteractionResult.SUCCESS;
    }

    public ItemStack getBagItem() { return ItemStack.of(getPersistentData().getCompound(ITEM_KEY)); }

    public int getbagId() { return BagItem.getbagId(getBagItem()); }
}