package com.limachi.dimensional_bags.client.screen;

import com.limachi.dimensional_bags.common.container.UpgradeContainer;
import com.limachi.dimensional_bags.common.upgradeManager.UpgradeManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

import static com.limachi.dimensional_bags.common.references.GUIs.ScreenParts.SLOT_SIZE_X;
import static com.limachi.dimensional_bags.common.references.GUIs.ScreenParts.SLOT_SIZE_Y;
import static com.limachi.dimensional_bags.common.references.GUIs.ScreenParts.UNUSED_SLOT;
import static com.limachi.dimensional_bags.common.references.GUIs.UpgradeScreen.*;

public class UpgradeGUI extends ContainerScreen<UpgradeContainer> {

    public UpgradeGUI(UpgradeContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
        this.xSize = BACKGROUND_X;
        this.ySize = BACKGROUND_Y;
        this.guiLeft = (this.width - BACKGROUND_X) / 2;
        this.guiTop = (this.height - BACKGROUND_Y) / 2;
    }

    private void blitGuiFull(MatrixStack matrixStack, int x, int y, int w, int h) {
        this.blit(matrixStack, x + this.guiLeft, y + this.guiTop, 0, 0, w, h, w, h);
    }

    @Override
    public void render(MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
//        this.renderHoveredToolTip(mouseX, mouseY);
        this.func_230459_a_(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
        this.font.drawString(matrixStack, this.title/*.getFormattedText()*/.getString(), TITLES_X, GUI_TITLE_Y, 4210752);
        this.font.drawString(matrixStack, this.playerInventory.getDisplayName()/*.getFormattedText()*/.getString(), TITLES_X, INVENTORY_TITLE_Y, 4210752);

        int slot = -1;
        for (int y = 0; y < 2; ++y)
            for (int x = 0; x < 9; ++x) {
                int lx = FIRST_SLOT_X + x * SLOT_SIZE_X + this.guiLeft;
                int ly = FIRST_SLOT_Y + y * SLOT_SIZE_Y + this.guiTop;
                int p = x + y * 9;
                if (p < UpgradeManager.upgradesCount() && mouseX > lx && mouseX < lx + SLOT_SIZE_X && mouseY > ly && mouseY < ly + SLOT_SIZE_Y)
                    slot = p;
            }
//        this.font.drawSplitString(/*"Description: " + */(slot != -1 ? UpgradeManager.getDescription(slot) : ""), HELP_X, HELP_Y, BACKGROUND_X - HELP_X * 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1f, 1f, 1f, 1f);
        if (this.minecraft == null) return;
        TextureManager tm = this.minecraft.getTextureManager();
        tm.bindTexture(BACKGROUND);
        this.blitGuiFull(matrixStack, 0, 0, BACKGROUND_X, BACKGROUND_Y);
        for (int y = 0; y < 2; ++y)
            for (int x = 0; x < 9; ++x)
                if (x + 9 * y < UpgradeManager.upgradesCount())
                    ;
                else {
                    tm.bindTexture(UNUSED_SLOT);
                    this.blitGuiFull(matrixStack, FIRST_SLOT_X + x * SLOT_SIZE_X, FIRST_SLOT_Y + y * SLOT_SIZE_Y, SLOT_SIZE_X, SLOT_SIZE_Y);
                }
    }
}
