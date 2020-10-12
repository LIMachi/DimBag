package com.limachi.dimensional_bags.common.data.EyeDataMK2;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.common.WorldUtils;
import com.limachi.dimensional_bags.common.data.IMarkDirty;
import com.limachi.dimensional_bags.common.inventory.Wrapper;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nullable;

public class InventoryData extends WorldSavedDataManager.EyeWorldSavedData implements IMarkDirty {

//    private final int id;
    private Wrapper inv;
    private int rows;
    private int columns;

    public InventoryData(String suffix, int id, boolean client) {
        super(suffix, id, client);
//        super(DimBag.MOD_ID + "_eye_" + id + "_inventory_data");
//        this.id = id;
        UpgradeManager upgradeManager = UpgradeManager.getInstance(null, id);
        inv = new Wrapper(UpgradeManager.getUpgrade("upgrade_slot").getCount(upgradeManager), this);
        rows = UpgradeManager.getUpgrade("upgrade_row").getCount(upgradeManager);
        columns = UpgradeManager.getUpgrade("upgrade_column").getCount(upgradeManager);
    }

//    public int getId() { return id; }

    public Wrapper getInventory() { return inv; }

    public int getRows() { return rows; }

    public int getColumns() { return columns; }

    @Override
    public void read(CompoundNBT nbt) {
        inv.read(nbt);
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        return inv.write(nbt);
    }

//    static public InventoryData getInstance(@Nullable ServerWorld world, int id) {
//        if (id <= 0) return null;
//        if (world == null)
//            world = WorldUtils.getOverWorld();
//        if (world != null)
//            return world.getSavedData().getOrCreate(()->new InventoryData(id), DimBag.MOD_ID + "_eye_" + id + "_inventory_data");
//        return null;
//    }

    static public InventoryData getInstance(@Nullable ServerWorld world, int id) {
        return WorldSavedDataManager.getInstance(InventoryData.class, world, id);
    }
}
