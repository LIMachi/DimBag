package com.limachi.dimensional_bags.common.inventory;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Iterator;
import java.util.Set;
/*
public class EntityInventory implements IInventory {
    protected final Entity entity;

    public EntityInventory(Entity entity) {
        this.entity = entity;
    }

    public Iterator<ItemStack> getIterator() {
        return entity.getArmorSlots().iterator();
    }

    @Override
    public int getContainerSize() {
        int[] count = {0};
        entity.getEquipmentAndArmor().forEach(t->++count[0]);
        return count[0];
    }

    @Override
    public boolean isEmpty() {
        boolean[] empty = {true};
        entity.getEquipmentAndArmor().forEach(t->{if (empty[0] && !t.isEmpty()) empty[0] = false; });
        return empty[0];
    }

    @Override
    public ItemStack removeItemNoUpdate(int p_70304_1_) {
        return null;
    }

    @Override
    public ItemStack getItem(int index) {
        Iterator<ItemStack> it = getIterator();
        for (int i = 0; i < index; ++i) {
            if (!it.hasNext()) break;
                it.next();
        }
        return it.hasNext() ? it.next() : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        return null;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return null;
    }

    @Override
    public void setItem(int index, ItemStack stack) {

    }

    @Override
    public int getInventoryStackLimit() {
        return 0;
    }

    @Override
    public void setChanged() {

    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        return false;
    }

    @Override
    public void openInventory(PlayerEntity player) {

    }

    @Override
    public void closeInventory(PlayerEntity player) {

    }

    @Override
    public boolean mayPlaceForSlot(int index, ItemStack stack) {
        return false;
    }

    @Override
    public int count(Item itemIn) {
        return 0;
    }

    @Override
    public boolean hasAny(Set<Item> set) {
        return false;
    }

    @Override
    public void clearContent() {

    }
}*/
