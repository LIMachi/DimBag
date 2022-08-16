package com.limachi.utils;

import net.minecraft.nbt.*;

import javax.annotation.Nonnull;
import java.util.UUID;

@SuppressWarnings("unused")
public class NBT {
    public static CompoundTag clear(CompoundTag comp) {
        Object[] keys = comp.getAllKeys().toArray();
        for (Object key : keys)
            comp.remove((String)key);
        return comp;
    }

    protected static CompoundTag removeKeys(CompoundTag nbt, ListTag keys) {
        for (Tag k : keys) {
            if (k.getId() == Tag.TAG_COMPOUND) {
                String s = ((CompoundTag) k).getString("key");
                ListTag sk = (ListTag) ((CompoundTag) k).get("list");
                if (sk != null)
                    nbt.put(s, removeKeys(nbt.getCompound(s), sk));
            } else
                nbt.remove(k.getAsString());
        }
        return nbt;
    }

    protected static ListTag removedKeys(@Nonnull CompoundTag valid, @Nonnull CompoundTag diff) {
        ListTag list = new ListTag();
        for (String key : diff.getAllKeys())
            if (!valid.contains(key))
                list.add(StringTag.valueOf(key));
            else {
                Tag td = diff.get(key);
                Tag tv = valid.get(key);
                if (td instanceof CompoundTag && tv instanceof CompoundTag) {
                    ListTag sl = removedKeys((CompoundTag) tv, (CompoundTag) td);
                    if (!sl.isEmpty()) {
                        CompoundTag entry = new CompoundTag();
                        entry.put("list", sl);
                        entry.putString("key", key);
                        list.add(entry);
                    }
                } else
                    list.add(StringTag.valueOf(key));
            }
        return list;
    }

    public static CompoundTag extractDiff(@Nonnull CompoundTag valid, @Nonnull CompoundTag diff) {
        CompoundTag added = new CompoundTag();
        ListTag removed = new ListTag();
        CompoundTag changed = new CompoundTag();
        for (String key : valid.getAllKeys())
            if (diff.contains(key) && !diff.get(key).equals(valid.get(key))) {
                Tag tv = valid.get(key);
                Tag td = diff.get(key);
                if (tv instanceof CompoundTag && td instanceof CompoundTag)
                    changed.put(key, extractDiff((CompoundTag) tv, (CompoundTag) td));
                else if (tv != null)
                    changed.put(key, tv);
            }
            else if (!diff.contains(key)) {
                Tag t = valid.get(key);
                if (t != null)
                    added.put(key, t);
            }
        for (String key : diff.getAllKeys())
            if (!valid.contains(key))
                removed.add(StringTag.valueOf(key));
        CompoundTag out = new CompoundTag();
        if (!changed.isEmpty())
            out.put("Diff_Changed", changed);
        if (!added.isEmpty())
            out.put("Diff_Added", added);
        if (!removed.isEmpty())
            out.put("Diff_Removed", removed);
        if (!out.isEmpty())
            out.putBoolean("IsDiff", true);
        return out;
    }

    public static CompoundTag applyDiff(@Nonnull CompoundTag toChange, @Nonnull CompoundTag diff) {
        ListTag removed = diff.getList("Diff_Removed", Tag.TAG_LIST);
        for (int i = 0; i < removed.size(); ++i)
            toChange.remove(removed.getString(i));
        CompoundTag added = diff.getCompound("Diff_Added");
        for (String key : added.getAllKeys()) {
            Tag t = added.get(key);
            if (t != null)
                toChange.put(key, t);
        }
        CompoundTag changed = diff.getCompound("Diff_Changed");
        for (String key : changed.getAllKeys()) {
            Tag c = changed.get(key);
            if (toChange.contains(key) && c instanceof CompoundTag && toChange.get(key) instanceof CompoundTag) {
                Tag t = toChange.get(key);
                if (t != null)
                    toChange.put(key, applyDiff((CompoundTag) t, (CompoundTag) c));
            }
            else if (c != null)
                toChange.put(key, c);
        }
        return toChange;
    }

    public static CompoundTag ensurePathExistence(CompoundTag nbt, String ... nodes) {
        CompoundTag t = nbt;
        for (String node : nodes) {
            if (!t.contains(node))
                t.put(node, new CompoundTag());
            t = t.getCompound(node);
        }
        return t;
    }

    public static UUID readUUID(StringTag nbt) { return UUID.fromString(nbt.getAsString()); }
    public static StringTag writeUUID(UUID id) { return StringTag.valueOf(id.toString()); }
}
