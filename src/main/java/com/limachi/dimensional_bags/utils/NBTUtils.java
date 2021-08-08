package com.limachi.dimensional_bags.utils;

import com.limachi.dimensional_bags.DimBag;
import net.minecraft.nbt.*;
import net.minecraft.util.UUIDCodec;
import net.minecraftforge.common.util.INBTSerializable;
import org.apache.http.client.utils.CloneUtils;
import org.objectweb.asm.Type;

import javax.annotation.Nonnull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

//@StaticInit
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

    public static CompoundNBT clear(CompoundNBT comp) {
        Object[] keys = comp.keySet().toArray();
        for (Object key : keys)
            comp.remove((String)key);
        return comp;
    }

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

    @SuppressWarnings("unused")
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
        if (!out.isEmpty())
            out.putBoolean("IsDiff", true);
        return out;
    }

    @SuppressWarnings({"UnusedReturnValue", "unused"})
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

    /*
    static {
        CompoundNBT t1 = ItemStack.EMPTY.serializeNBT();
        CompoundNBT t2 = new ItemStack(Items.CHAIN, 2).serializeNBT();
        CompoundNBT t3 = new ItemStack(Items.CHAIN).serializeNBT();

        CompoundNBT d1 = extractDiff(t1, t2);
        CompoundNBT d2 = extractDiff(t2, t3);
        CompoundNBT d3 = extractDiff(t3, t1);

        DimBag.LOGGER.info("d1 " + d1 + " d2 " + d2 + " d3 " + d3);

        CompoundNBT t1c = t2.copy();
        t1c = applyDiff(t1c, d1);

        CompoundNBT t2c = t3.copy();
        t2c = applyDiff(t2c, d2);

        CompoundNBT t3c = t1.copy();
        t3c = applyDiff(t3c, d3);

        DimBag.LOGGER.info("t1 " + t1 + "->" + t1c + "t2 " + t2 + "->" + t2c + "t3 " + t3 + "->" + t3c);
        DimBag.LOGGER.info("we gud");
    }*/

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

    public static final HashMap<Type, Function<Object, CompoundNBT>> NBT_SERIALIZERS = new HashMap<>();
    public static final HashMap<Type, Function<CompoundNBT, Object>> NBT_DESERIALIZERS = new HashMap<>();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface NBT {
        Type type = Type.getType(NBT.class);
    }

    public static class example {
        @NBT
        UUID id;
//        @NBTCompound(key = Integer.class, value = Boolean.class)
        HashMap<Integer, Boolean> triStateHashSet;
//        @NBTCollection(ItemStack.class)
//        List<ItemStack> inv;
    }

    /*
    static {
        HashMap<Type, ArrayList<ModFileScanData.AnnotationData>> constructionMap = new HashMap<>();
        for (ModFileScanData mfsd : ModList.get().getAllScanData())
            for (ModFileScanData.AnnotationData ad : mfsd.getAnnotations())
                if (NBT.type.equals(ad.getAnnotationType()))
                    constructionMap.compute(ad.getClassType(), (k, v)->{
                        if (v == null)
                            v = new ArrayList<>();
                        v.add(ad);
                        return v;
                    });
        for (Type ct : constructionMap.keySet()) {
            ArrayList<ModFileScanData.AnnotationData> adl = constructionMap.get(ct);
            final ArrayList<BiConsumer<Object, CompoundNBT>> ser = new ArrayList<>();
            final ArrayList<BiConsumer<CompoundNBT, Object>> deser = new ArrayList<>();
            for (ModFileScanData.AnnotationData ad : adl) {
                try {
                    final Field f = Class.forName(ad.getClassType().getClassName()).getDeclaredField(ad.getMemberName());
                    ser.add((o, c)->{
                        try {
                            c.put(f.getName(), toNBT(f.get(o)));
                        } catch (IllegalAccessException e) {
                            DimBag.LOGGER.error("Aaaand, we failed");
                            e.printStackTrace();
                        }
                    }); //all the magic should be done there
                    deser.add((c, o)->{
                        try {
                            f.set(o, fromNBT(c.get(f.getName())));
                        } catch (IllegalAccessException e) {
                            DimBag.LOGGER.error("Aaaand, we failed");
                            e.printStackTrace();
                        }
                    });
                } catch (ClassNotFoundException | NoSuchFieldException e) {
                    DimBag.LOGGER.error("did we just lost a field? how the heck? ");
                }
            }
            NBT_SERIALIZERS.put(ct, o->{
                CompoundNBT out = new CompoundNBT();
                for (BiConsumer<Object, CompoundNBT> lser : ser)
                    lser.accept(o, out);
                return out;
            });
            NBT_DESERIALIZERS.put(ct, n->{
                Object out;
                try {
                    out = Class.forName(ct.getClassName()).newInstance();
                } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                    DimBag.LOGGER.error("Missing empty constructor for " + ct.getClassName() + " required for automatic nbt deserialization");
                    return null;
                }
                for (BiConsumer<CompoundNBT, Object> ldeser : deser)
                    ldeser.accept(n, out);
                return out;
            });
        }
    }*/

    public static CompoundNBT serialize(Object o) {
        Type t = Type.getType(o.getClass());
        if (NBT_SERIALIZERS.containsKey(t))
            return NBT_SERIALIZERS.get(t).apply(o);
        DimBag.LOGGER.error("Unknown serializer for: '" + t + "' (" + o + ")");
        return new CompoundNBT();
    }

    public static <T> T deserialize(CompoundNBT nbt, T ... typeReflection) {
        Class<T> clazz = (Class<T>) typeReflection.getClass().getComponentType();
        Type t = Type.getType(clazz);
        if (NBT_DESERIALIZERS.containsKey(t))
            return (T)NBT_DESERIALIZERS.get(t).apply(nbt);
        DimBag.LOGGER.error("Unknown serializer for: '" + clazz + "' (" + nbt + ")");
        return null;
    }

//    private static <T> ListNBT toListNBT(Iterable<T> l) {
//        ListNBT list = new ListNBT();
//        Object[] tl = (Object[])o;
//        for (Object t : tl)
//            list.add(toNBT(t));
//        return list;
//    }

    /**
     * will try to convert an object to an INBT
     * @param o curretly valid types: INBT, INBTSerializable<\T>, string, uuid, bool, byte, short, int, long, float, double, Iterable<v>
     * @return any of INBT (StringNBT, IntArrayNBT, ByteNBT, ShortNBT, IntNBT, LongNBT, FloatNBT, DoubleNBT, ListNBT)
     */

    public static INBT toNBT(Object o) {
        if (o instanceof INBT)
            return (INBT)o;
        else if (o instanceof net.minecraftforge.common.util.INBTSerializable<?>)
            return ((net.minecraftforge.common.util.INBTSerializable<?>)o).serializeNBT();
        else if (o instanceof String)
            return StringNBT.valueOf((String)o);
        else if (o instanceof UUID)
            return new IntArrayNBT(UUIDCodec.encodeUUID((UUID)o));
        else if (o instanceof Boolean)
            return ByteNBT.valueOf((Boolean)o);
        else if (o instanceof Byte)
            return ByteNBT.valueOf((Byte)o);
        else if (o instanceof Short)
            return ShortNBT.valueOf((Short)o);
        else if (o instanceof Integer)
            return IntNBT.valueOf((Integer)o);
        else if (o instanceof Long)
            return LongNBT.valueOf((Long)o);
        else if (o instanceof Float)
            return FloatNBT.valueOf((Float)o);
        else if (o instanceof Double)
            return DoubleNBT.valueOf((Double)o);
        else if (o instanceof Iterable<?>) {
            ListNBT list = new ListNBT();
            Iterable<?> tl = (Iterable<?>)o;
            tl.forEach(t->list.add(toNBT(t)));
            return list;
        } /*else if (o instanceof AbstractMap<?, ?>) { //beyond hacky, let's give up on maps for now
            CompoundNBT compound = new CompoundNBT();
            AbstractMap<?, ?> tm = (AbstractMap<?, ?>)o;
            for (Object k : tm.keySet())
                compound.put(k.toString(), toNBT(tm.get(k)));
            return compound;
        } else if (o.getClass().getComponentType() != null)
            return toListNBT((Iterable<?>)o);*/ //too anoying
//        DimBag.LOGGER.error("Missing toNBT implementation for type: " + o.getClass());
        throw new IllegalArgumentException("Missing toNBT implementation for type: " + o.getClass());
    }

    private static <T> Iterable<T> fromListNBT(ListNBT nbt) {
        List<T> out = new ArrayList<>();
        for (INBT inbt : nbt) out.add((T)fromNBT(inbt));
        return out;
    }

//    public static <T> T fromNBT(INBT nbt, T instance) {
//        try {
//            Method m = instance.getClass().getMethod("deserializeNBT", nbt.getClass());
//            m.invoke(instance, nbt);
//            return instance;
//        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignore) {}
//        return fromNBT (nbt, (Class<T>)instance.getClass());
//    }

    /**
     * alternative function where you can manually set the expected class
     * @param nbt
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T fromNBT(INBT nbt, Class<T> clazz) {
        try {
            Method m = clazz.getMethod("deserializeNBT", nbt.getClass());
            T out = ReflectionUtils.newInstance(clazz);
            m.invoke(out, nbt);
            return out;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignore) {}
        if (clazz.isInstance(nbt))
            return (T)nbt;
        else if (clazz.isAssignableFrom(String.class) && nbt instanceof StringNBT)
            return (T)((StringNBT)nbt).getString();
        else if (clazz.isAssignableFrom(UUID.class) && nbt instanceof IntArrayNBT)
            return (T)NBTUtil.readUniqueId(nbt); //do I have no shame? maybe
        else if ((clazz.isAssignableFrom(Boolean.class) || clazz.isAssignableFrom(boolean.class)) && nbt instanceof ByteNBT)
            return (T)(Boolean)ByteNBT.ONE.equals(nbt);
        else if ((clazz.isAssignableFrom(Byte.class) || clazz.isAssignableFrom(byte.class)) && nbt instanceof ByteNBT)
            return (T)(Byte)((ByteNBT)nbt).getByte();
        else if ((clazz.isAssignableFrom(Short.class) || clazz.isAssignableFrom(short.class)) && nbt instanceof ShortNBT)
            return (T)(Short)((ShortNBT)nbt).getShort();
        else if ((clazz.isAssignableFrom(Integer.class) || clazz.isAssignableFrom(int.class)) && nbt instanceof IntNBT)
            return (T)(Integer)((IntNBT)nbt).getInt();
        else if ((clazz.isAssignableFrom(Long.class) || clazz.isAssignableFrom(long.class)) && nbt instanceof LongNBT)
            return (T)(Long)((LongNBT)nbt).getLong();
        else if ((clazz.isAssignableFrom(Float.class) || clazz.isAssignableFrom(float.class)) && nbt instanceof FloatNBT)
            return (T)(Float)((FloatNBT)nbt).getFloat();
        else if ((clazz.isAssignableFrom(Double.class) || clazz.isAssignableFrom(double.class)) && nbt instanceof DoubleNBT)
            return (T)(Double)((DoubleNBT)nbt).getDouble();
        else if (nbt instanceof ListNBT)
            return (T) fromListNBT((ListNBT) nbt); //might go boum
        throw new IllegalArgumentException("unknown fromNBT resolver for converting '" + nbt.getClass() + "' to '" + clazz + "'");
    }

    /**
     * hacky function that will try to convert an INBT to a type T
     * @param nbt
     * @param typeReflection actually not required as it is actually a hack to get a valid instance of Class<\T>
     * @param <T>
     * @return
     */
    public static <T> T fromNBT(INBT nbt, T... typeReflection) {
        Class<T> clazz = (Class<T>) typeReflection.getClass().getComponentType();
        if (clazz.getComponentType() != null && nbt instanceof ListNBT) {
            Iterable<?> tl = Arrays.asList(typeReflection);
            tl = fromListNBT((ListNBT) nbt);
            return (T)((List<?>)tl).toArray(); //we just went berserk
        } else
            return fromNBT(nbt, clazz);
    }

    /**
     * convert an array of alternating string keys and Object to a compoundNBT
     * @param l (the actual size of this array must be even and each even entry must be a string, and odd entries are to be converted using toNBT)
     * @return a new CompoundNBT or throws IllegalArgumentException on non string key or impossible to convert values using toNBT
     */
    public static CompoundNBT toCompoundNBT(Object ... l) {
        if ((l.length % 2) == 1) //odd length
            throw new IllegalArgumentException("Odd number of parameters");
        CompoundNBT out = new CompoundNBT();
        for (int i = 0; i < l.length; i += 2) {
            if (!(l[i] instanceof String)) {
//                DimBag.LOGGER.error("invalid argument at position: " + i + " '" + l[i] + "' should have been a key/string");
                throw new IllegalArgumentException("invalid argument at position: " + i + " '" + l[i] + "' should have been a key/string");
            }
            out.put((String)l[i], toNBT(l[i + 1]));
        }
        return out;
    }

    public static <K, V, T extends Map<K, V>> CompoundNBT toCompoundNBT(T map) { return toCompoundNBT(map, K::toString); }

    public static <K, V, T extends Map<K, V>> CompoundNBT toCompoundNBT(T map, Function<K, String> kc) {
        CompoundNBT out = new CompoundNBT();
        for (K k : map.keySet())
            out.put(kc.apply(k), toNBT(map.get(k)));
        return out;
    }

    public static <K, V, T extends Map<K, V>> T fromCompoundNBT(CompoundNBT nbt, T ... typeReflection) {
        return (T)fromCompoundNBTp(nbt, (Function<String, K>)s->(K)ReflectionUtils.fromString(s), typeReflection);
    }

    public static <K, V, T extends Map<K, V>> T fromCompoundNBTp(CompoundNBT nbt, Function<String, K> kc, T ... typeReflection) {
        Class<T> clazz = (Class<T>) typeReflection.getClass().getComponentType();
        T out;
        try {
            out = clazz.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            out = (T)new HashMap<K, V>();
        }
        for (String k : nbt.keySet())
            out.put((K)kc.apply(k), (V)fromNBT(nbt.get(k)));
        return out;
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

    public static <T> T getOrDefault(CompoundNBT nbt, String key, Function<String, T> method, T def) {
        if (nbt != null && nbt.contains(key))
            return method.apply(key);
        return def;
    }

    /**
     * for now, compatible with INBT, INBTSerializable<\?>, String, Boolean, Byte, Short, Integer, Long, Float, Double, UUID
     * for others, please use alternative version that takes a method in
     * @param nbt
     * @param key
     * @param def
     * @param <T>
     * @return
     */
    public static <T> T getOrDefault(CompoundNBT nbt, String key, T def) {
        if (nbt == null || !nbt.contains(key)) return def;
        INBT t = nbt.get(key);
        if (t.getClass().isInstance(def)) return (T)t;
        if (def instanceof net.minecraftforge.common.util.INBTSerializable<?> && def instanceof Cloneable) {
            try {
                T out = CloneUtils.cloneObject(def);
                ((INBTSerializable) out).deserializeNBT(t);
                return out;
            } catch (CloneNotSupportedException rip) {
                return def;
            }
        }
        if (def instanceof String && t instanceof StringNBT) return (T)((StringNBT)t).getString();
        if (def instanceof Boolean && t instanceof ByteNBT) return (T)(Boolean)(((ByteNBT)t).getByte() != (byte)0);
        if (def instanceof Byte && t instanceof ByteNBT) return (T)(Byte)((ByteNBT)t).getByte();
        if (def instanceof Short && t instanceof ShortNBT) return (T)(Short)((ShortNBT)t).getShort();
        if (def instanceof Integer && t instanceof IntNBT) return (T)(Integer)((IntNBT)t).getInt();
        if (def instanceof Long && t instanceof LongNBT) return (T)(Long)((LongNBT)t).getLong();
        if (def instanceof Float && t instanceof FloatNBT) return (T)(Float)((FloatNBT)t).getFloat();
        if (def instanceof Double && t instanceof DoubleNBT) return (T)(Double)((DoubleNBT)t).getDouble();
        if (def instanceof UUID && t instanceof IntArrayNBT) return (T)NBTUtil.readUniqueId(t);
        return def;
    }

    /**
     * for now, compatible with INBT, INBTSerializable<\?>, String, Boolean, Byte, Short, Integer, Long, Float, Double, UUID
     * for others, please use the methods in CompoundNBT
     * @param nbt
     * @param key
     * @param val
     * @param <T>
     * @return
     */
    public static <T> void put(CompoundNBT nbt, String key, T val) {
        if (val instanceof INBT) nbt.put(key, (INBT)val);
        if (val instanceof net.minecraftforge.common.util.INBTSerializable<?>) nbt.put(key, ((INBTSerializable) val).serializeNBT());
        if (val instanceof String) nbt.putString(key, (String)val);
        if (val instanceof Boolean) nbt.putBoolean(key, (Boolean)val);
        if (val instanceof Byte) nbt.putByte(key, (Byte)val);
        if (val instanceof Short) nbt.putShort(key, (Short)val);
        if (val instanceof Integer) nbt.putInt(key, (Integer)val);
        if (val instanceof Long) nbt.putLong(key, (Long)val);
        if (val instanceof Float) nbt.putFloat(key, (Float)val);
        if (val instanceof Double) nbt.putDouble(key, (Double)val);
        if (val instanceof UUID) nbt.putUniqueId(key, (UUID)val);
    }
}
