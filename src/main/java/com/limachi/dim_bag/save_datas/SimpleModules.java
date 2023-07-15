package com.limachi.dim_bag.save_datas;

import com.limachi.lim_lib.saveData.AbstractSyncSaveData;
import com.limachi.lim_lib.saveData.RegisterSaveData;
import com.limachi.lim_lib.saveData.SaveDataManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/*
@RegisterSaveData
public class SimpleModules extends AbstractSyncSaveData {

    protected CompoundTag data = new CompoundTag();

    public SimpleModules(String name) { super(name); }

    public static SimpleModules getInstance(int bagId) {
        if (bagId <= 0) return null;
        return SaveDataManager.getInstance("simple_modules:" + bagId, Level.OVERWORLD);
    }

//    public CompoundTag getUnmodifiableData() { return data; }

    public void deltaCountableModule(String name, int amount) {
        amount += data.getInt(name);
        if (amount > 0)
            data.putInt(name, amount);
        else
            data.remove(name);
        setDirty();
    }

    public void addPositionListModule(String name, BlockPos pos) {
        if (!data.contains(name))
            data.put(name, new ListTag());
        ListTag l = data.getList(name, Tag.TAG_LONG);
        l.add(LongTag.valueOf(pos.asLong()));
        setDirty();
    }

    public void removePositionListModule(String name, BlockPos pos) {
        ListTag l = data.getList(name, Tag.TAG_LONG);
        long p = pos.asLong();
        for (int i = 0; i < l.size(); ++i)
            if (l.get(i) instanceof LongTag t && t.getAsLong() == p)
                l.remove(i--);
        setDirty();
    }

    public BlockPos getPositionListModule(String name, int index, BlockPos def) {
        if (data.contains(name, Tag.TAG_LONG)) {
            ListTag l = data.getList(name, Tag.TAG_LONG);
            if (index >= 0 && index < l.size() && l.get(index) instanceof LongTag t)
                return BlockPos.of(t.getAsLong());
        }
        return def;
    }

    @NotNull
    @Override
    public CompoundTag save(CompoundTag compoundTag) { return data; }

    @Override
    public void load(CompoundTag compoundTag) { data = compoundTag; }
}
*/