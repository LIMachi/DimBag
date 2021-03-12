package com.limachi.dimensional_bags.common;

import javafx.util.Pair;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;

import javax.annotation.Nonnull;
import java.util.ArrayList;
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

    /**
     * ideally the correct way of making a diff would be to just extract 2 things: what keys to delete (first) and a mergeable compound
     * the return would then be a compound with a special key for a list of sub-keys to delete
     * and the merge would be donne by first removing the keys from the target
     * cloning and removing the special list from the diff
     * and finally merge the diff with the target
     */

//    public static CompoundNBT diff(@Nonnull CompoundNBT valid, @Nonnull CompoundNBT diff) {
//        ListNBT rem = removedKeys(valid, diff);
//        CompoundNBT out = removeKeys(valid.copy(), rem);
//
//    }

    protected static CompoundNBT removeKeys(CompoundNBT nbt, ListNBT keys) {
        for (int i = 0; i < keys.size(); ++i) {
            INBT k = keys.get(i);
            if (k.getId() == 10) {//compound
                String s = ((CompoundNBT)k).getString("key");
                ListNBT sk = (ListNBT)((CompoundNBT)k).get("list");
                nbt.put(s, removeKeys(nbt.getCompound(s), sk));
            } else
                nbt.remove(((StringNBT)k).getString());
        }
        return nbt;
    }

    protected static ListNBT removedKeys(@Nonnull CompoundNBT valid, @Nonnull CompoundNBT diff) {
        ListNBT list = new ListNBT();
        for (String key : diff.keySet())
            if (!valid.contains(key))
                list.add(StringNBT.valueOf(key));
            else {
                INBT td = diff.get(key);
                INBT tv = valid.get(key);
                if (td instanceof CompoundNBT && tv instanceof CompoundNBT) {
                    ListNBT sl = removedKeys((CompoundNBT) tv, (CompoundNBT) td);
                    if (!sl.isEmpty()) {
                        CompoundNBT entry = new CompoundNBT();
                        entry.put("list", sl);
                        entry.putString("key", key);
                        list.add(entry);
                    }
                } else
                    list.add(StringNBT.valueOf(key));
            }
        return list;
    }

    public static CompoundNBT extractDiff(@Nonnull CompoundNBT valid, @Nonnull CompoundNBT diff) {
        CompoundNBT added = new CompoundNBT();
        ListNBT removed = new ListNBT();
        CompoundNBT changed = new CompoundNBT();
        for (String key : valid.keySet())
            if (diff.contains(key) && !diff.get(key).equals(valid.get(key))) {
                INBT tv = valid.get(key);
                INBT td = diff.get(key);
                if (tv instanceof CompoundNBT && td instanceof CompoundNBT)
                    changed.put(key, extractDiff((CompoundNBT) tv, (CompoundNBT) td));
                else
                    changed.put(key, tv);
            }
            else if (!diff.contains(key))
                added.put(key, valid.get(key));
        for (String key : diff.keySet())
            if (!valid.contains(key))
                removed.add(StringNBT.valueOf(key));
        CompoundNBT out = new CompoundNBT();
        if (!changed.isEmpty())
            out.put("Diff_Changed", changed);
        if (!added.isEmpty())
            out.put("Diff_Added", added);
        if (!removed.isEmpty())
            out.put("Diff_Removed", removed);
        return out;
    }

    public static CompoundNBT applyDiff(@Nonnull CompoundNBT toChange, @Nonnull CompoundNBT diff) {
        ListNBT removed = diff.getList("Diff_Removed", 8);
        for (int i = 0; i < removed.size(); ++i)
            toChange.remove(removed.getString(i));
        CompoundNBT added = diff.getCompound("Diff_Added");
        for (String key : added.keySet())
            toChange.put(key, added.get(key));
        CompoundNBT changed = diff.getCompound("Diff_Changed");
        for (String key : changed.keySet()) {
            INBT c = changed.get(key);
            if (toChange.contains(key) && c instanceof CompoundNBT && toChange.get(key) instanceof CompoundNBT)
                toChange.put(key, applyDiff((CompoundNBT) toChange.get(key), (CompoundNBT) c));
            else
                toChange.put(key, c);
        }
        return toChange;
    }

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

    public static CompoundNBT ensurePathExistence(CompoundNBT nbt, String ... nodes) {
        CompoundNBT t = nbt;
        for (String node : nodes) {
            if (!t.contains(node))
                t.put(node, new CompoundNBT());
            t = t.getCompound(node);
        }
        return t;
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
