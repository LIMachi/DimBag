package com.limachi.dimensional_bags.utils;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.EnergyData;
import javafx.util.Pair;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.util.TriConsumer;
import org.objectweb.asm.Type;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@StaticInit
public class ReflectionUtils {

    public static <K, V> HashMap<K, V> createMap(Object ... init) {
        if (init.length % 2 == 1) throw new InvalidParameterException("init should be an even sized list of alternating key and values");
        HashMap<K, V> out = new HashMap<>();
        for (int i = 0; i < init.length; i += 2) {
            K key;
            try {
                key = (K) init[i];
            } catch (ClassCastException e) {
                throw new InvalidParameterException("invalid key at position " + i + " : " + init[i]);
            }
            V value;
            try {
                value = (V) init[i + 1];
            } catch (ClassCastException e) {
                throw new InvalidParameterException("invalid value at position " + (i + 1) + " : " + init[i + 1]);
            }
            out.put(key, value);
        }
        return out;
    }

    public static final HashMap<Class<?>, Function<Object, Object>> CAST_DOWN_MAP = new HashMap<>();
    public static final HashMap<Class<?>, Function<Object, Object>> CAST_UP_MAP = new HashMap<>();
    static {
        CAST_DOWN_MAP.put(Long.class, l->((Number)l).longValue());
        CAST_DOWN_MAP.put(Integer.class, l->((Number)l).intValue());
        CAST_DOWN_MAP.put(Short.class, l->((Number)l).shortValue());
        CAST_DOWN_MAP.put(Byte.class, l->((Number)l).byteValue());
        CAST_DOWN_MAP.put(Double.class, l->((Number)l).doubleValue());
        CAST_DOWN_MAP.put(Float.class, l->((Number)l).floatValue());
        CAST_DOWN_MAP.put(long.class, l->((Number)l).longValue());
        CAST_DOWN_MAP.put(int.class, l->((Number)l).intValue());
        CAST_DOWN_MAP.put(short.class, l->((Number)l).shortValue());
        CAST_DOWN_MAP.put(byte.class, l->((Number)l).byteValue());
        CAST_DOWN_MAP.put(double.class, l->((Number)l).doubleValue());
        CAST_DOWN_MAP.put(float.class, l->((Number)l).floatValue());
        CAST_UP_MAP.put(long.class, l->Long.valueOf((long)l));
        CAST_UP_MAP.put(int.class, l->Integer.valueOf((int)l));
        CAST_UP_MAP.put(short.class, l->Short.valueOf((short)l));
        CAST_UP_MAP.put(byte.class, l->Byte.valueOf((byte)l));
        CAST_UP_MAP.put(double.class, l->Double.valueOf((double)l));
        CAST_UP_MAP.put(float.class, l->Float.valueOf((float)l));
        CAST_UP_MAP.put(Long.class, l->Long.valueOf((long)l));
        CAST_UP_MAP.put(Integer.class, l->Integer.valueOf((int)l));
        CAST_UP_MAP.put(Short.class, l->Short.valueOf((short)l));
        CAST_UP_MAP.put(Byte.class, l->Byte.valueOf((byte)l));
        CAST_UP_MAP.put(Double.class, l->Double.valueOf((double)l));
        CAST_UP_MAP.put(Float.class, l->Float.valueOf((float)l));
    }

    public static <T, F> T cast(Class<T> clazz, F val) {
        Class<F> vt = (Class<F>)val.getClass();
        if (vt.equals(clazz)) return (T)val;
        try {
            return clazz.cast(val);
        } catch (Exception ignore) {}
        return (T)CAST_DOWN_MAP.get(clazz).apply(CAST_UP_MAP.get(val.getClass()).apply(val));
//        return (T)((Class<?>)fromPrimitive(clazz)).cast(fromPrimitive(val));
    }

    /**
     * convert a primitive object or class to it
     * @param prime
     * @return
     */
    public static Object fromPrimitive(Object prime) {
        if (prime instanceof Class) {
            if (!((Class<?>)prime).isPrimitive()) return prime;
            try {
                return ((Class<?>)prime).getField("TYPE").get(null);
            } catch (Exception ignore) {}
        }
        if (!prime.getClass().isPrimitive()) return prime;
        try {
            return ((Class<?>)prime.getClass().getField("TYPE").get(null)).getConstructor(prime.getClass()).newInstance(prime);
        } catch (Exception e) {
            throw new ClassCastException();
        }
    }

    static {
        int test = cast(int.class, new Long(42));
        Integer test2 = cast(Integer.class, (long)43);
        DimBag.LOGGER.info(" we gud");
    }

    /**
     * try to assign the given 'field' in 'object' with the new 'value' (ignoring private, protected and final). This version is not compatible with obfuscated code (only use it on our own code)
     * @param object any object
     * @param field a valid field name in 'object'
     * @param value the new value of the field, must be of the corresponding type
     * @return true if the assignation succeeded, false if an error was caught
     */
    @SuppressWarnings({"UnusedReturnValue", "unused"})
    public static boolean setField (Object object, String field, Object value) { return setField(object, field, null, value); }

    /**
     * try to assign the given 'field' in 'object' with the new 'value' (ignoring private, protected and final)
     * @param object any object
     * @param field a valid field name in 'object'
     * @param mcp_alternative a valid obfuscated field name to use as default alternative TODO: when upgrading, dont forget to update obfuscated names
     * @param value the new value of the field, must be of the corresponding type
     * @return true if the assignation succeeded, false if an error was caught
     */
    @SuppressWarnings({"UnusedReturnValue", "unused"})
    public static boolean setField (Object object, String field, String mcp_alternative, Object value) {
        DimBag.debug(object, 1, "accessing field " + field + " to set to " + value);
        try {
            Field f = classField(object.getClass(), field, mcp_alternative);
            if (f == null) return false;
            f.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);
            f.set(object, value);
            return true;
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    /**
     * try to assign the given static 'field' in 'clazz' with the new 'value' (ignoring private, protected and final)
     * @param clazz any class
     * @param field a valid field name in 'object'
     * @param mcp_alternative a valid obfuscated field name to use as default alternative TODO: when upgrading, dont forget to update obfuscated names
     * @param value the new value of the field, must be of the corresponding type
     * @return true if the assignation succeeded, false if an error was caught
     */
    public static boolean setField (Class<?> clazz, String field, String mcp_alternative, Object value) {
        DimBag.debug(clazz, 1, "accessing field " + field + " to set to " + value);
        try {
            Field f = classField(clazz, field, mcp_alternative);
            if (f == null) return false;
            f.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);
            f.set(null, value);
            return true;
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    /**
     * try to assign the given 'field' in 'object' with the new 'value' (ignoring private, protected and final)
     * @param object any object
     * @param field a valid field in 'object'
     * @param value the new value of the field, must be of the corresponding type
     * @return true if the assignation succeeded, false if an error was caught
     */
    @SuppressWarnings({"UnusedReturnValue", "unused"})
    public static boolean setField (Object object, Field field, Object value) {
        DimBag.debug(object, 1, "accessing field " + field + " to set to " + value);
        if (field == null) return false;
        try {
            field.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(object, value);
            return true;
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    /**
     * try to get the given 'field' in 'object' (ignoring private and protected). This version is not compatible with obfuscated code (only use it on our own code)
     * @param object any object
     * @param field a valid field name in 'objet', does not require the field to be public
     * @return the accessed object (null on error or if the object is null)
     */
    @SuppressWarnings("unused")
    public static Object getField(Object object, String field) { return getField(object, field, null); }

    /**
     * try to get the given 'field' in 'object' (ignoring private and protected)
     * @param object any object
     * @param field a valid field name in 'objet', does not require the field to be public
     * @param mcp_alternative a valid obfuscated field name to use as default alternative TODO: when upgrading, dont forget to update obfuscated names
     * @return the accessed object (null on error or if the object is null)
     */
    @SuppressWarnings("unused")
    public static Object getField(Object object, String field, String mcp_alternative) {
        DimBag.debug(object, 1, "accessing field " + field + " to read");
        try {
            Field f = classField(object.getClass(), field, mcp_alternative);
            if (f == null) return null;
            f.setAccessible(true);
            return f.get(object);
        } catch (ReflectiveOperationException | NullPointerException e) {
            return null;
        }
    }

    /**
     * try to get the given static 'field' in 'clazz' (ignoring private and protected)
     * @param clazz any class
     * @param field a valid field name in 'objet', does not require the field to be public
     * @param mcp_alternative a valid obfuscated field name to use as default alternative TODO: when upgrading, dont forget to update obfuscated names
     * @return the accessed object (null on error or if the object is null)
     */
    public static Object getField(Class<?> clazz, String field, String mcp_alternative) {
        DimBag.debug(clazz, 1, "accessing field " + field + " to read");
        try {
            Field f = classField(clazz, field, mcp_alternative);
            if (f == null) return null;
            f.setAccessible(true);
            return f.get(null);
        } catch (ReflectiveOperationException | NullPointerException e) {
            return null;
        }
    }

    /**
     * try to get the given 'field' in 'object' (ignoring private and protected)
     * @param object any object
     * @param field a valid 'field' in 'objet', does not require to be public
     * @return the accessed object (null on error or if the object is null)
     */
    @SuppressWarnings("unused")
    public static Object getField(Object object, Field field) {
        DimBag.debug(object, 1, "accessing field " + field + " to read");
        try {
            field.setAccessible(true);
            return field.get(object);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    //use caching for frequent usage
    private static final HashMap<Pair<Class<?>, String>, Field> FIELD_CACHE = new HashMap<>();
    private static Field cacheField(Pair<Class<?>, String> key, Field field) {
        if (field != null)
            FIELD_CACHE.put(key, field);
        return field;
    }

    //TODO: when upgrading, dont forget to update obfuscated names (mcp_alternative)
    public static Field classField(@Nonnull Class<?> clazz, @Nonnull String field, @Nullable String mcp_alternative) {
        Pair<Class<?>, String> key = new Pair<>(clazz, field);
        Field t = FIELD_CACHE.get(key);
        if (t != null) return t;
        if (mcp_alternative != null)
            try {
                return cacheField(key, clazz.getDeclaredField(mcp_alternative));
            } catch (NoSuchFieldException ignore) {}
        try {
            return cacheField(key, clazz.getDeclaredField(field));
        } catch (NoSuchFieldException ignore) {}
        Class<?> zuper = clazz.getSuperclass();
        return zuper == null ? null : cacheField(key, classField(zuper, field, mcp_alternative));
    }

    //use caching for frequent usage
//    private static final HashMap<Pair<Class<?>, String>, Method> METHOD_CACHE = new HashMap<>();
    private static final HashMap<Triple<Class<?>, String, List<Class<?>>>, Method> METHOD_CACHE = new HashMap<>();

    private static Method cacheMethod(Triple<Class<?>, String, List<Class<?>>> key, Method method) {
        if (method != null)
            METHOD_CACHE.put(key, method);
        return method;
    }

    //Arrays.stream(clazz.getDeclaredMethods()).filter(m->m.name.equals("reselectEntry")).findFirst()
    //generic and overloaded methods will be a pain in the ass (should not rely on 'getDeclaredMethod' but instead find the first valid method that matches the parameters types
    //or we can let the user call an alternative method in which it as the requirement to give what type should be used for each param instead of automatically finding them

    protected static Method getDeclaredMethod(@Nonnull Class<?> clazz, @Nonnull String name, Object ... params) throws NoSuchMethodException {
        for (Method m : clazz.getDeclaredMethods()) {
            if (!m.getName().equals(name) || m.getParameterCount() != params.length) continue; //invalid name or number of parameters, we skip
            Class<?>[] cp = m.getParameterTypes();
            boolean ok = true;
            for (int i = 0; i < params.length; ++i)
                if (!cp[i].isInstance(params[i])) {
                    ok = false;
                    break;
                }
            if (ok)
                return m;
        }
        throw new NoSuchMethodException(clazz.getName() + "." + name + " " + Arrays.toString(params));
    }

    protected static List<Class<?>> paramTypes(Object ... params) {
        return Arrays.stream(params).map(Object::getClass).collect(Collectors.toList());
    }

    public static Method getMethod(@Nonnull Class<?> clazz, @Nonnull String method, @Nullable String mcp_alternative, Object ... params) {
        Triple<Class<?>, String, List<Class<?>>> key = Triple.of(clazz, method, paramTypes(params));
        Class<?>[] types = key.getRight().toArray(new Class<?>[]{});
        Method t = METHOD_CACHE.get(key);
        if (t != null) return t;
        if (mcp_alternative != null)
            try {
                return cacheMethod(key, clazz.getDeclaredMethod(mcp_alternative, types));
            } catch (NoSuchMethodException ignore) {}
        try {
            return cacheMethod(key, clazz.getDeclaredMethod(method, types));
        } catch (NoSuchMethodException ignore) {}
        Class<?> zuper = clazz.getSuperclass();
        return zuper == null ? null : cacheMethod(key, getMethod(zuper, method, mcp_alternative, params));
    }

//    TODO: when upgrading, dont forget to update obfuscated names (mcp_alternative)
//    public static Method classMethod(@Nonnull Class<?> clazz, @Nonnull String method, @Nullable String mcp_alternative, Class<?> ... params) {
//        Pair<Class<?>, String> key = new Pair<>(clazz, method); //should not cache this way, as overloaded methods might fail
//        Method t = METHOD_CACHE.get(key);
//        if (t != null) return t;
//        if (mcp_alternative != null)
//            try {
//                return cacheMethod(key, clazz.getDeclaredMethod(mcp_alternative, params)/*getDeclaredMethod(clazz, mcp_alternative, params)*/); //might have problems with params of inherited types (method not found for this specific parameter, but would work with it super class)
//            } catch (NoSuchMethodException ignore) {}
//        try {
//            return cacheMethod(key, clazz.getDeclaredMethod(method, params)/*getDeclaredMethod(clazz, method, params)*/);
//        } catch (NoSuchMethodException ignore) {}
//        Class<?> zuper = clazz.getSuperclass();
//        return zuper == null ? null : cacheMethod(key, classMethod(zuper, method, mcp_alternative, params));
//    }

    //TODO: when upgrading, dont forget to update obfuscated names (mcp_alternative)
    @SuppressWarnings({"UnusedReturnValue", "unused"})
    public static Object runMethod(@Nonnull Object object, @Nonnull String method, @Nullable String mcp_alternative, Object ... params) {
//        Class<?>[] parameterTypes = new Class[params.length];
//        for (int i = 0; i < params.length; ++i)
//            parameterTypes[i] = params[i].getClass();
        try {
            Method m = getMethod(object.getClass(), method, mcp_alternative, params);
            m.setAccessible(true);
            return m.invoke(object, params);
        } catch (IllegalAccessException | InvocationTargetException | NullPointerException ignore) {
            return null;
        }
    }

    public static <T> T fromString(String s, Class<T> clazz) {
        try {
            return (T)clazz.getMethod("valueOf", String.class).invoke(null, s);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {}
        try {
            return (T)clazz.getMethod("fromString", String.class).invoke(null, s);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {}
        try {
            if (s.startsWith(clazz.getSimpleName()))
                s = s.substring(clazz.getSimpleName().length());
            if (s.startsWith("{")) { //probably a class
                T instance = newInstance(clazz);
                if (instance == null) throw new NullPointerException();
//                Field[] fl = clazz.getDeclaredFields();
//                String[] fn = new String[fl.length];
//                for (int i = 0; i < fl.length; ++i)
//                    fn[i] = fl[i].getName();
                String[] m1 = s.substring(1, s.indexOf('}')).split(" *, *"); //get all assignations in the form "k=v" or "k:v"
                for (String m : m1) {
                    String[] m2 = m.split(" *[=:] *");
                    if (m2.length == 2)
                        setField(instance, m2[0], fromString(m2[1], getField(instance, m2[0]).getClass()));
                }
                return instance;
            } else if (s.startsWith("[")) {//probably an array, will be implemented later

            }
        } catch (Exception ignore) {}
        throw new IllegalArgumentException("Unknown string converter for: " + clazz);
    }

    @SafeVarargs
    @SuppressWarnings("unused")
    public static <T> T fromString(String s, T ... typeReflection) {
        return fromString(s, (Class<T>) typeReflection.getClass().getComponentType());
    }

    /**
     * iterate on the fields of 'object' and for each one annotated with 'annotationType' run the 'consumer'
     * @param object any object
     * @param annotationType any valid annotation (please do not input Annotation.class, you dummy)
     * @param consumer parameters: object, annotation field
     * @param <T> see 'annotationType'
     */
    @SuppressWarnings("unused")
    public static <T extends Annotation, O> void forEachAnnotation(O object, Class<T> annotationType, TriConsumer<O, T, Field> consumer) {
        for (Field f : object.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            T a = f.getAnnotation(annotationType);
            if (a != null)
                consumer.accept(object, a, f);
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface NBT {
        Type type = Type.getType(NBTUtils.NBT.class);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface NBTCollection {
        Type type = Type.getType(NBTCollection.class);
        Class<?> value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface NBTCompound {
        Type type = Type.getType(NBTCompound.class);
        Class<?> value();
        Class<?> key() default String.class;
    }

    @SuppressWarnings("unused")
    public static INBT fieldAsNBT(Object object, Field field, Annotation a) {
        try {
            Object f = field.get(object);
            Class<?> clazz = field.getType();
            if (a instanceof NBT)
                return NBTUtils.toNBT(f);
            else if (Collection.class.isAssignableFrom(clazz) && a instanceof NBTCollection) {
                ListNBT out = new ListNBT();
                for (Object v : (Collection<?>)f)
                    out.add(NBTUtils.toNBT(v));
                return out;
            } else if (AbstractMap.class.isAssignableFrom(clazz) && a instanceof NBTCompound) {
                CompoundNBT out = new CompoundNBT();
                AbstractMap<Object, Object> m = (AbstractMap<Object, Object>)f;
                for (Object k : m.keySet())
                    out.put(k.toString(), NBTUtils.toNBT(m.get(k)));
                return out;
            }
            throw new IllegalArgumentException();
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    /**
     * will serialize an object's annotated field (@NBT) to a CompoundNBT
     * @param object any object with some of its fields annotated with @NBT
     * @return a new CompoundNBT (which might be empty if no @NBT annotation where used/valid)
     */
    @SuppressWarnings("unused")
    public static CompoundNBT asCompoundNBT(Object object) {
        CompoundNBT out = new CompoundNBT();
        forEachAnnotation(object, NBT.class, (o, a, f)->{
            INBT e = fieldAsNBT(o, f, a);
            if (e != null)
                out.put(f.getName(), e);
        });
        forEachAnnotation(object, NBTCollection.class, (o, a, f)->{
            INBT e = fieldAsNBT(o, f, a);
            if (e != null)
                out.put(f.getName(), e);
        });
        forEachAnnotation(object, NBTCompound.class, (o, a, f)->{
            INBT e = fieldAsNBT(o, f, a);
            if (e != null)
                out.put(f.getName(), e);
        });
        return out;
    }

    /**
     * will try to create a new instance of the given class, using automatic default parameters recursively
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T newInstance(Class<T> clazz) {
        if (ItemStack.class.isAssignableFrom(clazz)) return (T)ItemStack.EMPTY.copy(); //temporary fix for ItemStack, could try to go further down the recursive initialization
        if (Vector3i.class.isAssignableFrom(clazz)) return (T)new Vector3i(0, 0, 0); //temporary fix for Vector3i
//        if (clazz.isAssignableFrom(boolean.class)) return (T)(Boolean)false;
//        if (clazz.isAssignableFrom(byte.class)) return (T)(Byte)(byte)0;
//        if (clazz.isAssignableFrom(short.class)) return (T)(Short)(short)0;
//        if (clazz.isAssignableFrom(int.class)) return (T)(Integer)0;
//        if (clazz.isAssignableFrom(long.class)) return (T)(Long)(long)0;
//        if (clazz.isAssignableFrom(float.class)) return (T)(Float)(float)0;
//        if (clazz.isAssignableFrom(double.class)) return (T)(Double)(double)0;
        try {
            return clazz.newInstance();
        } catch (IllegalAccessException | InstantiationException ignore) {}
        try {
            Constructor<?>[] cl = clazz.getConstructors();
            if (cl.length == 0) return null;
            Constructor<T> c = (Constructor<T>)cl[0];
            Class<?>[] pt = c.getParameterTypes();
            Object[] p = new Object[pt.length];
            for (int i = 0; i < pt.length; ++i)
                p[i] = newInstance(pt[i]);
            return c.newInstance(p);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ignore) {}
        return null;
    }

    @SuppressWarnings("unused") //currently the behavior for arrays [] is not implemented
    public static void fieldFromNBT(Object object, Field field, INBT nbt, Annotation a) {
        Class<?> fc = field.getType();
        if (nbt instanceof CollectionNBT) {
            if (!(Collection.class.isAssignableFrom(fc) && a instanceof NBTCollection)) return; //invalid try
            Class<?> cv = ((NBTCollection) a).value();
            Collection<Object> l = (Collection<Object>)getField(object, field);
            if (l == null) return; //failed to get the field
            CollectionNBT<?> n = (CollectionNBT<?>)nbt;
            l.clear();
            for (int i = 0; i < n.size(); ++i) {
                INBT t = n.get(i);
                Object v = NBTUtils.fromNBT(t, cv);  //ok, if this work i'm mad scientist, sonovabitsh
                if (cv.isInstance(v))
                    l.add(v);
            }
        } else if (nbt instanceof CompoundNBT) { //this one will be fun too
            if (!(AbstractMap.class.isAssignableFrom(fc) && a instanceof NBTCompound)) return; //invalid try
            Class<?> ck = ((NBTCompound) a).key();
            Class<?> cv = ((NBTCompound) a).value();
            AbstractMap<Object, Object> m = (AbstractMap<Object, Object>)getField(object, field);
            if (m == null) return; //failed to get the field
            CompoundNBT n = (CompoundNBT)nbt;
            m.clear();
            for (String k : n.getAllKeys()) {
                Object key = fromString(k, ck);
                INBT tv = n.get(k);
                if (tv != null) {
                    Object value = NBTUtils.fromNBT(tv, cv); //same as above, cross your fingers
                    if (value != null)
                        m.put(key, value);
                }
            }
        } else if (a instanceof NBT) {
            setField(object, field, NBTUtils.fromNBT(nbt, field.getType()));
        } else
            throw new IllegalArgumentException();
    }

    @SuppressWarnings("unused")
    public static void fromCompoundNBT(Object object, CompoundNBT nbt) {
        forEachAnnotation(object, NBT.class, (o, a, f)->fieldFromNBT(o, f, nbt.get(f.getName()), a));
        forEachAnnotation(object, NBTCollection.class, (o, a, f)->fieldFromNBT(o, f, nbt.get(f.getName()), a));
        forEachAnnotation(object, NBTCompound.class, (o, a, f)->fieldFromNBT(o, f, nbt.get(f.getName()), a));
    }

    static class TestClass {
        @NBT
        int test1 = 42;
        @NBTCollection(ItemStack.class)
        ArrayList<ItemStack> test2 = new ArrayList<>(Arrays.asList(ItemStack.EMPTY, ItemStack.EMPTY));
        @NBTCompound(key = BlockPos.class, value = EnergyData.class)
        HashMap<BlockPos, EnergyData> test3 = new HashMap<>();
    }

//    static {
//        TestClass out = new TestClass();
//        out.test1 = 25562454;
//        out.test2.add(ItemStack.EMPTY);
//        out.test3.put(new BlockPos(1, 2, 3), new EnergyData("energy_data", 0, true));
//        CompoundNBT test = asCompoundNBT(out);
//        DimBag.LOGGER.info(test);
//        out = new TestClass();
//        fromCompoundNBT(out, test);
//        DimBag.LOGGER.info(out);
//    }
}
