package com.limachi.dimensional_bags.common.inventory;

import com.limachi.dimensional_bags.common.data.EyeData;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class BagInventory extends BaseInventory {

    public BagInventory(EyeData data) {
        super(data.getRows() * data.getColumns(), data.getRows(), data.getColumns(), data::markDirty);
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("inventory.bag.name");
    }
}
