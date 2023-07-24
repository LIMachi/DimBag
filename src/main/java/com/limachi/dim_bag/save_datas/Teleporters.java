package com.limachi.dim_bag.save_datas;

import com.limachi.dim_bag.DimBag;
//import com.limachi.dim_bag.bag_data.RoomData;
//import com.limachi.dim_bag.bag_modules.data.TeleporterData;
import com.limachi.lim_lib.saveData.AbstractSyncSaveData;
import com.limachi.lim_lib.saveData.RegisterSaveData;
import com.limachi.lim_lib.saveData.SaveDataManager;
import com.limachi.lim_lib.saveData.SaveSync;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Optional;

/*
@RegisterSaveData
public class Teleporters extends AbstractSyncSaveData {

    protected final ArrayList<TeleporterData> tps = new ArrayList<>();
    protected int selected = 0;

    public Teleporters(String name) { super(name, SaveSync.SERVER_ONLY); }

    public static Teleporters getInstance(int bagId) {
        if (bagId <= 0) return null;
        return SaveDataManager.getInstance("teleporters:" + bagId, Level.OVERWORLD);
    }

    public static Optional<TeleporterData> getTeleporter(Level level, BlockPos pos) {
        if (level.isClientSide || !level.dimension().equals(DimBag.BAG_DIM)) return Optional.empty();
        Teleporters t = getInstance(RoomData.getRoomId(pos));
        if (t != null)
            return t.tps.stream().filter(e->{
                if (e.pos.equals(pos)) {
                    e.with_listener(t);
                    return true;
                }
                return false;
            }).findFirst();
        return Optional.empty();
    }

    public void select(int delta) {
        int l = tps.size();
        if (l <= 1) return;
        int n = selected + delta;
        while (n < 0)
            n += l;
        while (n >= l)
            n -= l;
        if (n != selected) {
            selected = n;
            setDirty();
        }
    }

    public Entity getSelectedTeleporterTarget() {
        if (tps.isEmpty()) return null;
        if (selected >= tps.size()) {
            selected = tps.size() - 1;
            setDirty();
        }
        return tps.get(selected).target();
    }

    public Component getSelectedTeleporterLabel() {
        if (tps.isEmpty()) return Component.translatable("teleporter.data.label.none");
        if (selected >= tps.size()) {
            selected = tps.size() - 1;
            setDirty();
        }
        return tps.get(selected).getLabel();
    }

    public static BlockPos getDestination(int bagId, Entity entity) {
        Teleporters t = getInstance(bagId);
        if (t != null)
            for (TeleporterData tp : t.tps)
                if (tp.accept(entity))
                    return tp.pos.above();
        return null;
    }

    public static boolean install(int bagId, TeleporterData tp) {
        Teleporters t = getInstance(bagId);
        if (t != null && t.tps.stream().noneMatch(e->e.pos.equals(tp.pos))) {
            t.tps.add(tp);
            t.setDirty();
            return true;
        }
        return false;
    }

    public static CompoundTag uninstall(int bagId, BlockPos pos) {
        Teleporters t = getInstance(bagId);
        if (t != null)
            for (TeleporterData e : t.tps)
                if (e.pos.equals(pos)) {
                    CompoundTag out = e.serializeNBT();
                    t.tps.remove(e);
                    t.setDirty();
                    return out;
                }
        return new CompoundTag();
    }

    @NotNull
    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        ListTag list = new ListTag();
        for (TeleporterData tp : tps)
            list.add(tp.serializeNBT());
        compoundTag.put("tps", list);
        compoundTag.putInt("selected", selected);
        return compoundTag;
    }

    @Override
    public void load(CompoundTag compoundTag) {
        tps.clear();
        selected = compoundTag.getInt("selected");
        ListTag list = compoundTag.getList("tps", Tag.TAG_COMPOUND);
        for (Tag t : list)
            if (t instanceof CompoundTag e)
                tps.add(new TeleporterData(e).with_listener(this));
    }
}
*/