package com.limachi.utils;

import com.mojang.datafixers.util.Pair;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.*;

import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.objectweb.asm.Type;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/** <pre>
 * single class handling configs via annotations and events
 *
 * for now compatible with public static non final fields of following types:
 *   boolean, Boolean, boolean[], Boolean[],
 *   byte, Byte, byte[], Byte[],
 *   short, Short, short[], Short[],
 *   int, Integer, int[], Integer[],
 *   long, Long, long[], Long[],
 *   String, String[]
 * other types/classes will not be made compatible (Java annotation limitation), and enums will not be handled (I don't plan to support them for now)
 *
 * usage: at the creation of your mod instance, call:
 *   {@code Configs.register(<your_mod_id>, <your_mod_name>);}
 *
 * examples:
 *
 * {@code @}Mod(MyMod.MOD_ID)
 * public class MyMod {
 *     public static final MOD_ID = "my_mod_id";
 *
 *     public MyMod() {
 *         Configs.register(MOD_ID, "my_mod");
 *         ...
 *     }
 * }
 *
 * import {@code <path.to.this.class>}.Config;
 *
 * public exampleClass {
 *      {@code @}Config(cmt = "simple boolean") //we simply add a comment, it makes no sense to test if a boolean is inside a range or in a set of valid states
 *      public static boolean that_boolean = true; //the default value of a config is read from the static field itself
 *
 *      {@code @}Config(cmt = "a list of strings")
 *      public static String[] nice_array = {"my_first_default_string", "and_my_other_string"}; //the annotation will detect java arrays and adapt itself to expect toml lists
 *
 *      {@code @}Config(cmt = "an int, within a range", min = "0", max = "10") //note: min/max are strings to circumvent the Typing limitations of annotations
 *      public static int such_range = 3;
 *
 *      {@code @}Config(cmt = "an int, the only valid values are the prime between 2 and 13", valid = {"2", "3", "5", "7", "11", "13"}) //note: valid entries are strings to circumvent the Typing limitations of annotations
 *      public static int prime = 2;
 * }
 * </pre>
 */
@SuppressWarnings("unused")
public class Configs {
    /** <pre>
     * the type and default value of this config is extracted from the field
     *
     * all of those values are facultative and in string format (except for valid which is an array of strings)
     * min -> string representation of minimal value of a range (will be ignored if the field is not instanceof Compare)
     * max -> string representation of maximal value of a range (will be ignored if the field is not instanceof Compare)
     * valid -> an array of string representation of valid values that this field can accept (regex)
     * cmt -> a string comment (will be ignored if null or empty)
     * path -> override the path of this variable with this path, helps readability
     * name -> override the name of this variable with this name, helps readability
     *
     * there is also 2 values that will define when and how this config will be used
     * side -> in which file this config should be stored, defaults to COMMON
     * reload -> can this config be updated on reload, by default (false) you need to reload the game entirely
     * </pre>
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Config {
        java.lang.String min() default "";
        java.lang.String max() default "";
        java.lang.String[] valid() default {};
        java.lang.String cmt() default "";
        java.lang.String path() default "";
        java.lang.String name() default "";
        ModConfig.Type side() default ModConfig.Type.COMMON;
        boolean reload() default false;
    }

    private static final Map<ModConfig.Type, ForgeConfigSpec.Builder> BUILDERS = new HashMap<>();
    private static ForgeConfigSpec.Builder getOrCreateBuilder(ModConfig.Type side) {
        if (!BUILDERS.containsKey(side))
            BUILDERS.put(side, new ForgeConfigSpec.Builder());
        return BUILDERS.get(side);
    }

    private static final Map<ModConfig.Type, Map<String, Pair<Supplier<?>, Boolean>>> GETTERS = new HashMap<>();
    private static Map<String, Pair<Supplier<?>, Boolean>> getOrCreateGetterMap(ModConfig.Type side) {
        if (!GETTERS.containsKey(side))
            GETTERS.put(side, new HashMap<>());
        return GETTERS.get(side);
    }

    private static final HashMap<Class<?>, Pair<Object[], Function<String, ?>>> DEFAULTS = new HashMap<>();
    private static <T, P> void defaultsEntry(Class<T> clazz, Class<P> prim, T[] def, Function<String, T> conv) {
        Pair<Object[], Function<String, ?>> e = new Pair<>(def, conv);
        if (prim != null)
            DEFAULTS.put(prim, e);
        DEFAULTS.put(clazz, e);
    }

    static {
        defaultsEntry(Boolean.class, boolean.class, new Boolean[]{false, null, null}, Boolean::parseBoolean);
        defaultsEntry(Byte.class, byte.class, new Byte[]{(byte)0, Byte.MIN_VALUE, Byte.MAX_VALUE}, Byte::parseByte);
        defaultsEntry(Short.class, short.class, new Short[]{(short)0, Short.MIN_VALUE, Short.MAX_VALUE}, Short::parseShort);
        defaultsEntry(Integer.class, int.class, new Integer[]{0, Integer.MIN_VALUE, Integer.MAX_VALUE}, Integer::parseInt);
        defaultsEntry(Long.class, long.class, new Long[]{(long)0, Long.MIN_VALUE, Long.MAX_VALUE}, Long::parseLong);
        defaultsEntry(Float.class, float.class, new Float[]{0f, Float.MIN_VALUE, Float.MAX_VALUE}, Float::parseFloat);
        defaultsEntry(Double.class, double.class, new Double[]{0., Double.MIN_VALUE, Double.MAX_VALUE}, Double::parseDouble);
        defaultsEntry(String.class, null, new String[]{"", null, null}, s->s);
    }

    private static Field getUnlockedField(Class<?> clazz, String name) throws Exception {
        Field out = clazz.getDeclaredField(name);
        out.setAccessible(true);
        return out;
    }

    private static String pathCleaner(String path) {
        String[] l = path.split("[.]");
        if (l.length > 1)
            return l[l.length - 2];
        return path;
    }

    /**
     * populates the builders and getters by scanning @Config annotations on static
     */
    private static void runConfigAnnotationDiscovery(String modId) throws Exception {
        Type ct = Type.getType(Config.class);
        for (ModFileScanData fsd : ModList.get().getAllScanData())
            if (fsd.getTargets().containsKey(modId))
                for (ModFileScanData.AnnotationData annotation : fsd.getAnnotations())
                    if (annotation.annotationType().equals(ct)) {
                        Map<String, Object> data = annotation.annotationData();
                        String path = annotation.clazz().getClassName() + "." + annotation.memberName();
                        Field f = getUnlockedField(Class.forName(annotation.clazz().getClassName()), annotation.memberName());
                        Class<?> targetType = f.getType();
                        StringBuilder tcmt = new StringBuilder((String) data.getOrDefault("cmt", ""));
                        String nameOverride = (String)data.getOrDefault("name", "");
                        String pathOverride = (String)data.getOrDefault("path", "");
                        boolean reload = (boolean)data.getOrDefault("reload", false);
                        ModConfig.Type side = (ModConfig.Type)data.getOrDefault("side", ModConfig.Type.COMMON);
                        ForgeConfigSpec.Builder builder = getOrCreateBuilder(side);
                        Class<?> compType = targetType.isArray() ? targetType.getComponentType() : targetType;
                        List<?> valid = getValid(compType, data, tcmt);
                        Object min = getMin(compType, data, tcmt);
                        Object max = getMax(compType, data, tcmt);
                        String cmt = tcmt.toString();
                        Predicate<Object> pred1 = valid != null && !valid.isEmpty() ? valid::contains : o->true;
                        Predicate<Object> pred2 = min instanceof Comparable && max instanceof Comparable && compType.isAssignableFrom(Comparable.class) ? o-> o instanceof Comparable && ((Comparable)o).compareTo(min) >= 0 && ((Comparable)o).compareTo(max) <= 0 : o->true;
                        Predicate<Object> pred = o->o != null && pred1.test(o) && pred2.test(o);
                        String tomlPath = (pathOverride.equals("") ? pathCleaner(path) : pathOverride) + "." + (nameOverride.equals("") ? annotation.memberName() : nameOverride);
                        if (targetType.isArray()) {
                            final ArrayList<Object> def = new ArrayList<>(Arrays.asList((Object[])f.get(null)));
                            if (!cmt.equals(""))
                                builder.comment(cmt);
                            if (!reload)
                                builder.worldRestart();
                            getOrCreateGetterMap(side).put(path, Pair.of(builder.defineList(tomlPath, def, pred), reload));
                        } else {
                            final Object def = f.get(null);
                            if (!cmt.equals(""))
                                builder.comment(cmt);
                            if (!reload)
                                builder.worldRestart();
                            getOrCreateGetterMap(side).put(path, Pair.of(builder.define(tomlPath, def, pred), reload));
                        }
                    }
    }

    private static <T> T getMin(Class<T> targetType, Map<String, Object> annotationData, StringBuilder tcmt) {
        Pair<Object[], Function<String, ?>> e = DEFAULTS.get(targetType);
        if (annotationData.containsKey("min") && !((String)annotationData.get("min")).isEmpty()) {
            if (tcmt.length() > 0)
                tcmt.append('\n');
            tcmt.append("Minimum value: '").append(annotationData.get("min")).append("'");
            return (T) e.getSecond().apply((String) annotationData.get("min"));
        }
        return (T)e.getFirst()[1];
    }

    private static <T> T getMax(Class<T> targetType, Map<String, Object> annotationData, StringBuilder tcmt) {
        Pair<Object[], Function<String, ?>> e = DEFAULTS.get(targetType);
        if (annotationData.containsKey("max") && !((String)annotationData.get("min")).isEmpty()) {
            if (tcmt.length() > 0)
                tcmt.append('\n');
            tcmt.append("Maximum value: '" + annotationData.get("max") + "'");
            return (T) e.getSecond().apply((String) annotationData.get("max"));
        }
        return (T)e.getFirst()[2];
    }

    private static <T> List<T> getValid(Class<T> compType, Map<String, Object> annotationData, StringBuilder tcmt) {
        Pair<Object[], Function<String, ?>> e = DEFAULTS.get(compType);
        if (annotationData.containsKey("valid")) {
            String[] v = (String[])annotationData.get("valid");
            T[] out = (T[]) Array.newInstance(compType, v.length);
            if (tcmt.length() > 0)
                tcmt.append('\n');
            tcmt.append("Valid values: '");
            for (int i = 0; i < v.length; ++i) {
                out[i] = (T) e.getSecond().apply(v[i]);
                tcmt.append(v[i]);
                if (i < v.length - 1)
                    tcmt.append("', '");
            }
            tcmt.append("'");
            return Arrays.asList(out);
        }
        return null;
    }

    private static void loadConfig(ModConfig.Type side, boolean isReload) {
        if (!GETTERS.containsKey(side)) return;
        for (Map.Entry<String, Pair<Supplier<?>, Boolean>> entry : GETTERS.get(side).entrySet()) {
            if (isReload && !entry.getValue().getSecond()) continue;
            String path = entry.getKey();
            Supplier<?> getter = entry.getValue().getFirst();
            try {
                int cut = path.lastIndexOf('.');
                Field f = getUnlockedField(Class.forName(path.substring(0, cut)), path.substring(cut + 1));
                if (!f.getType().isArray())
                    f.set(null, getter.get());
                else {
                    ArrayList<Object> ar = (ArrayList<Object>)getter.get();
                    f.set(null, Array.newInstance(f.getType().getComponentType(), ar.size()));
                    Object[] fo = (Object[])f.get(null);
                    for (int i = 0; i < ar.size(); ++i)
                        fo[i] = ar.get(i);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void onLoadConfig(ModConfigEvent.Loading event) { loadConfig(event.getConfig().getType(), false); }

    public static void onReloadConfig(ModConfigEvent.Reloading event) { loadConfig(event.getConfig().getType(), true); }

    public static void register(String modId, String configsName) {
        try {
            runConfigAnnotationDiscovery(modId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(Configs::onLoadConfig);
        bus.addListener(Configs::onReloadConfig);
        for (ModConfig.Type t : ModConfig.Type.values()) {
            if (BUILDERS.containsKey(t))
                ModLoadingContext.get().registerConfig(t, BUILDERS.get(t).build(), configsName + "-" + t.extension() + ".toml");
        }
    }
}
