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

    public static final ModeEntry DEFAULT = registerMode(new BaseMode("Default", null){});

    static  {
        registerMode(new TankMode());
        registerMode(new SettingsMode());
        registerMode(new CaptureMode());
        registerMode(new ParasiteMode());
    }

    public static BaseMode getMode(int index) { return index >= 0 && index < modesList.size() ? modesList.get(index).mode : DEFAULT.mode; }
    public static BaseMode getMode(String id) { return modesMap.getOrDefault(id, DEFAULT).mode; }
    public static int getModeIndex(String id) { return modesMap.getOrDefault(id, DEFAULT).index; }

    public static int cycleMode(int currentMode, long mask, int amount) {
        if (amount == 0 || mask == 0) return currentMode;
        if (amount > 0)
            while (amount > 0) {
                currentMode = (currentMode + 1) % modesList.size();
                --amount;
                while ((mask & (1L << currentMode)) == 0)
                    currentMode = (currentMode + 1) % modesList.size();
            }
        else
            while (amount < 0) {
                currentMode = currentMode <= 0 ? modesList.size() - 1 : currentMode - 1;
                ++amount;
                while ((mask & (1L << currentMode)) == 0)
                    currentMode = currentMode <= 0 ? modesList.size() - 1 : currentMode - 1;
            }
        return currentMode;
    }
}
