package com.limachi.dim_bag.save_datas;

import com.limachi.dim_bag.bag_modes.BaseMode;
import com.limachi.dim_bag.bag_modes.Capture;
import com.limachi.dim_bag.bag_modes.Settings;
import com.limachi.lim_lib.saveData.AbstractSyncSaveData;
import com.limachi.lim_lib.saveData.RegisterSaveData;
import com.limachi.lim_lib.saveData.SaveDataManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/*
@RegisterSaveData
public class ModesManager extends AbstractSyncSaveData {

    public ModesManager(String name) { super(name); }

    public record ModeEntry(String name, int index, BaseMode mode) {}
    protected static final LinkedHashMap<String, ModeEntry> modesMap = new LinkedHashMap<>();
    protected static final ArrayList<ModeEntry> modesList = new ArrayList<>();

    public static ModeEntry registerMode(BaseMode mode) {
        ModeEntry entry = new ModeEntry(mode.name, modesList.size(), mode);
        modesList.add(entry);
        modesMap.put(mode.name, entry);
        return entry;
    }

    public static final ModeEntry DEFAULT = registerMode(new BaseMode("Default", true){});

    static  {
        registerMode(new BaseMode("Tank", false){});
        registerMode(new Settings());
        registerMode(new Capture());
    }

    public static void installInitialModes(int bag) {
        ModesManager m = getInstance(bag);
        if (m != null) {
            for (ModeEntry me : modesList) {
                BaseMode mode = me.mode;
                if (mode.autoInstall)
                    m.installMode(mode.name, false);
            }
            m.setDirty();
        }
    }

    public static boolean installMode(int bag, String name) {
        ModesManager m = getInstance(bag);
        if (m != null)
            return m.installMode(name, true);
        return false;
    }
    public boolean installMode(String name, boolean andDirty) {
            BaseMode mode = getMode(name);
            if (!order.contains(name)) {
                datas.put(name, mode.initialData());
                order.add(name);
                if (andDirty)
                    setDirty();
                return true;
            }
        return false;
    }

    public static BaseMode getMode(int index) { return index >= 0 && index < modesList.size() ? modesList.get(index).mode : DEFAULT.mode; }
    public static BaseMode getMode(String id) { return modesMap.getOrDefault(id, DEFAULT).mode; }

    public static int getModeIndex(String id) { return modesMap.getOrDefault(id, DEFAULT).index; }
    protected final LinkedHashMap<String, CompoundTag> datas = new LinkedHashMap<>();
    protected final ArrayList<String> order = new ArrayList<>();

    public static ModesManager getInstance(int bag) { return bag > 0 ? SaveDataManager.getInstance("modes_manager:" + bag) : null; }

    public String cycleMode(String mode, int amount) {
        if (mode == null || amount == 0 || order.size() < 2) return mode;
        int index;
        for (index = 0; index < order.size(); ++index)
            if (order.get(index).equals(mode))
                break;
        index += amount;
        int l = order.size();
        while (index < 0)
            index += l;
        while (index >= l)
            index -= l;
        return order.get(index);
    }

    public CompoundTag getModeData(String mode) { return datas.get(mode); }

    @NotNull
    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        CompoundTag datas = new CompoundTag();
        for (Map.Entry<String, CompoundTag> entry : this.datas.entrySet())
            datas.put(entry.getKey(), entry.getValue());
        ListTag order = new ListTag();
        for (String entry : this.order)
            order.add(StringTag.valueOf(entry));
        compoundTag.put("datas", datas);
        compoundTag.put("order", order);
        return compoundTag;
    }

    @Override
    public void load(CompoundTag compoundTag) {
        datas.clear();
        order.clear();
        if (compoundTag.get("order") instanceof ListTag list)
            for (Tag entry : list)
                if (entry instanceof StringTag mode)
                    order.add(mode.getAsString());
        CompoundTag datas = compoundTag.getCompound("datas");
        for (String key : order)
            if (datas.contains(key))
                this.datas.put(key, datas.getCompound(key));
            else
                this.datas.put(key, getMode(key).initialData());
    }
}
*/