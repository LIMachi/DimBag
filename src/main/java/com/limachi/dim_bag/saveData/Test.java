package com.limachi.dim_bag.saveData;

import com.limachi.lim_lib.saveData.AbstractSyncSaveData;
import com.limachi.lim_lib.saveData.RegisterSaveData;
import com.limachi.lim_lib.saveData.SaveSync;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

@RegisterSaveData
public class Test extends AbstractSyncSaveData {

    protected int counter;

    public Test(String name) { super(name, SaveSync.BOTH_WAY); }

    public int getCounter() { return counter; }

    public void setCounter(int counter) { this.counter = counter; setDirty(); }

    @Override
    public void load(CompoundTag nbt) { counter = nbt.getInt("counter"); }

    @Override
    public @NotNull CompoundTag save(CompoundTag nbt) {
        nbt.putInt("counter", counter);
        return nbt;
    }
}
