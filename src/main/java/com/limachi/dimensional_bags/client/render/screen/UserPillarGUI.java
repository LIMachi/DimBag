package com.limachi.dimensional_bags.client.render.screen;

import com.limachi.dimensional_bags.common.container.UserPillarContainer;
import com.limachi.dimensional_bags.common.container.UserPillarContainer.EntityInventoryProxySlot;
//import com.limachi.dimensional_bags.common.inventory.Wrapper;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

//import static com.limachi.dimensional_bags.common.inventory.Wrapper.IORights.CANINPUT;
//import static com.limachi.dimensional_bags.common.inventory.Wrapper.IORights.CANOUTPUT;
import static com.limachi.dimensional_bags.common.references.GUIs.PlayerInterface.*;
import static com.limachi.dimensional_bags.common.references.GUIs.ScreenParts.*;

public class UserPillarGUI extends SimpleContainerScreen<UserPillarContainer> {
    public UserPillarGUI(UserPillarContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
        imageWidth = BACKGROUND_X;
        imageHeight = BACKGROUND_Y;
        leftPos = (width - BACKGROUND_X) / 2;
        topPos = (height - BACKGROUND_Y) / 2;
    }

    private void blitGuiFull(MatrixStack matrixStack, int x, int y, int w, int h) {
        this.blit(matrixStack, x + leftPos, y + topPos, 0, 0, w, h, w, h);
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {
        this.font.drawShadow(matrixStack, new TranslationTextComponent("inventory.player_interface.name", menu.targetName()).getString(), TITLES_X, GUI_TITLE_Y, 4210752);
        this.font.drawShadow(matrixStack, inventory.getDisplayName().getString(), TITLES_X, INVENTORY_TITLE_Y, 4210752);
    }
/*
    protected void drawAccessRectangle(MatrixStack matrixStack, TextureManager tm, int x, int y, Wrapper.IORights rights) {
        int flags = rights.flags & (CANINPUT | CANOUTPUT);
        if (flags == (CANINPUT | CANOUTPUT)) return; //both input and output are enable, no need to change the render
        if (flags == CANINPUT) tm.bind(INPUT_SLOT);
        if (flags == CANOUTPUT) tm.bind(OUTPUT_SLOT);
        if (flags == 0) tm.bind(LOCKED_SLOT);
        this.blitGuiFull(matrixStack, x, y, SLOT_SIZE_X, SLOT_SIZE_Y);
    }
*/
    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1f, 1f, 1f, 1f);
        if (this.minecraft == null) return;
        TextureManager tm = this.minecraft.getTextureManager();
        tm.bind(BACKGROUND);
        this.blitGuiFull(matrixStack, 0, 0, BACKGROUND_X, BACKGROUND_Y);
//        for (int x = 0; x < 9; ++x)
//            drawAccessRectangle(matrixStack, tm, BELT_X + x * SLOT_SIZE_X, BELT_Y, menu.getRights(36 + x));
//        for (int y = 0; y < 3; ++y)
//            for (int x = 0; x < 9; ++x)
//                drawAccessRectangle(matrixStack, tm, MAIN_INVENTORY_X + x * SLOT_SIZE_X, MAIN_INVENTORY_Y + y * SLOT_SIZE_Y, menu.getRights(45 + x + 9 * y));
//        for (int x = 0; x < 4; ++x)
//            drawAccessRectangle(matrixStack, tm, ARMOR_SLOTS_X + x * SLOT_SIZE_X, SPECIAL_SLOTS_Y, menu.getRights(72 + x));
//        drawAccessRectangle(matrixStack, tm, OFF_HAND_SLOT_X, SPECIAL_SLOTS_Y, menu.getRights(76));
        tm.bind(LOCKED_SLOT);
        for (Slot slot : menu.slots)
            if (slot instanceof EntityInventoryProxySlot && !((EntityInventoryProxySlot)slot).isActive())
                this.blitGuiFull(matrixStack, slot.x - 1, slot.y - 1, SLOT_SIZE_X, SLOT_SIZE_Y);
    }
}
