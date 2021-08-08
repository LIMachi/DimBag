package com.limachi.dimensional_bags.common.items;

import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.Registries;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * fake item which expect to have a fluid serialized in his tags
 */
@StaticInit
public class FluidItem extends Item {

    public static final Supplier<FluidItem> INSTANCE_GETTER = Registries.registerItem("fluid_item", FluidItem::new);

    public static ItemStack createStack(FluidStack fluidStack) {
        ItemStack out = new ItemStack(INSTANCE_GETTER.get(), 1);
        out.setTag(fluidStack.writeToNBT(new CompoundNBT()));
        return out;
    }

    public FluidItem() { super(new Properties()); }

    @Override //should prevent merge/stacking
    public int getItemStackLimit(ItemStack stack) { return 0; }

    @Nonnull
    @Override
    public ITextComponent getDisplayName(ItemStack stack) {
        FluidStack fluid = FluidStack.loadFluidStackFromNBT(stack.getTag());
        if (fluid.isEmpty())
            return new TranslationTextComponent("item.fluid_item.empty_name");
        return ((IFormattableTextComponent)fluid.getDisplayName()).appendSibling(new TranslationTextComponent("item.fluid_item.append_qty_to_name", fluid.getAmount()));
    }
}
