package com.limachi.dim_bag.menus;

import com.limachi.dim_bag.bag_data.TankData;
import com.limachi.dim_bag.menus.slots.BagTankSlot;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.lim_lib.menus.IAcceptUpStreamNBT;
import com.limachi.lim_lib.registries.annotations.RegisterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;

public class TankMenu extends AbstractContainerMenu implements IAcceptUpStreamNBT {

    @RegisterMenu
    public static RegistryObject<MenuType<TankMenu>> R_TYPE;

    public static void open(Player player, int bag, BlockPos slot) {
        if (!player.level().isClientSide)
            NetworkHooks.openScreen((ServerPlayer) player, new SimpleMenuProvider((id, inv, p)->new TankMenu(id, inv, bag, slot), BagsData.runOnBag(bag, b->b.getTankLabel(slot), TankData.DEFAULT_TANK_LABEL)));
    }

    public TankMenu(int id, Inventory playerInventory, int bag, BlockPos slot) {
        super(R_TYPE.get(), id);

        addSlot(bag > 0 && slot != null ? new BagTankSlot(bag, slot, 79, 36, s->true) : new BagTankSlot(79, 36, s->true));

        for (int l = 0; l < 3; ++l)
            for (int j1 = 0; j1 < 9; ++j1)
                addSlot(new Slot(playerInventory, j1 + l * 9 + 9, 8 + j1 * 18, 85 + l * 18));

        for (int i1 = 0; i1 < 9; ++i1)
            addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 143));
    }

    public TankMenu(int id, Inventory playerInventory, FriendlyByteBuf buff) { this(id, playerInventory, 0, null); }

    @Override
    @Nonnull
    public ItemStack quickMoveStack(@Nonnull Player player, int index) {
//        return sections.quickMoveStack(this, index, this::moveItemStackTo);
        return ItemStack.EMPTY; //FIXME: should reimplement the fluids shift click system
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return slots.get(0).container.stillValid(player); //FIXME: wrong check
    }

    @Override
    public void upstreamNBTMessage(int i, CompoundTag compoundTag) {
        if (slots.get(0) instanceof BagTankSlot s && s.tank != null)
            BagsData.runOnBag(s.bag, b->b.setTankLabel(s.tank, Component.Serializer.fromJson(compoundTag.getString("label"))));
    }
}
