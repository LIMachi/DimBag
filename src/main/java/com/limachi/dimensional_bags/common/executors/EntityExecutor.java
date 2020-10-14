package com.limachi.dimensional_bags.common.executors;

import com.limachi.dimensional_bags.ClassUtils;
import com.limachi.dimensional_bags.DimBag;
import javafx.util.Pair;
import net.minecraft.entity.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

public class EntityExecutor {
    protected static class ExecutorMao<O extends Entity> {
        private final Class<O> clazz;
        private final HashMap<String, Pair<Class<?>[], BiConsumer<O, Object[]>>> map = new HashMap<>();

        public ExecutorMao(Class<O> clazz) { this.clazz = clazz; }

        public boolean isValidMethod(String command, Object entity) {
            if (entity instanceof Entity && clazz.isInstance(entity) && map.containsKey(ClassUtils.Strings.splitIgnoringQuotes(command, " ", 0)[0])) {
                String[] s = ClassUtils.Strings.splitIgnoringQuotes(command, " ", 0);
                Class<?>[] pt = map.get(s[0]).getKey();
                if (s.length - 1 != pt.length)
                    return false;
                for (int i = 0; i < pt.length; ++i)
                    if (!ClassUtils.canParse(pt[i], s[1 + i]))
                        return false;
                return true;
            }
            return false;
        }

        public Set<String> methods() { return map.keySet(); }

        public boolean run(String command, Object entity) {
            if (entity instanceof Entity && clazz.isInstance(entity) && map.containsKey(ClassUtils.Strings.splitIgnoringQuotes(command, " ", 0)[0])) {
                String[] s = ClassUtils.Strings.splitIgnoringQuotes(command, " ", 0);
                Class<?>[] pt = map.get(s[0]).getKey();
                if (s.length - 1 != pt.length)
                    return false;
                Object[] params = new Object[pt.length];
                for (int i = 0; i < pt.length; ++i)
                    if ((params[i] = ClassUtils.parse(pt[i], s[1 + i])) == null)
                        return false;
                map.get(s[0]).getValue().accept(clazz.cast(entity), params);
                return true;
            }
            return false;
        }

        public void register(String name, BiConsumer<O, Object[]> consumer, Class<? extends Object> ...paramTypes) { map.put(name, new Pair<>(paramTypes, consumer)); }
    }

    protected Optional<Boolean> boolFromString(String value) {
        if (value.equalsIgnoreCase("true"))
            return Optional.of(true);
        else if (value.equalsIgnoreCase("false"))
            return Optional.of(false);
        return Optional.empty();
    }

    static final protected HashMap<Class<? extends Entity>, ExecutorMao<?>> cache = new HashMap<>();

    static public <O extends Entity> void register(Class<O> clazz, String name, BiConsumer<O, Object[]> supplier, Class<? extends Object> ...paramTypes) {
        ExecutorMao<O> cached = (ExecutorMao<O>)cache.get(clazz);
        if (cached == null)
            cache.put(clazz, cached = new ExecutorMao<>(clazz));
        cached.register(name, supplier, paramTypes);
    }

    protected final Entity entity;

    public <T extends Entity> EntityExecutor(T entity) {
        this.entity = entity;
    }

    static {
        register(Entity.class, "test", (t,p) -> DimBag.getServer().getCommandManager().handleCommand(t.getCommandSource(), "/say Hello World"));
    }

    protected boolean isValidConsumer(String command) {
        for (Map.Entry<Class<? extends Entity>, ExecutorMao<?>> entry : cache.entrySet())
            if (entry.getValue().isValidMethod(command, entity))
                return true;
        return false;
    }

    public void run(String command) {
        if (isValidConsumer(command)) {
            for (Map.Entry<Class<? extends Entity>, ExecutorMao<?>> entry : cache.entrySet())
                if (entry.getValue().run(command, entity))
                    return;
        }
    }
}
