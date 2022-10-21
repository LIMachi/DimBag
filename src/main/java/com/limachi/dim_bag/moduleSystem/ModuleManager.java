package com.limachi.dim_bag.moduleSystem;

import com.limachi.lim_lib.saveData.AbstractSyncSaveData;
import com.limachi.lim_lib.saveData.RegisterSaveData;
import com.limachi.lim_lib.saveData.SaveDataManager;
import com.limachi.lim_lib.saveData.SaveSync;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.eventbus.api.Event;
import org.antlr.v4.runtime.misc.MultiMap;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RegisterSaveData(name = ModuleManager.NAME)
public class ModuleManager extends AbstractSyncSaveData {

    private final HashMap<BlockPos, String> modules_by_pos = new HashMap<>();
    private final MultiMap<String, BlockPos> modules_by_type = new MultiMap<>();

    public static final String NAME = "bag_modules";

    public ModuleManager(String name) { super(name, SaveSync.SERVER_TO_CLIENT); }

    protected ModuleManager(int id) { super(ModuleManager.NAME + ":" + id, SaveSync.SERVER_TO_CLIENT); }

    public static ModuleManager getInstance(int id) { return SaveDataManager.getInstance(ModuleManager.NAME + ":" + id); }

    public void event(Event event) {

    }

    public boolean installModuleAt(BlockPos pos, String module) {
        String li = modules_by_pos.get(pos);
        if (li == null || li.isBlank()) {
            modules_by_pos.put(pos, module);
            modules_by_type.map(module, pos);
            setDirty();
            return true;
        }
        return false;
    }

    public boolean removeModuleAt(BlockPos pos) {
        String type = modules_by_pos.get(pos);
        if (type == null || type.isBlank()) return false;
        modules_by_pos.remove(pos);
        modules_by_type.get(type).remove(pos);
        setDirty();
        return true;
    }

    public List<BlockPos> moduleInstances(String module) { return modules_by_type.get(module); }

    public String getModuleAt(BlockPos pos) { return modules_by_pos.get(pos); }

    @NotNull
    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        ListTag list = new ListTag();
        for (Map.Entry<BlockPos, String> e : modules_by_pos.entrySet()) {
            CompoundTag t = new CompoundTag();
            t.putLong("Pos", e.getKey().asLong());
            t.putString("Type", e.getValue());
            list.add(t);
        }
        compoundTag.put("Modules", list);
        return compoundTag;
    }

    @Override
    public void load(CompoundTag compoundTag) {
        modules_by_pos.clear();
        modules_by_type.clear();
        for (Tag t : compoundTag.getList("Modules", Tag.TAG_COMPOUND)) {
            String type = ((CompoundTag)t).getString("Type");
            BlockPos pos = BlockPos.of(((CompoundTag)t).getLong("Pos"));
            modules_by_pos.put(pos, type);
            modules_by_type.map(type, pos);
        }
    }
}
