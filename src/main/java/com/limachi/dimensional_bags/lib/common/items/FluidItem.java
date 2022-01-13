package com.limachi.dimensional_bags.lib.common.items;

import com.limachi.dimensional_bags.StaticInit;
import com.limachi.dimensional_bags.common.references.Registries;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.ItemFluidContainer;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * fake item which expect to have a fluid serialized in his tags
 */
@StaticInit
public class FluidItem extends ItemFluidContainer {

    public static final Supplier<FluidItem> INSTANCE_GETTER = Registries.registerItem("fluid_item", FluidItem::new);

    public static ItemStack createStack(FluidStack fluidStack) {
        ItemStack out = new ItemStack(INSTANCE_GETTER.get(), 1);
        getHandler(out).fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
        return out;
    }

    public FluidItem() { super(new Properties().stacksTo(0).setISTER(()->()->FluidItemRenderer.INSTANCE), Integer.MAX_VALUE); }

    public static IFluidHandlerItem getHandler(ItemStack stack) {
        return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).orElseThrow(()->new IllegalStateException("Missing FLUID_HANDLER_ITEM_CAPABILITY in FluidItem"));
    }

    @Nonnull
    @Override
    public ITextComponent getName(@Nonnull ItemStack stack) {
        FluidStack fluid = getHandler(stack).getFluidInTank(0);
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
