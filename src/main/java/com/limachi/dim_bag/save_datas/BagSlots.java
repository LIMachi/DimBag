package com.limachi.dim_bag.save_datas;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.menus.SlotMenu;
import com.limachi.lim_lib.containers.ISlotAccessContainer;
import com.limachi.lim_lib.saveData.AbstractSyncSaveData;
import com.limachi.lim_lib.saveData.RegisterSaveData;
import com.limachi.lim_lib.saveData.SaveDataManager;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;

@RegisterSaveData
public class BagSlots extends AbstractSyncSaveData implements ISlotAccessContainer {

    private record SlotEntry(BlockPos pos, ItemStack content, Component label) {
        private SlotEntry withContent(ItemStack newContent) {
            return new SlotEntry(pos, newContent, label);
        }

        private SlotEntry withLabel(Component newLabel) {
            return new SlotEntry(pos, content, newLabel);
        }
    };

    private int bagId = 0;
    private final ArrayList<SlotEntry> stacks = new ArrayList<>();

    private final LinkedList<Function<BagSlots, Boolean>> listeners = new LinkedList<>();

    public BagSlots(String name) { super(name); }

    public static BagSlots getSlots(int bag) {
        BagSlots out = SaveDataManager.getInstance("bag_slots:" + bag, Level.OVERWORLD);
        if (out != null && out.bagId != bag)
            out.bagId = bag;
        return out;
    }

    public int getBagId() { return bagId; }

    public ISlotAccessContainer getSingleSlot(BlockPos slot) {
        final BagSlots parent = this;
        return new ISlotAccessContainer() {
            @Override
            public SlotAccess getSlotAccess(int i) { return parent.getSlotAccess(parent.getSlotByPos(slot)); }

            @Override
            public void setChanged() { parent.setChanged(); }

            @Override
            public int getSlots() { return 1; }

            @Override
            public boolean isItemValid(int s, @Nonnull ItemStack stack) {
                return parent.isItemValid(parent.getSlotByPos(slot), stack);
            }

            @Override
            public boolean stillValid(@Nonnull Player player) {
                return DimBag.getBagAccess(player, bagId, false, true, true) == bagId && parent.stacks.stream().anyMatch(p->p.pos().equals(slot));
            }
        };
    }

    @Nonnull
    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        ListTag list = new ListTag();
        for (SlotEntry p : stacks) {
            CompoundTag t = p.content.serializeNBT();
            t.putLong("pos", p.pos.asLong());
            t.putString("label", Component.Serializer.toJson(p.label));
            list.add(t);
        }
        compoundTag.put("stacks", list);
        return compoundTag;
    }

    @Override
    public void load(CompoundTag compoundTag) {
        stacks.clear();
        for (Tag t : compoundTag.getList("stacks", Tag.TAG_COMPOUND))
            if (t instanceof CompoundTag c)
                stacks.add(new SlotEntry(BlockPos.of(c.getLong("pos")), ItemStack.of(c), Component.Serializer.fromJson(c.getString("label"))));
    }

    public int getSlotByPos(BlockPos pos) {
        for (int i = 0; i < stacks.size(); ++i)
            if (stacks.get(i).pos.equals(pos))
                return i;
        return -1;
    }

    public BlockPos getSlot(int slot) {
        if (slot >= 0 && slot < stacks.size())
            return stacks.get(slot).pos;
        return null; //FIXME
    }

    @Override
    public SlotAccess getSlotAccess(int slot) {
        if (slot >= 0 && slot < stacks.size())
            return new SlotAccess() {
                @Override
                @Nonnull
                public ItemStack get() { return stacks.get(slot).content; }

                @Override
                public boolean set(@Nonnull ItemStack stack) {
                    stacks.set(slot, stacks.get(slot).withContent(stack));
                    return true;
                }
            };
        return SlotAccess.NULL;
    };

    @Override
    public int getSlots() { return stacks.size(); }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return true;
    }

    public void installSlot(BlockPos slot, ItemStack stack, Component label) {
        stacks.add(new SlotEntry(slot, stack, label));
        slotStatusChanged();
        setChanged();
    }

    public void renameSlot(BlockPos slot, Component label) {
        for (int i = 0; i < stacks.size(); ++i)
            if (stacks.get(i).pos.equals(slot))
                stacks.set(i, stacks.get(i).withLabel(label));
        slotStatusChanged();
        setChanged();
    }

    public Component getSlotName(BlockPos slot) {
        for (SlotEntry p : stacks)
            if (p.pos.equals(slot))
                return p.label;
        return SlotMenu.TITLE;
    }

    public Pair<ItemStack, Component> uninstallSlot(BlockPos slot) {
        final Pair<ItemStack, Component>[] out = new Pair[]{ null };
        stacks.removeIf(p->{if (p.pos.equals(slot)) { out[0] = new Pair<>(p.content, p.label); return true; } return false; });
        slotStatusChanged();
        setChanged();
        return out[0];
    }

    @Override
    public void setChanged() {
        ISlotAccessContainer.super.setChanged();
        setDirty();
    }

    public void addSlotListener(Function<BagSlots, Boolean> runnable) {
        listeners.add(runnable);
    }

    protected void slotStatusChanged() {
        listeners.removeIf(r->!r.apply(this));
    }
}
