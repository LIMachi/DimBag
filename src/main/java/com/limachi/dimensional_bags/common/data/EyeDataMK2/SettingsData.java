package com.limachi.dimensional_bags.common.data.EyeDataMK2;

import com.google.common.collect.ImmutableList;
import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.utils.NBTUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.gui.widget.Slider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class SettingsData extends WorldSavedDataManager.EyeWorldSavedData {

    private CompoundNBT settingsNbt = new CompoundNBT();

    private static final ArrayList<SettingsReader> READERS = new ArrayList<>();

    public static class SettingsReader {
        public final String group; //"Upgrades", "Modes", "Others", etc
        public final String name; //"<upgrade_name>", "<mode_name>", "General", "Interact", etc
        public final Supplier<ItemStack> icon;
        public final HashMap<String, SettingsEntry<?>> settings = new HashMap<>();
        protected boolean isBuilt;

        protected static class SettingsEntry<T> {
            final private String label;
            final private Predicate<T> validate;
            private SettingsReader handler;

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
            @OnlyIn(Dist.CLIENT)
            public Widget getWidget(SettingsData data, CompoundNBT nbt, int x, int y) {
                ITextComponent title = new TranslationTextComponent("settings." + handler.group + "." + handler.name + "." + label);
                if (inputType.def instanceof Boolean)
                    return new CheckboxButton(x, y, 16, 16, title, (Boolean)get(nbt)) {
                        @Override
                        public void onPress() {
                            super.onPress();
                            set(nbt, (T)(Boolean)isChecked());
                            data.markDirty();
                        }
                    };
                if (inputType.def instanceof String) {
                    TextFieldWidget field = new TextFieldWidget(Minecraft.getInstance().fontRenderer, x, y, 200, 20, title);
                    field.writeText((String) get(nbt));
                    field.setResponder(s -> {set(nbt, (T) s); data.markDirty();});
                    return field;
                }
                if (inputType.start != null && inputType.end != null) {
                    return new Slider(x, y, title, ((Number) inputType.start).doubleValue(), ((Number) inputType.end).doubleValue(), ((Number) (T) get(nbt)).doubleValue(), onPress -> {
                    }, null) {
                        @Override
                        public void onRelease(double mouseX, double mouseY) {
                            super.onRelease(mouseX, mouseY);
                            if (inputType.def instanceof Integer)
                                set(nbt, (T) (Integer) getValueInt());
                            else
                                set(nbt, (T) (Double) getValue());
                            data.markDirty();
                        }
                    };
                }
                if (inputType.def instanceof Integer) {
                    TextFieldWidget field = new TextFieldWidget(Minecraft.getInstance().fontRenderer, x, y, 200, 20, title);
                    field.writeText((String) get(nbt));
                    field.setResponder(s -> {set(nbt, (T)(Integer)Integer.parseInt(s)); data.markDirty();});
                    return field;
                }
                if (inputType.def instanceof Double) {
                    TextFieldWidget field = new TextFieldWidget(Minecraft.getInstance().fontRenderer, x, y, 200, 20, title);
                    field.writeText((String) get(nbt));
                    field.setResponder(s -> {set(nbt, (T)(Double)Double.parseDouble(s)); data.markDirty();});
                    return field;
                }
                return null;
            }

            SettingsEntry<T> attach(SettingsReader handler) { this.handler = handler; return this; }
        }

        public SettingsReader(String group, String name, Supplier<ItemStack> icon) {
            this.group = group;
            this.name = name;
            this.icon = icon;
            this.isBuilt = false;
        }

        @OnlyIn(Dist.CLIENT)
        public ArrayList<Widget> getWidgets(SettingsData data, int x, int y) {
            CompoundNBT nbt = data.getSettings(group, name, true);
            ArrayList<Widget> out = new ArrayList<>();
            for (SettingsEntry<?> entry : settings.values()) {
                Widget widget = entry.getWidget(data, nbt, x, y);
                if (widget != null) {
                    y += 2 + widget.getHeight();
                    out.add(widget);
                }
            }
            return out;
        }

        public <T> T get(String label, SettingsData data) {
            if (isBuilt) {
                SettingsEntry<T> se = (SettingsEntry<T>) settings.get(label);
                if (se == null) return null;
                if (data != null) {
                    CompoundNBT t = data.getSettings(group, name, false);
                    return se.get(t);
                }
                return se.inputType.def;
            }
            DimBag.LOGGER.error("Accessing settings before build finished (get): " + group + "." + name + "." + label);
            return null;
        }

        public <T> void set(String label, SettingsData data, T value) {
            if (isBuilt) {
                SettingsEntry<T> se = (SettingsEntry<T>) settings.get(label);
                CompoundNBT t = data.getSettings(group, name, true);
                se.set(t, value);
                data.markDirty();
                return;
            }
            DimBag.LOGGER.error("Accessing settings before build finished (set): " + group + "." + name + "." + label);
        }

        public void build() {
            isBuilt = true;
            READERS.add(this);
        }

        public ItemStack getIcon() {
            if (isBuilt)
                return icon.get().setDisplayName(new TranslationTextComponent("settings." + group + "." + name));
            DimBag.LOGGER.error("Accessing settings before build finished (icon): " + group + "." + name);
            return ItemStack.EMPTY;
        }

        private SettingsReader val(String label, SettingsEntry entry) {
            if (isBuilt)
                DimBag.LOGGER.error("Creating settings after build finished: " + group + "." + name + "." + label);
            else
                settings.put(label, entry.attach(this));
            return this;
        }

        public SettingsReader bool(String label, boolean def) { return val(label, new SettingsEntry<>(label, b->true, SettingsEntry.InputType.inputOnly(def))); }
        public SettingsReader string(String label, String def, @Nullable Collection<String> valid) { return val(label, new SettingsEntry<>(label, s->valid == null || valid.contains(s), SettingsEntry.InputType.inputOnly(def))); }
        public SettingsReader integer(String label, int def, int min, int max, boolean withInput) { return val(label, new SettingsEntry<>(label, i->i >= min && i <= max, withInput ? SettingsEntry.InputType.rangeAndInput(def, min, max) : SettingsEntry.InputType.rangeOnly(def, min, max))); }
        public SettingsReader integer(String label, int def, @Nullable Collection<Integer> valid) { return val(label, new SettingsEntry<>(label, i->valid == null || valid.contains(i), SettingsEntry.InputType.inputOnly(def))); }
        public SettingsReader real(String label, double def, double min, double max, boolean withInput) { return val(label, new SettingsEntry<>(label, d->d >= min && d <= max, withInput ? SettingsEntry.InputType.rangeAndInput(def, min, max) : SettingsEntry.InputType.rangeOnly(def, min, max))); }
        public SettingsReader real(String label, double def, @Nullable Collection<Double> valid) { return val(label, new SettingsEntry<>(label, d->valid == null || valid.contains(d), SettingsEntry.InputType.inputOnly(def))); }
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

    public static ImmutableList<SettingsReader> getReaders() { return ImmutableList.copyOf(READERS); }

    public Inventory getSettingsIcons() {
        Inventory out = new Inventory(READERS.size());
        for (int i = 0; i < READERS.size(); ++i)
            out.setInventorySlotContents(i, READERS.get(i).getIcon());
        return out;
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

    public void initDefaultSettings() {
        for (SettingsReader reader : READERS)
            for (Map.Entry<String, SettingsReader.SettingsEntry<?>> e : reader.settings.entrySet())
                reader.set(e.getKey(), this, e.getValue().inputType.def);
    }

    @Override
    public void read(@Nonnull CompoundNBT nbt) {
        settingsNbt.merge(nbt); //this should ensure the same pointer is kept, but it is not the cleanest way (hold data will bloat if not rewritten)
    }

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
