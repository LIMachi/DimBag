package com.limachi.dimensional_bags.common.Config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.objectweb.asm.Type;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

import static com.limachi.dimensional_bags.DimBag.LOGGER;
import static com.limachi.dimensional_bags.DimBag.MOD_ID;

/**
 * ideally this system could be made into interfaces
 * ex:
 * class RandomClass {
 * \@Config.Range(def, min, max, cmt)
 * Integer value;
 *
 * \@Config.Value(def, cmt)
 * String somethingStringy;
 * }
 * we can extract a path for value -> get the class path and variable name of the value
 * we can extract the type of the value
 *
 * -> com.*author*.*mod*.RandomClass.value
 * -> com.*author*.*mod*.RandomClass.somethingStringy
 *
 * and we also have an access to the targeted variable (so we can treat it as the cachedValue in ConfigValueP<T>)
 *
 * we could also use reload events (used by resource packs) to also reload the config
 */

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ConfigEvents {
    protected static final Class<?>[] configAnnotations = {Config.IntRange.class, Config.String.class, Config.Enum.class, Config.Boolean.class, Config.Int.class, Config.Long.class};
    private static final ConfigBuilder CONFIG;
    private static final ForgeConfigSpec SPEC;
    protected static boolean isBuilt = false;
    protected static boolean isBaked = false;
    protected final static HashMap<String, ConfigValueP<?>> CONFIG_BUILDERS = new HashMap<>();
    static {
        try {
            runConfigAnnotationDiscovery();
        } catch (Exception e) {
            e.printStackTrace();
        }
        final org.apache.commons.lang3.tuple.Pair<ConfigBuilder, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ConfigBuilder::new);
        SPEC = specPair.getRight();
        CONFIG = specPair.getLeft();
    }

    protected static void runConfigAnnotationDiscovery() throws ClassNotFoundException, NoSuchFieldException {
        List<ModFileScanData> lsd =  ModList.get().getAllScanData();
        ArrayList<Type> configTypes = new ArrayList<>();
        for (Class<?> clazz : configAnnotations)
            configTypes.add(Type.getType(clazz));
        for (ModFileScanData fsd : lsd) {
            Set<ModFileScanData.AnnotationData> annotations = fsd.getAnnotations();
            for (ModFileScanData.AnnotationData annotation : annotations)
                for (int i = 0; i < configAnnotations.length; ++i)
                    if (annotation.getAnnotationType().equals(configTypes.get(i))) {
                        Map<String, Object> data = annotation.getAnnotationData();
                        String targetPath = annotation.getClassType().getClassName() + "." + annotation.getMemberName();
                        Class<?> targetType = Class.forName(annotation.getClassType().getClassName()).getDeclaredField(annotation.getMemberName()).getType();
                        switch (i) {
                            case 0: rangeValue(targetPath, (int)data.getOrDefault("def", 0), (int)data.getOrDefault("min", Integer.MIN_VALUE), (int)data.getOrDefault("max", Integer.MAX_VALUE), (String)data.getOrDefault("cmt", "")); break;
                            case 1: value(targetPath, (String)data.getOrDefault("def", ""), Arrays.asList(((String[])data.getOrDefault("valid", new String[]{})).clone()), (String)data.getOrDefault("cmt", "")); break;
                            case 3: value(targetPath, (boolean)data.getOrDefault("def", false), Arrays.asList(((Boolean[]) data.getOrDefault("valid", new Boolean[]{true, false})).clone()), (String)data.getOrDefault("cmt", "")); break;
                            case 2: //for now, virtually impossible to ensure any type safety TODO: do the missing states
                                break;
                            case 4: value(targetPath, (int)data.getOrDefault("def", 0), Arrays.asList(((Integer[])data.getOrDefault("valid", new Integer[]{})).clone()), (String)data.getOrDefault("cmt", "")); break;
                            case 5: value(targetPath, (long)data.getOrDefault("def", 0), Arrays.asList(((Long[])data.getOrDefault("valid", new Long[]{})).clone()), (String)data.getOrDefault("cmt", "")); break;
                        }
                    }
        }
    }

    protected static class ConfigValueP<T> {
        private String path;
        private Function<ForgeConfigSpec.Builder, ? extends ForgeConfigSpec.ConfigValue<T>> build;
        private ForgeConfigSpec.ConfigValue<T> getter = null;
//        private T cachedValue = null;

        protected ConfigValueP(String path, Function<ForgeConfigSpec.Builder, ? extends ForgeConfigSpec.ConfigValue<T>> build) {
            this.path = path;
            if (isBuilt || isBaked)
                LOGGER.error("creating value: '" + path + "' too late (build or bake phase already happened)");
            this.build = build;
        }

        protected void bakeConfig() {
            if (!isBuilt)
                LOGGER.error("backing value for: '" + path + "' before building config");
            if (getter != null) {
//                cachedValue = getter.get();
                try {
                    int cut = path.lastIndexOf('.');
                    Class.forName(path.substring(0, cut)).getDeclaredField(path.substring(cut + 1)).set(null, getter.get());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        protected void buildConfig(ForgeConfigSpec.Builder builder) {
            if (build != null && builder != null)
                getter = build.apply(builder);
        }

//        protected T getValue() {
//            if (!isBaked)
//                LOGGER.error("accessing value of: '" + path + "' before baking config");
//            return cachedValue;
//        }
    }

    protected static String cleanPath(String root, String path) {
        String[] sp = path.split("\\.");
        StringBuilder out = new StringBuilder(sp[sp.length - 1]);
        boolean rootFound = false;
        for (int i = sp.length - 2; i >= 0; --i) {
            out.insert(0, sp[i] + (rootFound ? '/' : '.'));
            if (sp[i].equals(root))
                rootFound = true;
        }
        return out.toString();
    }

    protected static <T extends Comparable<? super T>> /*Supplier<T>*/void rangeValue(@Nonnull String path, T def, T min, T max, @Nullable String cmt) {
        CONFIG_BUILDERS.put(path, new ConfigValueP<T>(path, b->{
            if (cmt != null && !cmt.isEmpty())
                b.comment(cmt);
            return b.defineInRange(cleanPath("dimensional_bags", path), def, min, max, (Class<T>)def.getClass());
        }));
//        return ()->(T)CONFIG_BUILDERS.get(path).getValue();
    }

    protected static <T> /*Supplier<T>*/ void value(@Nonnull String path, T def, @Nullable Collection<? extends T> valid, @Nullable String cmt) {
        CONFIG_BUILDERS.put(path, new ConfigValueP<>(path, b->{
            if (cmt != null && !cmt.isEmpty())
                b.comment(cmt);
            if (valid != null && !valid.isEmpty())
                return b.defineInList(cleanPath("dimensional_bags", path), def, valid);
            return b.define(cleanPath("dimensional_bags", path), def);
        }));
//        return ()->(T)CONFIG_BUILDERS.get(path).getValue();
    }

    protected static <T extends Enum<T>> /*Supplier<T>*/ void enumValue(@Nonnull String path, T def, @Nullable Collection<T> valid, @Nullable String cmt) {
        CONFIG_BUILDERS.put(path, new ConfigValueP<>(path, b->{
            if (cmt != null && !cmt.isEmpty())
                b.comment(cmt);
            if (valid != null && !valid.isEmpty())
                return b.defineEnum(cleanPath("dimensional_bags", path), def, valid);
            return b.defineEnum(cleanPath("dimensional_bags", path), def);
        }));
//        return ()->(T)CONFIG_BUILDERS.get(path).getValue();
    }

    public static ForgeConfigSpec getSpec() { return SPEC; }

    public static void bakeAll() {
        for (ConfigValueP<?> cv : CONFIG_BUILDERS.values())
            cv.bakeConfig();
    }

    @SubscribeEvent
    public static void onModConfigEvent(final ModConfig.ModConfigEvent event) {
        if (event.getConfig().getSpec() == ConfigEvents.SPEC) {
            UpgradeManager.bakeConfig(); //should be replaced by the new config system
            bakeAll();
            isBaked = true;
        }
    }

    private static class ConfigBuilder {
        ConfigBuilder(ForgeConfigSpec.Builder builder) {
            UpgradeManager.buildConfig(builder);
            for (ConfigValueP<?> cv : CONFIG_BUILDERS.values())
                cv.buildConfig(builder);
            isBuilt = true;
        }
    }
}
