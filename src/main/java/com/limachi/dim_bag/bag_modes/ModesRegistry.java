package com.limachi.dim_bag.bag_modes;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ModesRegistry {
    public record ModeEntry(String name, int index, BaseMode mode) {}
    private static final LinkedHashMap<String, ModeEntry> modesMap = new LinkedHashMap<>();
    public static final ArrayList<ModeEntry> modesList = new ArrayList<>();

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
}
