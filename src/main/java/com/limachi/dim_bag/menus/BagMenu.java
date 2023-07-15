package com.limachi.dim_bag.menus;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.bag_data.SlotData;
import com.limachi.dim_bag.menus.slots.BagSlot;
import com.limachi.dim_bag.menus.slots.EquipSlot;
import com.limachi.dim_bag.save_datas.BagsData;
import com.limachi.lim_lib.menus.IAcceptUpStreamNBT;
import com.limachi.lim_lib.registries.annotations.RegisterMenu;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;

public class BagMenu extends AbstractContainerMenu implements IAcceptUpStreamNBT {

    @RegisterMenu
    public static RegistryObject<MenuType<BagMenu>> R_TYPE;

    public final int bagId;
    public final int page;
    private LazyOptional<SlotData> slotsHandle;

    public static final Component TITLE = Component.literal("test");

    public static void open(Player player, int bagId, int page) {
        if (!player.level().isClientSide && bagId > 0)
            NetworkHooks.openScreen((ServerPlayer)player, new SimpleMenuProvider((id, inv, p)->new BagMenu(id, inv, bagId, page), TITLE), buff->buff.writeInt(page));
    }

    public BagMenu(int id, Inventory playerInventory, FriendlyByteBuf buff) {
        this(id, playerInventory, 0, buff.readInt());
    }

    private final SlotMenu.SlotsSectionManager sections = new SlotMenu.SlotsSectionManager();
    private double ds = 0.;
    private int scroll = 0;

    @Override
    public void upstreamNBTMessage(int i, CompoundTag compoundTag) {
        ds = compoundTag.getDouble("scroll");
        updateScroll();
    }

    private LazyOptional<SlotData> handle() {
        if (slotsHandle == null)
            slotsHandle = LazyOptional.empty();
        if (!slotsHandle.isPresent())
            BagsData.runOnBag(bagId, b-> slotsHandle = b.slotsHandle());
        return slotsHandle;
    }

    void updateScroll() {
        handle().ifPresent(s->{
            int l = s.getSlots();
            int t = 0;
            if (l > 36)
                t = 9 * (int) Math.round((double) ((l - 36) / 9 + 1) * ds);
            if (t != scroll) {
                scroll = t;
                slotStatesChanged();
            }
        });
    }

    public void slotStatesChanged() {
        handle().ifPresent(bagSlots->{
            updateScroll();
            int i;
            for (i = 0; i + scroll < bagSlots.getSlots() && i < 36; ++i)
                if (slots.get(i) instanceof BagSlot s)
                    s.changeSlotServerSide(bagSlots.getSlot(i + scroll));
            for (; i < slots.size(); ++i)
                if (slots.get(i) instanceof BagSlot s)
                    s.changeSlotServerSide(null);
            slotStates.reload(slots);
        });
    }

    private static class SlotStates implements ContainerData {
        int []data = {0, 0, 0};

        @Override
        public int get(int index) { return data[index]; }

        @Override
        public void set(int index, int value) { data[index] = value; }

        @Override
        public int getCount() { return 3; }

        public boolean getActive(int index) { return (data[index / 16] & (1 << (index % 16))) != 0; }

        public void setActive(int index, boolean state) {
            if (getActive(index) != state)
                data[index / 16] ^= 1 << (index % 16);
        }

        public void reload(NonNullList<Slot> slots) {
            for (int i = 0; i < 36; ++i)
                if (slots.get(i) instanceof BagSlot s)
                    setActive(i, s.isActive());
        }
    }

    private final SlotStates slotStates = new SlotStates();

    public BagMenu(int id, Inventory playerInventory, int bagId, int page) {
        super(R_TYPE.get(), id);
        this.page = page;
        this.bagId = bagId;

        LazyOptional<SlotData> bag = handle();
        if (bag.isPresent())
            for (int l = 0; l < 4; ++l)
                for (int c = 0; c < 9; ++c)
                    addSlot(new BagSlot(bagId, bag.resolve().get().getSlot(l * 9 + c), 29 + c * 18, 17 + l * 18, s -> s.getItemHandler().getSlots() > 0));
        else
            for (int l = 0; l < 4; ++l)
                for (int c = 0; c < 9; ++c) {
                    final int index = l * 9 + c;
                    addSlot(new BagSlot(0, null, 29 + c * 18, 17 + l * 18, s -> slotStates.getActive(index)));
                }

        sections.newSection(slots.size(), false, false);

        for (int i = 0; i < 4; ++i)
            addSlot(EquipSlot.armor(playerInventory.player, i, 7, 71 - 18 * i));

        for (int l = 0; l < 3; ++l)
            for (int j1 = 0; j1 < 9; ++j1)
                addSlot(new Slot(playerInventory, j1 + l * 9 + 9, 29 + j1 * 18, 102 + l * 18));

        for(int i1 = 0; i1 < 9; ++i1)
            this.addSlot(new Slot(playerInventory, i1, 29 + i1 * 18, 160));

        addSlot(EquipSlot.shield(playerInventory.player, 7, 102));

        sections.newSection(slots.size(), false);

        addDataSlots(slotStates);

        if (bagId != 0) {
            int[] initStates = new int[]{0, 0, 0};

            for (int i = 0; i < 36; ++i)
                if (slots.get(i) instanceof BagSlot slot && slot.isActive())
                    initStates[i / 16] |= 1 << (i % 16);

            slotStates.set(0, initStates[0]);
            slotStates.set(1, initStates[1]);
            slotStates.set(2, initStates[2]);
        }
    }

    @Override
    @Nonnull
    public ItemStack quickMoveStack(@Nonnull Player player, int index) {
        return sections.quickMoveStack(this, index, this::moveItemStackTo);
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return player.containerMenu.containerId == containerId && bagId != 0 && DimBag.getBagAccess(player, bagId, false, true, true) == bagId;
    }
}
