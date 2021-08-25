package com.limachi.dimensional_bags;

import javafx.util.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.ModFileScanData;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.objectweb.asm.Type;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/** <pre>
 * single class handling configs using annotations and events
 *
 * for now compatible with static fields of following types (works regardless of public/protected/private/final modifiers):
 *   boolean, Boolean, boolean[], Boolean[],
 *   byte, Byte, byte[], Byte[],
 *   short, Short, short[], Short[],
 *   int, Integer, int[], Integer[],
 *   long, Long, long[], Long[],
 *   String, String[]
 * other types/classes will not be made compatible (Java annotation limitation), and enums will not be handled (I don't plan to support them for now)
 *
 * usage: at the creation of your mod instance, call:
 *   {@code ConfigManager.create(ModLoadingContext.get(), ModConfig.Type.<COMMON, CLIENT, SERVER>, <your_mod_path>, [<redundant>, <paths>, <you>, <don't>, <want>]);}
 *
 * examples:
 *
 * {@code @}Mod(MyMod.MOD_ID)
 * public class MyMod {
 *     public static final MOD_ID = "my_mod_id";
 *
 *     public MyMod() {
 *         ConfigManager.create(MOD_ID, ModConfig.Type.COMMON, "my_mod", new String[]{".common", ".items"});
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
public class ConfigManager {

    protected static final Logger LOGGER = LogManager.getLogger();

    private static final ArrayList<ConfigManager> INSTANCES = new ArrayList<>();

    protected final String MOD_ROOT_PATH;
    protected final String[] REMOVE_FROM_PATH;
    protected final String MOD_ID;

    private final ForgeConfigSpec SPEC;

    private boolean isBuilt = false;
    private boolean isBaked = false;
    private final HashMap<String, ConfigValueP<?>> CONFIG_BUILDERS = new HashMap<>();

    /**
     * calling this too late is a bad idea
     * @param type
     * @param mod_root
     * @param remove_from_path
     * @return
     */
    public static ConfigManager create(String mod_id, ModConfig.Type type, String mod_root, String[] remove_from_path) {
        ConfigManager instance = new ConfigManager(mod_id, mod_root, remove_from_path);
        INSTANCES.add(instance);
        ModLoadingContext.get().registerConfig(type, instance.SPEC);
        return instance;
    }

    private ConfigManager(String mod_id, String mod_root, String[] remove_from_path) {
        this.MOD_ID = mod_id;
        this.MOD_ROOT_PATH = mod_root != null ? mod_root : "";
        this.REMOVE_FROM_PATH = remove_from_path != null ? remove_from_path : new String[]{};
        try {
            runConfigAnnotationDiscovery();
        } catch (Exception e) {
            e.printStackTrace();
        }
        final org.apache.commons.lang3.tuple.Pair<ConfigBuilder, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ConfigBuilder::new);
        SPEC = specPair.getRight();
    }

    protected String cleanPath(String path) {
        String[] sp = path.split("\\.");
        StringBuilder out = new StringBuilder(sp[sp.length - 1]);
        for (int i = sp.length - 2; i >= 0; --i) {
            out.insert(0, sp[i] + '.');
            if (sp[i].equals(MOD_ROOT_PATH))
                break;
        }
        String nr = out.toString();
        for (String r : REMOVE_FROM_PATH) {
            if (r == null || r.isEmpty()) continue;
            nr = nr.replace(r, "");
        }
        return nr;
    }

    private Field getUnlockedField(Class<?> clazz, String name) throws Exception {
        Field out = clazz.getDeclaredField(name);
        out.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(out, out.getModifiers() & ~Modifier.FINAL);
        return out;
    }

    private class ConfigValueP<T> {
        private String path;
        private Function<ForgeConfigSpec.Builder, ? extends ForgeConfigSpec.ConfigValue<T>> build;
        private ForgeConfigSpec.ConfigValue<T> getter = null;

        private ConfigValueP(String path, Function<ForgeConfigSpec.Builder, ? extends ForgeConfigSpec.ConfigValue<T>> build) {
            this.path = path;
            if (isBuilt || isBaked)
                LOGGER.error("creating value: '" + path + "' too late (build or bake phase already happened)");
            this.build = build;
        }

        private void bakeConfig() {
            if (!isBuilt)
                LOGGER.error("backing value for: '" + path + "' before building config");
            if (getter != null) {
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

        private void buildConfig(ForgeConfigSpec.Builder builder) {
            if (build != null && builder != null)
                getter = build.apply(builder);
        }
    }

    private void runConfigAnnotationDiscovery() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Type ct = Type.getType(Config.class);
        for (ModFileScanData fsd : ModList.get().getAllScanData())
            if (fsd.getTargets().containsKey(MOD_ID))
                for (ModFileScanData.AnnotationData annotation : fsd.getAnnotations())
                    if (annotation.getAnnotationType().equals(ct)) {
                        Map<String, Object> data = annotation.getAnnotationData();
                        String targetPath = annotation.getClassType().getClassName() + "." + annotation.getMemberName();
                        Field f;
                        try {
                            f = getUnlockedField(Class.forName(annotation.getClassType().getClassName()), annotation.getMemberName());
                        } catch (Exception e) {
                            e.printStackTrace();
                            continue;
                        }
                        Class<?> targetType = f.getType();
                        StringBuilder tcmt = new StringBuilder((String) data.getOrDefault("cmt", ""));
                        Class<?> compType = targetType.isArray() ? targetType.getComponentType() : targetType;
                        List<?> valid = getValid(compType, data, tcmt);
                        Object min = getMin(compType, data, tcmt);
                        Object max = getMax(compType, data, tcmt);
                        String cmt = tcmt.toString();
                        Predicate<Object> pred1 = valid != null && !valid.isEmpty() ? valid::contains : o->true;
                        Predicate<Object> pred2 = min instanceof Comparable && max instanceof Comparable && compType.isAssignableFrom(Comparable.class) ? o-> o instanceof Comparable && ((Comparable)o).compareTo(min) >= 0 && ((Comparable)o).compareTo(max) <= 0 : o->true;
                        Predicate<Object> pred = o->o != null && pred1.test(o) && pred2.test(o);
                        if (targetType.isArray()) {
                            final ArrayList<Object> def = new ArrayList<>(Arrays.asList((Object[])f.get(null)));
                            CONFIG_BUILDERS.put(targetPath, new ConfigValueP<>(targetPath, b -> {
                                if (cmt != null && !cmt.isEmpty())
                                    b.comment(cmt);
                                return b.defineList(cleanPath(targetPath), def, pred);
                            }));
                        } else {
                            final Object def = f.get(null);
                            CONFIG_BUILDERS.put(targetPath, new ConfigValueP<>(targetPath, b -> {
                                if (cmt != null && !cmt.isEmpty())
                                    b.comment(cmt);
                                return b.define(cleanPath(targetPath), def, pred);
                            }));
                        }
                    }
    }

    /** <pre>
     * all of those values are facultative and in string format (except for valid which is an array of strings)
     * min -> string representation of minimal value of a range (will be ignored if the field is not instanceof Compare)
     * max -> string representation of maximal value of a range (will be ignored if the field is not instanceof Compare)
     * valid -> an array of string representation of valid values that this field can accept
     * cmt -> a string comment
     * </pre>
     */
    public @interface Config {
        java.lang.String min() default "";
        java.lang.String max() default "";
        java.lang.String[] valid() default {};
        java.lang.String cmt() default "";
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

    private static <T> T getMin(Class<T> targetType, Map<String, Object> annotationData, StringBuilder tcmt) {
        Pair<Object[], Function<String, ?>> e = DEFAULTS.get(targetType);
        if (annotationData.containsKey("min") && !((String)annotationData.get("min")).isEmpty()) {
            if (tcmt.length() > 0)
                tcmt.append('\n');
            tcmt.append("Minimum value: '" + annotationData.get("min") + "'");
            return (T) e.getValue().apply((String) annotationData.get("min"));
        }
        return (T)e.getKey()[1];
    }
    private static <T> T getMax(Class<T> targetType, Map<String, Object> annotationData, StringBuilder tcmt) {
        Pair<Object[], Function<String, ?>> e = DEFAULTS.get(targetType);
        if (annotationData.containsKey("max") && !((String)annotationData.get("min")).isEmpty()) {
            if (tcmt.length() > 0)
                tcmt.append('\n');
            tcmt.append("Maximum value: '" + annotationData.get("max") + "'");
            return (T) e.getValue().apply((String) annotationData.get("max"));
        }
        return (T)e.getKey()[2];
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
                out[i] = (T) e.getValue().apply(v[i]);
                tcmt.append(v[i]);
                if (i < v.length - 1)
                    tcmt.append("', '");
            }
            tcmt.append("'");
            return Arrays.asList(out);
        }
        return null;
    }

    static { FMLJavaModLoadingContext.get().getModEventBus().addListener(ConfigManager::onModConfigEvent); }

    private static void onModConfigEvent(final ModConfig.ModConfigEvent event) {
        for (ConfigManager instance : INSTANCES) {
            if (event.getConfig().getSpec() == instance.SPEC) {
                for (ConfigValueP<?> cv : instance.CONFIG_BUILDERS.values())
                    cv.bakeConfig();
                instance.isBaked = true;
            }
        }
    }

    private class ConfigBuilder {
        ConfigBuilder(ForgeConfigSpec.Builder builder) {
            for (ConfigValueP<?> cv : CONFIG_BUILDERS.values())
                cv.buildConfig(builder);
            isBuilt = true;
        }
    }
}
