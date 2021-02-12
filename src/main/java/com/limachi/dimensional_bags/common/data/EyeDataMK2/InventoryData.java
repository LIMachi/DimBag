package com.limachi.dimensional_bags.common.data.EyeDataMK2;

import com.limachi.dimensional_bags.common.data.IMarkDirty;
import com.limachi.dimensional_bags.common.inventory.Wrapper;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import net.minecraft.nbt.CompoundNBT;

import java.util.function.Consumer;
import java.util.function.Function;

public class InventoryData extends WorldSavedDataManager.EyeWorldSavedData implements IMarkDirty {

    private Wrapper inv;
    private int rows;
    private int columns;

    public InventoryData(String suffix, int id, boolean client) {
        super(suffix, id, client);
        UpgradeManager upgradeManager = UpgradeManager.getInstance(id);
        inv = new Wrapper(UpgradeManager.getUpgrade("upgrade_slot").getCount(upgradeManager), this);
        rows = UpgradeManager.getUpgrade("upgrade_row").getCount(upgradeManager);
        columns = UpgradeManager.getUpgrade("upgrade_column").getCount(upgradeManager);
    }

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

    static public InventoryData getInstance(int id) {
        return WorldSavedDataManager.getInstance(InventoryData.class, null, id);
    }

    static public <T> T execute(int id, Function<InventoryData, T> executable, T onErrorReturn) {
        return WorldSavedDataManager.execute(InventoryData.class, null, id, executable, onErrorReturn);
    }

    static public boolean execute(int id, Consumer<InventoryData> executable) {
        return WorldSavedDataManager.execute(InventoryData.class, null, id, data->{executable.accept(data); return true;}, false);
    }
}
