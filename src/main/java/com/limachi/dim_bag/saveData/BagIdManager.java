package com.limachi.dim_bag.saveData;

import com.limachi.lim_lib.saveData.AbstractSyncSaveData;
import com.limachi.lim_lib.saveData.RegisterSaveData;
import com.limachi.lim_lib.saveData.SaveDataManager;
import com.limachi.lim_lib.saveData.SaveSync;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

@RegisterSaveData
public class BagIdManager extends AbstractSyncSaveData {
    protected int lastID = 0;

    public BagIdManager(String name) { super(name, SaveSync.SERVER_TO_CLIENT); }

    @Override
    public void load(CompoundTag nbt) { lastID = nbt.getInt("lastId"); }

    @Override
    public @NotNull CompoundTag save(CompoundTag nbt) {
        nbt.putInt("lastId", lastID);
        return nbt;
    }

    public static int getLastId() {
        BagIdManager manager = SaveDataManager.getInstance("bag_id_manager");
        return manager != null ? manager.lastID : 0;
    }
}
