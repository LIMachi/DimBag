package com.limachi.dimensional_bags.common.inventory;

import com.limachi.dimensional_bags.DimBag;
import javafx.util.Pair;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.EmptyHandler;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Function;
/*
public class DynamicMultiProxyInventory implements ISimpleMultiHandlerSerializable {

    protected final ArrayList<UUID> orderedProxiesUUID = new ArrayList<>();
    protected final HashMap<UUID, WeakReference<ISimpleMultiHandlerSerializable>> proxies = new HashMap<>();
    protected final HashSet<UUID> activeProxies = new HashSet<>();
    protected ArrayList<WeakReference<ISimpleMultiHandlerSerializable>> sortedActiveProxies = null;
    protected Function<UUID, ISimpleMultiHandlerSerializable> queryProxy; //how this inventory is supposed to repopulate proxies is left to the discretion of the creator of the instance

    @Override
    public void readFromBuff(PacketBuffer buff) {

    }

    @Override
    public void writeToBuff(PacketBuffer buff) {

    }

    public DynamicMultiProxyInventory() {}

    public DynamicMultiProxyInventory(@Nonnull Function<UUID, ISimpleMultiHandlerSerializable> queryProxy) {
        this.queryProxy = queryProxy;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT out = new CompoundNBT();
        ListNBT l1 = new ListNBT();
        for (UUID id : orderedProxiesUUID) {
            CompoundNBT c = new CompoundNBT();
            c.putUniqueId("UUID", id);
            l1.add(c);
        }
        out.put("OrderedProxies", l1);
        ListNBT l2 = new ListNBT();
        for (UUID id : activeProxies) {
            CompoundNBT c = new CompoundNBT();
            c.putUniqueId("UUID", id);
            l2.add(c);
        }
        out.put("ActiveProxies", l2);
        return out;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        orderedProxiesUUID.clear();
        ListNBT l1 = nbt.getList("OrderedProxies", 10);
        for (int i = 0; i < l1.size(); ++i)
            orderedProxiesUUID.add(i, l1.getCompound(i).getUniqueId("UUID"));
        activeProxies.clear();
        ListNBT l2 = nbt.getList("ActiveProxies", 10);
        for (int i = 0; i < l2.size(); ++i) {
            UUID id = l2.getCompound(i).getUniqueId("UUID");
            ISimpleMultiHandlerSerializable t = queryProxy.apply(id);
            if (t != null) {
                activeProxies.add(id);
                proxies.put(id, new WeakReference<>(t));
            }
        }
        rebuildSortedActiveInventories();
    }

    private void rebuildSortedActiveInventories() {
        sortedActiveProxies = new ArrayList<>();
        for (UUID id : orderedProxiesUUID)
            if (activeProxies.contains(id)) {
                WeakReference<ISimpleMultiHandlerSerializable> p = proxies.get(id);
                if (p.get() == null)
                    activeProxies.remove(id);
                else
                    sortedActiveProxies.add(p);
            }
    }

    public void addProxy(UUID id, ISimpleMultiHandlerSerializable inventory) {
        if (!orderedProxiesUUID.contains(id))
            orderedProxiesUUID.add(orderedProxiesUUID.size(), id);
        activeProxies.add(id);
        proxies.put(id, new WeakReference<>(inventory));
        rebuildSortedActiveInventories();
    }

    public void enableProxy(UUID id) {
        if (orderedProxiesUUID.contains(id)) {
            activeProxies.add(id);
            rebuildSortedActiveInventories();
        }
    }

    public void disabledProxy(UUID id) {
        if (activeProxies.contains(id)) {
            activeProxies.remove(id);
            rebuildSortedActiveInventories();
        }
    }

    public ISimpleMultiHandlerSerializable getProxy(UUID id) {
        WeakReference<ISimpleMultiHandlerSerializable> t = proxies.get(id);
        if (t == null) return null;
        return t.get();
    }

    protected ArrayList<WeakReference<ISimpleMultiHandlerSerializable>> getProxies() {
        if (sortedActiveProxies == null)
            rebuildSortedActiveInventories();
        return sortedActiveProxies;
    }

    protected Pair<Integer, Integer> getProxySlot(int slot) {
        if (slot < 0) return null;
        int t = 0;
        for (WeakReference<ISimpleMultiHandlerSerializable> w : getProxies()) {
            ISimpleMultiHandlerSerializable p = w.get();
            if (p != null) {
                int s = p.getSlots();
                if (slot < s)
                    return new Pair<>(t, slot);
                slot -= s;
            } else {
                DimBag.LOGGER.error(this.getClass() + ": getProxySlot: WeakReference<IItemHandlerModifiable> returned null!");
            }
            ++t;
        }
        return null;
    }

    protected IItemHandlerModifiable getProxy(int index) {
        IItemHandlerModifiable p = getProxies().get(index).get();
        if (p == null)
            p = new EmptyHandler();
        return p;
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        Pair<Integer, Integer> slotId = getProxySlot(slot);
        if (slotId != null)
            getProxy(slotId.getKey()).setStackInSlot(slotId.getValue(), stack);
    }

    @Override
    public int getSlots() {
        int c = 0;
        for (WeakReference<ISimpleMultiHandlerSerializable> p : getProxies()) {
            IItemHandlerModifiable h = p.get();
            if (h != null)
                c += h.getSlots();
        }
        return c;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        Pair<Integer, Integer> slotId = getProxySlot(slot);
        if (slotId != null)
            return getProxy(slotId.getKey()).getStackInSlot(slotId.getValue());
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        Pair<Integer, Integer> slotId = getProxySlot(slot);
        if (slotId != null)
            return getProxy(slotId.getKey()).insertItem(slotId.getValue(), stack, simulate);
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        Pair<Integer, Integer> slotId = getProxySlot(slot);
        if (slotId != null)
            return getProxy(slotId.getKey()).extractItem(slotId.getValue(), amount, simulate);
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        Pair<Integer, Integer> slotId = getProxySlot(slot);
        if (slotId != null)
            return getProxy(slotId.getKey()).getSlotLimit(slotId.getValue());
        return 0;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        Pair<Integer, Integer> slotId = getProxySlot(slot);
        if (slotId != null)
            return getProxy(slotId.getKey()).isItemValid(slotId.getValue(), stack);
        return false;
    }
}
*/