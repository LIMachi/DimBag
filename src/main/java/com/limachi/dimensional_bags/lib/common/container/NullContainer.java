package com.limachi.dimensional_bags.lib.common.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public abstract class NullContainer extends BaseContainer<NullContainer> {

    public static final class NullPlayerInventory extends PlayerInventory {
        public static final NullPlayerInventory NULL_PLAYER_CONTAINER = new NullPlayerInventory();

        public NullPlayerInventory() { super(null); }

        @Override
        public boolean add(int slotIn, ItemStack stack) { return false; }

        @Override
        public void placeItemBackInInventory(World worldIn, ItemStack stack) {}

        @Override
        public void hurtArmor(DamageSource p_234563_1_, float p_234563_2_) {}

        @Override
        public void dropAll() {}

        @Override
        public boolean stillValid(PlayerEntity player) { return true; }
    }

    public static final NullContainer NULL_CONTAINER = new NullContainer(null, -1, NullPlayerInventory.NULL_PLAYER_CONTAINER){};

    protected NullContainer(ContainerType<?> containerType, int windowId, PlayerInventory playerInv) { super(containerType, windowId, playerInv); }
    protected NullContainer(ContainerType<?> containerType, int windowId, PlayerInventory playerInv, @Nullable PacketBuffer buffer) { super(containerType, windowId, playerInv, buffer); }

    @Override
    final public ITextComponent getDisplayName() { return StringTextComponent.EMPTY; }

    @Override
    final public boolean stillValid(PlayerEntity playerIn) { return true; }

    @Override
    final public void readFromBuff(PacketBuffer buff) {}

    @Override
    final public void writeToBuff(PacketBuffer buff) {}
}
