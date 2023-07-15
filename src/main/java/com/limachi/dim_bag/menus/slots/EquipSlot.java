package com.limachi.dim_bag.menus.slots;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import javax.annotation.Nonnull;

public class EquipSlot extends SlotAccessSlot {
    public static final ResourceLocation[] EMPTY_ARMOR_SLOTS = {InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD};
    EquipmentSlot slot;
    LivingEntity entity;

    static public EquipSlot armor(LivingEntity entity, int armorSlot, int x, int y) {
        return new EquipSlot(entity, EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, armorSlot), x, y);
    }

    static public EquipSlot shield(LivingEntity entity, int x, int y) {
        return new EquipSlot(entity, EquipmentSlot.OFFHAND, x, y);
    }

    static public EquipSlot mainHand(LivingEntity entity, int x, int y) {
        return new EquipSlot(entity, EquipmentSlot.MAINHAND, x, y);
    }

    protected EquipSlot(LivingEntity entity, EquipmentSlot slot, int x, int y) {
        super(SlotAccess.forEquipmentSlot(entity, slot), x, y);
        this.slot = slot;
        this.entity = entity;
    }

    public void setByPlayer(@Nonnull ItemStack stack) {
        if (Equipable.get(stack) != null)
            entity.onEquipItem(slot, getItem(), stack);
        super.setByPlayer(stack);
    }

    @Override
    public int getMaxStackSize() {
        return slot.isArmor() ? 1 : super.getMaxStackSize();
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        return slot.isArmor() ? stack.canEquip(slot, entity) : super.mayPlace(stack);
    }

    @Override
    public boolean mayPickup(@Nonnull Player player) {
        if (!slot.isArmor()) return super.mayPickup(player);
        ItemStack itemstack = this.getItem();
        return (itemstack.isEmpty() || player.isCreative() || !EnchantmentHelper.hasBindingCurse(itemstack)) && super.mayPickup(player);
    }

    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        if (slot.isArmor())
            return Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_ARMOR_SLOTS[slot.getIndex()]);
        else if (slot.getIndex() == EquipmentSlot.OFFHAND.getIndex())
            return Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_ARMOR_SLOTS[4]);
        return super.getNoItemIcon();
    }
}
