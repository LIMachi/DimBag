package com.limachi.dim_bag.saveData;

import com.limachi.lim_lib.SaveData;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

@SaveData.RegisterSaveData(sync = SaveData.Sync.SERVER_TO_CLIENT)
public class BagIdManager extends SaveData.SyncSaveData {
    protected int lastID = 0;

    public BagIdManager(String name, SaveData.Sync sync) { super(name, sync); }

    @Override
    public void load(CompoundTag nbt) { lastID = nbt.getInt("lastId"); }

    @Override
    public @NotNull CompoundTag save(CompoundTag nbt) {
        nbt.putInt("lastId", lastID);
        return nbt;
    }

    public static int getLastId() {
        BagIdManager manager = SaveData.getInstance("bag_id_manager");
        return manager != null ? manager.lastID : 0;
    }
}
