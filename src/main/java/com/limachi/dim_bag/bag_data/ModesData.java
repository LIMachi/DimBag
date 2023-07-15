package com.limachi.dim_bag.bag_data;

import com.limachi.dim_bag.bag_modes.BaseMode;
import com.limachi.dim_bag.bag_modes.Capture;
import com.limachi.dim_bag.bag_modes.Settings;
import com.limachi.dim_bag.utils.Tags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/*
public class ModesData {

    protected final int id;

    public ModesData(int bag) { id = bag; }

    public int getId() { return id; }

    protected ListTag data() { return Tags.getOrCreateList(AllBagsData.getRawBag(id), "modes", ListTag::new); }

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

    public static BaseMode getMode(int index) { return index >= 0 && index < modesList.size() ? modesList.get(index).mode : DEFAULT.mode; }
    public static BaseMode getMode(String id) { return modesMap.getOrDefault(id, DEFAULT).mode; }
    public static int getModeIndex(String id) { return modesMap.getOrDefault(id, DEFAULT).index; }

    public boolean installMode(String name) {
        ListTag data = data();
        BaseMode mode = getMode(name);
        for (Tag tag : data)
            if (tag instanceof CompoundTag entry && name.equals(entry.getString("name")))
                return false;
        CompoundTag entry = mode.initialData();
        entry.putString("name", name);
        data.add(entry);
        return true;
    }

    public void installInitialModes() {
        for (ModeEntry me : modesList)
            if (me.mode.autoInstall)
                installMode(me.mode.name);
    }

    public String cycleMode(String mode, int amount) {
//        if (mode == null || amount == 0 || order.size() < 2) return mode;
//        int index;
//        for (index = 0; index < order.size(); ++index)
//            if (order.get(index).equals(mode))
//                break;
//        index += amount;
//        int l = order.size();
//        while (index < 0)
//            index += l;
//        while (index >= l)
//            index -= l;
//        return order.get(index);
        return "Default"; //FIXME
    }
}
*/