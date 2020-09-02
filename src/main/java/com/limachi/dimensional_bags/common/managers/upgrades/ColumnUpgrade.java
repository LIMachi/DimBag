package com.limachi.dimensional_bags.common.managers.upgrades;

import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.managers.Upgrade;
import com.limachi.dimensional_bags.common.managers.UpgradeManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.world.World;

public class ColumnUpgrade extends Upgrade {

    public ColumnUpgrade() { super("column", true, 9, 12, 1, 18); }

    @Override
    public ActionResultType upgradeCrafted(EyeData data, ItemStack stack, World world, Entity crafter) {
        int rows = data.getRows();
        int columns = data.getColumns();
        int size = data.getInventory().getSlots();
        data.getInventory().resizeInventory(size + 3, rows, columns + 1, rows, columns);
        incrementInt(data, "Count", 1);
        UpgradeManager.getUpgrade( "upgrade_slot").incrementInt(data, "Count", 3);
        data.markDirty();
        return ActionResultType.SUCCESS;
    }
}
