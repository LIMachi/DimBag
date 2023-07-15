package com.limachi.dim_bag.bag_data;

import com.limachi.dim_bag.utils.Tags;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;

/*
public class ModulesData extends BaseData {
    public ModulesData(int bag) { super(bag, "modules"); }

    public void simpleInstall(String name, BlockPos pos) {
        ListTag modules = Tags.getOrCreateList(data(), name, ListTag::new);
        LongTag p = LongTag.valueOf(pos.asLong());
        if (!modules.contains(p))
            modules.add(p);
    }

    public void simpleUninstall(String name, BlockPos pos) {
        CompoundTag data = data();
        ListTag modules = data.getList(name, Tag.TAG_LONG);
        modules.remove(LongTag.valueOf(pos.asLong()));
        if (modules.isEmpty())
            data.remove(name);
    }

    public boolean isPresent(String name) { return data().contains(name); }
}
*/