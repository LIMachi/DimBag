package com.limachi.dim_bag.settings;

import com.google.common.collect.ImmutableList;
//import com.limachi.dim_bag.modes.ModeManager;
import com.limachi.lim_lib.NBT;
import com.limachi.lim_lib.saveData.AbstractSyncSaveData;
import com.limachi.lim_lib.saveData.RegisterSaveData;
import com.limachi.lim_lib.saveData.SaveSync;
import net.minecraft.nbt.CompoundTag;
//import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;
/*
@RegisterSaveData
public class SettingsData extends AbstractSyncSaveData {
    public SettingsData(String name, SaveSync sync) {
        super(name, sync);
    }

    private static final ArrayList<SettingsReader> READERS = new ArrayList<>();
    private CompoundTag settingsNbt = new CompoundTag();

    public CompoundTag getSettings(String group, String name, boolean generatePath) {
        if (!settingsNbt.contains(group) && generatePath) {
            settingsNbt.put(group, new CompoundTag());
            setDirty();
        }
        CompoundTag t = settingsNbt.getCompound(group);
        if (!t.contains(name) && generatePath) {
            t.put(name, new CompoundTag());
            setDirty();
        }
        return t.getCompound(name);
    }

    public static ImmutableList<SettingsReader> getReaders() { return ImmutableList.copyOf(READERS); }

//    public Inventory getSettingsIcons() {
//        Inventory out = new Inventory(READERS.size());
//        for (int i = 0; i < READERS.size(); ++i)
//            out.setItem(i, READERS.get(i).getIcon());
//        return out;
//    }

    public void writeSettings(String group, String name, CompoundTag nbt, boolean isDiff) {
        if (!settingsNbt.contains(group))
            settingsNbt.put(group, new CompoundTag());
        CompoundTag t = settingsNbt.getCompound(group);
        if (!t.contains(name) && isDiff)
            t.put(name, new CompoundTag());
        if (isDiff)
            NBT.applyDiff(t.getCompound(name), nbt);
        else
            t.put(name, nbt);
        setDirty();
    }

    public void initDefaultSettings() {
        for (SettingsReader reader : READERS)
            for (Map.Entry<String, SettingsReader.SettingsEntry<?>> e : reader.settings.entrySet())
                reader.set(e.getKey(), this, e.getValue().inputType.def);
        ModeManager.getMode("Default").settingsReader.set("bag_name", this, new TranslatableComponent("item.dim_bag.bag").getString() + " " + getbagId());
    }

    public String getBagName() { return ModeManager.getMode("Default").getSetting(getbagId(), "bag_name"); }

    @NotNull
    @Override
    public CompoundTag save(CompoundTag nbt) {
        nbt.merge(settingsNbt);
        return nbt;
    }

    @Override
    public void load(CompoundTag nbt) {
        settingsNbt.merge(nbt);
    }
}
*/