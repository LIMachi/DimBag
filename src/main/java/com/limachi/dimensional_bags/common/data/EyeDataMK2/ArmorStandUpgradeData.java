package com.limachi.dimensional_bags.common.data.EyeDataMK2;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.WorldUtils;
import com.limachi.dimensional_bags.common.container.BaseContainer;
import com.limachi.dimensional_bags.common.items.Bag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nullable;

/*
public class ArmorStandUpgradeData extends WorldSavedData implements INamedContainerProvider {
    private ItemStack chestplate = ItemStack.EMPTY;
    private ItemStack elytra = ItemStack.EMPTY;

    public ArmorStandUpgradeData(int id) {
        super(DimBag.MOD_ID + "_eye_" + id + "_armor_stand_upgrade_data");
    }

    public ItemStack getChestplate() { return chestplate.copy(); }

    public ItemStack getElytra() { return elytra.copy(); }

    public void setElytra(ItemStack elytra) {
        if (elytra.getItem() instanceof ElytraItem) {
            this.elytra = elytra;
            markDirty();
        }
    }

    public void setChestplate(ItemStack chestplate) {
        Item item = chestplate.getItem();
        if (item instanceof ArmorItem && !(item instanceof Bag)) {
            this.chestplate = chestplate;
            markDirty();
        }
    }

    @Override
    public void read(CompoundNBT nbt) {
        chestplate = ItemStack.read(nbt.getCompound("Chestplate"));
        elytra = ItemStack.read(nbt.getCompound("Elytra"));
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        nbt.put("Chestplate", chestplate.write(new CompoundNBT()));
        nbt.put("Elytra", elytra.write(new CompoundNBT()));
        return nbt;
    }

    static public ArmorStandUpgradeData getInstance(@Nullable ServerWorld world, int id) {
        if (id <= 0) return null;
        if (world == null)
            world = WorldUtils.getOverWorld();
        if (world != null)
            return world.getSavedData().getOrCreate(()->new ArmorStandUpgradeData(id), DimBag.MOD_ID + "_eye_" + id + "_armor_stand_upgrade_data");
        return null;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("inventory.armor_stand_upgrade.name");
    }

    @Nullable
    @Override
    public Container createMenu(int windowId, PlayerInventory playerInv, PlayerEntity player) {
        return new BaseContainer(Registries.) {
            @Override
            public void init() {

            }
        };
    }
}
*/