package com.limachi.dim_bag.save_datas;

import com.limachi.lim_lib.saveData.AbstractSyncSaveData;
import com.limachi.lim_lib.saveData.RegisterSaveData;
import com.limachi.lim_lib.saveData.SaveDataManager;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

/*
@RegisterSaveData
public class BagIdCounter extends AbstractSyncSaveData {
    int nextId = 1;

    public static int getMaxId() {
        return ((BagIdCounter)SaveDataManager.getInstance("bag_id_counter")).nextId - 1;
    }

    public static int newBagId() {
        BagIdCounter instance = SaveDataManager.getInstance("bag_id_counter");
        int out = instance.nextId;
        instance.nextId++;
        instance.setDirty();
        ModesManager.installInitialModes(out);
        BagRoom room = BagRoom.getRoom(out);
        room.initRoomData();
        room.build();
        return out;
    }

    public BagIdCounter(String name) {
        super(name);
    }

    @NotNull
    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        compoundTag.putInt("nextId", nextId);
        return compoundTag;
    }

    @Override
    public void load(CompoundTag compoundTag) {
        nextId = compoundTag.getInt("nextId");
    }
}
*/