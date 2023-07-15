package com.limachi.dim_bag.menus;

import com.limachi.dim_bag.menus.slots.BagSlot;
import com.limachi.dim_bag.save_datas.BagSlots;
import com.limachi.lim_lib.menus.IAcceptUpStreamNBT;
import com.limachi.lim_lib.registries.annotations.RegisterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
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
import java.util.ArrayList;

public class SlotMenu extends AbstractContainerMenu implements IAcceptUpStreamNBT {

    @RegisterMenu
    public static RegistryObject<MenuType<SlotMenu>> R_TYPE;

    public static final Component TITLE = Component.translatable("block.dim_bag.slot_module");

    public static void open(Player player, int bag, BlockPos slot) {
        if (!player.level().isClientSide)
            NetworkHooks.openScreen((ServerPlayer) player, new SimpleMenuProvider((id, inv, p)->new SlotMenu(id, inv, bag, slot), BagSlots.getSlots(bag).getSlotName(slot)));
    }

    public static class SlotsSectionManager {
        private record Entry(int min, int max, boolean direction, boolean order, int jump){}

        private final ArrayList<Entry> entries = new ArrayList<>();
        private int total_slots = 0;

        public SlotsSectionManager() {}

        public void newSection(int slots, boolean quickMoveAbove) { newSection(slots, quickMoveAbove, !quickMoveAbove, 0); }
        public void newSection(int slots, boolean quickMoveAbove, boolean fromLastSlot) { newSection(slots, quickMoveAbove, fromLastSlot, 0); }
        public void newSection(int slots, boolean quickMoveAbove, int jumpSections) { newSection(slots, quickMoveAbove, !quickMoveAbove, jumpSections); }
        public void newSection(int slots, boolean quickMoveAbove, boolean fromLastSlot, int jumpSections) {
            entries.add(new Entry(total_slots, slots, quickMoveAbove, fromLastSlot, jumpSections));
            total_slots = slots;
        }

        @FunctionalInterface
        public interface MoveItemStackTo {
            boolean apply(ItemStack stack, int min, int max, boolean up);
        }

        public ItemStack quickMoveStack(AbstractContainerMenu menu, int slot, MoveItemStackTo mover) {
            ItemStack out = ItemStack.EMPTY;
            Slot s = menu.slots.get(slot);
            if (s.hasItem()) {
                ItemStack stack = s.getItem();
                out = stack.copy();
                for (int i = 0; i < entries.size(); ++i) {
                    Entry entry = entries.get(i);
                    if (slot >= entry.min && slot < entry.max) {
                        int i2 = i;
                        do {
                            int delta = 1 + (i2 == i ? entry.jump : 0);
                            if (entry.direction) {
                                i2 -= delta;
                                while (i2 < 0)
                                    i2 += entries.size();
                            } else {
                                i2 += delta;
                                while (i2 >= entries.size())
                                    i2 -= entries.size();
                            }
                            Entry target = entries.get(i2);
                            if (!mover.apply(stack, target.min, target.max, entry.order))
                                return ItemStack.EMPTY;
                        } while (i2 != i);
                        break;
                    }
                }

                if (stack.isEmpty())
                    s.setByPlayer(ItemStack.EMPTY);
                else
                    s.setChanged();
            }

            return out;
        }
    }

    private final SlotsSectionManager sections = new SlotsSectionManager();

    public SlotMenu(int id, Inventory playerInventory, int bag, BlockPos slot) {
        super(R_TYPE.get(), id);

        addSlot(bag > 0 && slot != null ? new BagSlot(bag, slot, 79, 36, s->true) : new BagSlot(0, slot, 79, 36, s->true));

        sections.newSection(slots.size(), false, false);

        for(int l = 0; l < 3; ++l) {
            for(int j1 = 0; j1 < 9; ++j1)
                this.addSlot(new Slot(playerInventory, j1 + l * 9 + 9, 8 + j1 * 18, 85 + l * 18));
        }

        sections.newSection(slots.size(), true);

        for(int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 143));
        }

        sections.newSection(slots.size(), false);
    }

    public SlotMenu(int id, Inventory playerInventory, FriendlyByteBuf buff) {
        this(id, playerInventory, 0, null);
    }

    private static Container containerFromBagSlot(int bag, BlockPos slot) {
        BagSlots slots = BagSlots.getSlots(bag);
        if (slots != null)
            return slots.getSingleSlot(slot);
        return new SimpleContainer(1);
    }

    @Override
    @Nonnull
    public ItemStack quickMoveStack(@Nonnull Player player, int index) {
        return sections.quickMoveStack(this, index, this::moveItemStackTo);
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return slots.get(0).container.stillValid(player); //FIXME: wrong check
    }

    @Override
    public void upstreamNBTMessage(int i, CompoundTag compoundTag) {
        if (slots.get(0) instanceof BagSlot s)
            BagSlots.getSlots(s.bag).renameSlot(s.slot, Component.Serializer.fromJson(compoundTag.getString("label")));
    }
}
