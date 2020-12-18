package com.limachi.dimensional_bags.common.executors;

import com.limachi.dimensional_bags.ClassUtils;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.data.EyeDataMK2.EnergyData;
import javafx.util.Pair;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

public class EntityExecutor {
    protected static class ExecutorMao<O extends Entity> {

        protected class Entry {
            final public String name;
            final public Class<?>[] paramTypes;
            final public int energyCost;
            final public BiConsumer<O, Pair<Integer, Object[]>> executable;

            public Entry(String name, Class<?>[] paramTypes, int energyCost, BiConsumer<O, Pair<Integer, Object[]>> executable) {
                this.name = name;
                this.paramTypes = paramTypes;
                this.energyCost = energyCost;
                this.executable = executable;
            }

            public TranslationTextComponent getHelp() {
                return new TranslationTextComponent("executor.help." + name);
            }

            public ITextComponent getMethodPrototype() {
                IFormattableTextComponent out = new TranslationTextComponent("executor.method." + name);
                for (Class<?> p : paramTypes)
                    out.append(new StringTextComponent(" <")).append(new TranslationTextComponent("type." + p.getName())).append(new StringTextComponent(">"));
                return out;
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
                    e.executable.accept(clazz.cast(entity), new Pair<>(eyeId, params));
                    return true;
                }
            }
            return false;
        }

        public void register(String name, int energyCost, BiConsumer<O, Pair<Integer, Object[]>> consumer, Class<? extends Object> ...paramTypes) { map.put(name, new Entry(name, paramTypes, energyCost, consumer)); }
    }

    protected Optional<Boolean> boolFromString(String value) {
        if (value.equalsIgnoreCase("true"))
            return Optional.of(true);
        else if (value.equalsIgnoreCase("false"))
            return Optional.of(false);
        return Optional.empty();
    }

    static final protected HashMap<Class<? extends Entity>, ExecutorMao<?>> cache = new HashMap<>();

    static public <O extends Entity> void register(Class<O> clazz, String name, int energyCost, BiConsumer<O, Pair<Integer, Object[]>> supplier, Class<? extends Object> ...paramTypes) {
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
        register(PlayerEntity.class, "jump_key", 16, (t, p) -> {KeyMapController.KeyBindings.JUMP_KEY.forceKeyState(t, (Boolean)p.getValue()[0]);
            DimBag.delayedTask(2, ()->KeyMapController.KeyBindings.JUMP_KEY.forceKeyState(t, false));
        }, Boolean.TYPE);

        register(Entity.class, "set_velocity", 0, (t, p)->{
            Vector3d pv = t.getMotion();
            Vector3d nv = new Vector3d((Double)p.getValue()[0], (Double)p.getValue()[1], (Double)p.getValue()[2]);
            int cost = (int)(pv.squareDistanceTo(nv) * 5); //the energy requirement is calculated on by the difference of the previous velocity
            if (EnergyData.execute(p.getKey(), energyData -> energyData.extractEnergy(cost, true) == cost, false)) {
                EnergyData.execute(p.getKey(), energyData -> energyData.extractEnergy(cost, false));
                t.setVelocity(nv.x, nv.y, nv.z);
                t.velocityChanged = true;
            }
        }, Double.TYPE, Double.TYPE, Double.TYPE);
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
