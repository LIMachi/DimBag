package com.limachi.dimensional_bags.client.render.screen;

import com.limachi.dimensional_bags.client.render.Box2d;
import com.limachi.dimensional_bags.client.render.RenderUtils;
import com.limachi.dimensional_bags.common.container.SimpleContainer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.text.ITextComponent;

import static com.limachi.dimensional_bags.common.references.GUIs.ScreenParts.*;

public class SimpleContainerScreen extends ContainerScreen<SimpleContainer> {

    Box2d playerBackGround;
    Box2d containerBackGround;

    public SimpleContainerScreen(SimpleContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
    }

    @Override
    protected void init() {
        Minecraft.getInstance().keyboardListener.enableRepeatEvents(true);
        super.init();
        calculateBackGround();
    }

    @Override
    public void onClose() {
        super.onClose();
        Minecraft.getInstance().keyboardListener.enableRepeatEvents(false);
    }

    //based on the container's slot, calculate the size and shape of the background (player part, container part, scroll part, tabs part, widgets part)
    protected void calculateBackGround() {
        playerBackGround = new Box2d(-1, 0, 0, 0);
        containerBackGround = new Box2d(-1, 0, 0, 0);

        for (Slot slot : getContainer().inventorySlots) {
            if (slot.inventory instanceof PlayerInventory) {
                if (playerBackGround.getX1() == -1)
                    playerBackGround.setX1(slot.xPos).setY1(slot.yPos);
                playerBackGround.expandToContain(slot.xPos - 1, slot.yPos - 1, 3, 3);
                playerBackGround.expandToContain(slot.xPos + SLOT_SIZE_X - 1, slot.yPos + SLOT_SIZE_Y - 1, 3, 3);
            } else {
                if (containerBackGround.getX1() == -1)
                    containerBackGround.setX1(slot.xPos).setY1(slot.yPos);
                containerBackGround.expandToContain(slot.xPos - 1, slot.yPos - 1, 3, 3);
                containerBackGround.expandToContain(slot.xPos + SLOT_SIZE_X - 1, slot.yPos + SLOT_SIZE_Y - 1, 3, 3);
            }
        }
        playerBackGround.move(guiLeft, guiTop);
    }

    private void blitGuiFull(MatrixStack matrixStack, int x, int y, int w, int h) {
        this.blit(matrixStack, x + this.guiLeft, y + this.guiTop, 0, 0, w, h, w, h);
    }

    protected void renderPlayerBackground(MatrixStack matrixStack, TextureManager tm) {
        RenderUtils.drawBox(matrixStack, playerBackGround, 0xFFFF0000);
        tm.bindTexture(SLOT);
        for (Slot slot : getContainer().inventorySlots)
            if (slot.inventory instanceof PlayerInventory)
                this.blitGuiFull(matrixStack, slot.xPos - 1, slot.yPos - 1, SLOT_SIZE_X, SLOT_SIZE_Y);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        if (this.minecraft == null) return;
        TextureManager tm = this.minecraft.getTextureManager();
        if (playerBackGround == null || containerBackGround == null)
            calculateBackGround();
        if (playerBackGround.getX1() != -1)
            renderPlayerBackground(matrixStack, tm);
//        RenderUtils.drawBox(matrixStack, playerBackGround, 0xFFFF0000);
//        RenderUtils.drawBox(matrixStack, containerBackGround, 0xFFFFFF00);
//        this.render_player_inventory(matrixStack, tm);
//        this.render_separator(matrixStack, tm);
//        this.render_rows(matrixStack, tm);
//        this.render_top(matrixStack, tm);
    }
}
