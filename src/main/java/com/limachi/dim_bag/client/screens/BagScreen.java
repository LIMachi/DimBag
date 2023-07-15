package com.limachi.dim_bag.client.screens;

import com.limachi.dim_bag.DimBag;
import com.limachi.dim_bag.menus.BagMenu;
import com.limachi.dim_bag.client.widgets.VerticalSlider;
import com.limachi.lim_lib.network.messages.ScreenNBTMsg;
import com.limachi.lim_lib.registries.clientAnnotations.RegisterMenuScreen;
import com.limachi.lim_lib.render.GuiUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
@RegisterMenuScreen
public class BagScreen extends AbstractContainerScreen<BagMenu> {

    public static final ResourceLocation BACKGROUND = new ResourceLocation(DimBag.MOD_ID, "textures/screen/bag_inventory.png");
    public static final int BACKGROUND_WIDTH = 218;
    public static final int BACKGROUND_HEIGHT = 184;

    protected VerticalSlider slider;

    public BagScreen(BagMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = BACKGROUND_WIDTH;
        imageHeight = BACKGROUND_HEIGHT;
        titleLabelX = 30;
        inventoryLabelX = 30;
        inventoryLabelY = 90;
    }

    @Override
    protected void init() {
        super.init();
        slider = new VerticalSlider(195 + getGuiLeft(), 16 + getGuiTop(), 16, 103, 0., this::sliderUpdate);
        addRenderableWidget(slider);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double fromX, double fromY) {
        return ((getFocused() != null && isDragging() && button == 0) && getFocused().mouseDragged(mouseX, mouseY, button, fromX, fromY)) || super.mouseDragged(mouseX, mouseY, button, fromX, fromY);
    }

    protected void sliderUpdate(VerticalSlider slider) {
        CompoundTag out = new CompoundTag();
        out.putDouble("scroll", slider.getValue());
        ScreenNBTMsg.send(0, out);
    }

    @Override
    public void render(@Nonnull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gui);
        super.render(gui, mouseX, mouseY, partialTick);
        this.renderTooltip(gui, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        gui.blit(BACKGROUND, getGuiLeft(), getGuiTop(), 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);
        for (Slot slot : menu.slots)
            if (!slot.isActive())
                GuiUtils.slots(gui, slot.x - 1, slot.y - 1, 1, 1, true);
    }
}
