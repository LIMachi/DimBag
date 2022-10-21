package com.limachi.dim_bag.rooms;

import com.limachi.lim_lib.saveData.AbstractSyncSaveData;
import com.limachi.lim_lib.saveData.RegisterSaveData;
import com.limachi.lim_lib.saveData.SaveDataManager;
import com.limachi.lim_lib.saveData.SaveSync;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

@RegisterSaveData
public class RoomsData extends AbstractSyncSaveData {

    private HashMap<Vec3i, Integer> posToSubRoomId = new HashMap<>();
    private int selectedPad = -1;
    private boolean isLoaded = false;
    private ArrayList<Vec3i> activePads = new ArrayList<>();
    private ArrayList<SubRoom> subRooms = new ArrayList<>();

    public RoomsData(String name) { super(name, SaveSync.SERVER_TO_CLIENT); }

    public ArrayList<SubRoom> getSubRooms() { return subRooms; }

    @NotNull
    @Override
    public CompoundTag save(CompoundTag nbt) {
        ListTag listSubRooms = new ListTag();
        for (SubRoom s : subRooms)
            listSubRooms.add(s.toNBT());
        nbt.put("SubRooms", listSubRooms);
        ListTag pads = new ListTag();
        for (Vec3i pad : activePads) {
            CompoundTag e = new CompoundTag();
            e.putInt("X", pad.getX());
            e.putInt("Y", pad.getY());
            e.putInt("Z", pad.getZ());
        }
        nbt.put("ActivePads", pads);
        nbt.putInt("SelectedPad", selectedPad);
        nbt.putBoolean("IsLoaded", isLoaded);
        return nbt;
    }

    @Override
    public void load(CompoundTag nbt) {
        ListTag listSubRooms = nbt.getList("SubRooms", Tag.TAG_LIST);
        subRooms = new ArrayList<>();
        posToSubRoomId = new HashMap<>();
        activePads = new ArrayList<>();
        selectedPad = nbt.getInt("SelectedPad");
        isLoaded = nbt.getBoolean("IsLoaded");
        for (int i = 0; i < listSubRooms.size(); ++i) {
            CompoundTag entry = listSubRooms.getCompound(i);
            subRooms.add(i, SubRoom.fromNBT(entry));
            posToSubRoomId.put(subRooms.get(i).pos, i);
        }
        for (Tag t : nbt.getList("ActivePads", Tag.TAG_LIST))
            activePads.add(new Vec3i(((CompoundTag)t).getInt("X"), ((CompoundTag)t).getInt("Y"), ((CompoundTag)t).getInt("Z")));
    }

    public static RoomsData getInstance(int id) { return SaveDataManager.getInstance("rooms:" + id); }
}
