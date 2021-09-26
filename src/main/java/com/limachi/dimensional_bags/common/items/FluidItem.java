package com.limachi.dimensional_bags.common.items;

import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.Registries;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
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

    public FluidItem() { super(new Properties().stacksTo(0).setISTER(()->()->FluidItemRenderer.INSTANCE)); }

    @Nonnull
    @Override
    public ITextComponent getName(ItemStack stack) {
        FluidStack fluid = FluidStack.loadFluidStackFromNBT(stack.getTag());
        if (fluid.isEmpty())
            return new TranslationTextComponent("item.fluid_item.empty_name");
        return ((IFormattableTextComponent)fluid.getDisplayName()).append(new TranslationTextComponent("item.fluid_item.append_qty_to_name", fluid.getAmount()));
    }

    protected static class FluidItemRenderer extends ItemStackTileEntityRenderer {
        protected static FluidItemRenderer INSTANCE = new FluidItemRenderer();

        @Override
        public void renderByItem(ItemStack p_239207_1_, ItemCameraTransforms.TransformType p_239207_2_, MatrixStack p_239207_3_, IRenderTypeBuffer p_239207_4_, int p_239207_5_, int p_239207_6_) {
            super.renderByItem(p_239207_1_, p_239207_2_, p_239207_3_, p_239207_4_, p_239207_5_, p_239207_6_);
        }
    }
}
