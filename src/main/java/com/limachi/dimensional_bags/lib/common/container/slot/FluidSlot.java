package com.limachi.dimensional_bags.lib.common.container.slot;

import com.limachi.dimensional_bags.client.render.FluidStackRenderer;
import com.limachi.dimensional_bags.lib.common.fluids.ISimpleFluidHandlerSerializable;
import com.limachi.dimensional_bags.lib.common.items.FluidItem;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;

import static com.limachi.dimensional_bags.common.references.GUIs.ScreenParts.*;

public class FluidSlot extends Slot implements IRenderable {

    private final ISimpleFluidHandlerSerializable fluidHandler;

    public FluidSlot(ISimpleFluidHandlerSerializable fluidHandler, int index, int xPosition, int yPosition) {
        super(DisabledSlot.EMPTY_INVENTORY, index, xPosition, yPosition);
        this.fluidHandler = fluidHandler;
    }

    @Override
    public void set(ItemStack stack) {
        if (stack.getItem() instanceof FluidItem) {
            FluidStack fluid = /*FluidStack.loadFluidStackFromNBT(stack.getTag())*/FluidItem.getHandler(stack).getFluidInTank(0);
            int ps = fluidHandler.getSelectedTank();
            fluidHandler.selectTank(getSlotIndex());
            FluidStack local = fluidHandler.getFluidInTank(getSlotIndex());
            if (fluid.isFluidEqual(local)) {
                if (fluid.getAmount() > local.getAmount()) {
                    fluid.setAmount(fluid.getAmount() - local.getAmount());
                    fluidHandler.fill(fluid, IFluidHandler.FluidAction.EXECUTE);
                } else if (fluid.getAmount() < local.getAmount())
                    fluidHandler.drain(local.getAmount() - fluid.getAmount(), IFluidHandler.FluidAction.EXECUTE);
            } else {
                if (!local.isEmpty())
                    fluidHandler.drain(local.getAmount(), IFluidHandler.FluidAction.EXECUTE);
                if (!fluid.isEmpty())
                    fluidHandler.fill(fluid, IFluidHandler.FluidAction.EXECUTE);
            }
            fluidHandler.selectTank(ps);
        }
        this.setChanged();
    }

    @Nonnull
    @Override
    public ItemStack getItem() {
        FluidStack fluid = getFluid();
        if (fluid.isEmpty())
            return ItemStack.EMPTY;
        return FluidItem.createStack(fluid);
    }

    public void selectTank() { fluidHandler.selectTank(getSlotIndex()); }

    public boolean isSelected() { return fluidHandler.getSelectedTank() == getSlotIndex(); }

    @Override
    public boolean hasItem() { return !fluidHandler.getFluidInTank(getSlotIndex()).isEmpty(); }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) { return false; }

    @Override
    public boolean mayPickup(@Nonnull PlayerEntity playerIn) { return false; }

    @Nonnull
    @Override
    public ItemStack remove(int amount) { return ItemStack.EMPTY; }

    @Nonnull
    public FluidStack getFluid() { return fluidHandler.getFluidInTank(getSlotIndex()); }

    public int getCapacity() { return fluidHandler.getTankCapacity(getSlotIndex()); }

    public ISimpleFluidHandlerSerializable getHandler() { return fluidHandler; }

    @Override
    public void render(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        TextureManager tm = mc.getTextureManager();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableBlend();
        tm.bind(FLUID_SLOT);
        AbstractGui.blit(matrixStack, x - 1, y - 1, 100, 0, 0, 18, 18, 18, 18);
        FluidStack fluid = getFluid();
        FluidStackRenderer.INSTANCE.render(matrixStack, x, y, fluid);
        tm.bind(FLUID_SLOT_OVERLAY);
        AbstractGui.blit(matrixStack, x - 1, y - 1, 250, 0, 0, 18, 18, 18, 18);
        if (isSelected()) {
            tm.bind(SELECTED_FLUID_SLOT_OVERLAY);
            AbstractGui.blit(matrixStack, x - 1, y - 1, 300, 0, 0, 18, 18, 18, 18);
        }
        int amountInMB = fluid.getAmount();
        if (amountInMB > 0) {
            String amount = amountInMB >= 1000 ? (amountInMB / 1000) + I18n.get("screen.fluid.bucket_acronym") : amountInMB + I18n.get("screen.fluid.milli_bucket_acronym");
            RenderSystem.pushMatrix();
            RenderSystem.translatef(x, y, 250);
            if (amount.length() > 3) {
                RenderSystem.scalef(0.5f, 0.5f, 1);
                mc.font.drawShadow(matrixStack, amount, 31 - mc.font.width(amount), 23, 16777215);
            } else
                mc.font.drawShadow(matrixStack, amount, 17 - mc.font.width(amount), 9, 16777215);
            RenderSystem.popMatrix();
        }
        RenderSystem.disableBlend();
        RenderSystem.disableAlphaTest();
    }
}
