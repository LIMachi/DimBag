package com.limachi.dimensional_bags.client.render.screen;

//import com.limachi.dimensional_bags.common.container.WrappedPlayerInventoryContainer;
//import com.limachi.dimensional_bags.common.inventory.Wrapper;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

//import static com.limachi.dimensional_bags.common.inventory.Wrapper.IORights.CANINPUT;
//import static com.limachi.dimensional_bags.common.inventory.Wrapper.IORights.CANOUTPUT;
import static com.limachi.dimensional_bags.common.references.GUIs.PlayerInterface.*;
import static com.limachi.dimensional_bags.common.references.GUIs.ScreenParts.*;

/*
public class TankGUI extends ContainerScreen<WrappedPlayerInventoryContainer> {

    public TankGUI(WrappedPlayerInventoryContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
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
        this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
//        super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
        this.font.drawString(matrixStack, new TranslationTextComponent("inventory.player_interface.name", container.getLocalUserName()).getString(), TITLES_X, GUI_TITLE_Y, 4210752);
        this.font.drawString(matrixStack, this.playerInventory.getDisplayName().getString(), TITLES_X, INVENTORY_TITLE_Y, 4210752);
    }

    protected void drawAccessRectangle(MatrixStack matrixStack, TextureManager tm, int x, int y, Wrapper.IORights rights) {
        int flags = rights.flags & (CANINPUT | CANOUTPUT);
        if (flags == (CANINPUT | CANOUTPUT)) return; //both input and output are enable, no need to change the render
        if (flags == CANINPUT) tm.bindTexture(INPUT_SLOT);
        if (flags == CANOUTPUT) tm.bindTexture(OUTPUT_SLOT);
        if (flags == 0) tm.bindTexture(LOCKED_SLOT);
        this.blitGuiFull(matrixStack, x, y, SLOT_SIZE_X, SLOT_SIZE_Y);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1f, 1f, 1f, 1f);
        if (this.minecraft == null) return;
        TextureManager tm = this.minecraft.getTextureManager();
        tm.bindTexture(BACKGROUND);
        this.blitGuiFull(matrixStack, 0, 0, BACKGROUND_X, BACKGROUND_Y);
        for (int x = 0; x < 9; ++x)
            drawAccessRectangle(matrixStack, tm, BELT_X + x * SLOT_SIZE_X, BELT_Y, container.getRights(36 + x));
        for (int y = 0; y < 3; ++y)
            for (int x = 0; x < 9; ++x)
                drawAccessRectangle(matrixStack, tm, MAIN_INVENTORY_X + x * SLOT_SIZE_X, MAIN_INVENTORY_Y + y * SLOT_SIZE_Y, container.getRights(45 + x + 9 * y));
        for (int x = 0; x < 4; ++x)
            drawAccessRectangle(matrixStack, tm, ARMOR_SLOTS_X + x * SLOT_SIZE_X, SPECIAL_SLOTS_Y, container.getRights(72 + x));
        drawAccessRectangle(matrixStack, tm, OFF_HAND_SLOT_X, SPECIAL_SLOTS_Y, container.getRights(76));
    }
}
*/