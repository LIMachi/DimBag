package com.limachi.dim_bag.saveData;

import com.limachi.utils.SaveData;
import com.limachi.utils.StaticInitializer;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

@StaticInitializer.Static
public class Test extends SaveData.SyncSaveData {

    public static final String NAME = "test";
    static {
        SaveData.register(NAME, SaveData.Sync.BOTH_WAY, Test.class);
    }

    protected int counter;

    public Test(String name, SaveData.Sync sync) { super(name, sync); }

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
