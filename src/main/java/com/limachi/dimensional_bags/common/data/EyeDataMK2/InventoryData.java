package com.limachi.dimensional_bags.common.data.EyeDataMK2;

import com.limachi.dimensional_bags.common.NBTUtils;
import com.limachi.dimensional_bags.common.data.IMarkDirty;
import com.limachi.dimensional_bags.common.inventory.InventoryUtils;
import com.limachi.dimensional_bags.common.inventory.NBTStoredItemHandler;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import net.minecraft.nbt.CompoundNBT;

import java.util.function.Consumer;
import java.util.function.Function;

public class InventoryData extends WorldSavedDataManager.EyeWorldSavedData implements IMarkDirty {

    private CompoundNBT localNBT;
    private NBTStoredItemHandler inv;

    public InventoryData(String suffix, int id, boolean client) {
        super(suffix, id, client);
        UpgradeManager upgradeManager = UpgradeManager.getInstance(id);
        localNBT = new CompoundNBT();
        localNBT.putInt("Size", UpgradeManager.getUpgrade("upgrade_slot").getCount(upgradeManager));
        localNBT.putInt("Columns", UpgradeManager.getUpgrade("upgrade_column").getCount(upgradeManager));
        localNBT.putInt("Rows", UpgradeManager.getUpgrade("upgrade_row").getCount(upgradeManager));
        inv = NBTStoredItemHandler.createInPlace(localNBT);
    }

    public InventoryUtils.IFormatAwareItemHandler getInventory() { return inv; }

    public int getRows() { return localNBT.getInt("Rows"); }

    public int getColumns() { return localNBT.getInt("Columns"); }

    @Override
    public void read(CompoundNBT nbt) {
        localNBT = nbt;
        inv = NBTStoredItemHandler.createInPlace(localNBT);
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        return (CompoundNBT)NBTUtils.deepMergeNBTInternal(nbt, localNBT);
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
