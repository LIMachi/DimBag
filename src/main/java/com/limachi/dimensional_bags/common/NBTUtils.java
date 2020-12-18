package com.limachi.dimensional_bags.common;

import com.limachi.dimensional_bags.DimBag;
import javafx.util.Pair;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NBTUtils {
    /*
    private static final int NBT_VERSION = 1;
    private static final String NBT_VERSION_KEY = "nbt_holder_version";
    private static final String NBT_COMPOUND_KEY = "nbt_holder_compound";
    private static final String NBT_WAS_UPDATED_KEY = "nbt_holder_updated";
    private static final HashMap<String, Pair<Integer, Function<CompoundNBT, CompoundNBT>>> NBT_UPDATERS = new HashMap<>();

    public static void addNbtFixer(Class<?> clazz, int previousVersion, int newVersion, Function<CompoundNBT, CompoundNBT> updater) {
        NBT_UPDATERS.put(previousVersion + "_" + clazz, new Pair<>(newVersion, updater));
    }

    private static CompoundNBT updateNbt(Class<?> clazz, int version, CompoundNBT nbt) {
        Pair<Integer, Function<CompoundNBT, CompoundNBT>> pf;
        while (version != NBT_VERSION && (pf = NBT_UPDATERS.get(version + "_" + clazz)) != null) {
            Function<CompoundNBT, CompoundNBT> f = pf.getValue();
            version = pf.getKey();
            nbt.putBoolean(NBT_WAS_UPDATED_KEY, true);
            nbt.put(NBT_COMPOUND_KEY, f.apply(nbt.getCompound(NBT_COMPOUND_KEY)));
            nbt.putInt(NBT_VERSION_KEY, version);
        }
        return nbt;
    }
    */

    public static INBT deepMergeNBTInternal(INBT to, INBT from) {
        if (from == null)
            return to;
        if (to == null
                || to.getType() != from.getType()
                || from.getType() == StringNBT.TYPE
                || from.getType() == ByteNBT.TYPE
                || from.getType() == DoubleNBT.TYPE
                || from.getType() == FloatNBT.TYPE
                || from.getType() == IntNBT.TYPE
                || from.getType() == LongNBT.TYPE
                || from.getType() == ShortNBT.TYPE)
            return from;
        if (from.getType() == CompoundNBT.TYPE) {
            CompoundNBT tc = (CompoundNBT) to;
            CompoundNBT fc = (CompoundNBT) from;
            for (String key : fc.keySet()) {
                INBT p = deepMergeNBTInternal(tc.get(key), fc.get(key));
                if (p != null)
                    tc.put(key, p);
            }
            return to;
        }
        if (from.getType() == ByteArrayNBT.TYPE
                || from.getType() == IntArrayNBT.TYPE
                || from.getType() == ListNBT.TYPE
                || from.getType() == LongArrayNBT.TYPE) {
            int sf = ((CollectionNBT) from).size();
            int st = ((CollectionNBT) to).size();
            for (int i = 0; i < sf; ++i)
                if (i < st)
                    ((CollectionNBT) to).set(i, deepMergeNBTInternal((INBT) ((CollectionNBT) to).get(i), (INBT) ((CollectionNBT) from).get(i)));
                else
                    ((CollectionNBT) to).add(i, (INBT) ((CollectionNBT) from).get(i));
            return to;
        }
        return null;
    }

    /**
     * will generate a nbt list from a list, using the function 'conv' applied to each element of the list 'in'
     * @param in the list to convert to a ListNBT
     * @param storeIndexes if non null, will use this key to store the index of the elements inside the ListNBT entry
     * @param conv should convert whatever type the original list's elements are to compound nbt (if null is returned, the element will be skipped)
     * @return a ListNBT that can be used in another convertList to get back the original list
     */
    public static <T> ListNBT convertList(@Nonnull List<T> in, @Nullable String storeIndexes, @Nonnull Function<T, CompoundNBT> conv) {
        ListNBT out = new ListNBT();
        for (int i = 0; i < in.size(); ++i) {
            CompoundNBT entry = conv.apply(in.get(i));
            if (entry != null) {
                if (storeIndexes != null)
                    entry.putInt(storeIndexes, i);
                out.add(entry);
            }
        }
        return out;
    }

    /**
     * will populate a list from a ListNBT (note: the list must already have been initialized with the correct size before population)
     * @param in the ListNBT that should be converted back to elements in the List<T> 'list'
     * @param list the output list with a size big enough to contain all the elements stored (and potentially indexed) in the ListNBT 'in'
     * @param storeIndexes if non null, will use this key to get the index of the elements inside the ListNBT entry
     * @param conv should convert the entries to whatever type the output list is
     */
    public static <T> void populateList(@Nonnull ListNBT in, @Nonnull List<T> list, @Nullable String storeIndexes, @Nonnull Function<CompoundNBT, T> conv) {
        for (int i = 0; i < in.size(); ++i) {
            CompoundNBT entry = in.getCompound(i);
            list.set(storeIndexes == null ? i : entry.getInt(storeIndexes), conv.apply(entry));
        }
    }

    public static CompoundNBT newCompound(Object ...init) {
        Function<CompoundNBT, CompoundNBT> ft = (t)->t;
        CompoundNBT out = new CompoundNBT();
        for (int i = 0; i < init.length; ++i) {
            if (init[i] instanceof String) {
                String key = (String)init[i];
                ++i;
                if (init[i] instanceof String) {out.putString(key, (String)init[i]); continue;}
                if (init[i] instanceof Integer) {out.putInt(key, (Integer)init[i]); continue;}
                if (init[i] instanceof Boolean) {out.putBoolean(key, (Boolean)init[i]); continue;}
                if (init[i] instanceof Byte) {out.putByte(key, (Byte)init[i]); continue;}
                if (ft.getClass().isInstance(init[i])) {out.put(key, ((Function<CompoundNBT, CompoundNBT>)init[i]).apply(new CompoundNBT())); continue;}
                if (init[i] instanceof List<?>) {
                    List<?> list = (List<?>)init[i + 1];
                    if (list.size() == 0) continue;
                    Object o = list.get(0);
                    if (o instanceof String) {out.put(key, convertList((List<String>)list, "ListIndex", t->newCompound("String", t))); continue;}
                }
            }
            DimBag.breakPoint();
        }
        return out;
    }

    public static <T> T getOrDefault(CompoundNBT nbt, String key, Function<String, T> method, T def) {
        if (nbt.contains(key))
            return method.apply(key);
        return def;
    }

    /*
    private static CompoundNBT internalGetNbt(Class<?> clazz, CompoundNBT nbt) {
        if (nbt == null) {
            nbt = new CompoundNBT();
            nbt.putInt(NBT_VERSION_KEY, NBT_VERSION);
            nbt.put(NBT_COMPOUND_KEY, new CompoundNBT());
            nbt.putBoolean(NBT_WAS_UPDATED_KEY, true);
            return nbt;
        }
        return updateNbt(clazz, nbt.getInt(NBT_VERSION_KEY), nbt);
    }

    public static CompoundNBT getNbt(Class<?> clazz, Supplier<CompoundNBT> readNbt, Consumer<CompoundNBT> writeNbt) {
        CompoundNBT o = internalGetNbt(clazz, readNbt.get());
        if (o.getBoolean(NBT_WAS_UPDATED_KEY)) {
            o.remove(NBT_WAS_UPDATED_KEY);
            writeNbt.accept(o);
        }
        return o.getCompound(NBT_COMPOUND_KEY);
    }

    public static void mergeNbt(Class<?> clazz, Supplier<CompoundNBT> readNbt, CompoundNBT addNbt, Consumer<CompoundNBT> writeNbt, String ... removeKeys) {
        if (clazz == null) return;
        CompoundNBT nbt = internalGetNbt(clazz, readNbt.get());
        CompoundNBT c = nbt.getCompound(NBT_COMPOUND_KEY);
        for (String rk : removeKeys)
            c.remove(rk);
        nbt.put(NBT_COMPOUND_KEY, deepMergeNBTInternal(c, addNbt));
        writeNbt.accept(nbt);
    }

    public static boolean isNbtUpToDate(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTag()) return true;
        return stack.getTag().getInt(NBT_VERSION_KEY) == NBT_VERSION;
    }

    public static CompoundNBT getNbt(ItemStack stack) {
        CompoundNBT nbt = internalGetNbt(stack.getItem().getClass(), stack.getTag());
        if (nbt.getBoolean(NBT_WAS_UPDATED_KEY)) {
            nbt.remove(NBT_WAS_UPDATED_KEY);
            stack.setTag(nbt);
        }
        return nbt.getCompound(NBT_COMPOUND_KEY);
    }

    public static void mergeNbt(ItemStack stack, CompoundNBT addNbt, String ... removeKeys) {
        if (stack == null || stack.isEmpty() || addNbt == null) return;
        CompoundNBT o = internalGetNbt(stack.getItem().getClass(), stack.getTag());
        CompoundNBT c = o.getCompound(NBT_COMPOUND_KEY);
        for (String rk : removeKeys)
            c.remove(rk);
        o.put(NBT_COMPOUND_KEY, deepMergeNBTInternal(c, addNbt));
        stack.setTag(o);
    }

    public static boolean isNbtUpToDate(Entity entity) {
        if (entity == null) return true;
        return entity.getPersistentData().getInt(NBT_VERSION_KEY) == NBT_VERSION;
    }

    public static CompoundNBT getNbt(Entity entity) {
        CompoundNBT nbt = internalGetNbt(entity.getClass(), entity.getPersistentData());
        if (nbt.getBoolean(NBT_WAS_UPDATED_KEY)) {
            CompoundNBT pd = entity.getPersistentData();
            pd.putString(NBT_VERSION_KEY, nbt.getString(NBT_VERSION_KEY));
            pd.put(NBT_COMPOUND_KEY, nbt.getCompound(NBT_COMPOUND_KEY));
        }
        return nbt.getCompound(NBT_COMPOUND_KEY);
    }

    public static void mergeNbt(Entity entity, CompoundNBT addNbt, String ... removeKeys) {
        if (entity == null || addNbt == null) return;
        CompoundNBT pd = entity.getPersistentData();
        CompoundNBT o = internalGetNbt(entity.getClass(), pd);
        CompoundNBT c = o.getCompound(NBT_COMPOUND_KEY);
        for (String rk : removeKeys)
            c.remove(rk);
        pd.putString(NBT_VERSION_KEY, o.getString(NBT_VERSION_KEY));
        pd.put(NBT_COMPOUND_KEY, deepMergeNBTInternal(c, addNbt));
    }

    public static HashMap<String, Pair<INBT, INBT>> getNBTDiff(CompoundNBT nbt1, CompoundNBT nbt2) {
        HashMap<String, Pair<INBT, INBT>> out = new HashMap<>();
        HashSet<String> keys = new HashSet<>();
        keys.addAll(nbt1.keySet());
        keys.addAll(nbt2.keySet());
        for (String key : keys) {
            INBT n1 = nbt1.get(key);
            INBT n2 = nbt2.get(key);
            if (n1 != null && (n2 == null || !n2.equals(n1)))
                out.put(key, new Pair<>(n1, n2));
            else if (n2 != null && n1 == null)
                out.put(key, new Pair<>(n1, n2));
        }
        return out;
    }
     */
}
