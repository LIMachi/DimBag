package com.limachi.dim_bag.utils;

import net.minecraft.nbt.*;

import java.util.function.Function;
import java.util.function.Supplier;

public class Tags {
    public static CompoundTag getOrCreateCompound(CompoundTag self, String key, Supplier<CompoundTag> build) {
        if (!self.contains(key, Tag.TAG_COMPOUND))
            self.put(key, build.get());
        return self.getCompound(key);
    }

    public static ListTag getOrCreateList(CompoundTag self, String key, Supplier<ListTag> build) {
        if (!self.contains(key, Tag.TAG_LIST))
            self.put(key, build.get());
        return (ListTag)self.get(key);
    }

    public static int getOrCreateInt(CompoundTag self, String key, Supplier<Integer> build) {
        if (!self.contains(key, Tag.TAG_INT))
            self.put(key, IntTag.valueOf(build.get()));
        return self.getInt(key);
    }

    public static double getOrCreateDouble(CompoundTag self, String key, Supplier<Double> build) {
        if (!self.contains(key, Tag.TAG_DOUBLE))
            self.put(key, DoubleTag.valueOf(build.get()));
        return self.getDouble(key);
    }

    public static <T extends Tag> T getOrExtendList(ListTag self, int index, Function<Integer, T> build) {
        if (index < 0)
            index = 0;
        for (int i = self.size() - 1; i < index; ++i)
            self.add(build.apply(i));
        return (T)self.get(index);
    }
}
