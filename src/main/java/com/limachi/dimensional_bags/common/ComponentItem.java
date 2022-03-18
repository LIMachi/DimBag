package com.limachi.dimensional_bags.common;

import com.limachi.dimensional_bags.DimBag;
import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.references.Registries;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

/**
 * components are blank items that are only used as a step in a craft, they do have models, names, etc... (note: we could use a file to generate those items)
 */

@StaticInit
public class ComponentItem extends Item {

    static {
        registerComponents(/*"battery_component",*/ "blank_upgrade", /*"compression_field",*/ "end_fragment");
    }

    public ComponentItem() { super(DimBag.DEFAULT_PROPERTIES.stacksTo(64)); }

    public static void registerComponents(String ...names) {
        for (String name : names)
            Registries.registerItem(name, ()->new ComponentItem(){
                @Override
                public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
                    if (Screen.hasShiftDown()) {
                        tooltip.add(new TranslationTextComponent("tooltip.items." + name).withStyle(TextFormatting.YELLOW));
                    }  else
                        tooltip.add(new TranslationTextComponent("tooltip.shift_for_info"));
                    super.appendHoverText(stack, worldIn, tooltip, flagIn);
                }
            });
    }
}
