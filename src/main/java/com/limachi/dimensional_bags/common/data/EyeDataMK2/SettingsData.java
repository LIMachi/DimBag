package com.limachi.dimensional_bags.common.data.EyeDataMK2;

import com.limachi.dimensional_bags.common.NBTUtils;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class SettingsData extends WorldSavedDataManager.EyeWorldSavedData {

    private CompoundNBT settingsNbt = new CompoundNBT();

    public static class Settings {
        protected final String group; //"Upgrades", "Modes", "Others", etc
        protected final String name; //"<upgrade_name>", "<mode_name>", "General", "Interact", etc
        protected final HashMap<String, SettingsEntry<?>> settings = new HashMap<>();

        protected static class SettingsEntry<T> {
            final private String label;
            final private Predicate<T> validate;

            static class InputType<T> {
                public final T def;
                public final T start;
                public final T end;
                public final boolean withInput;

                private InputType(T def, T start, T end, boolean withInput) {
                    this.def = def;
                    this.start = start;
                    this.end = end;
                    this.withInput = withInput;
                }

                public static <T> InputType<T> inputOnly(T def) { return new InputType<>(def, null, null, true); }
                public static <T> InputType<T> rangeOnly(T def, T start, T end) { return new InputType<>(def, start, end, false); }
                public static <T> InputType<T> rangeAndInput(T def, T start, T end) { return new InputType<>(def, start, end, true); }
            };

            final private InputType<T> inputType;

            SettingsEntry(String label, Predicate<T> validate, InputType<T> inputType) {
                this.label = label;
                this.validate = validate;
                this.inputType = inputType;
            }

            public T get(CompoundNBT nbt) { return NBTUtils.getOrDefault(nbt, label, inputType.def); }
            public void set(CompoundNBT nbt, T value) { NBTUtils.put(nbt, label, value); }
            public boolean validate(T value) { return validate.test(value); }
        }

        public Settings(String group, String name) {
            this.group = group;
            this.name = name;
        }

        public <T> T get(String label, SettingsData data) {
            SettingsEntry<T> se = (SettingsEntry<T>)settings.get(label);
            if (se == null) return null;
            if (data != null) {
                CompoundNBT t = data.getSettings(group, name, false);
                return se.get(t);
            }
            return se.inputType.def;
        }

        public <T> void set(String label, SettingsData data, T value) {
            SettingsEntry<T> se = (SettingsEntry<T>)settings.get(label);
            CompoundNBT t = data.getSettings(group, name, true);
            se.set(t, value);
            data.markDirty();
        }

        public Settings bool(String label, boolean def) { settings.put(label, new SettingsEntry<>(label, b->true, SettingsEntry.InputType.inputOnly(def))); return this; }
        public Settings string(String label, String def, @Nullable Collection<String> valid) { settings.put(label, new SettingsEntry<>(label, s->valid == null || valid.contains(s), SettingsEntry.InputType.inputOnly(def))); return this; }
        public Settings integer(String label, int def, int min, int max, boolean withInput) { settings.put(label, new SettingsEntry<>(label, i->i >= min && i <= max, withInput ? SettingsEntry.InputType.rangeAndInput(def, min, max) : SettingsEntry.InputType.rangeOnly(def, min, max))); return this; }
        public Settings integer(String label, int def, @Nullable Collection<Integer> valid) { settings.put(label, new SettingsEntry<>(label, i->valid == null || valid.contains(i), SettingsEntry.InputType.inputOnly(def))); return this; }
        public Settings real(String label, double def, double min, double max, boolean withInput) { settings.put(label, new SettingsEntry<>(label, d->d >= min && d <= max, withInput ? SettingsEntry.InputType.rangeAndInput(def, min, max) : SettingsEntry.InputType.rangeOnly(def, min, max))); return this; }
        public Settings real(String label, double def, @Nullable Collection<Double> valid) { settings.put(label, new SettingsEntry<>(label, d->valid == null || valid.contains(d), SettingsEntry.InputType.inputOnly(def))); return this; }
    }

    public SettingsData(String suffix, int id, boolean client) { super(suffix, id, client, true); }

    public CompoundNBT getSettings(String group, String name, boolean generatePath) {
        if (!settingsNbt.contains(group) && generatePath) {
            settingsNbt.put(group, new CompoundNBT());
            markDirty();
        }
        CompoundNBT t = settingsNbt.getCompound(group);
        if (!t.contains(name) && generatePath) {
            t.put(name, new CompoundNBT());
            markDirty();
        }
        return t.getCompound(name);
    }

    public void writeSettings(String group, String name, CompoundNBT nbt, boolean isDiff) {
        if (!settingsNbt.contains(group))
            settingsNbt.put(group, new CompoundNBT());
        CompoundNBT t = settingsNbt.getCompound(group);
        if (!t.contains(name) && isDiff)
            t.put(name, new CompoundNBT());
        if (isDiff)
            NBTUtils.applyDiff(t.getCompound(name), nbt);
        else
            t.put(name, nbt);
        markDirty();
    }

    @Override
    public void read(@Nonnull CompoundNBT nbt) { settingsNbt = nbt.copy(); }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        nbt.merge(this.settingsNbt);
        return nbt;
    }

    static public SettingsData getInstance(int id) {
        return WorldSavedDataManager.getInstance(SettingsData.class, null, id);
    }

    static public <T> T execute(int id, Function<SettingsData, T> executable, T onErrorReturn) {
        return WorldSavedDataManager.execute(SettingsData.class, null, id, executable, onErrorReturn);
    }

    static public boolean execute(int id, Consumer<SettingsData> executable) {
        return WorldSavedDataManager.execute(SettingsData.class, null, id, data->{executable.accept(data); return true;}, false);
    }
}
