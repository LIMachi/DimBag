package com.limachi.dimensional_bags.client.gui;

import com.limachi.dimensional_bags.common.container.BagEyeContainer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static com.limachi.dimensional_bags.DimensionalBagsMod.MOD_ID;

@OnlyIn(Dist.CLIENT)
public class BagGUI extends ContainerScreen<BagEyeContainer> {

    private static final ResourceLocation SLOT_TEXTURE = new ResourceLocation(MOD_ID, "textures/gui/item_slot.png");

    public BagGUI(BagEyeContainer container, PlayerInventory inv, ITextComponent titleIn) {
        super(container, inv, titleIn);
        this.guiLeft = 0;
        this.guiTop = 0;
        this.xSize = 175; //will need to be calculated based on the size of the actual gui
        this.ySize = 183; //same
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
        this.font.drawString(this.title.getFormattedText(), 8.0f, 6.0f, 4210752);
        this.font.drawString(this.playerInventory.getDisplayName().getFormattedText(), 8.0f, 90.0f, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.minecraft.getTextureManager().bindTexture(SLOT_TEXTURE);
        int x = (this.width = this.xSize) / 2;
        int y = (this.height = this.ySize) / 2;
        this.blit(x, y, 0, 0, this.xSize, this.ySize);
    }
}
