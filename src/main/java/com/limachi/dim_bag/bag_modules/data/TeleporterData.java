package com.limachi.dim_bag.bag_modules.data;

import com.google.common.collect.ImmutableList;
import com.limachi.dim_bag.DimBag;
import com.limachi.lim_lib.World;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;

//FIXME
public class TeleporterData implements INBTSerializable<CompoundTag> {

    protected boolean active = true;
    protected boolean affectPlayers = false;
    protected Component label = Component.translatable("teleporter.data.label.default");
    protected ArrayList<String> filters = new ArrayList<>();
    protected boolean whitelist = false;
    public final BlockPos pos;
//    protected Teleporters listener;

    public TeleporterData(BlockPos pos) { this.pos = pos; }

//    public TeleporterData with_listener(Teleporters listener) {
//        this.listener = listener;
//        return this;
//    }

    public TeleporterData(CompoundTag data) {
        this(BlockPos.of(data.getLong("pos")));
        deserializeNBT(data);
    }

    public void replace(CompoundTag data) {
        deserializeNBT(data);
//        if (listener != null)
//            listener.setDirty();
    }

    public boolean accept(Entity entity) {
        if (!affectPlayers && entity instanceof Player) return false;
        String name = entity.getDisplayName().getString();
        if (!active) return false;
        boolean c = filters.stream().anyMatch(p->name.toLowerCase().contains(p.toLowerCase()) || name.matches("(?i)" + p)); // (?i) is a flag to set the matching to case-insensitive
        return whitelist == c;
    }

    public void setActiveState(boolean state) {
        if (state != active) {
            active = state;
//            if (listener != null)
//                listener.setDirty();
        }
    }

    public void setWhitelistState(boolean state) {
        if (state != whitelist) {
            whitelist = state;
//            if (listener != null)
//                listener.setDirty();
        }
    }

    public void setAffectPlayersState(boolean state) {
        if (state != affectPlayers) {
            affectPlayers = state;
//            if (listener != null)
//                listener.setDirty();
        }
    }

    public void setLabel(Component name) {
        if (!label.equals(name)) {
            label = name;
//            if (listener != null)
//                listener.setDirty();
        }
    }

    public void replaceFilter(String original, String replacement) {
        if (replacement != null) {
            if (!filters.contains(replacement)) {
                if (original != null && filters.contains(original))
                    filters.replaceAll(s -> s.equals(original) ? replacement : s);
                else
                    filters.add(replacement);
//                if (listener != null)
//                    listener.setDirty();
            }
        } else if (original != null && filters.contains(original)) {
            filters.removeIf(s->s.equals(original));
//            if (listener != null)
//                listener.setDirty();
        }
    }

    public void setFilters(List<String> filters) {
        this.filters.clear();
        this.filters.addAll(filters);
//        if (listener != null)
//            listener.setDirty();
    }

    public ImmutableList<String> getFilters() { return ImmutableList.copyOf(filters); }
    public Component getLabel() { return label; }
    public boolean isActive() { return active; }
    public boolean isWhitelist() { return whitelist; }
    public boolean doesAffectPlayers() { return affectPlayers; }

    public Entity target() {
        if (active) {
            Level level = World.getLevel(DimBag.BAG_DIM);
            if (level != null) {
                List<Entity> t = level.getEntities((Entity) null, new AABB(pos.above()), e -> true);
                return t.size() > 0 ? t.get(0) : null;
            }
        }
        return null; //FIXME
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag data = new CompoundTag();
        data.putLong("pos", pos.asLong());
        data.putBoolean("active", active);
        data.putBoolean("affectPlayers", affectPlayers);
        data.putBoolean("whitelist", whitelist);
        data.putString("label", Component.Serializer.toJson(label));
        ListTag fl = new ListTag();
        for (String f : filters)
            fl.add(StringTag.valueOf(f));
        data.put("filters", fl);
        return data;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("active"))
            active = nbt.getBoolean("active");
        whitelist = nbt.getBoolean("whitelist");
        affectPlayers = nbt.getBoolean("affectPlayers");
        label = Component.Serializer.fromJson(nbt.getString("label"));
        if (label == null)
            label = Component.translatable("teleporter.data.label.default");
        filters.clear();
        ListTag fl = nbt.getList("filters", Tag.TAG_STRING);
        for (Tag t : fl)
            if (t instanceof StringTag f)
                filters.add(f.getAsString());
    }
}
