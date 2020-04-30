package com.limachi.dimensional_bags.client.screen;

import com.limachi.dimensional_bags.common.data.EyeData;
import com.limachi.dimensional_bags.common.data.container.SCActions;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import static com.limachi.dimensional_bags.DimensionalBagsMod.MOD_ID;

public class ActionsGUI extends ContainerScreen<SCActions> {
    private static final ResourceLocation DEMO_BACKGROUND = new ResourceLocation(MOD_ID, "textures/screens/vanilla/demo_background.png");
    private static final ResourceLocation WIDGETS = new ResourceLocation(MOD_ID, "textures/screens/actions_screen.png");

    final EyeData data;

    public ActionsGUI(SCActions screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
        this.data = this.container.getData();
    }

    @Override
    public void render(final int mouseX, final int mouseY, final float partialTicks) {
        this.xSize = 248;
        this.ySize = 166;
        this.guiLeft = (this.width - this.xSize) / 2; //start of the gui in the x axis (this.width is the size of the screens on the x axis)
        this.guiTop = (this.height - this.ySize) / 2; //start of the gui in the y axis (this.height is the size of the screens on the y axis)
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        mouseY -= this.guiTop;
        mouseX -= this.guiLeft;
        for (int i = 0; i < 6; ++i)
            if (mouseY < 10 + i * 25 || mouseY >= 30 + i * 25 || mouseX < 0 || mouseX >= this.xSize)
                this.font.drawString(this.container.getData().getTriggers()[i].printable(), 29, 15 + i * 25, 0xE5E5E5);
            else
                this.font.drawString(this.container.getData().getAction(this.container.getData().getTriggers()[i].mappedAction()).getName(), 29, 15 + i * 25, 0xE5E5E5);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1f, 1f, 1f, 1f);
        mouseY -= this.guiTop;
        mouseX -= this.guiLeft;
        TextureManager tm = this.minecraft.getTextureManager();
        tm.bindTexture(DEMO_BACKGROUND);
        this.blit(this.guiLeft, this.guiTop, 0, 0, 248, 166);
        tm.bindTexture(WIDGETS);
        for (int i = 0; i < 6; ++i) {
            this.blit(this.guiLeft + 24, this.guiTop + 10 + 25 * i, 0, 20, 200, 20);
            if (mouseY >= 10 + i * 25 && mouseY < 30 + i * 25 && mouseX >= 0 && mouseX < this.xSize) {
                this.blit(this.guiLeft + 7, this.guiTop + 9 + 25 * i, 214, 0, 14, 22);
                this.blit(this.guiLeft + this.xSize - 7 - 14, this.guiTop + 9 + 25 * i, 200, 0, 14, 22);
            }
        }
    }

    @Override
    public boolean keyPressed(int key, int m1, int m2) {
//        GLFW.
        return super.keyPressed(key, m1, m2);
    }

    @Override
    public void onClose() {
        super.onClose();
        this.data.markDirty();
    }

    /*
    @Override
    protected void handleMouseClick(Slot slot, int slotId, int mouseButton, ClickType type) {
        if (type != ClickType.PICKUP) return;
        if ()
    }
    */

}
