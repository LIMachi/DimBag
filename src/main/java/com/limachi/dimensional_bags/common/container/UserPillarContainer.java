package com.limachi.dimensional_bags.common.container;

import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.Registries;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.HolderData;
import com.limachi.dimensional_bags.common.inventory.EntityInventoryProxy;
import com.limachi.dimensional_bags.common.inventory.IEntityInventoryProxyIsActiveSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nonnull;

import static com.limachi.dimensional_bags.common.inventory.EntityInventoryProxy.TOTAL_INVENTORY_SIZE;
import static com.limachi.dimensional_bags.common.references.GUIs.PlayerInterface.*;
import static com.limachi.dimensional_bags.common.references.GUIs.ScreenParts.*;

@StaticInit
public class UserPillarContainer extends BaseEyeContainer<UserPillarContainer> {

    public static final String NAME = "user_pillar";

    IEntityInventoryProxyIsActiveSlot target;

    static {
        Registries.registerContainer(NAME, UserPillarContainer::new);
    }

    public static void open(PlayerEntity player, int eye) {
        if (player instanceof ServerPlayerEntity)
            BaseContainer.open(player, new UserPillarContainer(((ServerPlayerEntity)player).containerCounter + 1, player.inventory, eye));
    }

    public UserPillarContainer(int windowId, PlayerInventory playerInv, PacketBuffer extraData) {
        super(Registries.getContainerType(NAME), windowId, playerInv, extraData);
        init();
    }

    private UserPillarContainer(int windowId, PlayerInventory playerInv, int eye) {
        super(Registries.getContainerType(NAME), windowId, playerInv, eye);
        target = HolderData.execute(eye, HolderData::getEntityInventory, new EntityInventoryProxy());
        init();
    }

    @Override
    public void writeToBuff(PacketBuffer buff) {
        super.writeToBuff(buff);
        boolean s = playerInv.player.equals(target.getEntity());
        buff.writeBoolean(s);
        if (!s)
            for (int i = 0; i < TOTAL_INVENTORY_SIZE; ++i)
                buff.writeBoolean(target.isActiveSlot(i));
    }

    @Override
    public void readFromBuff(PacketBuffer buff) {
        super.readFromBuff(buff);
        target = buff.readBoolean() ? new EntityInventoryProxy(playerInv.player) : new EntityInventoryProxy.EntityInventoryMirror(buff);
    }

    public String targetName() { return target.getEntityName(); }

    public static class EntityInventoryProxySlot extends Slot {

        protected IEntityInventoryProxyIsActiveSlot proxy;

        public EntityInventoryProxySlot(IEntityInventoryProxyIsActiveSlot inventoryIn, int index, int xPosition, int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
            this.proxy = inventoryIn;
        }

        public boolean isActive() { return proxy.isActiveSlot(getSlotIndex()); }

        @Override
        public boolean mayPickup(PlayerEntity playerIn) { return isActive() && super.mayPickup(playerIn); }

        @Override
        public void set(ItemStack stack) { if (isActive()) super.set(stack); }

        @Override
        public boolean mayPlace(ItemStack stack) { return isActive() && super.mayPlace(stack); }

        @Override
        public ItemStack onTake(PlayerEntity thePlayer, ItemStack stack) { return isActive() ? super.onTake(thePlayer, stack) : ItemStack.EMPTY; }

        @Override
        public ItemStack remove(int amount) { return isActive() ? super.remove(amount) : ItemStack.EMPTY; }
    }

    protected void init() {
        addPlayerInventory(PLAYER_INVENTORY_FIRST_SLOT_X + 1, PLAYER_INVENTORY_PART_Y + PLAYER_INVENTORY_FIRST_SLOT_Y + 1);
        for (int x = 0; x < 9; ++x)
            addSlot(new EntityInventoryProxySlot(target, x, BELT_X + 1 + x * SLOT_SIZE_X, BELT_Y + 1));
        for (int y = 0; y < 3; ++y)
            for (int x = 0; x < 9; ++x)
                addSlot(new EntityInventoryProxySlot(target, x + 9 * (y + 1), MAIN_INVENTORY_X + 1 + x * SLOT_SIZE_X, MAIN_INVENTORY_Y + 1 + y * SLOT_SIZE_Y));
        for (int x = 0; x < 4; ++x)
            addSlot(new EntityInventoryProxySlot(target, 36 + x, ARMOR_SLOTS_X + 1 + x * SLOT_SIZE_X, SPECIAL_SLOTS_Y + 1));
        addSlot(new EntityInventoryProxySlot(target, 40, OFF_HAND_SLOT_X + 1, SPECIAL_SLOTS_Y + 1));
    }

    protected void addPlayerInventory(int x, int y) {
//        this.inventoryStart = this.inventorySlots.size();
        for (int i = 0; i < 9; ++i)
//            if (disabledSlots().contains(i))
//                addSlot(new LockedSlot(playerInv, i, x + i * SLOT_SIZE_X, y + 4 + 3 * SLOT_SIZE_Y));
//            else
                addSlot(new Slot(playerInv, i, x + i * SLOT_SIZE_X, y + 4 + 3 * SLOT_SIZE_Y));
        for (int ty = 0; ty < 3; ++ty)
            for (int tx = 0; tx < 9; ++tx)
//                if (disabledSlots().contains(x + y * 9 + 9))
//                    addSlot(new LockedSlot(playerInv, tx + ty * 9 + 9, x + tx * SLOT_SIZE_X, y + ty * SLOT_SIZE_Y));
//                else
                    addSlot(new Slot(playerInv, tx + ty * 9 + 9, x + tx * SLOT_SIZE_X, y + ty * SLOT_SIZE_Y));
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() { return new TranslationTextComponent("container.display_name." + NAME); }
}
