package com.limachi.dimensional_bags.client.screen;

import com.limachi.dimensional_bags.common.config.DimBagConfig;
import com.limachi.dimensional_bags.common.data.container.UpgradeContainer;
import com.limachi.dimensional_bags.common.upgradesManager.UpgradeManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import static com.limachi.dimensional_bags.DimensionalBagsMod.MOD_ID;

public class UpgradeGUI extends ContainerScreen<UpgradeContainer> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(MOD_ID, "textures/screens/upgrade_screen.png");
    private static final ResourceLocation UNUSED_SLOT = new ResourceLocation(MOD_ID, "textures/screens/parts/no_slot.png");
    private static final int BACKGROUND_X = 174;
    private static final int BACKGROUND_Y = 180;
    private static final int SLOT_X = 18;
    private static final int SLOT_Y = 18;
    private static final int FIRST_SLOT_X = 6;
    private static final int FIRST_SLOT_Y = 11;
    private static final int TITLES_X = 6;
    private static final int GUI_TITLE_Y = 3;
    private static final int INVENTORY_TITLE_Y = 89;
    private static final int HELP_X = 8;
    private static final int HELP_Y = 50;

    public UpgradeGUI(UpgradeContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
        this.xSize = BACKGROUND_X;
        this.ySize = BACKGROUND_Y;
        this.guiLeft = (this.width - BACKGROUND_X) / 2;
        this.guiTop = (this.height - BACKGROUND_Y) / 2;
    }

    private void blitGuiFull(int x, int y, int w, int h) {
        this.blit(x + this.guiLeft, y + this.guiTop, 0, 0, w, h, w, h);
    }

    @Override
    public void render(final int mouseX, final int mouseY, final float partialTicks) {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        this.font.drawString(this.title.getFormattedText(), TITLES_X, GUI_TITLE_Y, 4210752);
        this.font.drawString(this.playerInventory.getDisplayName().getFormattedText(), TITLES_X, INVENTORY_TITLE_Y, 4210752);

        this.font.drawString("Description: ", HELP_X, HELP_Y, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1f, 1f, 1f, 1f);
        if (this.minecraft == null) return;
        TextureManager tm = this.minecraft.getTextureManager();
        tm.bindTexture(BACKGROUND);
        this.blitGuiFull(0, 0, BACKGROUND_X, BACKGROUND_Y);
        for (int y = 0; y < 2; ++y)
            for (int x = 0; x < 9; ++x)
                if (x + 9 * y < UpgradeManager.upgradesCount())
                    ;
                else {
                    tm.bindTexture(UNUSED_SLOT);
                    this.blitGuiFull(FIRST_SLOT_X + x * SLOT_X, FIRST_SLOT_Y + y * SLOT_Y, SLOT_X, SLOT_Y);
                }
    }
}
