package com.limachi.dimensional_bags.client.render.screen;

import com.limachi.dimensional_bags.common.container.BaseNBTSincContainer;
import com.limachi.dimensional_bags.common.inventory.InventoryUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

import com.limachi.dimensional_bags.common.references.GUIs;

import static com.limachi.dimensional_bags.common.references.GUIs.ScreenParts.*;

/**
 * universal inventory screen that can be used by any container using the InventoryUtils.IFormatAwareItemHandler interface for it's target inventory
 * the render used will be determined by the format/rows/columns/names given by the InventoryUtils.IFormatAwareItemHandler dynamically
 */
public class BaseNBTSincContainerScreen<T extends BaseNBTSincContainer> extends BaseScreen<T> {

    public BaseNBTSincContainerScreen(T screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
    }

    protected void drawAccessRectangle(MatrixStack matrixStack, TextureManager tm, int x, int y, InventoryUtils.ItemStackIORights rights) {
        if (rights != null) {
            if (rights.canInput && rights.canOutput) tm.bindTexture(SLOT);
            if (rights.canInput && !rights.canOutput) tm.bindTexture(INPUT_SLOT);
            if (!rights.canInput && rights.canOutput) tm.bindTexture(OUTPUT_SLOT);
            if (!rights.canInput && !rights.canOutput) tm.bindTexture(LOCKED_SLOT);
        }
        else
            tm.bindTexture(UNUSED_SLOT);
        this.blitGuiFull(matrixStack, x, y, SLOT_SIZE_X, SLOT_SIZE_Y);
    }

    protected void blitGuiFull(MatrixStack matrixStack, int x, int y, int w, int h) {
        blit(matrixStack, x + this.guiLeft, y + this.guiTop, 0, 0, w, h, w, h);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        if (this.minecraft == null) return;
        TextureManager tm = this.minecraft.getTextureManager();
//        if (container.)
    }
}
