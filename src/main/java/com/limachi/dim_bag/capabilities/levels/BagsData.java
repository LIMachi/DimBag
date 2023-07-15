package com.limachi.dim_bag.capabilities.levels;

import com.limachi.dim_bag.bag_data.BagInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraftforge.common.util.LazyOptional;

import java.util.ArrayList;

/*
public class BagsData implements IBagsData {

    private ListTag rawData = new ListTag();
    private final ArrayList<LazyOptional<BagInstance>> instances = new ArrayList<>();

    @Override
    public int newBagId() {
        rawData.add(new CompoundTag());
        int id = rawData.size();
        instances.add(IBagsData.bag(id).cast());
        return id;
    }

    @Override
    public int maxBagId() { return rawData.size(); }

    @Override
    public LazyOptional<IBagInstance> getBag(int id) {
        if (id <= 0 || id > instances.size()) return LazyOptional.empty();
        return instances.get(id - 1).cast();
    }

    @Override
    public ListTag serializeNBT() {
        for (int i = 0; i < instances.size(); ++i) {
            int finalI = i;
            instances.get(i).ifPresent(b->b.storeOn(rawData.getCompound(finalI)));
        }
        return rawData;
    }

    @Override
    public void deserializeNBT(ListTag nbt) {
        invalidate();
        instances.clear();
        rawData = nbt;
        for (int i = 0; i < rawData.size(); ++i)
            instances.add(IBagsData.bag(i).cast());
    }

    protected void invalidate() {
        for (LazyOptional<BagInstance> instance : instances)
            instance.invalidate();
    }
}
*/