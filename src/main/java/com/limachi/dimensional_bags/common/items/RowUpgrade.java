package com.limachi.dimensional_bags.common.items;

import com.limachi.dimensional_bags.common.config.DimBagConfig;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class RowUpgrade extends BaseUpgrade {

    public RowUpgrade() {
        super("Row", 1);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        if (world == null) return;
        tooltip.add(new TranslationTextComponent("tooltip.upgrade.row", DimBagConfig.maxRows));
    }
}
