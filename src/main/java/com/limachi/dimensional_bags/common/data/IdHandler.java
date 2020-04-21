package com.limachi.dimensional_bags.common.data;

import com.limachi.dimensional_bags.common.entities.BagEntity;
import com.limachi.dimensional_bags.common.items.Bag;
import com.limachi.dimensional_bags.common.tileentity.BagEyeTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import static com.limachi.dimensional_bags.DimensionalBagsMod.MOD_ID;

public class IdHandler {

    private static final String KEY = MOD_ID + "_eye_id";

    private int id = 0;

    public IdHandler(int id) { this.id = id; }

    public IdHandler(ItemStack dataHolder) {
        if (dataHolder.getItem() instanceof Bag && dataHolder.getTag() != null)
            this.id = dataHolder.getTag().getInt(KEY);
    }

    public IdHandler(BagEntity dataHolder) {
        this.id = dataHolder.getPersistentData().getInt(KEY);
    }

    public IdHandler(BagEyeTileEntity dataHolder) {
        this.id = dataHolder.getTileData().getInt(KEY);
    }

    public void write(ItemStack dataHolder) {
        if (!(dataHolder.getItem() instanceof Bag)) return;
        CompoundNBT nbt = dataHolder.getTag(); //get previous data to not overwrite it
        if (nbt == null)
            nbt = new CompoundNBT();
        nbt.putInt(KEY, this.id);
        dataHolder.setTag(nbt);
    }

    public void write(BagEntity dataHolder) {
        dataHolder.getPersistentData().putInt(KEY, this.id);
    }

    public void write(BagEyeTileEntity dataHolder) {
        dataHolder.getTileData().putInt(KEY, this.id);
    }

    public int getId() { return this.id; }
}
