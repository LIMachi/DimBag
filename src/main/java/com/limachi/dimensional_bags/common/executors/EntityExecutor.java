package com.limachi.dimensional_bags.common.executors;

import com.limachi.dimensional_bags.utils.ClassUtils;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.KeyMapController;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.EnergyData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

import com.limachi.dimensional_bags.StaticInit;

@StaticInit
public class EntityExecutor {
    protected static class ExecutorMao<O extends Entity> {

        protected class Entry {
            final public Class<?>[] paramTypes;
            final public int energyCost;
            final public BiConsumer<O, Object[]> executable;

            public Entry(Class<?>[] paramTypes, int energyCost, BiConsumer<O, Object[]> executable) {
                this.paramTypes = paramTypes;
                this.energyCost = energyCost;
                this.executable = executable;
            }
        }

        private final Class<O> clazz;
        private final HashMap<String, Entry> map = new HashMap<>();

        public ExecutorMao(Class<O> clazz) { this.clazz = clazz; }

        public boolean isValidMethod(String command, Object entity) {
            if (entity instanceof Entity && clazz.isInstance(entity) && map.containsKey(ClassUtils.Strings.splitIgnoringQuotes(command, " ", 0)[0])) {
                String[] s = ClassUtils.Strings.splitIgnoringQuotes(command, " ", 0);
                Class<?>[] pt = map.get(s[0]).paramTypes;
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

        public boolean run(String command, int eyeId, Object entity) {
            if (entity instanceof Entity && clazz.isInstance(entity) && map.containsKey(ClassUtils.Strings.splitIgnoringQuotes(command, " ", 0)[0])) {
                String[] s = ClassUtils.Strings.splitIgnoringQuotes(command, " ", 0);
                Entry e = map.get(s[0]);
                Class<?>[] pt = e.paramTypes;
                if (s.length - 1 != pt.length)
                    return false;
                Object[] params = new Object[pt.length];
                for (int i = 0; i < pt.length; ++i)
                    if ((params[i] = ClassUtils.parse(pt[i], s[1 + i])) == null)
                        return false;
                if (EnergyData.execute(eyeId, energyData -> energyData.extractEnergy(e.energyCost, true) == e.energyCost, false)) {
                    EnergyData.execute(eyeId, energyData -> energyData.extractEnergy(e.energyCost, false));
                    e.executable.accept(clazz.cast(entity), params);
                    return true;
                }
            }
            return false;
        }

        public void register(String name, int energyCost, BiConsumer<O, Object[]> consumer, Class<? extends Object> ...paramTypes) { map.put(name, new Entry(paramTypes, energyCost, consumer)); }
    }

    protected Optional<Boolean> boolFromString(String value) {
        if (value.equalsIgnoreCase("true"))
            return Optional.of(true);
        else if (value.equalsIgnoreCase("false"))
            return Optional.of(false);
        return Optional.empty();
    }

    static final protected HashMap<Class<? extends Entity>, ExecutorMao<?>> cache = new HashMap<>();

    static public <O extends Entity> void register(Class<O> clazz, String name, int energyCost, BiConsumer<O, Object[]> supplier, Class<? extends Object> ...paramTypes) {
        ExecutorMao<O> cached = (ExecutorMao<O>)cache.get(clazz);
        if (cached == null)
            cache.put(clazz, cached = new ExecutorMao<>(clazz));
        cached.register(name, energyCost, supplier, paramTypes);
    }

    protected final Entity entity;
    protected final int eyeId;

    public <T extends Entity> EntityExecutor(T entity, int eyeId) {
        this.entity = entity;
        this.eyeId = eyeId;
    }

    static {
        register(PlayerEntity.class, "jump_key", 16, (t, p) -> {KeyMapController.KeyBindings.JUMP_KEY.forceKeyState(t, (Boolean)p[0]);
            DimBag.delayedTask(2, ()->KeyMapController.KeyBindings.JUMP_KEY.forceKeyState(t, false));
        }, Boolean.TYPE);
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
                if (entry.getValue().run(command, eyeId, entity))
                    return;
        }
    }
}
